/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Hotkeys globales
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

class ReubicadorItemsOMatic {
    private final Funciones cron;
    private String ufiDestino;
    private final Keyboard key;
    private List<String> barcodes;
    private int barcodeCount;
    private Stage dialog;

    // Control de flujo
    private final AtomicBoolean pausado = new AtomicBoolean(false);
    private final AtomicBoolean detenido = new AtomicBoolean(false);  // detiene lote actual
    private final AtomicBoolean stopAll = new AtomicBoolean(false);   // detiene todo (no sigue con otros lotes)
    private final AtomicBoolean stepOnce = new AtomicBoolean(false);  // avanza 1 item estando en pausa

    // Delays (ms)
    private static final int PRE_ACTION_DELAY_MS = 250;  // settle antes de tipear
    private static final int STEP_DELAY_MS = 700;        // espera entre items
    private static final int PAUSE_POLL_MS = 60;         // polling en pausa

    // Listener global (se crea/borra por corrida)
    private NativeKeyListener hotkeysListener = null;

    public ReubicadorItemsOMatic(Funciones cron, boolean programa) throws AWTException, InterruptedException {
        this.cron = cron;
        this.key = new Keyboard(new Robot());
        this.barcodes = new ArrayList<>();

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Ingresar UFI");
        alert.setHeaderText(null);
        alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DialogPane dialogPane = alert.getDialogPane();

        if (programa) {
            // Reubicador por UFI
            TextField ufi = new TextField();
            ufi.setPromptText("Ingrese la UFI de destino");

            GridPane grid = new GridPane();
            grid.add(ufi, 0, 0);

            dialogPane.setContent(grid);
            alert.setOnShown(evt -> Platform.runLater(ufi::requestFocus));

            alert.showAndWait().ifPresent(result -> {
                ufiDestino = ufi.getText();
                if (ufiDestino == null || ufiDestino.isBlank()) {
                    Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                    validationAlert.setTitle("Error");
                    validationAlert.setHeaderText("Ingrese la UFI de destino");
                    validationAlert.showAndWait();
                } else {
                    // empezar fresco cada pasada manual
                    barcodes.clear();
                    agregaBarcodes();
                }
            });

            reubicaEnAleph(null);

        } else {
            // Reubicador por lote (barcode \t ufi)
            TextArea data = new TextArea();
            data.setPrefSize(300, 500);
            data.setPromptText("Lote a reubicar: barcode \\t ufi");

            GridPane grid = new GridPane();
            grid.add(data, 0, 0);

            dialogPane.setContent(grid);
            alert.setOnShown(evt -> Platform.runLater(data::requestFocus));

            alert.showAndWait().ifPresent(result -> {
                String dataText = data.getText();
                if (dataText == null || dataText.isBlank()) {
                    Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                    validationAlert.setTitle("Error");
                    validationAlert.setHeaderText("Ingrese la lista de destino");
                    validationAlert.showAndWait();
                } else {
                    HashMap<String, List<String>> dataMap = new HashMap<>();
                    String[] dataList = dataText.split("\n");
                    for (String par : dataList) {
                        if (par == null || par.isBlank()) continue;
                        String[] split = par.split("\t");
                        if (split.length < 2) continue;
                        String barcode = split[0].trim();
                        String ufi = split[1].trim();
                        if (barcode.isEmpty() || ufi.isEmpty()) continue;
                        dataMap.computeIfAbsent(ufi, k -> new ArrayList<>()).add(barcode);
                    }
                    Queue<Map.Entry<String, List<String>>> colaDeLotes = new LinkedList<>(dataMap.entrySet());
                    procesarLote(colaDeLotes);
                }
            });
        }
    }

    private void agregaBarcodes() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Barcode para agregar a " + ufiDestino);
        alert.setHeaderText(null);

        TextField bar = new TextField();
        bar.setPromptText("Ingrese el barcode");
        DialogPane dialogPane = alert.getDialogPane();

