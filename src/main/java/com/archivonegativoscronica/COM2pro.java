package com.archivonegativoscronica;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

/**
 * Segundo intento del Catalogador-O-Matic.
 *
 * Objetivo:
 * 1. REG IND: usa exactamente COMregIndi original.
 * 2. REG GRU: usa exactamente COMregGrupal original.
 * 3. UPD AUTO: punto nuevo de trabajo.
 * 4. Se descarta UPD MANUAL y cualquier función no desarrollada.
 *
 * COM2:
 * - mantiene vivo botoneraPlayer para que los atajos que hacen fire()
 *   sobre sus botones sigan funcionando;
 * - pero no lo agrega al layout, así no ocupa espacio abajo;
 * - el trabajo visual queda concentrado en botoneraProgramas + resumen.
 */
public class COM2pro extends COM {

    private ToggleButton btnRegInd;
    private ToggleButton btnRegGru;
    private ToggleButton btnUpdAuto;

    public COM2pro(CatalogadorOMatic.InventariosPendientes pendientes, Funciones cron, Keyboard key) {
        super(pendientes, cron, key);

        ventana.setTitle("Catalogador-O-Matic 2");

        prepararVentanaCOM2();
        prepararBotoneraCOM2();
        ocultarBotoneraInferiorCOM2();
        prepararLayoutCOM2();
        mostrarInicio();
    }

    /**
     * El COM viejo necesita botoneraPlayer porque sus flujos registran acciones
     * sobre esos botones y algunos atajos hacen .fire().
     *
     * En COM2 la dejamos existente, pero fuera del layout.
     */
    private void ocultarBotoneraInferiorCOM2() {
        if (botoneraPlayer == null) {
            return;
        }

        botoneraPlayer.setVisible(false);
        botoneraPlayer.setManaged(false);
        botoneraPlayer.setMinHeight(0);
        botoneraPlayer.setPrefHeight(0);
        botoneraPlayer.setMaxHeight(0);
    }

    /**
     * Ya no usamos el botón Reducir/Restaurar de la botonera inferior.
     * Dejo el método como no-op por compatibilidad conceptual con versiones
     * anteriores de COM2pro.
     */
    private void corregirBotonReducirCOM2() {
        ocultarBotoneraInferiorCOM2();
    }

    private void prepararLayoutCOM2() {
        if (!(scene.getRoot() instanceof VBox)) {
            return;
        }

        VBox mainLayout = (VBox) scene.getRoot();

        mainLayout.getChildren().clear();

        /*
         * COM2 sin botonera inferior:
         *   1. botones principales arriba
         *   2. panel central crece y ocupa todo el resto
         *
         * botoneraPlayer sigue existiendo, pero no se agrega.
         */
        mainLayout.getChildren().addAll(
                botoneraProgramas,
                resumen
        );

        mainLayout.setSpacing(3);
        mainLayout.setPadding(new javafx.geometry.Insets(5));

        VBox.setVgrow(botoneraProgramas, javafx.scene.layout.Priority.NEVER);
        VBox.setVgrow(resumen, javafx.scene.layout.Priority.ALWAYS);

        botoneraProgramas.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        botoneraProgramas.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        resumen.setMinHeight(0);
        resumen.setMaxHeight(Double.MAX_VALUE);
        resumen.setMaxWidth(Double.MAX_VALUE);
        resumen.setFillWidth(true);

        ocultarBotoneraInferiorCOM2();
    }

    public void restaurarLayoutCOM2() {
        prepararLayoutCOM2();
    }

    private void prepararBotoneraCOM2() {
        if (botoneraProgramas == null) {
            return;
        }

        if (botoneraProgramas.getChildren().size() >= 3) {
            Node n1 = botoneraProgramas.getChildren().get(0);
            Node n2 = botoneraProgramas.getChildren().get(1);
            Node n3 = botoneraProgramas.getChildren().get(2);

            if (n1 instanceof ToggleButton) {
                btnRegInd = (ToggleButton) n1;
            }

            if (n2 instanceof ToggleButton) {
                btnRegGru = (ToggleButton) n2;
            }

            if (n3 instanceof ToggleButton) {
                btnUpdAuto = (ToggleButton) n3;
            }

            botoneraProgramas.getChildren().clear();

            if (btnRegInd != null) {
                botoneraProgramas.getChildren().add(btnRegInd);
            }

            if (btnRegGru != null) {
                botoneraProgramas.getChildren().add(btnRegGru);
            }

            if (btnUpdAuto != null) {
                botoneraProgramas.getChildren().add(btnUpdAuto);
            }
        }

        botoneraProgramas.setSpacing(8);
        botoneraProgramas.setPadding(new javafx.geometry.Insets(4));
        botoneraProgramas.setAlignment(javafx.geometry.Pos.CENTER);

        achicarBotonPrograma(btnRegInd);
        achicarBotonPrograma(btnRegGru);
        achicarBotonPrograma(btnUpdAuto);

        reasignarAcciones();
    }

