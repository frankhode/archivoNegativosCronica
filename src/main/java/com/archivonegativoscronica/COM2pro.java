package com.archivonegativoscronica;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Segundo intento del Catalogador-O-Matic.
 *
 * Objetivo:
 * 1. REG IND: usa exactamente COMregIndi original.
 * 2. REG GRU: usa exactamente COMregGrupal original.
 * 3. UPD AUTO: punto nuevo de trabajo.
 * 4. Se descarta UPD MANUAL y cualquier función no desarrollada.
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
        prepararLayoutCOM2();
        corregirBotonReducirCOM2();
        mostrarInicio();
    }
    
    private void corregirBotonReducirCOM2() {
        if (!(scene.getRoot() instanceof VBox)) {
            return;
        }

        VBox mainLayout = (VBox) scene.getRoot();

        for (javafx.scene.Node n : botoneraPlayer.getChildren()) {
            if (!(n instanceof Button)) {
                continue;
            }

            Button btn = (Button) n;

            if (!btn.getText().equals("Reducir") && !btn.getText().equals("Restaurar")) {
                continue;
            }

            btn.setOnAction(e -> {
                if (mainLayout.getChildren().contains(resumen)) {
                    mainLayout.getChildren().clear();
                    mainLayout.getChildren().addAll(botoneraProgramas, botoneraPlayer);

                    ventana.setWidth(botoneraPlayer.getWidth() + 90);
                    ventana.setHeight(botoneraProgramas.getHeight() + botoneraPlayer.getHeight() + 45);

                    btn.setText("Restaurar");
                } else {
                    prepararLayoutCOM2();

                    ventana.setWidth(350);
                    ventana.setHeight(520);

                    btn.setText("Reducir");
                }
            });

            break;
        }
    }
    
    private void prepararLayoutCOM2() {
        if (!(scene.getRoot() instanceof VBox)) {
            return;
        }

        VBox mainLayout = (VBox) scene.getRoot();

        mainLayout.getChildren().clear();

        /*
         * COM2 definitivo:
         *   1. botones principales siempre arriba
         *   2. panel central crece
         *   3. botonera player siempre abajo
         */
        mainLayout.getChildren().addAll(
                botoneraProgramas,
                resumen,
                botoneraPlayer
        );

        mainLayout.setSpacing(4);
        mainLayout.setPadding(new javafx.geometry.Insets(6));

        VBox.setVgrow(botoneraProgramas, javafx.scene.layout.Priority.NEVER);
        VBox.setVgrow(resumen, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(botoneraPlayer, javafx.scene.layout.Priority.NEVER);

        botoneraProgramas.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        botoneraProgramas.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        resumen.setMinHeight(0);
        resumen.setMaxHeight(Double.MAX_VALUE);
        resumen.setMaxWidth(Double.MAX_VALUE);
        resumen.setFillWidth(true);

        botoneraPlayer.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        botoneraPlayer.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
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

        } catch (InterruptedException ex) {
            Logger.getLogger(COM2pro.class.getName()).log(Level.SEVERE, null, ex);
            mostrarMensaje("Error iniciando UPD AUTO COM2: " + ex.getMessage());
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

        if (botoneraPlayer != null) {
            botoneraPlayer.setVisible(false);
        }

        if (scene != null) {
            scene.setOnKeyPressed(null);
        }
    }

    private void mostrarInicio() {
        mostrarMensaje(
                "COM2pro listo.\n\n"
                + "REG IND: usa el flujo original.\n"
                + "REG GRU: usa el flujo original.\n"
                + "UPD AUTO: flujo nuevo en desarrollo.\n\n"
                + "UPD MANUAL fue descartado."
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