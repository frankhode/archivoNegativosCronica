package com.archivonegativoscronica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

class PreCatalogadorOMatic {

    // --- existentes ---
    ComboBox<String> conjuntos;
    HBox regExistentes;
    Funciones cron;
    int numReg, nroProgr;
    Label labelCounter;
    VBox display;             // contenedor de formulario (izquierda)
    BackgroundFill backgroundFill;
    Background background;
    String conjuntoTemp;
    List<String[]> regIndi;
    Button siguienteButton, anteriorButton, aceptarButton, cancelarButton, guardarCambiosButton;
    ListView<String> listView;
    HashMap<String, List<String[]>> regsParaActualizar;
    ConjuntosParaAleph conjuntosParaAleph;

    // paneles
    VBox leftPane;
    VBox rightPane;
    HBox bottomBar;

    // selector tipo
    ChoiceBox<String> choiceBox;

    // --- NUEVO: editores + estado ---
    private final HashMap<Integer, TextField> editors = new HashMap<>();
    private final HashMap<Integer, String> originalValues = new HashMap<>();
    private String barcodeOriginal = null;

    // --- NUEVO: para tab-order y atajos ---
    private final ArrayList<Node> focusOrder = new ArrayList<>();
    private TextField regSearchField = null;

    PreCatalogadorOMatic(Funciones cron) throws IOException {
        Stage primaryStage = new Stage();
        this.cron = cron;
        numReg = 0;
        nroProgr = 0;

        conjuntosParaAleph = new ConjuntosParaAleph(cron, 0, true, true, true);
        regIndi = conjuntosParaAleph.getRegIndi();
        if (regIndi == null) regIndi = new ArrayList<>();
        Collections.sort(regIndi, (a, b) -> safe(a, 5).compareToIgnoreCase(safe(b, 5)));

        regsParaActualizar = new HashMap<>();
        conjuntoTemp = "";

        // --- UI base ---
        BorderPane root = new BorderPane();
        root.setPrefWidth(980);
        root.setPrefHeight(520);

        // Top: contador + (espaciador)
        labelCounter = new Label();
        labelCounter.setPrefWidth(120);
        labelCounter.setAlignment(Pos.CENTER);
        labelCounter.setBackground(new Background(new BackgroundFill(Color.CORAL, null, null)));
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label titulo = new Label("PreCatalogador-O-Matic");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        topBar.getChildren().addAll(titulo, spacer, labelCounter);
        root.setTop(topBar);

        // Left: formulario inventario
        display = new VBox();
        display.setSpacing(8);
        display.setPadding(new Insets(12));
        display.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, null, null)));

        leftPane = new VBox(display);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(560);

        // Right: selector tipo + UI específica
        rightPane = new VBox();
        rightPane.setPadding(new Insets(10));
        rightPane.setSpacing(10);
        rightPane.setPrefWidth(380);

        // ChoiceBox tipo
        choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("", "Conjuntos", "Reg. exist", "Reg. ind.");
        choiceBox.setPrefWidth(180);

        Label tipoLbl = new Label("Tipo:");
        HBox tipoRow = new HBox(tipoLbl, choiceBox);
        tipoRow.setAlignment(Pos.CENTER_LEFT);
        tipoRow.setSpacing(10);

        // contenedores dinámicos
        VBox rightDynamic = new VBox();
        rightDynamic.setSpacing(10);

        rightPane.getChildren().addAll(tipoRow, rightDynamic);

        // Bottom: botones
        aceptarButton = new Button("Aceptar");
        guardarCambiosButton = new Button("Guardar cambios");
        anteriorButton = new Button("Anterior");
        siguienteButton = new Button("Siguiente");
        cancelarButton = new Button("Cancelar");

        // Guardar: empieza deshabilitado hasta detectar cambios
        guardarCambiosButton.setDisable(true);

        bottomBar = new HBox(aceptarButton, guardarCambiosButton, anteriorButton, siguienteButton, cancelarButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setSpacing(10);
        bottomBar.setPadding(new Insets(12));
        root.setBottom(bottomBar);

        // Centro: Left + Right
        HBox center = new HBox(leftPane, rightPane);
        center.setSpacing(10);
        root.setCenter(center);

        Scene scene = new Scene(root);

        // --- Eventos ---
        choiceBox.setOnAction((ActionEvent e) -> {
            String v = choiceBox.getValue();
            if (v == null) v = "";

            rightDynamic.getChildren().clear();
            regSearchField = null;
            listView = null;
            conjuntos = null;

            switch (v) {
                case "Conjuntos":
                    nroProgr = 1;
                    conjuntos = conjuntos(primaryStage);
                    rightDynamic.getChildren().add(conjuntos);
                    break;

                case "Reg. exist":
                    nroProgr = 2;
                    Node regUI = regExistentesUI(); // crea search + list
                    rightDynamic.getChildren().add(regUI);
                    break;

                case "Reg. ind.":
                    nroProgr = 3;
                    // no necesita UI extra
                    rightDynamic.getChildren().add(new Label("Se cargará como IND (Ctrl+I en cualquier momento)."));
                    break;

                default:
                    nroProgr = 0;
                    break;
            }

            rebuildFocusOrder();
        });

        aceptarButton.setOnAction((ActionEvent event) -> correPrograma(nroProgr));
        guardarCambiosButton.setOnAction((ActionEvent event) -> guardarCambiosInventario());
        siguienteButton.setOnAction((ActionEvent event) -> {
            Platform.runLater(() -> {
                if (regIndi.isEmpty()) return;
                numReg = Math.min(numReg + 1, regIndi.size() - 1);
                muestraInventarioEditable();
                rebuildFocusOrder();
                requestFocusFirstField();
            });
        });
        anteriorButton.setOnAction((ActionEvent event) -> {
            Platform.runLater(() -> {
                if (regIndi.isEmpty()) return;
                numReg = Math.max(numReg - 1, 0);
                muestraInventarioEditable();
                rebuildFocusOrder();
                requestFocusFirstField();
            });
        });
        cancelarButton.setOnAction((ActionEvent event) -> primaryStage.close());

        // --- Atajos globales ---
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            
            // ESC: descartar registro actual (solo de la lista de trabajo)
            if (e.getCode() == KeyCode.ESCAPE) {
                descartarRegistroActual();
                e.consume();
                return;
            }

            // Ctrl+C: ir a Conjuntos
            if (e.isControlDown() && e.getCode() == KeyCode.C) {
                choiceBox.getSelectionModel().select("Conjuntos");
                // fuerza el handler del choiceBox para armar UI
                choiceBox.getOnAction().handle(new ActionEvent());
                rebuildFocusOrder();
                e.consume();
                return;
            }

            // Ctrl+R: ir a Reg. exist
            if (e.isControlDown() && e.getCode() == KeyCode.R) {
                choiceBox.getSelectionModel().select("Reg. exist");
                choiceBox.getOnAction().handle(new ActionEvent());
                rebuildFocusOrder();
                // foco directo al campo de búsqueda si existe
                Platform.runLater(() -> {
                    if (regSearchField != null) regSearchField.requestFocus();
                });
                e.consume();
                return;
            }

            // Ctrl+I: IND inmediato, siempre disponible
            if (e.isControlDown() && e.getCode() == KeyCode.I) {
                choiceBox.getSelectionModel().select("Reg. ind.");
                nroProgr = 3;
                correPrograma(3);
                e.consume();
                return;
            }

            // Enter dentro de inventario: Guardar cambios (si está habilitado)
            if (e.getCode() == KeyCode.ENTER) {
                if (isFocusInInventarioFields() && !guardarCambiosButton.isDisabled()) {
                    guardarCambiosButton.fire();
                    e.consume();
                    return;
                }
            }

            // Ctrl+Enter en Reg. exist: aceptar
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                if (nroProgr == 2) {
                    aceptarButton.fire();
                    e.consume();
                    return;
                }
            }

            // Navegación con PageUp / PageDown (como antes)
            if (e.getCode() == KeyCode.PAGE_DOWN) {
                siguienteButton.fire();
                e.consume();
            }
            if (e.getCode() == KeyCode.PAGE_UP) {
                anteriorButton.fire();
                e.consume();
            }

            // Tab order controlado
            if (e.getCode() == KeyCode.TAB) {
                if (!focusOrder.isEmpty()) {
                    boolean backwards = e.isShiftDown();
                    moveFocus(backwards);
                    e.consume();
                }
            }
        });

        // Setup ventana
        primaryStage.setTitle("PreCatalogador-O-Matic");
        primaryStage.setScene(scene);

        // Inicial: mostrar inventario
        muestraInventarioEditable();
        rebuildFocusOrder();

        primaryStage.show();

        // foco inicial en barcode
        Platform.runLater(this::requestFocusFirstField);
    }

    // =========================================================
    // UI: Inventario editable (izquierda)
    // =========================================================
    private void muestraInventarioEditable() {
        display.getChildren().clear();
        editors.clear();
        originalValues.clear();

        if (regIndi == null || regIndi.isEmpty()) {
            display.getChildren().add(new Label("No hay registros IND para procesar."));
            labelCounter.setText("0 / 0");
            actualizarEstadoBotones();
            return;
        }

        if (numReg < 0) numReg = 0;
        if (numReg > regIndi.size() - 1) numReg = regIndi.size() - 1;

        String[] row = regIndi.get(numReg);
        barcodeOriginal = safe(row, 0);

        // Form compacto
        display.getChildren().add(rowEditable("BARCODE", 0, safe(row, 0), 220));
        display.getChildren().add(rowEditable("nroA", 1, safe(row, 1), 220));
        display.getChildren().add(rowTexto("nroNid", safe(row, 2)));
        display.getChildren().add(rowTexto("nroAnm", safe(row, 3)));
        display.getChildren().add(rowEditable("Fotógrafo", 4, safe(row, 4), 360));
        display.getChildren().add(rowEditable("Título", 5, safe(row, 5), 420));
        display.getChildren().add(rowEditable("Fecha", 6, safe(row, 6), 180));
        display.getChildren().add(rowTexto("Observaciones", safe(row, 7)));
        display.getChildren().add(rowEditable("UFI", 8, safe(row, 8), 240));

        labelCounter.setText((numReg + 1) + " / " + regIndi.size());

        // Al mostrar un registro nuevo: guardar deshabilitado
        guardarCambiosButton.setDisable(true);
        actualizarEstadoBotones();
    }

    private Node rowTexto(String label, String value) {
        Label l = new Label(label + ":");
        l.setMinWidth(110);
        l.setStyle("-fx-font-weight: bold;");
        Label v = new Label(value == null ? "" : value);
        v.setWrapText(true);
        HBox hb = new HBox(l, v);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setSpacing(10);
        return hb;
    }

    private Node rowEditable(String label, int index, String value, double width) {
        Label l = new Label(label + ":");
        l.setMinWidth(110);
        l.setStyle("-fx-font-weight: bold;");
        TextField tf = new TextField(value == null ? "" : value);
        tf.setPrefWidth(width);

        editors.put(index, tf);
        originalValues.put(index, tf.getText());

        // Dirty tracking: habilita Guardar si hay cambios
        tf.textProperty().addListener((obs, oldV, newV) -> updateDirtyState());

        HBox hb = new HBox(l, tf);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setSpacing(10);
        return hb;
    }

    private void updateDirtyState() {
        boolean dirty = false;
        for (Integer idx : originalValues.keySet()) {
            String orig = originalValues.get(idx);
            TextField tf = editors.get(idx);
            if (tf == null) continue;
            String now = tf.getText() == null ? "" : tf.getText();
            if (!now.equals(orig == null ? "" : orig)) {
                dirty = true;
                break;
            }
        }
        guardarCambiosButton.setDisable(!dirty);
    }

    private boolean isFocusInInventarioFields() {
        Node f = (aceptarButton.getScene() == null) ? null : aceptarButton.getScene().getFocusOwner();
        if (f == null) return false;
        return editors.values().contains(f);
    }

    private void requestFocusFirstField() {
        //TextField tf = editors.get(0); // barcode
        TextField tf = editors.get(5); // barcode
        if (tf != null) tf.requestFocus();
    }

    // =========================================================
    // Derecha: UI de programas
    // =========================================================
    private ComboBox<String> conjuntos(Stage stage) {
        ComboBox<String> conjuntoComboBox = new ComboBox<>();
        List<String> conj = obtenerConjuntosDeLaBaseDeDatos();
        conjuntoComboBox.getItems().addAll(conj);
        conjuntoComboBox.getItems().add("Crear nuevo conjunto");

        conjuntoComboBox.setOnAction((event) -> {
            String seleccion = conjuntoComboBox.getSelectionModel().getSelectedItem();
            if (seleccion == null) return;

            if (seleccion.equals("Crear nuevo conjunto")) {
                Stage modalStage = new Stage();
                modalStage.setTitle("Nuevo conjunto");
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.initOwner(stage);

                Label label = new Label("Ingrese el nombre del nuevo conjunto:");
                TextField textField = new TextField();
                Button aceptarButtonModal = new Button("Aceptar");

                VBox vbox = new VBox(label, textField, aceptarButtonModal);
                vbox.setAlignment(Pos.CENTER);
                vbox.setSpacing(10);
                vbox.setPadding(new Insets(10));

                Scene modalScene = new Scene(vbox, 320, 200);
                modalStage.setScene(modalScene);

                aceptarButtonModal.setOnAction((e) -> {
                    String nuevoConjunto = textField.getText();
                    if (nuevoConjunto != null && !nuevoConjunto.trim().isEmpty()) {
                        conjuntoComboBox.getItems().add(nuevoConjunto);
                        conjuntoComboBox.getSelectionModel().select(nuevoConjunto);
                        conjuntoTemp = nuevoConjunto;
                    }
                    modalStage.close();
                });

                modalStage.showAndWait();
            } else {
                conjuntoTemp = seleccion;
            }
        });

        return conjuntoComboBox;
    }

    private List<String> obtenerConjuntosDeLaBaseDeDatos() {
        String consulta = "SELECT DISTINCT titulo FROM conjuntos WHERE "
                + "status LIKE '1' AND "
                + "titulo NOT LIKE 'IND' AND "
                + "barcode NOT IN (SELECT barcode FROM items) "
                + "ORDER BY titulo";
        return cron.consultaSimple(consulta, 1);
    }

    private Node regExistentesUI() {
        Label lbl = new Label("Buscar reg. existente:");
        regSearchField = new TextField();
        regSearchField.setPrefWidth(280);

        listView = new ListView<>();
        listView.setPrefHeight(320);

        // carga dinámica
        regSearchField.textProperty().addListener((obs, o, n) -> {
            List<String> registros = obtenerRegistrosDeLaBaseDeDatos(n == null ? "" : n);
            Collections.sort(registros);
            listView.getItems().setAll(registros);
        });

        // Ctrl+Enter desde búsqueda: aceptar
        regSearchField.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                aceptarButton.fire();
                e.consume();
            }
        });

        // Ctrl+Enter desde lista: aceptar
        listView.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                aceptarButton.fire();
                e.consume();
            }
        });

        VBox v = new VBox(lbl, regSearchField, listView);
        v.setSpacing(8);
        return v;
    }

    public List<String> obtenerRegistrosDeLaBaseDeDatos(String busqueda) {
        String b = busqueda == null ? "" : busqueda.trim();
        if (b.isEmpty()) return new ArrayList<>();
        String consulta = "SELECT titulo245 FROM registros WHERE titulo245 LIKE '%" + sqlEscape(b) + "%';";
        return cron.consultaSimple(consulta, 1);
    }

    // =========================================================
    // Guardar inventario
    // =========================================================
    private void guardarCambiosInventario() {
        if (regIndi == null || regIndi.isEmpty()) return;
        if (barcodeOriginal == null || barcodeOriginal.trim().isEmpty()) return;

        String[] row = regIndi.get(numReg);

        String newBarcode = getEditorValue(0, safe(row, 0));
        String newNroA = getEditorValue(1, safe(row, 1));
        String newAutor = getEditorValue(4, safe(row, 4));
        String newTitulo = getEditorValue(5, safe(row, 5));
        String newFecha = getEditorValue(6, safe(row, 6));
        String newUfi = getEditorValue(8, safe(row, 8));

        String consulta =
                "UPDATE inventario SET "
                        + "barcode='" + sqlEscape(newBarcode) + "',"
                        + "nroA='" + sqlEscape(newNroA) + "',"
                        + "autor='" + sqlEscape(newAutor) + "',"
                        + "titulo='" + sqlEscape(newTitulo) + "',"
                        + "fechaISO='" + sqlEscape(newFecha) + "',"
                        + "ufi='" + sqlEscape(newUfi) + "' "
                        + "WHERE barcode='" + sqlEscape(barcodeOriginal) + "';";

        cron.consultaSimple(consulta, 1);

        // reflejo en memoria
        row[0] = newBarcode;
        row[1] = newNroA;
        row[4] = newAutor;
        row[5] = newTitulo;
        row[6] = newFecha;
        row[8] = newUfi;

        barcodeOriginal = newBarcode;

        // “commit” de originales → deshabilita Guardar
        for (Integer idx : originalValues.keySet()) {
            TextField tf = editors.get(idx);
            if (tf != null) originalValues.put(idx, tf.getText() == null ? "" : tf.getText());
        }
        guardarCambiosButton.setDisable(true);
    }

    private String getEditorValue(int index, String fallback) {        
        TextField tf = editors.get(index);
        if (tf == null) return fallback == null ? "" : fallback;
        String v = tf.getText();
        return v == null ? "" : v.trim();
    }

    // =========================================================
    // Corre programa (igual a tu lógica, con mínimos checks)
    // =========================================================
    private void correPrograma(int nroProgr) {
        if (regIndi == null || regIndi.isEmpty()) return;

        String consulta;
        switch (nroProgr) {
            case 1: { // Conjuntos
                String nombre = (conjuntos == null) ? "" : conjuntos.getValue();
                String bar = regIndi.get(numReg)[0];
                if (nombre == null || nombre.trim().isEmpty()) return;

                consulta = "INSERT INTO conjuntos(titulo, barcode, status) "
                        + "VALUES ('" + sqlEscape(nombre) + "','" + sqlEscape(bar) + "','1')";
                cron.consultaSimple(consulta, 1);

                regIndi.remove(numReg);
                if (numReg > 0) numReg--;
                muestraInventarioEditable();
                break;
            }
            case 2: { // Reg. exist
                String bar = regIndi.get(numReg)[0];
                String tit = (listView == null) ? null : listView.getSelectionModel().getSelectedItem();
                if (tit == null || tit.trim().isEmpty()) return;

                consulta = "SELECT sys FROM registros WHERE titulo245 LIKE '" + sqlEscape(tit) + "'";
                List<String> lista = cron.consultaSimple(consulta, 1);
                if (lista == null || lista.isEmpty()) return;

                String sys = lista.get(0);

                consulta = "INSERT INTO conjuntos(titulo, barcode, status) "
                        + "VALUES ('" + sqlEscape(sys) + "','" + sqlEscape(bar) + "','2')";
                cron.consultaSimple(consulta, 1);

                regIndi.remove(numReg);
                if (numReg > 0) numReg--;
                muestraInventarioEditable();
                break;
            }
            case 3: { // IND
                String bar = regIndi.get(numReg)[0];
                consulta = "INSERT INTO conjuntos(titulo, barcode, status) "
                        + "VALUES ('IND','" + sqlEscape(bar) + "','1')";
                cron.consultaSimple(consulta, 1);

                regIndi.remove(numReg);
                if (numReg > 0) numReg--;
                muestraInventarioEditable();
                break;
            }
            default:
                break;
        }

        actualizarEstadoBotones();
        rebuildFocusOrder();
        Platform.runLater(this::requestFocusFirstField);
    }

    // =========================================================
    // Tab order controlado
    // =========================================================
    private void rebuildFocusOrder() {
        focusOrder.clear();

        // Inventario: en el orden deseado
        addIfNotNull(focusOrder, editors.get(0)); // barcode
        addIfNotNull(focusOrder, editors.get(1)); // nroA
        addIfNotNull(focusOrder, editors.get(4)); // fotógrafo
        addIfNotNull(focusOrder, editors.get(5)); // título
        addIfNotNull(focusOrder, editors.get(6)); // fecha
        addIfNotNull(focusOrder, editors.get(8)); // ufi

        // Botón guardar (después de inventario)
        addIfNotNull(focusOrder, guardarCambiosButton);

        // Tipo
        addIfNotNull(focusOrder, choiceBox);

        // Si es Reg. exist: búsqueda y lista
        if (nroProgr == 2) {
            addIfNotNull(focusOrder, regSearchField);
            addIfNotNull(focusOrder, listView);
        }

        // Aceptar al final (si querés, lo dejamos; si no, lo saco)
        addIfNotNull(focusOrder, aceptarButton);
    }

    private void addIfNotNull(List<Node> list, Node n) {
        if (n != null) list.add(n);
    }

    private void moveFocus(boolean backwards) {
        Scene sc = aceptarButton.getScene();
        if (sc == null) return;
        Node current = sc.getFocusOwner();
        if (current == null) {
            focusOrder.get(0).requestFocus();
            return;
        }

        int idx = focusOrder.indexOf(current);
        if (idx < 0) {
            // Si está enfocado algo fuera de la lista, volvemos al inicio
            focusOrder.get(0).requestFocus();
            return;
        }

        int next = backwards ? idx - 1 : idx + 1;
        if (next < 0) next = focusOrder.size() - 1;
        if (next >= focusOrder.size()) next = 0;

        focusOrder.get(next).requestFocus();
    }

    // =========================================================
    // estado botones
    // =========================================================
    private void actualizarEstadoBotones() {
        boolean has = regIndi != null && !regIndi.isEmpty();

        if (aceptarButton != null) aceptarButton.setDisable(!has);
        if (anteriorButton != null) anteriorButton.setDisable(!has || numReg <= 0);
        if (siguienteButton != null) siguienteButton.setDisable(!has || numReg >= (regIndi.size() - 1));
        // guardarCambiosButton: lo maneja updateDirtyState / muestraInventarioEditable
        if (!has && guardarCambiosButton != null) guardarCambiosButton.setDisable(true);
    }

    // =========================================================
    // helpers
    // =========================================================
    private String safe(String[] arr, int idx) {
        if (arr == null) return "";
        if (idx < 0 || idx >= arr.length) return "";
        return arr[idx] == null ? "" : arr[idx];
    }

    private String sqlEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "''");
    }
    
    private void descartarRegistroActual() {
        if (regIndi == null || regIndi.isEmpty()) return;

        // Quitar el registro actual SOLO de la lista de trabajo (no DB)
        regIndi.remove(numReg);

        // Ajustar índice
        if (numReg > 0) numReg--;
        if (numReg < 0) numReg = 0;
        if (!regIndi.isEmpty() && numReg > regIndi.size() - 1) numReg = regIndi.size() - 1;

        // Refrescar UI
        muestraInventarioEditable();
        actualizarEstadoBotones();
        rebuildFocusOrder();
        Platform.runLater(this::requestFocusFirstField);
    }

    public static String quitarPuntoFinal(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.endsWith(".")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

}