    private void achicarBotonPrograma(ToggleButton btn) {
        if (btn == null) {
            return;
        }

        btn.setPrefWidth(68);
        btn.setMinWidth(68);
        btn.setMaxWidth(68);

        btn.setPrefHeight(36);
        btn.setMinHeight(36);
        btn.setMaxHeight(36);

        btn.setStyle(
                "-fx-font-size: 10px;"
                + "-fx-padding: 2 4 2 4;"
        );
    }

    private void reasignarAcciones() {
        if (btnRegInd != null) {
            btnRegInd.setOnAction((t) -> iniciarRegistrosIndividuales());
        }

        if (btnRegGru != null) {
            btnRegGru.setOnAction((t) -> iniciarRegistrosGrupales());
        }

        if (btnUpdAuto != null) {
            btnUpdAuto.setOnAction((t) -> iniciarActualizacionAutomatica());
        }
    }

    private void iniciarRegistrosIndividuales() {
        try {
            limpiarVistaPrograma();

            /*
             * Misma clase original.
             * COM2pro hereda de COM, por eso podemos pasar this.
             */
            new COMregIndi(this);

            /*
             * COMregIndi original puede volver a mostrar botoneraPlayer.
             * La escondemos otra vez para que COM2 siga sin botonera inferior.
             */
            ocultarBotoneraInferiorCOM2();
            prepararLayoutCOM2();

        } catch (InterruptedException ex) {
            Logger.getLogger(COM2pro.class.getName()).log(Level.SEVERE, null, ex);
            mostrarMensaje("Error iniciando REG IND: " + ex.getMessage());
        }
    }

    private void iniciarRegistrosGrupales() {
        try {
            limpiarVistaPrograma();

            /*
             * Misma clase original.
             */
            new COMregGrupal(this);

            /*
             * COMregGrupal original puede volver a mostrar botoneraPlayer.
             */
            ocultarBotoneraInferiorCOM2();
            prepararLayoutCOM2();

        } catch (InterruptedException ex) {
            Logger.getLogger(COM2pro.class.getName()).log(Level.SEVERE, null, ex);
            mostrarMensaje("Error iniciando REG GRU: " + ex.getMessage());
        }
    }

    private void iniciarActualizacionAutomatica() {
        try {
            limpiarVistaPrograma();

            /*
             * Acá va el nuevo flujo COM2.
             * Por ahora lo dejamos encapsulado en una clase propia,
             * para no tocar COMregActualizar original.
             */
            new COM2regActualizarPro(this, cron);

            ocultarBotoneraInferiorCOM2();
            prepararLayoutCOM2();

        } catch (Exception ex) {
            Logger.getLogger(COM2pro.class.getName()).log(Level.SEVERE, null, ex);
            mostrarMensaje("Error iniciando UPD AUTO COM2: " + ex.getMessage());
        }
    }

    private void limpiarVistaPrograma() {
        running = false;
        regActual = 0;
        itemActual = 0;
        status = 0;

        if (resumen != null) {
            resumen.getChildren().clear();
        }

        ocultarBotoneraInferiorCOM2();

        if (scene != null) {
            scene.setOnKeyPressed(null);
        }
    }

    private void mostrarInicio() {
        mostrarMensaje(
                "COM2pro listo.\n"
                + "REG IND: flujo original.\n"
                + "REG GRU: flujo original.\n"
                + "UPD AUTO: flujo nuevo.\n"
                + "Botonera inferior oculta; usar atajos de teclado."
        );
    }

    public void mostrarMensaje(String mensaje) {
        if (resumen == null) {
            return;
        }

        resumen.getChildren().clear();
        resumen.getChildren().add(createScrollPane(mensaje == null ? "" : mensaje));
    }

    public void enfocarVentana() {
        if (ventana == null) {
            return;
        }

        boolean estabaAlwaysOnTop = ventana.isAlwaysOnTop();

        ventana.setAlwaysOnTop(true);
        ventana.toFront();
        ventana.requestFocus();

        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().requestFocus();
        }

        ventana.setAlwaysOnTop(estabaAlwaysOnTop);
    }

    private void prepararVentanaCOM2() {
        ventana.setWidth(350);
        ventana.setHeight(520);

        ventana.setX(960);
        ventana.setY(135);

        ventana.setResizable(true);
    }
}
