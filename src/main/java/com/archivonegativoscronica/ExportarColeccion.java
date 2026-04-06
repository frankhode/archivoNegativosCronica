package com.archivonegativoscronica;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.Tab;

/**
 * Versión compatible con Java 11.
 * Genera un HTML "hardcodeado" con las rutas de imágenes y permite:
 *  - Guardarlo mediante FileChooser (Windows)
 *  - Mostrarlo en un WebView de JavaFX
 *
 * Requisitos: JavaFX y JDBC.
 */
public class ExportarColeccion {
    Funciones cron ;

    public ExportarColeccion(Funciones cron) throws SQLException, Exception {
        this.cron = cron ;
        ColeccionadorOMatic com = new ColeccionadorOMatic(cron, true) ;
        exportar(com.coleccion, cron.tabPane.getScene().getWindow(),true) ;
    }

    // ===================== Datos =====================

    /**
     * Obtiene las rutas de los archivos pertenecientes a una colección.Ajusta la SQL a tu esquema real (tabla/columnas).
     * @param coleccion
     * @return 
     * @throws java.sql.SQLException
     */
    private List<String> listaArchivos(String coleccion) throws SQLException {
        List<String> paths ;
        String consulta = "SELECT CONCAT"
                + "('U:/Mapo-Cronica/004-ordenados_DMFC/', digitales.carpeta, '/', "
                + "digitales.cajon, '/', digitales.inv, '/', digitales.nombramiento) "
                + "AS cadena FROM digitales JOIN titulos on titulos.barcode=digitales.inv "
                + "WHERE nombramiento IN "
                + "(SELECT nombramiento FROM colecciones WHERE "
                + "coleccion like '"+coleccion+"') ORDER BY titulos.fecha";
        paths = cron.consultaSimple(consulta, 1) ;
        return paths;        
    }

    // ===================== HTML =====================

