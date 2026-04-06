package com.archivonegativoscronica;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportadorInventarioUI {

    private final Stage stage;
    private final TableView<ImportadorInventarioDesdeExcel.InventarioRow> table = new TableView<>();
    private final Label lblArchivo = new Label("Archivo: (ninguno)");
    private final Label lblStats = new Label("");
    private final CheckBox chkDryRun = new CheckBox("Simular (no escribir en DB)");
    private final Spinner<Integer> spPreviewRows = new Spinner<>(10, 2000, 200);

    private File excelFile;

    private final ImportadorInventarioDesdeExcel importer = new ImportadorInventarioDesdeExcel();
    private Funciones cron;

    public ImportadorInventarioUI(Stage owner, Funciones cron) {
        this.cron = cron ;
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Importador Inventario (PRUEBA)");

        // Top controls
        Button btnElegir = new Button("Elegir Excel...");
        Button btnPreview = new Button("Preview");
        Button btnImportar = new Button("IMPORTAR");

        chkDryRun.setSelected(true); // por defecto: seguro
        spPreviewRows.setEditable(true);

        btnElegir.setOnAction(e -> {
            File f = elegirArchivoExcel(stage);
            if (f != null) {
                excelFile = f;
                lblArchivo.setText("Archivo: " + f.getAbsolutePath());
                table.getItems().clear();
                lblStats.setText("");
            }
        });

        btnPreview.setOnAction(e -> hacerPreviewSeguro());

        btnImportar.setOnAction(e -> {
            try {
                importarSeguro(owner);
            } catch (Exception ex) {
                Logger.getLogger(ImportadorInventarioUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        HBox top = new HBox(10,
                btnElegir,
                new Label("Preview filas:"),
                spPreviewRows,
                btnPreview,
                new Separator(),
                chkDryRun,
                btnImportar
        );
        top.setPadding(new Insets(10));

        VBox header = new VBox(6, lblArchivo, lblStats);
        header.setPadding(new Insets(0, 10, 10, 10));

        configureTable();

        BorderPane root = new BorderPane();
        root.setTop(new VBox(top, header));
        root.setCenter(table);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 1100, 650));
        stage.show();
    }

    public void show() {
        stage.showAndWait();
    }

    private void configureTable() {
        //table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        table.getColumns().add(col("ExcelRow", r -> String.valueOf(r.excelRowNumber)));
        table.getColumns().add(col("barcode", r -> nullSafe(r.barcode)));
        table.getColumns().add(col("ufi", r -> nullSafe(r.ufi)));
        table.getColumns().add(col("nroA (pad6)", r -> nullSafe(r.nroA)));
        table.getColumns().add(col("titulo", r -> nullSafe(r.titulo)));
        table.getColumns().add(col("fechaISO", r -> nullSafe(r.fechaISO)));
        table.getColumns().add(col("autor", r -> nullSafe(r.autor)));
        table.getColumns().add(col("error", r -> nullSafe(r.error)));

        // Estilo simple: marcar filas con error
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ImportadorInventarioDesdeExcel.InventarioRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (!item.ok()) {
                    setStyle("-fx-background-color: rgba(255,0,0,0.10);");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private TableColumn<ImportadorInventarioDesdeExcel.InventarioRow, String> col(
            String title,
            java.util.function.Function<ImportadorInventarioDesdeExcel.InventarioRow, String> fn
    ) {
        TableColumn<ImportadorInventarioDesdeExcel.InventarioRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(fn.apply(cd.getValue())));
        return c;
    }

    private void hacerPreviewSeguro() {
        if (excelFile == null) {
            alert("Primero elegí un Excel.", Alert.AlertType.WARNING);
            return;
        }
        try {
            int max = spPreviewRows.getValue();
            List<ImportadorInventarioDesdeExcel.InventarioRow> rows = importer.preview(excelFile, 0, max);
            table.setItems(FXCollections.observableArrayList(rows));

            long ok = rows.stream().filter(ImportadorInventarioDesdeExcel.InventarioRow::ok).count();
            long bad = rows.size() - ok;

            lblStats.setText("Preview: " + rows.size() + " filas | OK: " + ok + " | Con error: " + bad +
                    " | (nroA pad6 + fechaISO yyyyMMdd aplicados en preview)");
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Error en preview:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void importarSeguro(Stage owner) throws Exception {
        if (excelFile == null) {
            alert("Primero elegí un Excel.", Alert.AlertType.WARNING);
            return;
        }

        boolean dryRun = chkDryRun.isSelected();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar importación");
        confirm.setHeaderText(dryRun ? "SIMULACIÓN (no escribe en DB)" : "IMPORTACIÓN REAL (escribe en DB)");
        confirm.setContentText("Archivo:\n" + excelFile.getAbsolutePath() + "\n\n¿Continuar?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        ImportadorInventarioDesdeExcel.ImportResult res = importer.importar(cron.conn, excelFile, 0, dryRun);
        alert((dryRun ? "Simulación OK.\n" : "Importación OK.\n") +
                "Filas leídas: " + res.leidas + "\n" +
                "Errores (barcode vacío, etc.): " + res.errores,
                Alert.AlertType.INFORMATION);
        
    }

    private static File elegirArchivoExcel(Stage owner) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Excel de inventario");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));
        return fc.showOpenDialog(owner);
    }

    private void alert(String msg, Alert.AlertType type) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.initOwner(stage);
        a.showAndWait();
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
