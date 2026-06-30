package com.archivonegativoscronica;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Generador505Seguro {

    private static final int LIMITE_CAMPO_505 = 1300;

    private final Funciones cron;

    public Generador505Seguro(Funciones cron) {
        this.cron = cron;
    }

    public Resultado505 construir(String registroOriginal, List<String[]> inventariosNuevos) {
        Resultado505 resultado = new Resultado505();

        Map<String, ItemActual505> itemsActuales = extraerItemsActuales(registroOriginal);
        Map<String, String> titulos505Conservados = extraerTitulos505Conservados(
                registroOriginal,
                itemsActuales,
                resultado
        );

        HashSet<String> nroAsUsados = new HashSet<>();

        for (String nroA : itemsActuales.keySet()) {
            ItemActual505 item = itemsActuales.get(nroA);

            if (titulos505Conservados.containsKey(nroA)) {
                resultado.addTitulo(titulos505Conservados.get(nroA));
                nroAsUsados.add(nroA);
            } else {
                String tituloInventario = tituloDesdeInventarioPorBarcode(item.getBarcode());

                if (!tituloInventario.equals("")) {
                    resultado.addTitulo(tituloInventario);
                    nroAsUsados.add(nroA);
                    resultado.addMensaje("505: item actual sin título previo en 505; se tomó desde inventario. nroA "
                            + nroA + " / barcode " + item.getBarcode() + ".");
                } else {
                    resultado.addMensaje("505: item actual sin título en 505 y sin inventario encontrado. nroA "
                            + nroA + " / barcode " + item.getBarcode() + ".");
                }
            }
        }

        if (inventariosNuevos != null) {
            for (String[] reg : inventariosNuevos) {
                String nroA = nroADesdeInventario(reg);

                if (nroA.equals("")) {
                    continue;
                }

                if (nroAsUsados.contains(nroA)) {
                    resultado.addMensaje("505: inventario nuevo omitido porque el nroA ya existe en items/505: " + nroA + ".");
                    continue;
                }

                String titulo = tituloCompletoInventario(reg);

                if (!titulo.equals("")) {
                    resultado.addTitulo(titulo);
                    nroAsUsados.add(nroA);
                    resultado.addMensaje("505: título nuevo agregado desde inventario: " + nroA + ".");
                }
            }
        }

        ordenarPorFecha(resultado.getTitulos());
        generarCampos505(resultado);

        return resultado;
    }

    private Map<String, ItemActual505> extraerItemsActuales(String registroOriginal) {
        Map<String, ItemActual505> salida = new HashMap<>();

        if (registroOriginal == null || registroOriginal.trim().equals("")) {
            return salida;
        }

        String[] lineas = registroOriginal.split("\\r?\\n");

        for (String linea : lineas) {
            if (!linea.contains(" Z30-")) {
                continue;
            }

            String nroA = normalizarNroA(extraerSubcampo(linea, "h"));
            String barcode = extraerSubcampo(linea, "5");

            if (!nroA.equals("")) {
                salida.put(nroA, new ItemActual505(nroA, barcode));
            }
        }

        return salida;
    }

    private Map<String, String> extraerTitulos505Conservados(
            String registroOriginal,
            Map<String, ItemActual505> itemsActuales,
            Resultado505 resultado
    ) {
        Map<String, String> salida = new HashMap<>();
        List<String> titulos505 = extraerTitulosDe505(registroOriginal);

        for (String titulo : titulos505) {
            String tituloLimpio = limpiarTitulo505(titulo);
            String nroA = normalizarNroA(extraerNroADesdeTitulo505(tituloLimpio));

            if (nroA.equals("")) {
                resultado.addMensaje("505: título descartado porque no se pudo reconocer nroA: " + tituloLimpio);
                continue;
            }

            if (!itemsActuales.containsKey(nroA)) {
                resultado.addMensaje("505: título descartado porque no hay item actual con nroA " + nroA + ": " + tituloLimpio);
                continue;
            }

            if (salida.containsKey(nroA)) {
                resultado.addMensaje("505: título duplicado descartado para nroA " + nroA + ": " + tituloLimpio);
                continue;
            }

            salida.put(nroA, tituloLimpio);
        }

        return salida;
    }

    private List<String> extraerTitulosDe505(String registroOriginal) {
        List<String> salida = new ArrayList<>();

        if (registroOriginal == null || registroOriginal.trim().equals("")) {
            return salida;
        }

        String[] lineas = registroOriginal.split("\\r?\\n");

        for (String linea : lineas) {
            if (!linea.contains(" 505")) {
                continue;
            }

            int pos = linea.indexOf("$$a");

            if (pos < 0) {
                continue;
            }

            String contenido = limpiarTitulo505(linea.substring(pos + 3));

            /*
             * El separador 505 histórico es " -- ". En registros viejos puede
             * quedar pegado al final del campo cuando una 505 se parte en varias
             * líneas. Por eso el split acepta falta de espacio posterior y cada
             * fragmento se vuelve a limpiar.
             */
            String[] partes = contenido.split("\\s+--\\s*");

            for (String parte : partes) {
                String titulo = limpiarTitulo505(parte);

                if (!titulo.equals("")) {
                    salida.add(titulo);
                }
            }
        }

        return salida;
    }

    private String extraerNroADesdeTitulo505(String titulo) {
        if (titulo == null) {
            return "";
        }

        String limpio = limpiarTitulo505(titulo);
        int posPunto = limpio.indexOf(".");

        if (posPunto < 0) {
            return "";
        }

        return limpio.substring(0, posPunto).trim();
    }

    private String tituloDesdeInventarioPorBarcode(String barcode) {
        if (barcode == null || barcode.trim().equals("")) {
            return "";
        }

        String consulta = "SELECT * FROM inventario WHERE barcode LIKE '" + escaparSql(barcode.trim()) + "'";
        List<String[]> filas = cron.consultaCompleta(consulta);

        if (filas == null || filas.isEmpty()) {
            return "";
        }

        return tituloCompletoInventario(filas.get(0));
    }

    private String tituloCompletoInventario(String[] reg) {
        if (reg == null) {
            return "";
        }

        String barcode = valorSeguro(reg, 0);
        String nroA = valorSeguro(reg, 1);
        String tituloBase = limpiarTitulo505(valorSeguro(reg, 5));
        String fecha = valorSeguro(reg, 6);

        if (tituloBase.equals("")) {
            return "";
        }

        String titulo;

        if (nroA.equals("")) {
            titulo = "[" + barcode + "]. " + tituloBase;
        } else {
            titulo = nroA + ". " + tituloBase;
        }

        if (!fecha.equals("")) {
            titulo = titulo + ", " + RegistrosParaAleph.fechaFormateada(fecha);
        }

        return limpiarTitulo505(titulo);
    }

    private String nroADesdeInventario(String[] reg) {
        if (reg == null) {
            return "";
        }

        String nroA = valorSeguro(reg, 1);

        if (!nroA.equals("")) {
            return normalizarNroA(nroA);
        }

        return normalizarNroA(valorSeguro(reg, 0));
    }

    private void generarCampos505(Resultado505 resultado) {
        List<String> titulos = resultado.getTitulos();

        if (titulos == null || titulos.isEmpty()) {
            return;
        }

        StringBuilder actual = new StringBuilder();

        for (String tituloRaw : titulos) {
            String titulo = limpiarTitulo505(tituloRaw);

            if (titulo.equals("")) {
                continue;
            }

            String agregado = actual.length() == 0 ? titulo : " -- " + titulo;

            if (actual.length() > 0 && actual.length() + agregado.length() >= LIMITE_CAMPO_505) {
                resultado.addCampo505(campo505(actual.toString()));
                actual = new StringBuilder(titulo);
            } else {
                actual.append(agregado);
            }
        }

        if (actual.length() > 0) {
            resultado.addCampo505(campo505(actual.toString()));
        }
    }

    private String campo505(String contenido) {
        String limpio = limpiarTitulo505(contenido);

        if (!limpio.endsWith(".")) {
            limpio = limpio + ".";
        }

        return "5050  L $$a" + limpio;
    }

    private void ordenarPorFecha(List<String> titulos) {
        if (titulos == null || titulos.size() <= 1) {
            return;
        }

        Collections.sort(titulos, (a, b) -> {
            String fechaA = claveFechaOrden(a);
            String fechaB = claveFechaOrden(b);

            int cmpFecha = fechaA.compareTo(fechaB);

            if (cmpFecha != 0) {
                return cmpFecha;
            }

            return normalizarTexto(a).compareTo(normalizarTexto(b));
        });
    }

    private String claveFechaOrden(String titulo) {
        String fecha = extraerFechaDesdeUltimaComa(titulo);

        if (fecha.equals("")) {
            return "99999999";
        }

        String normalizada = normalizarFecha(fecha);

        if (normalizada.equals("")) {
            return "99999999";
        }

        return normalizada;
    }

    private String extraerFechaDesdeUltimaComa(String titulo) {
        if (titulo == null) {
            return "";
        }

        String limpio = limpiarTitulo505(titulo);
        int pos = limpio.lastIndexOf(",");

        if (pos < 0 || pos + 1 >= limpio.length()) {
            return "";
        }

        return limpio.substring(pos + 1).trim();
    }

    private String normalizarFecha(String fecha) {
        if (fecha == null) {
            return "";
        }

        String f = fecha.trim();

        java.util.regex.Matcher completa = java.util.regex.Pattern
                .compile("^(\\d{1,2})/(\\d{1,2})/(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (completa.find()) {
            return completa.group(3) + pad2(completa.group(2)) + pad2(completa.group(1));
        }

        java.util.regex.Matcher mesAnio = java.util.regex.Pattern
                .compile("^(\\d{1,2})/(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (mesAnio.find()) {
            return mesAnio.group(2) + pad2(mesAnio.group(1)) + "00";
        }

        java.util.regex.Matcher anio = java.util.regex.Pattern
                .compile("^(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (anio.find()) {
            return anio.group(1) + "0000";
        }

        return "";
    }

    private String extraerSubcampo(String linea, String codigo) {
        if (linea == null) {
            return "";
        }

        String marca = "$$" + codigo;
        int pos = linea.indexOf(marca);

        if (pos < 0) {
            return "";
        }

        int inicio = pos + marca.length();
        int siguiente = linea.indexOf("$$", inicio);

        if (siguiente < 0) {
            return linea.substring(inicio).trim();
        }

        return linea.substring(inicio, siguiente).trim();
    }

    private String normalizarNroA(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim();

        while (limpio.startsWith("[")) {
            limpio = limpio.substring(1).trim();
        }

        while (limpio.endsWith("]")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }

        return limpio.trim();
    }

    private String limpiarTitulo505(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        limpio = normalizarSeparadoresRepetidos(limpio);

        while (limpio.startsWith("--")) {
            limpio = limpio.substring(2).trim();
        }

        while (limpio.endsWith(".")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }

        while (limpio.endsWith("--")) {
            limpio = limpio.substring(0, limpio.length() - 2).trim();
        }

        while (limpio.endsWith(".")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }

        return normalizarSeparadoresRepetidos(limpio).trim();
    }

    private String normalizarSeparadoresRepetidos(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        while (limpio.contains(" -- -- ")) {
            limpio = limpio.replace(" -- -- ", " -- ");
        }

        while (limpio.matches(".*\\s+--\\s+--\\s+.*")) {
            limpio = limpio.replaceAll("\\s+--\\s+--\\s+", " -- ");
        }

        return limpio.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = limpiarTitulo505(valor).replaceAll("\\s+", " ");
        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD);
        limpio = limpio.replaceAll("\\p{M}", "");

        return limpio.toLowerCase();
    }

    private String valorSeguro(String[] arr, int pos) {
        try {
            if (arr[pos] == null) {
                return "";
            }

            return arr[pos].trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String escaparSql(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replace("'", "''");
    }

    private String pad2(String valor) {
        if (valor == null) {
            return "00";
        }

        String limpio = valor.trim();

        if (limpio.length() == 1) {
            return "0" + limpio;
        }

        if (limpio.length() >= 2) {
            return limpio.substring(0, 2);
        }

        return "00";
    }

    private static class ItemActual505 {

        private final String nroA;
        private final String barcode;

        ItemActual505(String nroA, String barcode) {
            this.nroA = nroA;
            this.barcode = barcode == null ? "" : barcode.trim();
        }

        public String getNroA() {
            return nroA;
        }

        public String getBarcode() {
            return barcode;
        }
    }
}
