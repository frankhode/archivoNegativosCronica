/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser; // ★
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Contactos {

    /* ========== CONFIGURACIÓN ========== */
    private static final String BASE_DIR = "U:\\\\Mapo-Cronica\\\\004-ordenados_DMFC";

    private static final int TILE   = 300;  // lado mayor miniatura
    private static final int COLS   = 5;    // columnas
    private static final int MARGIN = 10;   // márgenes
    private static final int BORDER = 2;    // marco blanco miniatura
    private static final int FONT   = 18;   // px
    private static final int SHEET_W = 1920;
    private static final String CSS_SEL =
            "-fx-border-color:#3a84ff;-fx-border-width:4;" +
            "-fx-background-color:rgba(58,132,255,0.30);";

    private static final int PAGE_SIZE_OUT = 15; // ★ máx. miniaturas por hoja de salida

    /* ========== ESTADO ========== */
    private final Funciones cron;
    private final List<ImageEntry> imagenes = new ArrayList<>();
    private final Set<Integer> seleccionadas = new HashSet<>();
    private final Map<Integer, VBox> idx2box = new HashMap<>();

    private Stage stage;
    private TilePane tile;
    private Button btnGenerar;

    String sql ;

    public Contactos(Funciones cron) { this.cron = cron; }

    /* ========== PÚBLICO ========== */
    public void abrir() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Hoja de contactos");
        dlg.setHeaderText("Ingrese el código de barras (inv)");
        dlg.setContentText("Barcode:");
        dlg.showAndWait().filter(s -> !s.trim().isEmpty()).ifPresent(this::cargar);
    }

    /* ========== 1 · Cargar datos ========== */
    public void cargar(String barcode) {
        /* —— Task de carga —— */
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                try {
                    boolean bool = verificaAltasBajas(barcode) ;
                    if (bool) {
                        sql = "SELECT nombramiento, inv, cajon, carpeta FROM digitales WHERE inv='" + barcode + "' AND carpeta LIKE 'Bajas'";
                    } else {
                        sql = "SELECT nombramiento, inv, cajon, carpeta FROM digitales WHERE inv='" + barcode + "'";
                    }
                    List<String[]> rows = cron.consultaCompleta(sql);
                    if (rows == null || rows.isEmpty())
                        throw new IllegalStateException("No se encontraron registros.");

                    for (String[] r : rows) {
                        String nom = r[0];
                        Path p = Path.of(BASE_DIR, r[3], r[2], r[1], nom);
                        imagenes.add(new ImageEntry(p.toFile(), nom));
                    }
                    if (imagenes.isEmpty())
                        throw new IllegalStateException("Solo había _000.*, nada que mostrar.");

                    imagenes.sort(Comparator.comparing(ImageEntry::getNombre));

                    /* generar miniaturas con progreso */
                    for (int i = 0; i < imagenes.size(); i++) {
                        updateMessage("Cargando imagen " + (i + 1) + " / " + imagenes.size());
                        updateProgress(i + 1, imagenes.size());
                        imagenes.get(i).loadThumb();
                    }
                } catch (Exception ex) { updateMessage(ex.getMessage()); cancel(); }
                return null;
            }

            private boolean verificaAltasBajas(String barcode) {
                boolean bool = false ;
                String sql1 = "SELECT nombramiento FROM digitales WHERE inv='" + barcode +
                        "' AND carpeta LIKE 'Bajas'";
                String sql2 = "SELECT nombramiento FROM digitales WHERE inv='" + barcode +
                        "' AND carpeta LIKE 'Altas'";
                List<String[]> bajas = cron.consultaCompleta(sql1);
                List<String[]> altas = cron.consultaCompleta(sql2);
                if (bajas.size() == altas.size()) {
                    bool = true ;
                }
                return bool ;
            }
        };

        /* —— Ventana de progreso —— */
        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(260);
        bar.progressProperty().bind(task.progressProperty());

        Label info = new Label();
        info.textProperty().bind(task.messageProperty());
        info.setMinWidth(260); info.setAlignment(Pos.CENTER_LEFT);
        info.setStyle("-fx-text-fill:white;");

        VBox box = new VBox(10, bar, info);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:#2b2b2b;");

        Stage loadStage = new Stage();
        loadStage.initModality(Modality.APPLICATION_MODAL);
        loadStage.setTitle("Cargando miniaturas…");
        loadStage.setScene(new Scene(box));
        loadStage.show();

        task.setOnSucceeded(e -> { loadStage.close(); construirUI(barcode); });
        task.setOnCancelled(e -> { loadStage.close(); alert("Error: " + task.getMessage(), Alert.AlertType.ERROR); });
        new Thread(task).start();
    }

    /* ========== 2 · Construir UI ========== */
    private void construirUI(String barcode) {
        tile = new TilePane(MARGIN, MARGIN);
        tile.setPadding(new Insets(MARGIN));
        tile.setPrefColumns(COLS);
        tile.setStyle("-fx-background-color:black;");

        for (int i = 0; i < imagenes.size(); i++)
            tile.getChildren().add(crearCelda(i, imagenes.get(i)));

        btnGenerar = new Button("Generar contactos");
        btnGenerar.setDisable(true);
        btnGenerar.setOnAction(e -> exportar(barcode));

        /* —— ComboBox de selección rápida —— */
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(
                "Seleccionar todo",
                "Seleccionar todos los jpg",
                "Seleccionar todos los tif"));
        combo.setPromptText("Selección rápida");

        combo.setOnAction(e -> {
            String opt = combo.getValue();
            seleccionadas.clear();
            for (int i = 0; i < imagenes.size(); i++) {
                String ext = ext(imagenes.get(i).nombre);
                if (opt.equals("Seleccionar todo") ||
                    (opt.contains("jpg") && (ext.equals("jpg") || ext.equals("jpeg"))) ||
                    (opt.contains("tif") && (ext.equals("tif") || ext.equals("tiff"))))
                    seleccionadas.add(i);
            }
            refrescar();
        });

        Button btnClear = new Button("Deseleccionar todo");
        btnClear.setOnAction(e -> { seleccionadas.clear(); refrescar(); });

        HBox barra = new HBox(20, combo, btnClear, btnGenerar);
        barra.setPadding(new Insets(10)); barra.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane(new ScrollPane(tile));
        root.setBottom(barra);

        stage = new Stage();
        stage.setTitle("Contactos – " + barcode);
        stage.setScene(new Scene(root, 1100, 720));
        stage.show();
    }

    /* —— Crea celda miniatura —— */
    private VBox crearCelda(int idx, ImageEntry ie) {
        ImageView iv = new ImageView(ie.thumb);
        iv.setFitWidth(TILE); iv.setFitHeight(TILE);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-border-color:white;-fx-border-width:" + BORDER + ";");

        Label lbl = new Label(ie.nombre);
        lbl.setWrapText(true); lbl.setMaxWidth(TILE);
        lbl.setTextFill(Color.WHITE); lbl.setAlignment(Pos.CENTER);

        VBox box = new VBox(iv, lbl);
        box.setAlignment(Pos.CENTER);
        box.setPickOnBounds(true);

        /* capturamos cualquier clic dentro de la celda */
        box.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> { toggle(idx); e.consume(); });

        idx2box.put(idx, box);
        return box;
    }

    private void toggle(int idx) {
        if (seleccionadas.contains(idx)) seleccionadas.remove(idx);
        else seleccionadas.add(idx);
        refrescar();
    }
    private void refrescar() {
        idx2box.forEach((k, b) -> b.setStyle(seleccionadas.contains(k) ? CSS_SEL : ""));
        btnGenerar.setDisable(seleccionadas.isEmpty());
    }

    /* ========== 3 · Exportar hoja ========== */
    private void exportar(String barcode) {
        if (seleccionadas.isEmpty()) return;

        // Preparamos la lista seleccionada y calculamos páginas de salida (★)
        List<ImageEntry> listaSel = seleccionadas.stream()
                .sorted()
                .map(imagenes::get)
                .collect(Collectors.toList());
        int total = listaSel.size();
        int pages = (int) Math.ceil(total / (double) PAGE_SIZE_OUT);

        final File singleDst;      // cuando hay una sola página
        final File directoryDst;   // cuando hay varias páginas

        if (pages <= 1) {
            // Caso 1: una sola salida JPG (como antes)
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar hoja");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
            fc.setInitialFileName(barcode + "_contactos.jpg");
            File dst = fc.showSaveDialog(stage);
            if (dst == null) return;
            singleDst = dst;
            directoryDst = null;
        } else {
            // Caso 2: múltiples páginas → pedimos carpeta destino (★)
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Seleccionar carpeta para " + pages + " hojas");
            File dir = dc.showDialog(stage);
            if (dir == null) return;
            singleDst = null;
            directoryDst = dir;
        }

        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                if (pages <= 1) {
                    BufferedImage hoja = renderHoja(listaSel);
                    ImageIO.write(hoja, "jpg", singleDst);
                } else {
                    for (int p = 0; p < pages; p++) {
                        int start = p * PAGE_SIZE_OUT;
                        int end = Math.min(start + PAGE_SIZE_OUT, total);
                        List<ImageEntry> sub = listaSel.subList(start, end);
                        BufferedImage hoja = renderHoja(sub);
                        String name = String.format("%s_contactos_p%02d.jpg", barcode, p + 1);
                        File out = new File(directoryDst, name);
                        ImageIO.write(hoja, "jpg", out);
                    }
                }
                return null;
            }
        };
        t.setOnSucceeded(e -> okCargarOtro());
        t.setOnFailed(e -> alert("Error: " + t.getException().getMessage(), Alert.AlertType.ERROR));
        new Thread(t).start();
    }

    /* ========== 4 · Render hoja ========== */
    private BufferedImage renderHoja(List<ImageEntry> lista) throws IOException {
        int cellW = (SHEET_W - 2 * MARGIN) / COLS;
        int cellH = TILE + FONT + BORDER * 2 + MARGIN;
        int filas = (int) Math.ceil(lista.size() / (double) COLS);
        int sheetH = filas * cellH + 2 * MARGIN;

        BufferedImage img = new BufferedImage(SHEET_W, sheetH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.BLACK); g.fillRect(0, 0, SHEET_W, sheetH);
        g.setFont(new Font("SansSerif", Font.PLAIN, FONT)); g.setColor(java.awt.Color.WHITE);

        int idx = 0;
        for (int r = 0; r < filas; r++) {
            for (int c = 0; c < COLS && idx < lista.size(); c++, idx++) {
                ImageEntry ie = lista.get(idx);
                BufferedImage buf = SwingFXUtils.fromFXImage(ie.thumb, null);
                if (buf == null) {
                    // fallback: intentar leer directo del archivo (salva casos raros)
                    BufferedImage raw = ImageIO.read(ie.file);
                    if (raw == null) {
                        throw new IOException("No se pudo convertir thumb ni leer archivo: " + ie.file.getAbsolutePath());
                    }
                    buf = raw;
                }

                float sc = TILE / (float) Math.max(buf.getWidth(), buf.getHeight());
                int w = Math.round(buf.getWidth() * sc), h = Math.round(buf.getHeight() * sc);

                int x = MARGIN + c * cellW + (cellW - w) / 2;
                int y = MARGIN + r * cellH;

                g.setColor(java.awt.Color.WHITE);
                g.fillRect(x - BORDER, y - BORDER, w + 2 * BORDER, h + 2 * BORDER);
                g.drawImage(buf, x, y, w, h, null);
                g.drawString(ie.nombre, x, y + h + FONT + 2);
            }
        }
        g.dispose();
        return img;
    }

    /* ========== Utils ========== */
    private static String ext(String n) {
        int p = n.lastIndexOf('.'); return p < 0 ? "" : n.substring(p + 1).toLowerCase(Locale.ROOT);
    }
    private void alert(String m, Alert.AlertType t) {
        Platform.runLater(() -> new Alert(t, m, ButtonType.OK).showAndWait());
    }

    private void okCargarOtro() {
        Alert al = new Alert(Alert.AlertType.CONFIRMATION) ;
        al.setContentText("Hoja(s) de contactos generada(s) con éxito!\n¿Desea generar otra?");
        Optional<ButtonType> result = al.showAndWait();
        if(!result.isPresent()){
            // nada
        } else if(result.get() == ButtonType.OK){
            al.close();
            stage.close();
            Contactos nuevo = new Contactos(cron) ;
            nuevo.abrir();
        } else if(result.get() == ButtonType.CANCEL){
            al.close();
            stage.close();
        }
    }

    private static class ImageEntry {
        final File file; final String nombre; Image thumb;
        ImageEntry(File f, String n) { file = f; nombre = n; }
        String getNombre() { return nombre; }

        void loadThumb() throws IOException {
            if (thumb != null) return;

            String e = ext(nombre);

            if (e.equals("tif") || e.equals("tiff")) {
                BufferedImage bi = ImageIO.read(file);
                if (bi == null) {
                    throw new IOException("No se pudo leer TIFF: " + file.getAbsolutePath());
                }
                thumb = SwingFXUtils.toFXImage(resize(bi, TILE), null);

            } else {
                // backgroundLoading = false => la imagen queda lista en este mismo hilo
                Image img = new Image(file.toURI().toString(), TILE, TILE, true, true, false);

                if (img.isError() || img.getWidth() <= 0) {
                    throw new IOException("No se pudo leer imagen: " + file.getAbsolutePath()
                            + (img.getException() != null ? " -> " + img.getException().getMessage() : ""));
                }
                thumb = img;
            }
        }
        
        private static BufferedImage resize(BufferedImage src, int max) {
            float sc = max / (float) Math.max(src.getWidth(), src.getHeight());
            int w = Math.round(src.getWidth() * sc), h = Math.round(src.getHeight() * sc);
            java.awt.Image tmp = src.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
            BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics(); g.drawImage(tmp, 0, 0, null); g.dispose();
            return dst;
        }
    }
    
    /** Genera hojas de contactos masivas (sin UI de miniaturas) a partir de una lista de barcodes. */
    public void generarContactosMasivos(List<String> barcodes, File outDir) {
        if (barcodes == null || barcodes.isEmpty()) {
            alert("Lista de barcodes vacía.", Alert.AlertType.WARNING);
            return;
        }
        if (outDir == null || !outDir.exists() || !outDir.isDirectory()) {
            alert("Carpeta destino inválida.", Alert.AlertType.ERROR);
            return;
        }

        // Limpieza
        List<String> lista = barcodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Task<Void> t = new Task<>() {
            @Override protected Void call() {
                int ok = 0, vacios = 0, errores = 0;

                for (int bi = 0; bi < lista.size(); bi++) {
                    if (isCancelled()) break;

                    String barcode = lista.get(bi);
                    updateMessage("[" + (bi + 1) + "/" + lista.size() + "] " + barcode);

                    try {
                        // Reutilizá tu helper actual (el que ya hiciste)
                        List<ImageEntry> imgs = cargarImagenesPorBarcode(barcode); // <- si ya lo tenés
                        if (imgs == null || imgs.isEmpty()) { vacios++; updateProgress(bi + 1, lista.size()); continue; }

                        for (int i = 0; i < imgs.size(); i++) {
                            imgs.get(i).loadThumb();
                        }

                        int total = imgs.size();
                        int pages = (int) Math.ceil(total / (double) PAGE_SIZE_OUT);

                        for (int p = 0; p < pages; p++) {
                            int start = p * PAGE_SIZE_OUT;
                            int end = Math.min(start + PAGE_SIZE_OUT, total);
                            List<ImageEntry> sub = imgs.subList(start, end);

                            BufferedImage hoja = renderHoja(sub);
                            String name = String.format("%s_contactos_p%02d.jpg", barcode, p + 1);
                            File out = new File(outDir, name);
                            ImageIO.write(hoja, "jpg", out);
                        }

                        ok++;
                    } catch (Exception ex) {
                        errores++;
                        // si querés, loguealo:
                        System.out.println("ERROR barcode=" + barcode + " -> " + ex.getMessage());
                    }

                    updateProgress(bi + 1, lista.size());
                }

                updateMessage("Terminado.");
                return null;
            }
        };

        // tu UI de progreso (reutilizá la que ya tenés en Contactos)
        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(360);
        bar.progressProperty().bind(t.progressProperty());

        Label info = new Label();
        info.textProperty().bind(t.messageProperty());
        info.setMinWidth(360);
        info.setStyle("-fx-text-fill:white;");

        VBox box = new VBox(10, bar, info);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:#2b2b2b;");

        Stage loadStage = new Stage();
        loadStage.initModality(Modality.APPLICATION_MODAL);
        loadStage.setTitle("Contactos masivos…");
        loadStage.setScene(new Scene(box));
        loadStage.show();

        t.setOnSucceeded(e -> { loadStage.close(); alert("Contactos masivos: OK\nDestino: " + outDir.getAbsolutePath(), Alert.AlertType.INFORMATION); });
        t.setOnFailed(e -> { loadStage.close(); alert("Error: " + t.getException().getMessage(), Alert.AlertType.ERROR); });
        t.setOnCancelled(e -> { loadStage.close(); alert("Cancelado.", Alert.AlertType.WARNING); });

        new Thread(t).start();
    }

    
    /** Devuelve la lista de ImageEntry para un barcode según tu tabla digitales (ignorando *_000.*). */
    private List<ImageEntry> cargarImagenesPorBarcode(String barcode) {
        boolean usarBajas = verificaAltasBajasSafe(barcode);

        String q;
        if (usarBajas) {
            q = "SELECT nombramiento, inv, cajon, carpeta FROM digitales WHERE inv='" + barcode + "' AND carpeta LIKE 'Bajas'";
        } else {
            q = "SELECT nombramiento, inv, cajon, carpeta FROM digitales WHERE inv='" + barcode + "'";
        }

        List<String[]> rows = cron.consultaCompleta(q);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<ImageEntry> out = new ArrayList<>();
        for (String[] r : rows) {
            String nom = r[0];
            if (nom == null || nom.isBlank()) continue;
            if (esIgnorablePorNombre(nom)) continue;

            Path p = Path.of(BASE_DIR, r[3], r[2], r[1], nom);
            out.add(new ImageEntry(p.toFile(), nom));
        }

        out.sort(Comparator.comparing(ImageEntry::getNombre));
        return out;
    }

    /** Variante null-safe de tu verificaAltasBajas (para uso interno en masivo). */
    private boolean verificaAltasBajasSafe(String barcode) {
        String sql1 = "SELECT nombramiento FROM digitales WHERE inv='" + barcode + "' AND carpeta LIKE 'Bajas'";
        String sql2 = "SELECT nombramiento FROM digitales WHERE inv='" + barcode + "' AND carpeta LIKE 'Altas'";

        List<String[]> bajas = cron.consultaCompleta(sql1);
        List<String[]> altas = cron.consultaCompleta(sql2);

        int nb = (bajas == null) ? 0 : bajas.size();
        int na = (altas == null) ? 0 : altas.size();

        return nb > 0 && nb == na;
    }

    /** Regla: ignorar *_000.* (como venías manejando). */
    private boolean esIgnorablePorNombre(String nom) {
        String base = nom.toLowerCase(Locale.ROOT);
        return base.matches(".*_000\\.(jpg|jpeg|tif|tiff)$");
    }
    
    public void generarContactosMasivos(List<String> barcodes) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta destino (contactos masivos)");
        File outDir = dc.showDialog(new Stage());
        if (outDir == null) return;
        generarContactosMasivos(barcodes, outDir);
    }

}
