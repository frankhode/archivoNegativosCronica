package com.archivonegativoscronica;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javafx.concurrent.Task;


public class IndiceMarc extends BorderPane {

    private final Funciones cron;
    private final TabPane tabPane;

    private final ComboBox<TipoIndiceMarc> cboTipo;
    private final TextField txtDesde;
    private final ComboBox<String> cboCantidad;
    private final Button btnBuscar;
    private final Button btnPrev;
    private final Button btnNext;

    private final TableView<FilaIndiceMarc> table;

    private int inicio = 0;          // offset para LIMIT
    private int cantidad = 100;      // filas por página
    private String ultimoDesde = ""; // para recordar desde qué término arrancamos
    private TipoIndiceMarc ultimoTipo; // tipo actual

    public IndiceMarc(Funciones cron, TabPane tabPane) {
        this.cron = cron;
        this.tabPane = tabPane;

        // ----- Barra superior: tipo índice + navegación "a partir de" -----
        cboTipo = new ComboBox<>(
                FXCollections.observableArrayList(TipoIndiceMarc.values()));
        cboTipo.setValue(TipoIndiceMarc.TEMAS); // por defecto

        txtDesde = new TextField();
        txtDesde.setPromptText("a partir de...");

        cboCantidad = new ComboBox<>(
                FXCollections.observableArrayList("10", "50", "100", "500", "1000"));
        cboCantidad.setValue("100");

        btnBuscar = new Button("Buscar");
        btnPrev = new Button("◀");
        btnNext = new Button("▶");

        HBox barra = new HBox(10,
                new Label("Índice:"),
                cboTipo,
                new Label("Comenzar desde:"),
                txtDesde,
                new Label("Por página:"),
                cboCantidad,
                btnPrev,
                btnNext,
                btnBuscar
        );
        barra.setPadding(new Insets(5, 5, 5, 5));

        setTop(barra);

        // ----- Tabla -----
        table = TablaHelper.crearTabla(
                ColumnasPredefinidas.indiceMarc(cboTipo.getValue().getTituloColumna())
        );
        setCenter(table);

        // ----- Wiring eventos -----

        // Cuando cambias tipo de índice, reseteamos todo y recargamos
        cboTipo.setOnAction(e -> {
            inicio = 0;
            ultimoDesde = txtDesde.getText().trim();
            ultimoTipo = cboTipo.getValue();
            configurarColumnasSegunTipo();
            cargarPaginaActual();
        });

        // Cambio de cantidad por página
        cboCantidad.setOnAction(e -> {
            try {
                cantidad = Integer.parseInt(cboCantidad.getValue());
            } catch (NumberFormatException ex) {
                cantidad = 100;
            }
            inicio = 0;
            cargarPaginaActual();
        });

        // Botón Buscar: arranca desde 0 usando el "a partir de"
        btnBuscar.setOnAction(e -> {
            inicio = 0;
            ultimoDesde = txtDesde.getText().trim();
            ultimoTipo = cboTipo.getValue();
            configurarColumnasSegunTipo();
            cargarPaginaActual();
        });

        // Prev
        btnPrev.setOnAction(e -> {
            if (inicio >= cantidad) {
                inicio -= cantidad;
                cargarPaginaActual();
            }
        });

        // Next
        btnNext.setOnAction(e -> {
            inicio += cantidad;
            cargarPaginaActual();
        });

        // Estado inicial
        ultimoTipo = cboTipo.getValue();
        ultimoDesde = "";
        cargarPaginaActual();
    }

    private void configurarColumnasSegunTipo() {
        TablaHelper.configurarColumnas(
                table,
                ColumnasPredefinidas.indiceMarc(cboTipo.getValue().getTituloColumna())
        );
    }

    private void cargarPaginaActual() {
        TipoIndiceMarc tipo = cboTipo.getValue();
        if (tipo == null) {
            return;
        }
        String comienzo = ultimoDesde != null ? ultimoDesde : "";

        // deshabilitamos controles mientras carga
        btnBuscar.setDisable(true);
        btnPrev.setDisable(true);
        btnNext.setDisable(true);
        table.setDisable(true);

        Task<ObservableList<FilaIndiceMarc>> task = new Task<ObservableList<FilaIndiceMarc>>() {
            @Override
            protected ObservableList<FilaIndiceMarc> call() {
                // esto corre en segundo plano
                return cargarDatosIndice(tipo, comienzo, inicio, cantidad);
            }
        };

        task.setOnSucceeded(ev -> {
            // esto corre en el hilo de JavaFX
            ObservableList<FilaIndiceMarc> datos = task.getValue();
            table.setItems(datos);
            // volver al principio de la tabla después de cargar
            Platform.runLater(() -> {
                table.scrollTo(0);
            });

            btnPrev.setDisable(inicio == 0);
            btnNext.setDisable(datos.size() < cantidad);
            btnBuscar.setDisable(false);
            table.setDisable(false);
        });

        task.setOnFailed(ev -> {
            btnBuscar.setDisable(false);
            table.setDisable(false);
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
                cron.mensajeSalida("Error al cargar índice:\n" + ex.getMessage());
            }
        });

