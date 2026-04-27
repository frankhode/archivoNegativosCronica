package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class UnificadorRegistrosIndividualesAleph extends Tab {

    private final Funciones cron;

    private final ObservableList<RegistroMSInventario> data;
    private final TableView<RegistroMSInventario> table;

    private final Label lblEstado;
    private final Button btnGenerar;
    private final Button btnCargarInventario;
    private final Button btnVerLoteInventario;
    private final Button btnEnviarPreunificador;
    private final Button btnEnviarPrecatalogador;
    private final Button btnLimpiarOrigenAleph;
    private static final String FILTRO_LOTE_MS =
        "observaciones LIKE 'Importado desde registro MS SYS %'";
    

    private final String[] CAMPOS_MATERIA = {
        "600", "610", "611", "630", "650", "651"
    };

    public UnificadorRegistrosIndividualesAleph(Funciones cron) {
        super("Unificar individuales ALEPH");

        this.cron = cron;
        this.data = FXCollections.observableArrayList();
        this.table = new TableView<>();
        this.lblEstado = new Label("Listo.");

        this.btnGenerar = new Button("Generar conjunto MS sin materias");
        this.btnCargarInventario = new Button("Cargar conjunto en inventario");
        this.btnVerLoteInventario = new Button("Ver lote en inventario");
        this.btnEnviarPreunificador = new Button("Enviar al preunificador");
        this.btnEnviarPrecatalogador = new Button("Enviar al precatalogador");
        this.btnLimpiarOrigenAleph = new Button("Limpiar origen ALEPH");

        configurarBotones();
        configurarTabla();
        construirVista();

        btnCargarInventario.setDisable(true);
        btnVerLoteInventario.setDisable(true);
        btnEnviarPreunificador.setDisable(true);
        btnEnviarPrecatalogador.setDisable(true);
        btnLimpiarOrigenAleph.setDisable(false);
    }

    private void configurarBotones() {
        btnGenerar.setOnAction(event -> generarConjunto());

        btnCargarInventario.setOnAction(event -> cargarConjuntoEnInventario());

        btnVerLoteInventario.setOnAction(event -> verLoteEnInventario());

        /*
         * Quedan preparados para el paso siguiente.
         * En esta primera etapa solo generamos el conjunto, lo cargamos al inventario
         * y verificamos el lote cargado.
         */
        btnEnviarPreunificador.setOnAction(event -> {
            try {
                new PreUnificadorOMatic(cron, FILTRO_LOTE_MS);
            } catch (IOException ex) {
                mensaje("Error", "No se pudo abrir el preunificador para el lote MS:\n" + ex.getMessage());
            }
        });

        btnEnviarPrecatalogador.setOnAction(event -> {
            try {
                new PreCatalogadorOMatic(cron, FILTRO_LOTE_MS);
            } catch (IOException ex) {
                mensaje("Error", "No se pudo abrir el precatalogador para el lote MS:\n" + ex.getMessage());
            }
        });
        
        btnLimpiarOrigenAleph.setOnAction(event -> {
            try {
                new LimpiezaOrigenAleph(cron);
            } catch (Exception ex) {
                mensaje("Error", "No se pudo abrir la limpieza de origen ALEPH:\n" + ex.getMessage());
            }
        });
    }

    private void configurarTabla() {
        TableColumn<RegistroMSInventario, String> colSys = new TableColumn<>("SYS origen");
        TableColumn<RegistroMSInventario, String> colBarcode = new TableColumn<>("Barcode");
        TableColumn<RegistroMSInventario, String> colNroA = new TableColumn<>("NroA");
        TableColumn<RegistroMSInventario, String> colTitulo = new TableColumn<>("Título");
        TableColumn<RegistroMSInventario, String> colAutor = new TableColumn<>("Autor/Fotógrafo");
        TableColumn<RegistroMSInventario, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<RegistroMSInventario, String> colUfi = new TableColumn<>("UFI");
        TableColumn<RegistroMSInventario, String> colEstado = new TableColumn<>("Estado");
        TableColumn<RegistroMSInventario, Button> colVer = new TableColumn<>("Registro");

        colSys.setCellValueFactory(new PropertyValueFactory<>("sys"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colNroA.setCellValueFactory(new PropertyValueFactory<>("nroA"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaISO"));
        colUfi.setCellValueFactory(new PropertyValueFactory<>("ufi"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colVer.setCellValueFactory(new PropertyValueFactory<>("verRegistro"));

        colSys.setPrefWidth(95);
        colBarcode.setPrefWidth(95);
        colNroA.setPrefWidth(120);
        colTitulo.setPrefWidth(420);
        colAutor.setPrefWidth(170);
        colFecha.setPrefWidth(95);
        colUfi.setPrefWidth(120);
        colEstado.setPrefWidth(120);
        colVer.setPrefWidth(110);

        table.getColumns().addAll(
                colSys,
                colBarcode,
                colNroA,
                colTitulo,
                colAutor,
                colFecha,
                colUfi,
                colEstado,
                colVer
        );

        table.setItems(data);
    }

    private void construirVista() {
        HBox botonera = new HBox(
                10,
                btnGenerar,
                btnCargarInventario,
                btnVerLoteInventario,
                btnEnviarPreunificador,
                btnEnviarPrecatalogador,
                btnLimpiarOrigenAleph,
                lblEstado
        );
        botonera.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(botonera);
        root.setCenter(table);
        root.setPadding(new Insets(10));

        setContent(root);
    }

    private void generarConjunto() {
        data.clear();
        lblEstado.setText("Generando conjunto...");

        btnGenerar.setDisable(true);
        btnCargarInventario.setDisable(true);
        btnVerLoteInventario.setDisable(true);
        btnEnviarPreunificador.setDisable(true);
        btnEnviarPrecatalogador.setDisable(true);

        cron.ejecutarEnSegundoPlano(
                () -> {
                    obtenerRegistrosMSSinMaterias();
                    return null;
                },
                () -> {
                    table.refresh();
                    lblEstado.setText("Conjunto generado: " + data.size() + " sobres.");

                    btnGenerar.setDisable(false);
                    btnCargarInventario.setDisable(data.isEmpty());
                },
                (ex) -> {
                    btnGenerar.setDisable(false);
                    lblEstado.setText("Error al generar conjunto.");
                    mensaje("Error", "No se pudo generar el conjunto:\n" + ex.getMessage());
                }
        );
    }

    private void obtenerRegistrosMSSinMaterias() throws SQLException {
        List<RegistroMSInventario> registrosMS = new ArrayList<>();

        String sql = "SELECT registro FROM registros";

        try (PreparedStatement ps = cron.conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String secuencial = rs.getString("registro");

                try {
                    Registro reg = new Registro(secuencial);

                    if (esRegistroMSSinMaterias(reg)) {
                        List<Item> items = reg.getItems();

                        if (items == null || items.isEmpty()) {
                            continue;
                        }

                        for (Item item : items) {
                            RegistroMSInventario fila = new RegistroMSInventario(reg, item);

                            if (!fila.getBarcode().isEmpty()) {
                                Button verRegistro = new Button("Ver registro");
                                verRegistro.setOnAction(event -> new OpacWeb(fila.getSys()));
                                fila.setVerRegistro(verRegistro);

                                registrosMS.add(fila);
                            }
                        }
                    }
                } catch (Exception e) {
                    /*
                     * Registro malformado o inesperado.
                     * Por ahora se omite para no cortar todo el lote.
                     */
                }
            }
        }

        Platform.runLater(() -> data.setAll(registrosMS));
    }

    private boolean esRegistroMSSinMaterias(Registro reg) {
        String ultimoCatalogador = reg.getUltimoCatalogador();

        return "MSEDRAN".equalsIgnoreCase(ultimoCatalogador)
                && reg.getCampos(CAMPOS_MATERIA).isEmpty();
    }

    private void cargarConjuntoEnInventario() {
        if (data.isEmpty()) {
            mensaje("Sin registros", "Primero hay que generar el conjunto.");
            return;
        }

        lblEstado.setText("Cargando conjunto en inventario...");
        btnCargarInventario.setDisable(true);
        btnVerLoteInventario.setDisable(true);
        btnEnviarPreunificador.setDisable(true);
        btnEnviarPrecatalogador.setDisable(true);

        cron.ejecutarEnSegundoPlano(
                () -> {
                    insertarEnInventario();
                    return null;
                },
                () -> {
                    table.refresh();
                    lblEstado.setText("Carga de inventario finalizada.");

                    btnCargarInventario.setDisable(false);
                    btnVerLoteInventario.setDisable(false);
                    btnEnviarPreunificador.setDisable(false);
                    btnEnviarPrecatalogador.setDisable(false);

                    mensaje(
                            "Carga finalizada",
                            "El conjunto fue procesado contra archivocronica.inventario.\n"
                            + "Revisá la columna Estado para ver insertados u omitidos."
                    );
                },
                (ex) -> {
                    btnCargarInventario.setDisable(false);
                    lblEstado.setText("Error al cargar inventario.");
                    mensaje("Error", "No se pudo cargar el conjunto en inventario:\n" + ex.getMessage());
                }
        );
    }

    private void insertarEnInventario() throws SQLException {
        String sql = ""
            + "INSERT INTO inventario "
            + "(barcode, nroA, nroNid, nroAnm, autor, titulo, fechaISO, observaciones, ufi) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "observaciones = VALUES(observaciones), "
            + "autor = IF((autor IS NULL OR autor = '') "
            + "AND VALUES(autor) IS NOT NULL "
            + "AND VALUES(autor) <> '', VALUES(autor), autor)";

        try (PreparedStatement ps = cron.conn.prepareStatement(sql)) {
            for (RegistroMSInventario fila : data) {
                ps.setString(1, fila.getBarcode());
                ps.setString(2, fila.getNroA());
                ps.setString(3, "");
                ps.setString(4, "");
                ps.setString(5, fila.getAutor());
                ps.setString(6, fila.getTitulo());
                ps.setString(7, fila.getFechaISO());
                ps.setString(8, "Importado desde registro MS SYS " + fila.getSys());
                ps.setString(9, fila.getUfi());

                int resultado = ps.executeUpdate();

                if (resultado == 1) {
                    fila.setEstado("insertado");
                } else if (resultado == 2) {
                    fila.setEstado("actualizado / existente");
                } else {
                    fila.setEstado("sin cambios");
                }
            }
        }

        Platform.runLater(() -> table.refresh());
    }

    private void verLoteEnInventario() {
        String consulta = ""
                + "SELECT * FROM inventario "
                + "WHERE observaciones LIKE 'Importado desde registro MS SYS %' "
                + "AND barcode NOT IN (SELECT barcode FROM items) "
                + "AND barcode NOT IN (SELECT barcode FROM conjuntos) "
                + "ORDER BY titulo";

        List<String[]> lote = cron.consultaCompleta(consulta);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Lote MS en inventario");
        a.setHeaderText("Registros pendientes detectados: " + lote.size());

        StringBuilder sb = new StringBuilder();

        int limite = Math.min(lote.size(), 40);
        for (int i = 0; i < limite; i++) {
            String[] r = lote.get(i);

            String barcode = r.length > 0 ? r[0] : "";
            String nroA = r.length > 1 ? r[1] : "";
            String titulo = r.length > 5 ? r[5] : "";

            sb.append(barcode)
                    .append(" | ")
                    .append(nroA)
                    .append(" | ")
                    .append(titulo)
                    .append("\n");
        }

        if (lote.size() > limite) {
            sb.append("\n... ").append(lote.size() - limite).append(" más.");
        }

        a.setContentText(sb.toString());
        a.showAndWait();
    }

    private void mensaje(String titulo, String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, texto, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(titulo);
        a.showAndWait();
    }
}