    /**
     * Genera el HTML listo para abrir en el navegador, con las rutas "hardcodeadas" en RAW_LIST.Incluye links: Abrir imagen, Abrir carpeta y Ver sobre (reemplaza _NNN o _NNNN por _000 antes de la extensión).
     * @param rutas
     * @return 
     */
    private String generarHtml(List<String> rutas) {
        StringBuilder sb = new StringBuilder();

        // Cabecera y estilos
        sb.append("<!DOCTYPE html>\n")
          .append("<html lang=\"es\">\n<head>\n")
          .append("  <meta charset=\"utf-8\" />\n")
          .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n")
          .append("  <title>Vista previa de imágenes — simple (hardcode)</title>\n")
          .append("  <style>\n")
          .append("    :root{ --bg:#0b0f14; --panel:#111822; --muted:#94a3b8; --text:#e5e7eb; --accent:#60a5fa; --border:#1f2937; --card:#0f172a }\n")
          .append("    *{box-sizing:border-box}\n    html,body{height:100%}\n")
          .append("    body{margin:0; font-family:system-ui,-apple-system,Segoe UI,Roboto,Ubuntu,Helvetica,Arial,sans-serif; color:var(--text); background:var(--bg)}\n")
          .append("    header{position:sticky; top:0; z-index:10; background:rgba(15,23,42,.9); backdrop-filter: blur(8px); border-bottom:1px solid var(--border)}\n")
          .append("    .wrap{max-width:1200px; margin:0 auto; padding:12px 16px}\n")
          .append("    .controls{display:flex; flex-wrap:wrap; gap:10px; align-items:center}\n")
          .append("    .pill{display:flex; align-items:center; gap:8px; background:#0e1729; border:1px solid var(--border); padding:6px 10px; border-radius:999px; font-size:12px}\n")
          .append("    input[type=\\\"range\\\"]{accent-color:var(--accent)}\n")
          .append("    select{background:var(--panel); color:var(--text); border:1px solid var(--border); padding:6px 10px; border-radius:10px}\n")
          .append("    .grid{--w:220px; display:grid; grid-template-columns:repeat(auto-fill, minmax(var(--w), 1fr)); gap:12px; padding:16px}\n")
          .append("    .card{background:var(--card); border:1px solid var(--border); border-radius:14px; overflow:hidden}\n")
          .append("    .thumb{position:relative; background:#0b1220; aspect-ratio:4/3; display:flex; align-items:center; justify-content:center}\n")
          .append("    .thumb img{width:100%; height:100%; object-fit:contain; display:block}\n")
          .append("    .meta{padding:10px 12px; font-size:12px; color:var(--muted); display:flex; justify-content:space-between; gap:8px; align-items:center}\n")
          .append("    .name{flex:1; min-width:0; overflow:hidden; white-space:nowrap; text-overflow:ellipsis}\n")
          .append("    .links{display:flex; gap:10px}\n")
          .append("    .links a{color:var(--accent); text-decoration:none}\n")
          .append("    .links a:hover{text-decoration:underline}\n")
          .append("    .footer{padding:10px 16px; border-top:1px solid var(--border); color:var(--muted); display:flex; justify-content:space-between; align-items:center}\n")
          .append("    .btn{background:var(--panel); border:1px solid var(--border); color:var(--text); padding:6px 10px; border-radius:10px; cursor:pointer}\n")
          .append("  </style>\n</head>\n");

        // Body + controles
        sb.append("<body>\n  <header>\n    <div class=\"wrap\">\n      <div class=\"controls\">\n")
          .append("        <span class=\"pill\">Tamaño vista: <input id=\"size\" type=\"range\" min=\"120\" max=\"480\" value=\"240\" /></span>\n")
          .append("        <label class=\"pill\">Por página\n          <select id=\"perPage\">\n            <option>24</option>\n            <option selected>60</option>\n            <option>120</option>\n            <option>Todos</option>\n          </select>\n        </label>\n")
          .append("        <span id=\"count\" class=\"pill\">0 imágenes</span>\n      </div>\n    </div>\n  </header>\n");

        // Main y footer
        sb.append("  <main class=\"wrap\">\n    <div id=\"grid\" class=\"grid\"></div>\n    <div class=\"footer\">\n      <div>Enlaces por tarjeta: «Abrir imagen», «Abrir carpeta», «Ver sobre».</div>\n      <div>\n        <button id=\"prev\" class=\"btn\">◀ Anterior</button>\n        <span id=\"pageInfo\">1/1</span>\n        <button id=\"next\" class=\"btn\">Siguiente ▶</button>\n      </div>\n    </div>\n  </main>\n");

        // Script de la app + bloque RAW_LIST (template literal con backticks)
        sb.append("  <script>\n")
          .append("    const RAW_LIST = `\n");

        // Pegar rutas dentro del template-literal (escapando backticks por seguridad)
        StringBuilder lista = new StringBuilder();
        for (String r : rutas) {
            if (r == null) continue;
            lista.append(escapeBackticks(r)).append("\n");
        }
        sb.append(lista);
        sb.append("    `;\n\n");

        // Resto del JS
        sb.append("    function normalizePath(p){\n")
          .append("      if(!p) return p; let q = p.trim();\n")
          .append("      if ((q.startsWith('\"') && q.endsWith('\"')) || (q.startsWith(\"'\") && q.endsWith(\"'\"))) q = q.slice(1,-1);\n")
          .append("      q = q.split('\\\\\\\\').join('/').split('\\\\').join('/');\n")
          .append("      if (/^[a-zA-Z]:\\//.test(q) && !q.startsWith('file:///')) q = 'file:///' + q;\n")
          .append("      return q;\n    }\n")
          .append("    function basename(p){ try{ return decodeURIComponent((p.split('/').pop()||'').trim()); }catch{ return (p.split('/').pop()||'').trim(); } }\n")
          .append("    function dirOf(url){ const parts = url.split('/'); parts.pop(); return parts.join('/'); }\n")
          .append("    function sobreOf(url){ return url.replace(/_(\\d{3,4})(\\.[^\\/]+)$/i, '_000$2'); }\n\n")
          .append("    const PATHS = RAW_LIST.split(/\\r?\\n/).map(s=>s.trim()).filter(Boolean).map(normalizePath);\n")
          .append("    const ITEMS = PATHS.map((src,i)=>({ idx:i, src, name: basename(src) }));\n")
          .append("    let page = 1; let perPage = 60; let totalPages = 1;\n")
          .append("    const grid = document.getElementById('grid');\n")
          .append("    const countEl = document.getElementById('count');\n")
          .append("    const pageInfo = document.getElementById('pageInfo');\n")
          .append("    function render(){\n")
          .append("      const total = ITEMS.length;\n")
          .append("      const perVal = document.getElementById('perPage').value;\n")
          .append("      totalPages = (perVal === 'Todos') ? 1 : Math.max(1, Math.ceil(total / perPage));\n")
          .append("      if(page>totalPages) page = totalPages;\n")
          .append("      const start = (perVal === 'Todos') ? 0 : (page-1)*perPage;\n")
          .append("      const end = (perVal === 'Todos') ? total : start + perPage;\n")
          .append("      const slice = ITEMS.slice(start, end);\n\n")
          .append("      grid.innerHTML = '';\n")
          .append("      slice.forEach(it => {\n")
          .append("        const card = document.createElement('div'); card.className='card';\n")
          .append("        const th = document.createElement('div'); th.className='thumb';\n")
          .append("        const img = document.createElement('img'); img.loading='lazy'; img.decoding='async'; img.alt = it.name; img.src = it.src; th.appendChild(img);\n")
          .append("        const meta = document.createElement('div'); meta.className='meta';\n")
          .append("        const name = document.createElement('div'); name.className='name'; name.textContent = it.name;\n")
          .append("        const links = document.createElement('div'); links.className='links';\n")
          .append("        const aOpen = document.createElement('a'); aOpen.href = it.src; aOpen.target='_blank'; aOpen.rel='noopener'; aOpen.textContent='Abrir imagen';\n")
          .append("        const aFolder = document.createElement('a'); aFolder.href = dirOf(it.src) + '/'; aFolder.target='_blank'; aFolder.rel='noopener'; aFolder.textContent='Abrir carpeta';\n")
          .append("        const aSobre = document.createElement('a'); aSobre.href = sobreOf(it.src); aSobre.target='_blank'; aSobre.rel='noopener'; aSobre.textContent='Ver sobre';\n")
          .append("        links.appendChild(aOpen); links.appendChild(aFolder); links.appendChild(aSobre);\n")
          .append("        meta.appendChild(name); meta.appendChild(links);\n")
          .append("        card.appendChild(th); card.appendChild(meta); grid.appendChild(card);\n")
          .append("      });\n\n")
          .append("      countEl.textContent = total + ' imágenes';\n")
          .append("      pageInfo.textContent = page + '/' + totalPages;\n")
          .append("    }\n\n")
          .append("    document.getElementById('size').addEventListener('input', (e)=>{ document.querySelector('.grid').style.setProperty('--w', e.target.value + 'px'); });\n")
          .append("    document.getElementById('perPage').addEventListener('change', (e)=>{ const v = e.target.value; perPage = (v==='Todos') ? Infinity : parseInt(v,10) || 60; page = 1; render(); });\n")
          .append("    document.getElementById('prev').addEventListener('click', ()=>{ if(page>1){ page--; render(); } });\n")
          .append("    document.getElementById('next').addEventListener('click', ()=>{ if(page<totalPages){ page++; render(); } });\n")
          .append("    render();\n")
          .append("  </script>\n");

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    // ===================== Guardar / Exportar =====================

    /** Guarda el HTML con un FileChooser (Windows) y devuelve la ruta escrita, o null si se cancela.
     * @param owner
     * @param nombreSugerido
     * @param html
     * @return 
     * @throws java.io.IOException*/
    private Path guardarHtmlEnWindows(Window owner, String nombreSugerido, String html) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar vista previa (HTML)");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivo HTML", "*.html"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        if (nombreSugerido != null && !nombreSugerido.isBlank()) {
            chooser.setInitialFileName(nombreSugerido.endsWith(".html") ? nombreSugerido : nombreSugerido + ".html");
        }
        File file = chooser.showSaveDialog(owner);
        if (file == null) return null;
        Files.writeString(file.toPath(), html, StandardCharsets.UTF_8);
        return file.toPath();
    }

