package com.archivonegativoscronica;


import java.io.File;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.DirectoryChooser;

public class ContactosPorLotes {

    private final Funciones cron;

    private Stage stage;
    private ListView<String> listCajones;
    private Button btnGenerar;
    private ProgressIndicator progress;
    private Label lblEstado;

    public ContactosPorLotes(Funciones cron) {
        this.cron = cron;
    }

    /** Llamar desde el menú */
    public void abrir() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Contactos por lotes (por cajón)");

        listCajones = new ListView<>();
        listCajones.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listCajones.setPlaceholder(new Label("Cargando cajones…"));

        btnGenerar = new Button("Generar contactos");
        btnGenerar.setDisable(true);

        Button btnSelAll   = new Button("Seleccionar todo");
        Button btnSelNone  = new Button("Deseleccionar");
        Button btnReload   = new Button("Recargar");

        progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setPrefSize(40, 40);

        lblEstado = new Label(" ");

        HBox botones = new HBox(10, btnReload, btnSelAll, btnSelNone, btnGenerar);
        botones.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(12,
                new Label("Seleccioná uno o más cajones:"),
                listCajones,
                botones,
                new Separator(),
                new HBox(10, progress, lblEstado)
        );
        root.setPadding(new Insets(12));
        root.setPrefSize(600, 500);

        // eventos
        btnReload.setOnAction(e -> cargarCajones());
        btnSelAll.setOnAction(e -> {
            listCajones.getSelectionModel().selectAll();
            actualizarEstado();
        });
        btnSelNone.setOnAction(e -> {
            listCajones.getSelectionModel().clearSelection();
            actualizarEstado();
        });
        listCajones.getSelectionModel().getSelectedItems()
                .addListener((javafx.collections.ListChangeListener<String>) c -> actualizarEstado());

        btnGenerar.setOnAction(e -> generar());

        stage.setScene(new Scene(root));
        stage.show();

        cargarCajones();
    }

    /* ========== UI helpers ========== */

    private void actualizarEstado() {
        btnGenerar.setDisable(listCajones.getSelectionModel().getSelectedItems().isEmpty());
    }

    private void setBusy(boolean busy, String msg) {
        progress.setVisible(busy);
        lblEstado.setText(msg == null ? "" : msg);
        btnGenerar.setDisable(busy);
    }

    /* ========== Carga de cajones ========== */

    private void cargarCajones() {
        setBusy(true, "Cargando cajones…");
        listCajones.setItems(FXCollections.observableArrayList());

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                List<String> cajones =
                        cron.consultaSimple("SELECT DISTINCT cajon FROM digitales ORDER BY cajon",1);
                if (cajones == null) return Collections.emptyList();

                return cajones.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            listCajones.setItems(FXCollections.observableArrayList(task.getValue()));
            setBusy(false, "");
            actualizarEstado();
        });

        task.setOnFailed(e -> {
            setBusy(false, "");
            alert("Error cargando cajones:\n" + task.getException().getMessage(),
                    Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    /* ========== Generación ========== */

    private void generar() {
        List<String> cajones = new ArrayList<>(listCajones.getSelectionModel().getSelectedItems());
        if (cajones.isEmpty()) return;
        DirectoryChooser dc = new DirectoryChooser();        
        dc.setTitle("Seleccionar carpeta destino (contactos por lotes)");
        File outDir = dc.showDialog(stage);        
        if (outDir == null) return;

        setBusy(true, "Buscando barcodes…");

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                String in = cajones.stream()
                        .map(c -> "'" + c.replace("'", "''") + "'")
                        .collect(Collectors.joining(","));

                String sql =
                        "SELECT DISTINCT inv FROM digitales WHERE cajon IN (" + in + ") ORDER BY inv";

                List<String> invs = cron.consultaSimple(sql,1);
                if (invs == null) return Collections.emptyList();

                return invs.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            setBusy(false, "");
            List<String> barcodes = task.getValue();

            if (barcodes.isEmpty()) {
                alert("No se encontraron barcodes para los cajones seleccionados.",
                        Alert.AlertType.WARNING);
                return;
            }

            // 🔥 delegamos TODO a Contactos
            Contactos contactos = new Contactos(cron);
            contactos.generarContactosMasivos(barcodes, outDir);
            stage.close();

        });

        task.setOnFailed(e -> {
            setBusy(false, "");
            alert("Error buscando barcodes:\n" + task.getException().getMessage(),
                    Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    /* ========== Utils ========== */

    private void alert(String msg, Alert.AlertType type) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
