package com.archivonegativoscronica;


import java.util.ArrayList;
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

public class ResultadosEquipos extends BorderPane {

    private final Funciones cron;
    private final TabPane tabPane;
    private final String equipo;

    private final TableView<FilaResultadoEquipo> table = new TableView<>();

    public ResultadosEquipos(Funciones cron, String equipo, TabPane tabPane) {
        this.cron = cron;
        this.tabPane = tabPane;
        this.equipo = equipo;

        construirUI();
        cargarResultadosAsync();
    }

    private void construirUI() {
        TableColumn<FilaResultadoEquipo, String> colPartido   = new TableColumn<>("Partido");
        TableColumn<FilaResultadoEquipo, String> colCamp      = new TableColumn<>("Campeonato");
        TableColumn<FilaResultadoEquipo, String> colFecha     = new TableColumn<>("Fecha");
        TableColumn<FilaResultadoEquipo, Button> colVerDigi   = new TableColumn<>("Ver digital");
        TableColumn<FilaResultadoEquipo, Button> colVerEdImp  = new TableColumn<>("Ver ed. impresa");

        colPartido.setCellValueFactory(new PropertyValueFactory<>("partido"));
        colCamp.setCellValueFactory(new PropertyValueFactory<>("campeonato"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colVerDigi.setCellValueFactory(new PropertyValueFactory<>("verDigital"));
        colVerEdImp.setCellValueFactory(new PropertyValueFactory<>("verEdicionImpresa"));

        colPartido.setPrefWidth(350);
        colCamp.setPrefWidth(250);
        colFecha.setPrefWidth(120);
        colVerDigi.setPrefWidth(110);
        colVerEdImp.setPrefWidth(130);

        table.getColumns().setAll(colPartido, colCamp, colFecha, colVerDigi, colVerEdImp);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        BorderPane root = new BorderPane(table);
        root.setPadding(new Insets(10));

        Tab tab = new Tab(equipo + " ×");
        cron.shortCutTab(tab);
        tab.setContent(root);

        if (!tabPane.getTabs().contains(tab)) {
            tabPane.getTabs().add(tab);
        }
        tabPane.getSelectionModel().select(tab);
    }

    private void cargarResultadosAsync() {
        Task<ObservableList<FilaResultadoEquipo>> task = new Task<ObservableList<FilaResultadoEquipo>>() {
            @Override
            protected ObservableList<FilaResultadoEquipo> call() {
                return cargarDesdeBD();
            }
        };

        task.setOnSucceeded(e -> {
            table.setItems(task.getValue());
            table.scrollTo(0);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                Logger.getLogger(ResultadosEquipos.class.getName()).log(Level.SEVERE, null, ex);
                cron.mensajeSalida("Error al cargar resultados del equipo:\n" + ex.getMessage());
            }
        });

        Thread t = new Thread(task, "ResultadosEquipos-" + equipo);
        t.setDaemon(true);
        t.start();
    }

    private ObservableList<FilaResultadoEquipo> cargarDesdeBD() {
        ObservableList<FilaResultadoEquipo> data = FXCollections.observableArrayList();

        String eq = equipo.replace("'", "''");

        // Unimos barcodes que:
        //  - tienen materia = equipo  (materias+items)
        //  - o aparecen en partidos.equipo1/equipo2
        String consulta =
            "SELECT be.barcode, " +
            "       COALESCE(p.tituloSobre, t.titulo) AS partido, " +
            "       p.tituloReg AS campeonato, " +
            "       t.fecha, " +
            "       CASE WHEN d.inv IS NOT NULL THEN '✓' ELSE 'X' END AS tiene_digital " +
            "FROM ( " +
            "   SELECT DISTINCT i.barcode " +
            "   FROM materias m " +
            "   JOIN items i ON i.sys = m.sys " +
            "   WHERE m.materia LIKE '" + eq + "' " +
            "   UNION " +
            "   SELECT DISTINCT p2.barcode " +
            "   FROM partidos p2 " +
            "   WHERE p2.equipo1 LIKE '" + eq + "' " +
            "      OR p2.equipo2 LIKE '" + eq + "' " +
            ") AS be " +
            "JOIN titulos t ON t.barcode = be.barcode " +
            "LEFT JOIN partidos p ON p.barcode = be.barcode " +
            // 👇 acá está el cambio importante
            "LEFT JOIN ( " +
            "    SELECT inv " +
            "    FROM digitales " +
            "    GROUP BY inv " +
            ") d ON d.inv = be.barcode " +
            "ORDER BY t.fecha, partido ;";

        List<String[]> filas = cron.consultaCompleta(consulta);
        // fila: [barcode, partido, campeonato, fecha, tiene_digital]

        for (String[] fila : filas) {
            if (fila == null || fila.length < 5) continue;

            String barcode = fila[0];
            String partido = fila[1] != null ? fila[1] : "";
            String campeonato = fila[2] != null ? fila[2] : "";
            String fecha = fila[3] != null ? fila[3] : "";
            String digFlag = fila[4] != null ? fila[4] : "X";

            // Botón ver digital
            Button verDigital = new Button("Ver digital");
            if (!"✓".equals(digFlag)) {
                verDigital.setDisable(true);
            } else {
                verDigital.setOnAction(ev -> {
                    try {
                        List<String[]> digitales = cron.getDigitales(barcode);
                        if (digitales == null || digitales.isEmpty()) {
                            cron.mensajeSalida("No se encontraron digitales para este sobre.");
                        } else {
                            cron.abreConjunto(digitales);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ResultadosEquipos.class.getName()).log(Level.SEVERE, null, ex);
                        cron.mensajeSalida("Error al abrir digitales:\n" + ex.getMessage());
                    }
                });
            }

            // Botón ver edición impresa
            Button verEdImp = new Button("Ver ed. impresa");
            verEdImp.setOnAction(ev -> {
                try {
                    new EdicionImpresa(barcode, cron);
                } catch (Exception ex) {
                    Logger.getLogger(ResultadosEquipos.class.getName()).log(Level.SEVERE, null, ex);
                    cron.mensajeSalida("Error al abrir edición impresa:\n" + ex.getMessage());
                }
            });

            data.add(new FilaResultadoEquipo(partido, campeonato, fecha, verDigital, verEdImp));
        }

        return data;
    }

    // ------- fila resultados -------

    public static class FilaResultadoEquipo {

        private final SimpleStringProperty partido;
        private final SimpleStringProperty campeonato;
        private final SimpleStringProperty fecha;
        private final Button verDigital;
        private final Button verEdicionImpresa;

        public FilaResultadoEquipo(String partido,
                                   String campeonato,
                                   String fecha,
                                   Button verDigital,
                                   Button verEdicionImpresa) {
            this.partido = new SimpleStringProperty(partido);
            this.campeonato = new SimpleStringProperty(campeonato);
            this.fecha = new SimpleStringProperty(fecha);
            this.verDigital = verDigital;
            this.verEdicionImpresa = verEdicionImpresa;
        }

        public String getPartido() {
            return partido.get();
        }
        public void setPartido(String v) { partido.set(v); }

        public String getCampeonato() {
            return campeonato.get();
        }
        public void setCampeonato(String v) { campeonato.set(v); }

        public String getFecha() {
            return fecha.get();
        }
        public void setFecha(String v) { fecha.set(v); }

        public Button getVerDigital() {
            return verDigital;
        }

        public Button getVerEdicionImpresa() {
            return verEdicionImpresa;
        }
    }
}