        Thread t = new Thread(task, "CargaIndiceMarc");
        t.setDaemon(true);
        t.start();
    }
    
    private ObservableList<FilaIndiceMarc> cargarDatosIndice(TipoIndiceMarc tipo,
                                                         String comienzo,
                                                         int inicio,
                                                         int cantidad) {
        ObservableList<FilaIndiceMarc> data = FXCollections.observableArrayList();

        String consulta;

        if (tipo == TipoIndiceMarc.AREAS_GEOGRAFICAS) {
            // CASO ESPECIAL: áreas geográficas salen de mapaareas + areas
            consulta =
                "SELECT ma.espaniol AS termino, " +
                "       COUNT(DISTINCT i.barcode) AS cant, " +
                "       CASE WHEN SUM(CASE WHEN d.inv IS NOT NULL THEN 1 ELSE 0 END) > 0 " +
                "            THEN '✓' ELSE 'X' END AS tiene_digital " +
                "FROM mapaareas ma " +
                "JOIN areas a ON ma.cod = a.area " +
                "LEFT JOIN items i ON i.sys = a.sys " +
                "LEFT JOIN digitales d ON d.inv = i.barcode " +
                "WHERE ma.espaniol >= '" + comienzo + "' " +
                "GROUP BY ma.espaniol " +
                "ORDER BY ma.espaniol " +
                "LIMIT " + inicio + ", " + cantidad + " ;";
        } else {
            // CASO GENERAL: materias 6XX
            consulta =
                "SELECT m.materia AS termino, " +
                "       COUNT(DISTINCT i.barcode) AS cant, " +
                "       CASE WHEN SUM(CASE WHEN d.inv IS NOT NULL THEN 1 ELSE 0 END) > 0 " +
                "            THEN '✓' ELSE 'X' END AS tiene_digital " +
                "FROM materias m " +
                "LEFT JOIN items i ON i.sys = m.sys " +
                "LEFT JOIN digitales d ON d.inv = i.barcode " +
                "WHERE m.campo = '" + tipo.getCampoMarc() + "' " +
                "  AND m.materia >= '" + comienzo + "' " +
                "GROUP BY m.materia " +
                "ORDER BY m.materia " +
                "LIMIT " + inicio + ", " + cantidad + " ;";
        }

        List<String[]> filas = cron.consultaCompleta(consulta);
        // cada fila: { termino, cant, tiene_digital }

        for (String[] fila : filas) {
            if (fila == null || fila.length < 3) continue;

            final String termino = fila[0];

            final int cantSobres;
            try {
                cantSobres = Integer.parseInt(fila[1]);
            } catch (Exception e) {
                System.out.println("Error parseando cantidad para " + termino + ": " + fila[1]);
                continue;
            }

            final String dig = (fila[2] != null && !fila[2].isEmpty()) ? fila[2] : "X";

            Button verSobres = new Button("Ver sobres");
            verSobres.setOnAction(evt -> {
                try {
                    new Resultados(cron, termino, tabPane, tipo.getClaveResultados());
                } catch (Exception ex) {
                    Logger.getLogger(IndiceMarc.class.getName()).log(Level.SEVERE, null, ex);
                    cron.mensajeSalida("Error al abrir resultados:\n" + ex.getMessage());
                }
            });

            data.add(new FilaIndiceMarc(termino, cantSobres, dig, verSobres));
        }

        return data;
    }

    private ObservableList<FilaIndiceMarc> cargarDatosIndiceEnBD(TipoIndiceMarc tipo,
                                                             String comienzo,
                                                             int inicio,
                                                             int cantidad) {
        ObservableList<FilaIndiceMarc> data = FXCollections.observableArrayList();

        // Una sola query que trae:
        //  - termino (materia)
        //  - cant  = cantidad de sobres
        //  - tiene_digital = '✓' si alguno de esos sobres está en "digitales", sino 'X'
        String consulta =
                "SELECT m.materia AS termino, " +
                "       COUNT(DISTINCT i.barcode) AS cant, " +
                "       CASE WHEN SUM(CASE WHEN d.inv IS NOT NULL THEN 1 ELSE 0 END) > 0 " +
                "            THEN '✓' ELSE 'X' END AS tiene_digital " +
                "FROM materias m " +
                "LEFT JOIN items i ON i.sys = m.sys " +
                "LEFT JOIN digitales d ON d.inv = i.barcode " +
                "WHERE m.campo = '" + tipo.getCampoMarc() + "' " +
                "  AND m.materia >= '" + comienzo + "' " +
                "GROUP BY m.materia " +
                "ORDER BY m.materia " +
                "LIMIT " + inicio + ", " + cantidad + " ;";

        List<String[]> filas = cron.consultaCompleta(consulta);
        // consultaCompleta devuelve String[]{termino, cant, tiene_digital}

        for (String[] fila : filas) {
            if (fila == null || fila.length < 3) {
                continue;
            }

            final String termino = fila[0];
            final int cantSobres;
            try {
                cantSobres = Integer.parseInt(fila[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error parseando cantidad para " + termino + ": " + fila[1]);
                continue;
            }
            final String dig = (fila[2] != null && !fila[2].isEmpty()) ? fila[2] : "X";

            Button verSobres = new Button("Ver sobres");
            verSobres.setOnAction(evt -> {
                try {
                    new Resultados(cron, termino, tabPane, tipo.getClaveResultados());
                } catch (Exception ex) {
                    Logger.getLogger(IndiceMarc.class.getName()).log(Level.SEVERE, null, ex);
                    cron.mensajeSalida("Error al abrir resultados:\n" + ex.getMessage());
                }
            });

            data.add(new FilaIndiceMarc(termino, cantSobres, dig, verSobres));
        }

        return data;
    }

}
