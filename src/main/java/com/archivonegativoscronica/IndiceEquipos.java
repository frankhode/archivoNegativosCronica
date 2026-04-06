package com.archivonegativoscronica;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

public class IndiceEquipos extends BorderPane {

    private final Funciones cron;
    private final TabPane tabPane;

    private final TableView<FilaIndiceEquipo> table = new TableView<>();

    public IndiceEquipos(Funciones cron, TabPane tabPane) {
        this.cron = cron;
        this.tabPane = tabPane;

        construirUI();
        cargarEquiposAsync();
    }

    private void construirUI() {
        TableColumn<FilaIndiceEquipo, String> colEquipo  = new TableColumn<>("Equipo");
        TableColumn<FilaIndiceEquipo, String> colDigital = new TableColumn<>("Tiene digital");
        TableColumn<FilaIndiceEquipo, Button> colVer     = new TableColumn<>("");

        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colDigital.setCellValueFactory(new PropertyValueFactory<>("digital"));
        colVer.setCellValueFactory(new PropertyValueFactory<>("verSobres"));

        colEquipo.setPrefWidth(400);
        colDigital.setPrefWidth(120);
        colVer.setPrefWidth(110);

        table.getColumns().setAll(colEquipo, colDigital, colVer);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));

        Tab tab = new Tab("Equipos");
        cron.shortCutTab(tab);
        tab.setContent(root);

        if (!tabPane.getTabs().contains(tab)) {
            tabPane.getTabs().add(tab);
        }
        tabPane.getSelectionModel().select(tab);
    }

    private void cargarEquiposAsync() {
        Task<ObservableList<FilaIndiceEquipo>> task = new Task<ObservableList<FilaIndiceEquipo>>() {
            @Override
            protected ObservableList<FilaIndiceEquipo> call() {
                return cargarEquiposDesdeBD();
            }
        };

        task.setOnSucceeded(e -> {
            table.setItems(task.getValue());
            table.scrollTo(0);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                Logger.getLogger(IndiceEquipos.class.getName()).log(Level.SEVERE, null, ex);
                cron.mensajeSalida("Error al cargar índice de equipos:\n" + ex.getMessage());
            }
        });

        Thread t = new Thread(task, "IndiceEquipos");
        t.setDaemon(true);
        t.start();
    }

    private ObservableList<FilaIndiceEquipo> cargarEquiposDesdeBD() {
        ObservableList<FilaIndiceEquipo> data = FXCollections.observableArrayList();

        // Lista básica de equipos (como pasaste vos)
        // Acá además calculamos "tiene digital" en base a partidos + digitales
        String consulta =
            "SELECT e.equipo, " +
            "       CASE WHEN SUM(CASE WHEN d.inv IS NOT NULL THEN 1 ELSE 0 END) > 0 " +
            "            THEN '✓' ELSE 'X' END AS tiene_digital " +
            "FROM ( " +
            "    SELECT equipo1 AS equipo, barcode FROM partidos " +
            "    UNION ALL " +
            "    SELECT equipo2 AS equipo, barcode FROM partidos " +
            ") e " +
            "LEFT JOIN digitales d ON d.inv = e.barcode " +
            "GROUP BY e.equipo " +
            "ORDER BY e.equipo ;";

        List<String[]> filas = cron.consultaCompleta(consulta);
        // cada fila: [equipo, tiene_digital]

        for (String[] fila : filas) {
            if (fila == null || fila.length < 2) continue;

            String equipo = fila[0];
            String digital = (fila[1] != null && !fila[1].isEmpty()) ? fila[1] : "X";

            Button verSobres = new Button("Ver sobres");
            verSobres.setOnAction(ev -> {
                try {
                    new ResultadosEquipos(cron, equipo, tabPane);
                } catch (Exception ex) {
                    Logger.getLogger(IndiceEquipos.class.getName()).log(Level.SEVERE, null, ex);
                    cron.mensajeSalida("Error al abrir resultados de equipo:\n" + ex.getMessage());
                }
            });

            data.add(new FilaIndiceEquipo(equipo, digital, verSobres));
        }

        return data;
    }

    // -------- fila índice --------

    public static class FilaIndiceEquipo {
        private final SimpleStringProperty equipo;
        private final SimpleStringProperty digital;
        private final Button verSobres;

        public FilaIndiceEquipo(String equipo, String digital, Button verSobres) {
            this.equipo = new SimpleStringProperty(equipo);
            this.digital = new SimpleStringProperty(digital);
            this.verSobres = verSobres;
        }

        public String getEquipo() {
            return equipo.get();
        }

        public void setEquipo(String v) {
            equipo.set(v);
        }

        public String getDigital() {
            return digital.get();
        }

        public void setDigital(String v) {
            digital.set(v);
        }

        public Button getVerSobres() {
            return verSobres;
        }
    }
}
