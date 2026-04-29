package com.archivonegativoscronica;


import java.awt.AWTException;
import java.awt.Robot;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class LimpiezaOrigenAleph {

    private final Funciones cron;
    private final Keyboard key;
    private final List<CasoOrigenAleph> casos;

    private Stage stage;
    private Label lblContador;
    private Label lblSys;
    private Label lblBarcode;
    private Label lblEstado;
    private TextArea txtDetalle;

    private Button btnRegistro;
    private Button btnItem;
    private Button btnAnterior;
    private Button btnSiguiente;
    private Button btnCerrar;

    private int indexActual = 0;

    LimpiezaOrigenAleph(Funciones cron) throws AWTException, SQLException {
        this.cron = cron;
        this.key = new Keyboard(new Robot());
        this.casos = cargarCasos();

        if (casos.isEmpty()) {
            mensaje("Sin casos", "No hay registros de origen pendientes para limpiar.");
            return;
        }

        construirVista();
        mostrarCasoActual();
    }

    private List<CasoOrigenAleph> cargarCasos() throws SQLException {
        List<CasoOrigenAleph> salida = new ArrayList<>();

        String sql = ""
            + "SELECT i.barcode, i.observaciones, it.sys AS sys_item "
            + "FROM inventario i "
            + "JOIN items it ON it.barcode = i.barcode "
            + "WHERE i.observaciones LIKE 'Importado desde registro MS SYS %' "
            + "AND i.barcode IN (SELECT barcode FROM conjuntos) "
            + "AND it.sys = TRIM(REPLACE(i.observaciones, 'Importado desde registro MS SYS ', '')) "
            + "ORDER BY i.barcode";

        try (PreparedStatement ps = cron.conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String barcode = rs.getString("barcode");
                String observaciones = rs.getString("observaciones");
                String sysOrigen = extraerSysOrigen(observaciones);

                if (sysOrigen != null && !sysOrigen.isBlank()
                        && barcode != null && !barcode.isBlank()) {
                    salida.add(new CasoOrigenAleph(
                            sysOrigen.trim(),
                            barcode.trim(),
                            observaciones == null ? "" : observaciones.trim()
                    ));
                }
            }
        }

        return salida;
    }

    private String extraerSysOrigen(String observaciones) {
        if (observaciones == null) {
            return "";
        }

        String prefijo = "Importado desde registro MS SYS ";
        if (observaciones.startsWith(prefijo)) {
            return observaciones.substring(prefijo.length()).trim();
        }

        return "";
    }

    private void construirVista() {
        stage = new Stage();
        stage.setTitle("Limpieza origen ALEPH");
        stage.setAlwaysOnTop(true);

        lblContador = new Label();
        lblSys = new Label();
        lblBarcode = new Label();
        lblEstado = new Label();

        txtDetalle = new TextArea();
        txtDetalle.setEditable(false);
        txtDetalle.setWrapText(true);
        txtDetalle.setPrefHeight(120);

        btnRegistro = new Button("Registro: STA + guardar");
        btnItem = new Button("Item: abrir + borrar");
        btnAnterior = new Button("Anterior");
        btnSiguiente = new Button("Siguiente");
        btnCerrar = new Button("Cerrar");

        btnRegistro.setOnAction(e -> ejecutarRegistroActual());
        btnItem.setOnAction(e -> ejecutarItemActual());
        btnAnterior.setOnAction(e -> anterior());
        btnSiguiente.setOnAction(e -> siguiente());
        btnCerrar.setOnAction(e -> stage.close());

        HBox filaNavegacion = new HBox(10, btnAnterior, btnSiguiente, btnCerrar);
        filaNavegacion.setAlignment(Pos.CENTER);

        HBox filaAcciones = new HBox(10, btnRegistro, btnItem);
        filaAcciones.setAlignment(Pos.CENTER);

        VBox root = new VBox(
                10,
                lblContador,
                new Separator(),
                lblSys,
                lblBarcode,
                lblEstado,
                txtDetalle,
                new Separator(),
                filaAcciones,
                filaNavegacion,
                new Label("Atajos: Ctrl+R = Registro | Ctrl+I = Item | PageUp/PageDown = navegar | Esc = cerrar")
        );

        root.setPadding(new Insets(12));
        root.setPrefWidth(520);

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            KeyCombination ctrlR = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
            KeyCombination ctrlI = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);

            if (ctrlR.match(event)) {
                btnRegistro.fire();
                event.consume();
                return;
            }

            if (ctrlI.match(event)) {
                btnItem.fire();
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.PAGE_DOWN) {
                btnSiguiente.fire();
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.PAGE_UP) {
                btnAnterior.fire();
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
                event.consume();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    private void mostrarCasoActual() {
        if (casos.isEmpty()) {
            stage.close();
            mensaje("Finalizado", "No quedan casos pendientes.");
            return;
        }

        if (indexActual < 0) {
            indexActual = 0;
        }

        if (indexActual > casos.size() - 1) {
            indexActual = casos.size() - 1;
        }

        CasoOrigenAleph caso = getCasoActual();

        lblContador.setText("Caso " + (indexActual + 1) + " de " + casos.size());
        lblSys.setText("SYS origen: " + caso.getSysOrigen());
        lblBarcode.setText("Barcode: " + caso.getBarcode());
        lblEstado.setText("Estado: " + caso.getEstado());

        txtDetalle.setText(
                "Observaciones:\n"
                + caso.getObservaciones()
                + "\n\n"
                + "Secuencia recomendada:\n"
                + "1. Ctrl+R / Registro: abre bibliográfico, agrega STA $$aDELETED y guarda.\n"
                + "2. Resolver manualmente cualquier aviso/error de ALEPH.\n"
                + "3. Ctrl+I / Item: abre item por barcode, borra item y limpia observaciones."
        );

        btnAnterior.setDisable(indexActual <= 0);
        btnSiguiente.setDisable(indexActual >= casos.size() - 1);
    }

    private CasoOrigenAleph getCasoActual() {
        return casos.get(indexActual);
    }

    private void ejecutarRegistroActual() {
        CasoOrigenAleph caso = getCasoActual();

        btnRegistro.setDisable(true);
        lblEstado.setText("Estado: procesando registro...");

        try {
            key.enfocaAleph();

            key.abreBibliografico(caso.getSysOrigen());
            key.getRobot().delay(1000);

            key.irAlFinalDelRegistro();
            key.cargaCampoSTADeleted();
            key.guardarRegistro();

            caso.setEstado("registro_guardado");
            lblEstado.setText("Estado: registro_guardado");

            /*
             * Igual que en Catalogador-O-Matic:
             * después de ejecutar el bloque, vuelve el foco a la ventana de control.
             */
            stage.requestFocus();

        } catch (Exception ex) {
            caso.setEstado("error_registro");
            lblEstado.setText("Estado: error_registro");

            mensaje("Error", "Falló la acción de registro:\n" + ex.getMessage());

            stage.requestFocus();

        } finally {
            btnRegistro.setDisable(false);
        }
    }

    private void ejecutarItemActual() {
        CasoOrigenAleph caso = getCasoActual();

        btnItem.setDisable(true);
        lblEstado.setText("Estado: procesando item...");

        try {
            key.enfocaAleph();

            key.abreItem(caso.getBarcode());
            key.getRobot().delay(1000);

            key.borraItemActual();

            confirmarBorradoLocal(caso.getBarcode(), caso.getSysOrigen());

            caso.setEstado("item_borrado_local_actualizado");
            lblEstado.setText("Estado: item_borrado_local_actualizado");

            /*
             * Ya salió bien: lo quitamos de la cola visual.
             */
            casos.remove(caso);

            if (indexActual > casos.size() - 1) {
                indexActual = casos.size() - 1;
            }

            if (casos.isEmpty()) {
                stage.close();
                mensaje("Finalizado", "No quedan casos pendientes.");
            } else {
                mostrarCasoActual();
                stage.requestFocus();
            }

        } catch (Exception ex) {
            caso.setEstado("error_item");
            lblEstado.setText("Estado: error_item");

            mensaje("Error", "Falló la acción de item:\n" + ex.getMessage());

            stage.requestFocus();

        } finally {
            btnItem.setDisable(false);
        }
    }

    private void confirmarBorradoLocal(String barcode, String sysOrigen) throws SQLException {
        /*
         * Se ejecuta después de borrar el item en ALEPH.
         * IMPORTANTE:
         * No borrar por barcode solo, porque ese barcode puede haber sido cargado
         * correctamente en otro SYS después del proceso de unificación.
         */

        String deleteItem = ""
                + "DELETE FROM items "
                + "WHERE barcode = ? "
                + "AND sys = ?";

        String limpiarObs = ""
                + "UPDATE inventario "
                + "SET observaciones = '' "
                + "WHERE barcode = ? "
                + "AND observaciones LIKE 'Importado desde registro MS SYS %' "
                + "AND TRIM(REPLACE(observaciones, 'Importado desde registro MS SYS ', '')) = ?";

        try (PreparedStatement ps1 = cron.conn.prepareStatement(deleteItem);
             PreparedStatement ps2 = cron.conn.prepareStatement(limpiarObs)) {

            ps1.setString(1, barcode);
            ps1.setString(2, sysOrigen);
            ps1.executeUpdate();

            ps2.setString(1, barcode);
            ps2.setString(2, sysOrigen);
            ps2.executeUpdate();
        }
    }

    private void anterior() {
        if (indexActual > 0) {
            indexActual--;
            mostrarCasoActual();
        }
    }

    private void siguiente() {
        if (indexActual < casos.size() - 1) {
            indexActual++;
            mostrarCasoActual();
        }
    }

    private void mensaje(String titulo, String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, texto, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(titulo);
        a.showAndWait();
    }

    private static class CasoOrigenAleph {

        private final String sysOrigen;
        private final String barcode;
        private final String observaciones;
        private String estado;

        CasoOrigenAleph(String sysOrigen, String barcode, String observaciones) {
            this.sysOrigen = sysOrigen;
            this.barcode = barcode;
            this.observaciones = observaciones;
            this.estado = "pendiente";
        }

        String getSysOrigen() {
            return sysOrigen;
        }

        String getBarcode() {
            return barcode;
        }

        String getObservaciones() {
            return observaciones;
        }

        String getEstado() {
            return estado;
        }

        void setEstado(String estado) {
            this.estado = estado;
        }
    }
}