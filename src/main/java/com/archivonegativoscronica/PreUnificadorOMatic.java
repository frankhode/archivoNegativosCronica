package com.archivonegativoscronica;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import javafx.stage.Stage;

class PreUnificadorOMatic {

    private final Funciones cron;
    private final List<String[]> regIndi;
    private final Map<String, List<SugerenciaUnificacion>> cacheSugerencias = new HashMap<>();

    private int numReg = -1;

    private Label labelCounter;
    private VBox display;
    private ListView<SugerenciaUnificacion> listView;
    private Button confirmarButton, descartarButton, anteriorButton, siguienteButton, cancelarButton;

    private final HashMap<Integer, TextField> editors = new HashMap<>();
    private String barcodeOriginal = null;

    PreUnificadorOMatic(Funciones cron) throws IOException {
        this.cron = cron;

        ConjuntosParaAleph conjuntosParaAleph = new ConjuntosParaAleph(cron, 0, true, true, true);
        regIndi = conjuntosParaAleph.getRegIndi() == null ? new ArrayList<>() : conjuntosParaAleph.getRegIndi();

        Stage primaryStage = new Stage();

        BorderPane root = new BorderPane();
        root.setPrefWidth(980);
        root.setPrefHeight(520);

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

        Label titulo = new Label("PreUnificador-O-Matic");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        topBar.getChildren().addAll(titulo, spacer, labelCounter);
        root.setTop(topBar);

        display = new VBox();
        display.setSpacing(8);
        display.setPadding(new Insets(12));
        display.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, null, null)));

        VBox leftPane = new VBox(display);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(560);

        listView = new ListView<>();
        listView.setPrefHeight(420);
        listView.setCellFactory(lv -> new ListCell<SugerenciaUnificacion>() {
            @Override
            protected void updateItem(SugerenciaUnificacion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("[" + item.getNivel() + "] " + item.getTitulo()
                            + "  |  " + item.getMotivo()
                            + "  |  SYS: " + item.getSys());
                }
            }
        });

        VBox rightPane = new VBox();
        rightPane.setPadding(new Insets(10));
        rightPane.setSpacing(10);
        rightPane.setPrefWidth(380);

        Label rightTitle = new Label("Sugerencias de unificación");
        rightTitle.setStyle("-fx-font-weight: bold;");
        rightPane.getChildren().addAll(rightTitle, listView);

        HBox center = new HBox(leftPane, rightPane);
        center.setSpacing(10);
        root.setCenter(center);

        confirmarButton = new Button("Confirmar");
        descartarButton = new Button("Descartar");
        anteriorButton = new Button("Anterior");
        siguienteButton = new Button("Siguiente");
        cancelarButton = new Button("Cancelar");

        HBox bottomBar = new HBox(confirmarButton, descartarButton, anteriorButton, siguienteButton, cancelarButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setSpacing(10);
        bottomBar.setPadding(new Insets(12));
        root.setBottom(bottomBar);

        confirmarButton.setOnAction(e -> confirmarActual());
        descartarButton.setOnAction(e -> descartarActual());
        anteriorButton.setOnAction(e -> irAlAnteriorConSugerencias());
        siguienteButton.setOnAction(e -> irAlSiguienteConSugerencias());
        cancelarButton.setOnAction(e -> primaryStage.close());

        Scene scene = new Scene(root);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                descartarActual();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.PAGE_DOWN) {
                siguienteButton.fire();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.PAGE_UP) {
                anteriorButton.fire();
                e.consume();
                return;
            }
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                confirmarButton.fire();
                e.consume();
                return;
            }
        });

        primaryStage.setTitle("PreUnificador-O-Matic");
        primaryStage.setScene(scene);

        mostrarPrimeroConSugerencias();
        primaryStage.show();
    }
    
    PreUnificadorOMatic(Funciones cron, String filtroWhere) throws IOException {
        this.cron = cron;

        ConjuntosParaAleph conjuntosParaAleph =
        new ConjuntosParaAleph(cron, 0, true, true, true, filtroWhere, false);

        regIndi = conjuntosParaAleph.getRegIndi() == null
                ? new ArrayList<>()
                : conjuntosParaAleph.getRegIndi();

        Stage primaryStage = new Stage();

        BorderPane root = new BorderPane();
        root.setPrefWidth(980);
        root.setPrefHeight(520);

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

        Label titulo = new Label("PreUnificador-O-Matic");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        topBar.getChildren().addAll(titulo, spacer, labelCounter);
        root.setTop(topBar);

        display = new VBox();
        display.setSpacing(8);
        display.setPadding(new Insets(12));
        display.setBackground(new Background(new BackgroundFill(Color.GAINSBORO, null, null)));

        VBox leftPane = new VBox(display);
        leftPane.setPadding(new Insets(10));
        leftPane.setPrefWidth(560);

        listView = new ListView<>();
        listView.setPrefHeight(420);
        listView.setCellFactory(lv -> new ListCell<SugerenciaUnificacion>() {
            @Override
            protected void updateItem(SugerenciaUnificacion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("[" + item.getNivel() + "] " + item.getTitulo()
                            + "  |  " + item.getMotivo()
                            + "  |  SYS: " + item.getSys());
                }
            }
        });

        VBox rightPane = new VBox();
        rightPane.setPadding(new Insets(10));
        rightPane.setSpacing(10);
        rightPane.setPrefWidth(380);

        Label rightTitle = new Label("Sugerencias de unificación");
        rightTitle.setStyle("-fx-font-weight: bold;");
        rightPane.getChildren().addAll(rightTitle, listView);

        HBox center = new HBox(leftPane, rightPane);
        center.setSpacing(10);
        root.setCenter(center);

        confirmarButton = new Button("Confirmar");
        descartarButton = new Button("Descartar");
        anteriorButton = new Button("Anterior");
        siguienteButton = new Button("Siguiente");
        cancelarButton = new Button("Cancelar");

        HBox bottomBar = new HBox(confirmarButton, descartarButton, anteriorButton, siguienteButton, cancelarButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setSpacing(10);
        bottomBar.setPadding(new Insets(12));
        root.setBottom(bottomBar);

        confirmarButton.setOnAction(e -> confirmarActual());
        descartarButton.setOnAction(e -> descartarActual());
        anteriorButton.setOnAction(e -> irAlAnteriorConSugerencias());
        siguienteButton.setOnAction(e -> irAlSiguienteConSugerencias());
        cancelarButton.setOnAction(e -> primaryStage.close());

        Scene scene = new Scene(root);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                descartarActual();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.PAGE_DOWN) {
                siguienteButton.fire();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.PAGE_UP) {
                anteriorButton.fire();
                e.consume();
                return;
            }
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                confirmarButton.fire();
                e.consume();
                return;
            }
        });

        primaryStage.setTitle("PreUnificador-O-Matic");
        primaryStage.setScene(scene);

        mostrarPrimeroConSugerencias();
        primaryStage.show();
    }

    private void mostrarActual() {
        display.getChildren().clear();
        listView.getItems().clear();
        editors.clear();

        if (regIndi == null || regIndi.isEmpty() || numReg < 0 || numReg >= regIndi.size()) {
            display.getChildren().add(new Label("No hay registros con sugerencias de unificación. El resto pasa directo al Precatalogador-O-Matic."));
            labelCounter.setText("0 / 0");
            actualizarEstadoBotones();
            return;
        }

        String[] row = regIndi.get(numReg);
        barcodeOriginal = safe(row, 0);

        display.getChildren().add(rowTexto("BARCODE", safe(row, 0)));
        display.getChildren().add(rowTexto("nroA", safe(row, 1)));
        display.getChildren().add(rowTexto("nroNid", safe(row, 2)));
        display.getChildren().add(rowTexto("nroAnm", safe(row, 3)));
        display.getChildren().add(rowEditable("Fotógrafo", 4, safe(row, 4), 360));
        display.getChildren().add(rowEditable("Título", 5, safe(row, 5), 420));
        display.getChildren().add(rowTexto("Fecha", safe(row, 6)));
        display.getChildren().add(rowTexto("Observaciones", safe(row, 7)));
        display.getChildren().add(rowTexto("UFI", safe(row, 8)));

        List<SugerenciaUnificacion> sugerencias = getSugerencias(row);
        listView.getItems().setAll(sugerencias);
        if (!sugerencias.isEmpty()) {
            listView.getSelectionModel().select(0);
        }

        labelCounter.setText((numReg + 1) + " / " + regIndi.size());
        actualizarEstadoBotones();
        Platform.runLater(() -> {
            TextField tituloField = editors.get(5);
            if (tituloField != null) {
                tituloField.requestFocus();
                tituloField.positionCaret(0);
            }
        });
    }

    private HBox rowTexto(String label, String value) {
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

    private HBox rowEditable(String label, int index, String value, double width) {
        Label l = new Label(label + ":");
        l.setMinWidth(110);
        l.setStyle("-fx-font-weight: bold;");

        TextField tf = new TextField(value == null ? "" : value);
        tf.setPrefWidth(width);

        tf.setOnAction(e -> guardarCambiosInventarioSinReevaluar());

        editors.put(index, tf);

        HBox hb = new HBox(l, tf);
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setSpacing(10);
        return hb;
    }

    private void guardarCambiosInventarioSinReevaluar() {
        if (regIndi == null || regIndi.isEmpty()) return;
        if (numReg < 0 || numReg >= regIndi.size()) return;
        if (barcodeOriginal == null || barcodeOriginal.trim().isEmpty()) return;

        String[] row = regIndi.get(numReg);

        String newAutor = getEditorValue(4, safe(row, 4));
        String newTitulo = getEditorValue(5, safe(row, 5));

        String consulta = "UPDATE inventario SET "
                + "autor='" + sqlEscape(newAutor) + "', "
                + "titulo='" + sqlEscape(newTitulo) + "' "
                + "WHERE barcode='" + sqlEscape(barcodeOriginal) + "';";

        cron.envia(consulta);

        row[4] = newAutor;
        row[5] = newTitulo;

        Platform.runLater(() -> {
            TextField tituloField = editors.get(5);
            if (tituloField != null) {
                tituloField.requestFocus();
                tituloField.positionCaret(0);
            }
        });
    }

    private String getEditorValue(int index, String fallback) {
        TextField tf = editors.get(index);
        if (tf == null) return fallback == null ? "" : fallback;
        String v = tf.getText();
        return v == null ? "" : v.trim();
    }

    private void mostrarPrimeroConSugerencias() {
        int idx = buscarSiguienteIndiceConSugerencias(-1);
        if (idx >= 0) {
            numReg = idx;
            mostrarActual();
        } else {
            numReg = -1;
            mostrarActual();
        }
    }

    private void confirmarActual() {
        if (numReg < 0 || numReg >= regIndi.size()) return;

        SugerenciaUnificacion sug = listView.getSelectionModel().getSelectedItem();
        if (sug == null) return;

        String barcode = safe(regIndi.get(numReg), 0);
        String sys = sug.getSys();

        String consulta = "INSERT INTO conjuntos(titulo, barcode, status) VALUES ('"
                + sqlEscape(sys) + "','"
                + sqlEscape(barcode) + "','2')";

        cron.envia(consulta);

        cacheSugerencias.remove(barcode);
        regIndi.remove(numReg);

        if (regIndi.isEmpty()) {
            numReg = -1;
            mostrarActual();
            return;
        }

        int idx = buscarSiguienteIndiceConSugerencias(numReg - 1);
        if (idx < 0) {
            idx = buscarAnteriorIndiceConSugerencias(numReg);
        }

        numReg = idx;
        mostrarActual();
    }

    private void descartarActual() {
        if (numReg < 0 || numReg >= regIndi.size()) return;

        String barcode = safe(regIndi.get(numReg), 0);
        cacheSugerencias.remove(barcode);
        regIndi.remove(numReg);

        if (regIndi.isEmpty()) {
            numReg = -1;
            mostrarActual();
            return;
        }

        int idx = buscarSiguienteIndiceConSugerencias(numReg - 1);
        if (idx < 0) {
            idx = buscarAnteriorIndiceConSugerencias(numReg);
        }

        numReg = idx;
        mostrarActual();
    }

    private void irAlSiguienteConSugerencias() {
        if (numReg < 0) return;

        int idx = buscarSiguienteIndiceConSugerencias(numReg);
        if (idx >= 0) {
            numReg = idx;
            mostrarActual();
        }
    }

    private void irAlAnteriorConSugerencias() {
        if (numReg < 0) return;

        int idx = buscarAnteriorIndiceConSugerencias(numReg);
        if (idx >= 0) {
            numReg = idx;
            mostrarActual();
        }
    }

    private int buscarSiguienteIndiceConSugerencias(int desde) {
        for (int i = desde + 1; i < regIndi.size(); i++) {
            if (!getSugerencias(regIndi.get(i)).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private int buscarAnteriorIndiceConSugerencias(int desde) {
        for (int i = Math.min(desde - 1, regIndi.size() - 1); i >= 0; i--) {
            if (!getSugerencias(regIndi.get(i)).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private List<SugerenciaUnificacion> getSugerencias(String[] row) {
        String barcode = safe(row, 0);
        if (cacheSugerencias.containsKey(barcode)) {
            return cacheSugerencias.get(barcode);
        }

        List<SugerenciaUnificacion> sugerencias = buscarSugerencias(row);
        cacheSugerencias.put(barcode, sugerencias);
        return sugerencias;
    }

    private List<SugerenciaUnificacion> buscarSugerencias(String[] row) {
        String tituloSobre = safe(row, 5);
        List<SugerenciaUnificacion> out = new ArrayList<>();

        if (tituloSobre.isBlank()) return out;

        String normSobre = normalizarTituloBase(tituloSobre);
        List<String> palabrasSobre = tokens(normSobre);
        if (palabrasSobre.isEmpty()) return out;

        List<String[]> candidatos = cron.consultaCompleta(
                "SELECT sys, titulo245 FROM registros "
                + "WHERE titulo245 LIKE '[%' "
                + "AND titulo245 IS NOT NULL "
                + "AND titulo245 <> '' "
                + "ORDER BY titulo245;"
        );

        for (String[] cand : candidatos) {
            String sys = safe(cand, 0);
            String tituloReg = safe(cand, 1);

            String normReg = normalizarTituloBase(tituloReg);
            List<String> palabrasReg = tokens(normReg);

            MatchInfo match = evaluarMatch(palabrasSobre, palabrasReg);
            if (match != null) {
                out.add(new SugerenciaUnificacion(sys, tituloReg, match.nivel, match.motivo, match.peso));
            }
        }

        return filtrarSugerencias(out);
    }

    private List<SugerenciaUnificacion> filtrarSugerencias(List<SugerenciaUnificacion> lista) {
        List<SugerenciaUnificacion> altos = new ArrayList<>();
        List<SugerenciaUnificacion> medios = new ArrayList<>();
        List<SugerenciaUnificacion> bajos = new ArrayList<>();

        for (SugerenciaUnificacion s : lista) {
            switch (s.getNivel()) {
                case "ALTO":
                    altos.add(s);
                    break;
                case "MEDIO":
                    medios.add(s);
                    break;
                case "BAJO":
                    bajos.add(s);
                    break;
                default:
                    break;
            }
        }

        Comparator<SugerenciaUnificacion> cmp = Comparator
                .comparingInt(SugerenciaUnificacion::getPeso).reversed()
                .thenComparing(SugerenciaUnificacion::getTitulo, String.CASE_INSENSITIVE_ORDER);

        altos.sort(cmp);
        medios.sort(cmp);
        bajos.sort(cmp);

        if (!altos.isEmpty()) return altos;
        if (!medios.isEmpty()) return medios;
        return bajos;
    }

    private MatchInfo evaluarMatch(List<String> sobre, List<String> reg) {
        if (sobre.isEmpty() || reg.isEmpty()) return null;

        int iguales = 0;
        int max = Math.min(Math.min(sobre.size(), reg.size()), 3);

        for (int i = 0; i < max; i++) {
            String a = sobre.get(i);
            String b = reg.get(i);

            if (a.equals(b)) {
                iguales++;
            } else if (i == 2 && distanciaSimple(a, b) <= 1) {
                iguales++;
            } else {
                break;
            }
        }

        if (iguales >= 3) return new MatchInfo("ALTO", "3 palabras", 300);
        if (iguales >= 2) return new MatchInfo("MEDIO", "2 palabras", 200);
        if (iguales >= 1) return new MatchInfo("BAJO", "1 palabra", 100);

        return null;
    }

    private void actualizarEstadoBotones() {
        boolean actualValido = numReg >= 0 && numReg < regIndi.size();
        boolean haySugerenciasActuales = actualValido && !listView.getItems().isEmpty();

        confirmarButton.setDisable(!haySugerenciasActuales);
        descartarButton.setDisable(!actualValido);
        anteriorButton.setDisable(!actualValido || buscarAnteriorIndiceConSugerencias(numReg) < 0);
        siguienteButton.setDisable(!actualValido || buscarSiguienteIndiceConSugerencias(numReg) < 0);
    }

    private static String normalizarTituloBase(String s) {
        if (s == null) return "";

        String out = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);

        out = out.replaceAll("\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b", " ");
        out = out.replaceAll("\\b\\d{4}\\b", " ");
        out = out.replaceAll("[\\p{P}\\p{S}]+", " ");
        out = out.replaceAll("\\s+", " ").trim();

        return out;
    }

    private static List<String> tokens(String s) {
        List<String> out = new ArrayList<>();
        if (s == null || s.isBlank()) return out;

        String[] parts = s.split("\\s+");
        for (String p : parts) {
            if (p == null || p.isBlank()) continue;
            if (p.length() <= 1) continue;
            out.add(p);
        }
        return out;
    }

    private static int distanciaSimple(String a, String b) {
        if (a == null || b == null) return 99;
        if (a.equals(b)) return 0;
        if (Math.abs(a.length() - b.length()) > 1) return 99;

        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private String safe(String[] arr, int idx) {
        if (arr == null) return "";
        if (idx < 0 || idx >= arr.length) return "";
        return arr[idx] == null ? "" : arr[idx];
    }

    private String sqlEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "''");
    }

    private static class MatchInfo {
        private final String nivel;
        private final String motivo;
        private final int peso;

        private MatchInfo(String nivel, String motivo, int peso) {
            this.nivel = nivel;
            this.motivo = motivo;
            this.peso = peso;
        }
    }

    private static class SugerenciaUnificacion {
        private final String sys;
        private final String titulo;
        private final String nivel;
        private final String motivo;
        private final int peso;

        private SugerenciaUnificacion(String sys, String titulo, String nivel, String motivo, int peso) {
            this.sys = sys;
            this.titulo = titulo;
            this.nivel = nivel;
            this.motivo = motivo;
            this.peso = peso;
        }

        public String getSys() {
            return sys;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getNivel() {
            return nivel;
        }

        public String getMotivo() {
            return motivo;
        }

        public int getPeso() {
            return peso;
        }
    }
}