        GridPane grid = new GridPane();
        grid.add(bar, 0, 0);
        dialogPane.setContent(grid);
        alert.setOnShown(evt -> Platform.runLater(bar::requestFocus));
        alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(result -> {
            String barcode = bar.getText();
            if (barcode != null && !barcode.isBlank()) {
                barcodes.add(barcode.trim());
                agregaBarcodes();
            }
        });
    }

    /* =================== Núcleo del lote =================== */

    private void reubicaEnAleph(Runnable onFinish) {
        barcodeCount = 0;

        // reset completo por corrida
        stopAll.set(false);
        detenido.set(false);
        pausado.set(false);
        stepOnce.set(false);

        construirDialogoYHotkeys();

        Thread worker = new Thread(() -> {
            try {
                sleepQuiet(PRE_ACTION_DELAY_MS);

                while (barcodeCount < barcodes.size()) {
                    if (stopAll.get() || detenido.get()) break;

                    // Pausa (con “paso a paso”)
                    while (pausado.get() && !stopAll.get() && !detenido.get()) {
                        if (stepOnce.get()) {
                            stepOnce.set(false);
                            break; // procesa 1 y vuelve a pausar
                        }
                        sleepQuiet(PAUSE_POLL_MS);
                    }
                    if (stopAll.get() || detenido.get()) break;

                    final int idx = barcodeCount;
                    actualizarLabel(new LabelUpdater() {
                        @Override public String text() {
                            return "Item " + (idx + 1) + " de " + barcodes.size();
                        }
                    });

                    // Acción principal en Aleph
                    key.cambiaUfi(barcodes.get(idx), ufiDestino);

                    barcodeCount++;

                    // Espera para que Aleph procese
                    sleepQuiet(STEP_DELAY_MS);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(ReubicadorItemsOMatic.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                Platform.runLater(() -> {
                    if (dialog != null && dialog.isShowing()) dialog.close();
                });
                unregisterGlobalHotkeys(); // solo saca el listener, deja el hook activo

                if (stopAll.get()) {
                    cron.mensajeSalida("Salida: STOP ALL activado por el usuario.");
                } else if (detenido.get()) {
                    cron.mensajeSalida("Salida: Lote cancelado por el usuario.");
                    if (onFinish != null) onFinish.run();
                } else if (barcodeCount >= barcodes.size()) {
                    cron.mensajeSalida("Salida: fin de los barcodes (" + barcodeCount + ").");
                    if (onFinish != null) onFinish.run();
                } else {
                    cron.mensajeSalida("Salida: interrumpido inesperadamente.");
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void procesarLote(Queue<Map.Entry<String, List<String>>> cola) {
        if (cola.isEmpty()) return;

        Map.Entry<String, List<String>> loteActual = cola.poll();
        this.ufiDestino = loteActual.getKey();
        this.barcodes = loteActual.getValue();
        this.barcodeCount = 0;

        reubicaEnAleph(new Runnable() {
            @Override public void run() {
                if (!stopAll.get()) {
                    procesarLote(cola);
                }
            }
        });
    }

    /* =================== UI + Hotkeys =================== */

    private void construirDialogoYHotkeys() {
        Platform.runLater(() -> {
            dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setAlwaysOnTop(true);
            dialog.setTitle("Cambiar ubicación");

            Button btnSiguiente = new Button("Siguiente (paso)");
            Button btnCancelar = new Button("Cancelar lote");
            Label label = new Label("Item " + (barcodeCount + 1) + " de " + barcodes.size());
            label.setPadding(new Insets(10));

            HBox hb = new HBox(btnSiguiente, new Separator(), btnCancelar);
            hb.setPadding(new Insets(10));

            VBox vb = new VBox(label, new Separator(), hb);
            Scene dialogScene = new Scene(vb, 260, 160);
            dialog.setScene(dialogScene);

            dialog.setX(0);
            dialog.setY((Screen.getPrimary().getBounds().getHeight() - dialogScene.getHeight()) / 2);

            btnCancelar.setOnAction(e -> {
                detenido.set(true);   // corta lote actual
                stopAll.set(false);   // sigue con otros lotes
                if (dialog.isShowing()) dialog.close();
            });

            btnSiguiente.setOnAction(e -> {
                pausado.set(true);
                stepOnce.set(true);
                actualizarLabel(new LabelUpdater() {
                    @Override public String text() {
                        return "PAUSADO (paso) - Item " + (barcodeCount + 1) + " de " + barcodes.size();
                    }
                });
            });

            dialog.show();

            registerGlobalHotkeys(label);
        });
    }

    private void actualizarLabel(LabelUpdater supplier) {
        Platform.runLater(() -> {
            if (dialog != null && dialog.isShowing()) {
                Scene sc = dialog.getScene();
                if (sc != null && sc.getRoot() instanceof VBox) {
                    VBox vb = (VBox) sc.getRoot();
                    if (!vb.getChildren().isEmpty() && vb.getChildren().get(0) instanceof Label) {
                        Label lbl = (Label) vb.getChildren().get(0);
                        lbl.setText(supplier.text());
                    }
                }
            }
        });
    }

    private interface LabelUpdater {
        String text();
    }

    /** Registra el hook una sola vez en toda la app; por corrida solo se agrega/quita el listener. */
    private static void ensureNativeHookOnce() {
        // silenciar logging ruidoso de JNativeHook
        java.util.logging.Logger jnhLogger =
            java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jnhLogger.setLevel(java.util.logging.Level.OFF);
        jnhLogger.setUseParentHandlers(false);

        if (!GlobalScreen.isNativeHookRegistered()) {
            try {
                GlobalScreen.registerNativeHook();
                // al cerrar la app, desregistrar el hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try { GlobalScreen.unregisterNativeHook(); } catch (Exception ignore) {}
                }));
            } catch (NativeHookException e) {
                Logger.getLogger(ReubicadorItemsOMatic.class.getName())
                      .log(Level.WARNING, "No se pudo registrar NativeHook una vez", e);
            }
        }
    }

    private void registerGlobalHotkeys(Label label) {
        // asegurá hook activo
        ensureNativeHookOnce();

        // si quedó un listener viejo, sacarlo
        if (hotkeysListener != null) {
            try { GlobalScreen.removeNativeKeyListener(hotkeysListener); } catch (Exception ignore) {}
            hotkeysListener = null;
        }

        // crear listener NUEVO para esta corrida
        hotkeysListener = new NativeKeyListener() {            
            private boolean ctrlPressed = false;

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                int code = e.getKeyCode();
                if (code == NativeKeyEvent.VC_CONTROL) ctrlPressed = true;                

                // Ctrl+P → Pausar/Reanudar
                if (ctrlPressed && code == NativeKeyEvent.VC_P) {
                    boolean nowPaused = !pausado.get();
                    pausado.set(nowPaused);
                    Platform.runLater(() -> label.setText(
                        (nowPaused ? "PAUSADO - " : "") + "Item " + (barcodeCount + 1) + " de " + barcodes.size()
                    ));
                }

                // Ctrl+X → STOP ALL (no sigue a otros lotes)
                if (ctrlPressed && code == NativeKeyEvent.VC_X) {
                    stopAll.set(true);
                    detenido.set(true);
                    Platform.runLater(() -> { if (dialog != null && dialog.isShowing()) dialog.close(); });
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) ctrlPressed = false;
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) { /* no-op */ }
        };

        GlobalScreen.addNativeKeyListener(hotkeysListener);
    }

    private void unregisterGlobalHotkeys() {
        // NO desregistrar el hook aquí; solo quitar listener
        if (hotkeysListener != null) {
            try { GlobalScreen.removeNativeKeyListener(hotkeysListener); } catch (Exception ignore) {}
            hotkeysListener = null;
        }
    }

    /* =================== Utils =================== */

    private static void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