    /** Flujo completo: obtiene rutas por colección, genera HTML y pide dónde guardarlo.
     * @param coleccion
     * @param owner
     * @param abrirAlFinal
     * @return 
     * @throws java.lang.Exception */
    private Path exportar(String coleccion, Window owner, boolean abrirAlFinal) throws Exception {
        List<String> rutas = listaArchivos(coleccion);
        String html = generarHtml(rutas);
        Path out = guardarHtmlEnWindows(owner, safeFileName(coleccion) + ".html", html);
        if (abrirAlFinal && out != null) {
            try { Desktop.getDesktop().browse(out.toUri()); } catch (Throwable ignored) {}
        }
        return out;
    }

    // ===================== WebView =====================

    /** Muestra el HTML generado (en memoria) dentro de un WebView.
     * @param webView
     * @param rutas */
    private void mostrarEnWebView(WebView webView, List<String> rutas) {
        Objects.requireNonNull(webView, "webView no puede ser null");
        String html = generarHtml(rutas);
        WebEngine engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                try {
                    engine.executeScript(
                        "document.querySelectorAll('a[target=_blank]').forEach(a=>a.removeAttribute('target'));" +
                        "window.open = function(u){ location.href = u; return null; };"
                    );
                } catch (Throwable ignored) {}
            }
        });
        engine.loadContent(html, "text/html");
    }

    /** Variante: obtiene rutas por nombre de colección y las muestra en el WebView.
     * @param coleccion
     * @param webView
     * @throws java.lang.Exception */
    private void mostrarColeccionEnWebView(String coleccion, WebView webView) throws Exception {
        List<String> rutas = listaArchivos(coleccion);
        mostrarEnWebView(webView, rutas);
    }

    // ===================== Helpers =====================

    /** Evita que un backtick en una ruta rompa el template literal de JavaScript. */
    private static String escapeBackticks(String s) { return s.replace("`", "\\`"); }

    /** Convierte una cadena en un nombre de archivo amistoso. */
    private static String safeFileName(String s) {
        String base = s == null ? "coleccion" : s.trim();
        base = base.replaceAll("[\\\\/:*?\"<>|]+", "_");
        if (base.isBlank()) base = "coleccion";
        return base;
    }
}
