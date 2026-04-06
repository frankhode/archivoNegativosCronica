package com.archivonegativoscronica;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.nio.file.Path ;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

class PedidosParaUsuarios {

    // ---- UI
    private TextField barcodeField;
    private TableView<ImageEntry> tableView;
    private final CheckBox chkAllAlta = new CheckBox();
    private final CheckBox chkAllBaja = new CheckBox();

    // ---- Datos
    private final ObservableList<ImageEntry> filas = FXCollections.observableArrayList();
    private final Funciones cron;
    private final Stage stage;

    // ---- Config
    private String basePath = "U:/Mapo-Cronica/004-ordenados_DMFC/";

    // ---- Carga perezosa / rendimiento
    private final ExecutorService altaPool;   // TIFF → genera JPG temporal si hace falta
    private final ExecutorService bajaPool;   // JPG directo
    private final LruImageCache thumbCache = new LruImageCache(400); // ~400 thumbs

    // tracks por clave (ruta) para no duplicar trabajos
    private final ConcurrentMap<String, Future<?>> runningTasks = new ConcurrentHashMap<>();

    // temporales a borrar al salir
    private final Set<Path> tempFiles = ConcurrentHashMap.newKeySet();
    private Path tempDir;

    private Alert alertaProceso;

    PedidosParaUsuarios(Funciones cron) {
        this.cron = cron;
        this.stage = new Stage();

        // pools separados para que JPG no esperen a TIFF
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.altaPool = Executors.newFixedThreadPool(Math.max(1, cores / 2));
        this.bajaPool = Executors.newFixedThreadPool(Math.max(1, cores / 2));

        // preparar carpeta temporal
        try {
            String base = System.getProperty("user.dir");
            this.tempDir = Paths.get(base, "temp_images");
            Files.createDirectories(tempDir);
        } catch (IOException ignored) { }

        // cleanup al cerrar la ventana
        stage.setOnCloseRequest(e -> cleanupAndShutdown());

        // fallback por si se cierra la JVM sin cerrar esta ventana
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupAndShutdown));

        construirUI();
        stage.setTitle("Pedidos para Usuarios");
        stage.setScene(new Scene(crearRoot(), 980, 620));
        stage.show();
    }

    private void construirUI() {
        barcodeField = new TextField();
        barcodeField.setPromptText("Ingrese código de barras");
        barcodeField.setOnAction(e -> buscarDigital());

        tableView = new TableView<>();
        configurarTabla();
        configurarDobleClickPreview();
    }

    private Pane crearRoot() {
        Button buscarBtn = new Button("Buscar digital");
        buscarBtn.setOnAction(e -> buscarDigital());

        Button copiarBtn = new Button("Copiar imágenes");
        copiarBtn.setOnAction(e -> copiarSeleccion());

        VBox root = new VBox(10,
                barcodeField,
                buscarBtn,
                tableView,
                copiarBtn
        );
        root.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return root;
    }

    // =====================
    // Tabla y celdas
    // =====================
    private void configurarTabla() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Nombramiento
        TableColumn<ImageEntry, String> colNom = new TableColumn<>("Nombramiento");
        colNom.setCellValueFactory(d -> d.getValue().nombramientoProperty());
        colNom.setMinWidth(260);

        // Alta (thumb + checkbox)
        TableColumn<ImageEntry, Boolean> colAlta = new TableColumn<>("Alta (TIFF)");
        colAlta.setEditable(true);
        colAlta.setMinWidth(260);

        // header con maestro
        HBox hAlta = new HBox(6, new Label("Alta (TIFF)"), chkAllAlta);
        hAlta.setAlignment(Pos.CENTER_LEFT);
        colAlta.setGraphic(hAlta);
        chkAllAlta.setOnAction(e -> {
            boolean v = chkAllAlta.isSelected();
            for (ImageEntry it : filas) if (it.altaExistsProperty().get()) it.setSelAlta(v);
        });

        colAlta.setCellValueFactory(d -> d.getValue().selAltaProperty());
        colAlta.setCellFactory(col -> new ThumbCell(true)); // true = esAlta

        // Baja (thumb + checkbox)
        TableColumn<ImageEntry, Boolean> colBaja = new TableColumn<>("Baja (JPG)");
        colBaja.setEditable(true);
        colBaja.setMinWidth(260);

        HBox hBaja = new HBox(6, new Label("Baja (JPG)"), chkAllBaja);
        hBaja.setAlignment(Pos.CENTER_LEFT);
        colBaja.setGraphic(hBaja);
        chkAllBaja.setOnAction(e -> {
            boolean v = chkAllBaja.isSelected();
            for (ImageEntry it : filas) if (it.bajaExistsProperty().get()) it.setSelBaja(v);
        });

        colBaja.setCellValueFactory(d -> d.getValue().selBajaProperty());
        colBaja.setCellFactory(col -> new ThumbCell(false)); // false = esBaja

        tableView.getColumns().setAll(colNom, colAlta, colBaja);
        tableView.setItems(filas);
        tableView.setEditable(true);
    }

    // Celda con spinner + imageview + checkbox, y lazy-load por columna
    private class ThumbCell extends TableCell<ImageEntry, Boolean> {
        private final boolean esAlta;
        private final ImageView iv = new ImageView();
        private final ProgressIndicator spinner = new ProgressIndicator();

        ThumbCell(boolean esAlta) {
            this.esAlta = esAlta;
            iv.setPreserveRatio(true);
            iv.setFitWidth(200);
            spinner.setPrefSize(24, 24);

            CheckBox cb = new CheckBox();
            cb.setDisable(true); // se habilita si existe archivo
            cb.selectedProperty().addListener((o, ov, nv) -> {
                ImageEntry row = getCurrentItem();
                if (row != null) {
                    if (esAlta) row.setSelAlta(nv); else row.setSelBaja(nv);
                }
            });

            // layout: imagen/spinner arriba, checkbox abajo
            VBox box = new VBox(6, new StackPane(iv, spinner), cb);
            box.setAlignment(Pos.TOP_CENTER);
            box.setPadding(new Insets(6));
            setGraphic(box);

            // cuando la celda se vuelve visible, dispara la carga
            itemProperty().addListener((o, ov, nv) -> requestLoad());
        }

        @Override
        protected void updateItem(Boolean selected, boolean empty) {
            super.updateItem(selected, empty);
            if (empty || getCurrentItem() == null) {
                iv.setImage(null);
                spinner.setVisible(false);
                setDisable(true);
                return;
            }
            setDisable(false);

            ImageEntry row = getCurrentItem();
            boolean exists = esAlta ? row.altaExistsProperty().get() : row.bajaExistsProperty().get();

            // habilitar/disable checkbox de selección
            CheckBox cb = (CheckBox) ((VBox) getGraphic()).getChildren().get(1);
            cb.setDisable(!exists);
            cb.setSelected(esAlta ? row.isSelAlta() : row.isSelBaja());

            // Si no existe archivo, dejar vacío
            if (!exists) {
                iv.setImage(null);
                spinner.setVisible(false);
                return;
            }

            requestLoad();
        }

        private void requestLoad() {
            ImageEntry row = getCurrentItem();
            if (row == null) return;

            String ruta = esAlta ? row.getRutaAlta() : row.getRutaBaja();
            if (ruta == null) {
                iv.setImage(null);
                spinner.setVisible(false);
                return;
            }

            // cache
            Image cache = thumbCache.get(ruta);
            if (cache != null) {
                iv.setImage(cache);
                spinner.setVisible(false);
                return;
            }

            // mostrar spinner y lanzar tarea independiente por columna
            spinner.setVisible(true);
            iv.setImage(null);

            Supplier<Image> supplier = () -> {
                try {
                    if (esAlta) {
                        // TIFF → generar JPG temporal si hace falta
                        String jpgTmp = generarJpgTemporalDesdeTiff(ruta);
                        if (jpgTmp == null) return null;
                        return new Image(Paths.get(jpgTmp).toUri().toString(), 540, 540, true, true, true);
                    } else {
                        return new Image(Paths.get(ruta).toUri().toString(), 540, 540, true, true, true);
                    }
                } catch (Exception ex) {
                    return null;
                }
            };

            Runnable uiApply = () -> {
                spinner.setVisible(false);
                Image img = thumbCache.get(ruta);
                if (img != null) iv.setImage(img);
            };

            String key = (esAlta ? "A|" : "B|") + ruta;
            runningTasks.computeIfAbsent(key, k -> {
                Callable<Void> task = () -> {
                    Image img = supplier.get();
                    if (img != null) thumbCache.put(ruta, img);
                    Platform.runLater(uiApply);
                    runningTasks.remove(k);
                    return null;
                };
                return (esAlta ? altaPool : bajaPool).submit(task);
            });
        }

        private ImageEntry getCurrentItem() {
            return getIndex() >= 0 && getIndex() < tableView.getItems().size()
                    ? tableView.getItems().get(getIndex())
                    : null;
        }
    }

    // =====================
    // Buscar y unificar filas
    // =====================
    private void buscarDigital() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            mostrarAlerta("Ingrese un código de barras");
            return;
        }
        cargarImagenesDesdeBD(barcode);
    }

    private void cargarImagenesDesdeBD(String barcode) {
        filas.clear();
        chkAllAlta.setSelected(false);
        chkAllBaja.setSelected(false);

        String sql = "SELECT * FROM digitales WHERE inv LIKE '" + barcode + "'";
        List<String[]> res = cron.consultaCompleta(sql);

        // Unificar por nombramiento base (sin extensión)
        Map<String, ImageEntry> map = new LinkedHashMap<>();

        for (String[] f : res) {
            // [0]=nombramiento, [1]=inv, [2]=cajon, [3]=carpeta
            if (f.length < 4) continue;
            String nombramiento = f[0];
            String dir = Paths.get(basePath, f[3], f[2], f[1]).toString();

            String base = quitarExtension(nombramiento);
            ImageEntry row = map.computeIfAbsent(base, k -> new ImageEntry(base));

            // Resolver rutas
            Path pNom = Paths.get(dir, nombramiento);
            Path pAlta = resolverAlta(dir, base);
            Path pBaja = resolverBaja(dir, base);

            if (pAlta == null && pBaja == null && Files.exists(pNom)) {
                String ext = extensionDe(nombramiento);
                if (esTiff(ext)) pAlta = pNom;
                else if (esJpg(ext)) pBaja = pNom;
            }

            if (pAlta != null && Files.exists(pAlta)) row.setAlta(pAlta.toString());
            if (pBaja != null && Files.exists(pBaja)) row.setBaja(pBaja.toString());
        }

        filas.addAll(map.values());
        if (filas.isEmpty()) mostrarAlerta("No se encontraron imágenes para el código: " + barcode);
    }

    private static String quitarExtension(String n) {
        int i = n.lastIndexOf('.');
        return i > 0 ? n.substring(0, i) : n;
    }
    private static String extensionDe(String n) {
        int i = n.lastIndexOf('.');
        return i > 0 ? n.substring(i + 1) : "";
    }
    private static boolean esJpg(String ext) {
        String e = ext.toLowerCase(Locale.ROOT);
        return e.equals("jpg") || e.equals("jpeg");
    }
    private static boolean esTiff(String ext) {
        String e = ext.toLowerCase(Locale.ROOT);
        return e.equals("tif") || e.equals("tiff");
    }
    private static Path resolverConExt(String dir, String base, String... exts) {
        for (String e : exts) {
            Path p = Paths.get(dir, base + "." + e);
            if (Files.exists(p)) return p;
            p = Paths.get(dir, base + "." + e.toUpperCase(Locale.ROOT));
            if (Files.exists(p)) return p;
        }
        return null;
    }
    private static Path resolverAlta(String dir, String base) { return resolverConExt(dir, base, "tif", "tiff"); }
    private static Path resolverBaja(String dir, String base) { return resolverConExt(dir, base, "jpg", "jpeg"); }

    // =====================
    // Copiar selección
    // =====================
    private void copiarSeleccion() {
        List<Path> aCopiar = new ArrayList<>();
        for (ImageEntry it : filas) {
            if (it.isSelAlta() && it.getRutaAlta() != null && it.altaExistsProperty().get())
                aCopiar.add(Paths.get(it.getRutaAlta()));
            if (it.isSelBaja() && it.getRutaBaja() != null && it.bajaExistsProperty().get())
                aCopiar.add(Paths.get(it.getRutaBaja()));
        }
        if (aCopiar.isEmpty()) {
            mostrarAlerta("No hay archivos seleccionados.");
            return;
        }

        DirectoryChooser ch = new DirectoryChooser();
        ch.setTitle("Seleccionar carpeta de destino");
        File dest = ch.showDialog(stage);
        if (dest == null) return;

        TextInputDialog d = new TextInputDialog("NuevaCarpeta");
        d.setTitle("Nombre de la Carpeta");
        d.setHeaderText("Ingrese el nombre de la carpeta donde se guardarán los archivos:");
        d.setContentText("Nombre:");
        Optional<String> r = d.showAndWait();
        if (!r.isPresent() || r.get().trim().isEmpty()) {
            mostrarAlerta("Debe ingresar un nombre válido.");
            return;
        }
        Path destino = dest.toPath().resolve(r.get().trim());
        try { Files.createDirectories(destino); } catch (IOException ignored) { }

        int ok = 0, fail = 0;
        for (Path src : aCopiar) {
            try {
                Path dst = destino.resolve(src.getFileName().toString());
                Path finalDst = evitarOverwrite(dst);
                Files.copy(src, finalDst, StandardCopyOption.COPY_ATTRIBUTES);
                ok++;
            } catch (IOException ex) {
                fail++;
            }
        }
        mostrarAlerta("Copia finalizada.\nCopiados: " + ok + "\nFallidos: " + fail + "\nDestino: " + destino);
    }

    private static Path evitarOverwrite(Path dst) {
        if (!Files.exists(dst)) return dst;
        String name = dst.getFileName().toString();
        String base = quitarExtension(name);
        String ext = extensionDe(name);
        Path dir = dst.getParent();
        int i = 1;
        while (true) {
            String n = base + "_(" + i + ")" + (ext.isEmpty() ? "" : "." + ext);
            Path c = dir.resolve(n);
            if (!Files.exists(c)) return c;
            i++;
        }
    }

    // =====================
    // Preview (doble click)
    // =====================
    private void configurarDobleClickPreview() {
        tableView.setRowFactory(tv -> {
            TableRow<ImageEntry> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (row.isEmpty()) return;
                if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                    ImageEntry it = row.getItem();
                    mostrarPreview(it);
                }
            });
            return row;
        });
    }

    private void mostrarPreview(ImageEntry it) {
        // preferir baja si existe; si no, generar desde alta
        if (it.getRutaBaja() != null && it.bajaExistsProperty().get()) {
            Image img = new Image(Paths.get(it.getRutaBaja()).toUri().toString(), true);
            mostrarVentanaPreview(img, it.getNom() + " (JPG)");
            return;
        }
        if (it.getRutaAlta() != null && it.altaExistsProperty().get()) {
            mostrarModal("Generando vista previa desde TIFF...");
            altaPool.submit(() -> {
                try {
                    String tmp = generarJpgTemporalDesdeTiff(it.getRutaAlta());
                    Platform.runLater(() -> {
                        cerrarModal();
                        if (tmp != null) {
                            Image img = new Image(Paths.get(tmp).toUri().toString(), true);
                            mostrarVentanaPreview(img, it.getNom() + " (desde TIFF)");
                        } else {
                            mostrarAlerta("No se pudo generar la vista previa.");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> { cerrarModal(); mostrarAlerta("Error en preview."); });
                }
            });
            return;
        }
        mostrarAlerta("No hay archivo disponible para previsualizar.");
    }

    private void mostrarVentanaPreview(Image img, String titulo) {
        Stage dlg = new Stage();
        dlg.setTitle("Vista previa - " + titulo);
        dlg.initOwner(stage);
        dlg.initModality(Modality.NONE);

        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        ScrollPane sp = new ScrollPane(iv);
        sp.setPannable(true);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        Slider zoom = new Slider(0.1, 5.0, 1.0);
        zoom.valueProperty().addListener((o, ov, nv) -> iv.setFitWidth(img.getWidth() * nv.doubleValue()));
        CheckBox fit = new CheckBox("Ajustar al ancho");
        fit.setSelected(true);
        fit.selectedProperty().addListener((o, a, b) -> {
            if (b) iv.setFitWidth(sp.getViewportBounds().getWidth());
            else iv.setFitWidth(img.getWidth() * zoom.getValue());
        });
        sp.viewportBoundsProperty().addListener((o, ob, nb) -> { if (fit.isSelected()) iv.setFitWidth(nb.getWidth()); });

        HBox controls = new HBox(10, new Label("Zoom:"), zoom, fit);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8));

        BorderPane root = new BorderPane(sp);
        root.setBottom(controls);

        dlg.setScene(new Scene(root, 980, 720));
        dlg.show();
    }

    // =====================
    // Util: Python embebido
    // =====================
    private String generarJpgTemporalDesdeTiff(String rutaTiff) {
        try {
            String base = System.getProperty("user.dir");
            String py = Paths.get(base, "src", "main", "resources", "python", "python.exe").toString();
            String script = Paths.get(base, "src", "main", "resources", "python", "generar_miniatura.py").toString();

            String nombre = UUID.randomUUID().toString() + ".jpg";
            Path out = tempDir.resolve(nombre);

            ProcessBuilder pb = new ProcessBuilder(py, script, rutaTiff, out.toString());
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line; while ((line = r.readLine()) != null) System.out.println("Python: " + line);
            }
            int code = p.waitFor();
            if (code != 0) return null;

            if (Files.exists(out)) {
                tempFiles.add(out);
                return out.toString();
            }
        } catch (Exception ignored) { }
        return null;
    }

    // =====================
    // Modales y cleanup
    // =====================
    private void mostrarModal(String m) {
        Platform.runLater(() -> {
            if (alertaProceso == null || !alertaProceso.isShowing()) {
                alertaProceso = new Alert(Alert.AlertType.INFORMATION);
                alertaProceso.setTitle("Procesando...");
                alertaProceso.setHeaderText(null);
                alertaProceso.setContentText(m);
                alertaProceso.initOwner(stage);
                alertaProceso.show();
            }
        });
    }
    private void cerrarModal() {
        Platform.runLater(() -> {
            if (alertaProceso != null && alertaProceso.isShowing()) {
                alertaProceso.close();
                alertaProceso = null;
            }
        });
    }
    private void mostrarAlerta(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK);
        a.initOwner(stage);
        a.show();
    }

    private void cleanupAndShutdown() {
        // borrar temporales
        for (Path p : tempFiles) {
            try { Files.deleteIfExists(p); } catch (IOException ignored) { }
        }
        try { Files.deleteIfExists(tempDir); } catch (IOException ignored) { }

        altaPool.shutdownNow();
        bajaPool.shutdownNow();
    }

    // =====================
    // Modelo por fila
    // =====================
    static class ImageEntry {
        private final StringProperty nombramiento = new SimpleStringProperty();
        private final StringProperty rutaAlta = new SimpleStringProperty();
        private final StringProperty rutaBaja = new SimpleStringProperty();
        private final BooleanProperty altaExists = new SimpleBooleanProperty(false);
        private final BooleanProperty bajaExists = new SimpleBooleanProperty(false);
        private final BooleanProperty selAlta = new SimpleBooleanProperty(false);
        private final BooleanProperty selBaja = new SimpleBooleanProperty(false);

        ImageEntry(String nomBase) { this.nombramiento.set(nomBase); }

        public String getNom() { return nombramiento.get(); }
        public StringProperty nombramientoProperty() { return nombramiento; }

        public String getRutaAlta() { return rutaAlta.get(); }
        public String getRutaBaja() { return rutaBaja.get(); }
        public BooleanProperty altaExistsProperty() { return altaExists; }
        public BooleanProperty bajaExistsProperty() { return bajaExists; }

        public boolean isSelAlta() { return selAlta.get(); }
        public boolean isSelBaja() { return selBaja.get(); }
        public void setSelAlta(boolean v) { selAlta.set(v); }
        public void setSelBaja(boolean v) { selBaja.set(v); }
        public BooleanProperty selAltaProperty() { return selAlta; }
        public BooleanProperty selBajaProperty() { return selBaja; }

        public void setAlta(String ruta) {
            rutaAlta.set(ruta);
            altaExists.set(ruta != null && Files.exists(Paths.get(ruta)));
        }
        public void setBaja(String ruta) {
            rutaBaja.set(ruta);
            bajaExists.set(ruta != null && Files.exists(Paths.get(ruta)));
        }
    }

    // =====================
    // Cache LRU simple
    // =====================
    static class LruImageCache extends LinkedHashMap<String, Image> {
        private final int max;
        LruImageCache(int max) {
            super(16, 0.75f, true);
            this.max = max;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > max;
        }
        @Override
        public synchronized Image get(Object key) { return super.get(key); }
        @Override
        public synchronized Image put(String key, Image value) { return super.put(key, value); }
    }
}
