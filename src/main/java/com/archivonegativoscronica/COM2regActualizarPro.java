package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;

public class COM2regActualizarPro {

    private final COM2pro com;
    private final Funciones cron;

    private TabPane tabs;
    private Tab tabBibliografico;
    private Tab tabItems;
    private Tab tabMensajes;

    private TextArea areaBibliografico;
    private TextArea areaItems;
    private TextArea areaMensajes;

    private final List<String> mensajes = new ArrayList<>();
    private boolean hayMensajesNuevos = false;
    
    private RegistrosParaAgregar regsParaActualizar;
    private int cantRegs = 0;
    
    private RegistroCompletoCOM2Builder builderRegistro;
    private ResultadoRegistroCOM2 ultimoResultadoRegistro;
    private boolean color = false;
    private AlephAutomator2 alephAutomator;
    
    private List<String[]> itemsPendientesActuales;
    private int itemActual = 0;
    private int itemInicioSeleccionado = 0;

    public COM2regActualizarPro(COM2pro com, Funciones cron) throws InterruptedException {
        this.com = com;
        this.cron = cron;

        iniciar();
    }

    private void iniciar() {
        regsParaActualizar = com.regsParaActualizar;

        if (regsParaActualizar == null) {
            com.mostrarMensaje(
                    "COM2 ERROR\n\n"
                    + "regsParaActualizar es null.\n"
                    + "CatalogadorOMatic creó COM2pro pero no ejecutó com2.setRegistrosParaAgregar(...)."
            );
            return;
        }

        int cantHash = regsParaActualizar.getRegs().size();

        if (cantHash == 0) {
            com.mostrarMensaje(
                    "No hay registros para actualizar.\n\n"
                    + "Diagnóstico:\n"
                    + "regsParaActualizar existe, pero getRegs().size() = 0.\n\n"
                    + "Esto puede significar:\n"
                    + "- no se detectaron unificaciones para actualizar,\n"
                    + "- no se cargaron pendientes de precatalogación,\n"
                    + "- o el constructor COM2 de CatalogadorOMatic no está ejecutando la misma preparación que el COM original."
            );
            return;
        }

        cantRegs = cantHash;
        builderRegistro = new RegistroCompletoCOM2Builder(cron);
        try {
            alephAutomator = new AlephAutomator2();
        } catch (Exception ex) {
            alephAutomator = null;
            agregarMensaje("No se pudo iniciar AlephAutomator2: " + ex.getMessage());
        }
        prepararVista();
        prepararAtajos();
        mostrarPreviewInicial();

        com.botoneraPlayer.setVisible(true);
        com.enfocarVentana();
    }

    private void prepararVista() {
        com.resumen.getChildren().clear();

        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabBibliografico = new Tab("Bibliográfico");
        tabItems = new Tab("Ítems");
        tabMensajes = new Tab("Mensajes");

        areaBibliografico = crearArea();
        areaItems = crearArea();
        areaMensajes = crearArea();

        tabBibliografico.setContent(crearPanel(
                "Vista bibliográfica / ALEPH_TAG",
                areaBibliografico
        ));

        tabItems.setContent(crearPanel(
                "Ítems pendientes",
                areaItems
        ));

        tabMensajes.setContent(crearPanel(
                "Mensajes",
                areaMensajes
        ));

        tabs.getTabs().addAll(tabBibliografico, tabItems, tabMensajes);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabMensajes) {
                marcarMensajesComoLeidos();
            }
            prepararAtajos();
        });
        
        tabs.setMaxHeight(Double.MAX_VALUE);
        tabs.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(tabs, Priority.ALWAYS);
        com.resumen.getChildren().add(tabs);
    }

    private TextArea crearArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setStyle(
                "-fx-font-family: 'Consolas';"
                + "-fx-font-size: 11px;"
        );
        VBox.setVgrow(area, Priority.ALWAYS);
        return area;
    }

    private VBox crearPanel(String titulo, TextArea area) {
        VBox box = new VBox();
        box.setSpacing(4);
        box.setPadding(new Insets(4));
        box.setMinHeight(0);
        box.setMaxHeight(Double.MAX_VALUE);

        Label label = new Label(titulo);
        label.setStyle("-fx-font-weight: bold;");

        area.setMaxHeight(Double.MAX_VALUE);
        area.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(area, Priority.ALWAYS);

        box.getChildren().addAll(label, area);

        return box;
    }

    private void prepararAtajos() {
        com.scene.setOnKeyPressed(event -> {
            KeyCombination rehacerBibliografico = new KeyCodeCombination(
                    KeyCode.R,
                    KeyCombination.CONTROL_DOWN
            );
            

            KeyCombination cargarItem = new KeyCodeCombination(
                    KeyCode.I,
                    KeyCombination.CONTROL_DOWN
            );
            
            

            if (event.getCode() == KeyCode.PAGE_DOWN) {
                registroSiguiente();
                event.consume();
                return;
            }
            
            if (event.isControlDown() && event.getCode() == KeyCode.G) {
                if (estaVistaItems()) {
                    elegirItemInicial();
                } else {
                    agregarMensaje("Ctrl+G solo está habilitado en la pestaña Ítems.");
                }

                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.PAGE_UP) {
                registroAnterior();
                event.consume();
                return;
            }
            

            if (rehacerBibliografico.match(event)) {
                if (estaVistaBibliografico()) {
                    rehacerBibliografico();
                } else {
                    agregarMensaje("Ctrl+R ignorado: solo está habilitado en la vista Bibliográfico.");
                }
                event.consume();
                return;
            }

            if (cargarItem.match(event)) {
                if (estaVistaItems()) {
                    cargarItemActual();
                } else {
                    agregarMensaje("Ctrl+I ignorado: solo está habilitado en la vista Ítems.");
                }
                event.consume();
            }
        });
    }
    
    private void registroSiguiente() {
        if (com.regActual < cantRegs - 1) {
            com.regActual++;
            itemActual = 0;
            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }
        }
    }

    private void registroAnterior() {
        if (com.regActual > 0) {
            com.regActual--;
            itemActual = 0;
            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }
        }
    }

    private boolean estaVistaBibliografico() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabBibliografico;
    }

    private boolean estaVistaItems() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabItems;
    }

    private void mostrarPreviewInicial() {
        mostrarRegistroActual();
    }
    
    private void mostrarRegistroActual() {
        String sys = sysActual();
        
        itemsPendientesActuales = regsParaActualizar.returnRegInv(sys);
        itemActual = 0;
        itemInicioSeleccionado = 0;

        ultimoResultadoRegistro = builderRegistro.construir(
                sys,
                regsParaActualizar,
                color
        );

        StringBuilder bib = new StringBuilder();
        bib.append("Actualización ").append(com.regActual + 1).append(" de ").append(cantRegs).append("\n");
        bib.append("SYS: ").append(sys).append("\n\n");
        bib.append("Registro completo generado:\n\n");
        bib.append(ultimoResultadoRegistro.getPreviewConSys());

        areaBibliografico.setText(bib.toString());

        mostrarItemsActuales(itemsPendientesActuales);
        mostrarMensajesResultado(ultimoResultadoRegistro);
    }

    private String sysActual() {
        return regsParaActualizar.getIndex(com.regActual);
    }

    private String buscarRegistroOriginal(String sys) {
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '" + escaparSql(sys) + "'";
        List<String[]> res = cron.consultaCompleta(consulta);

        if (res == null || res.isEmpty() || res.get(0).length == 0) {
            return "";
        }

        return res.get(0)[0];
    }

    private void mostrarItemsActuales(List<String[]> inventariosNuevos) {
        StringBuilder sb = new StringBuilder();

        sb.append("Actualización ").append(com.regActual + 1).append(" de ").append(cantRegs).append("\n");
        sb.append("SYS: ").append(sysActual()).append("\n\n");

        sb.append("Ítems nuevos pendientes para cargar:\n\n");

        if (inventariosNuevos == null || inventariosNuevos.isEmpty()) {
            sb.append("Sin ítems pendientes.\n");
        } else {
            for (int i = 0; i < inventariosNuevos.size(); i++) {
                String[] r = inventariosNuevos.get(i);

                if (i < itemInicioSeleccionado) {
                    sb.append("[SKIP] ");
                } else if (i < itemActual) {
                    sb.append("[OK]   ");
                } else if (i == itemActual) {
                    sb.append(">>>    ");
                } else {
                    sb.append("       ");
                }

                sb.append(i + 1).append(". ");
                sb.append(valorSeguro(r, 0)).append(" | ");
                sb.append(valorSeguro(r, 1)).append(" | ");
                sb.append(valorSeguro(r, 8)).append(" | ");
                sb.append(valorSeguro(r, 5)).append(" | ");
                sb.append(valorSeguro(r, 6));
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Ctrl+G: elegir ítem inicial.\n");
            sb.append("Ctrl+I: cargar ítem actual.\n");
        }

        areaItems.setText(sb.toString());
    }

    private void mostrarMensajesResultado(ResultadoRegistroCOM2 resultado) {
        mensajes.clear();

        if (resultado != null && resultado.getMensajes() != null) {
            mensajes.addAll(resultado.getMensajes());
        }

        refrescarMensajes();

        if (!mensajes.isEmpty() && !estaVistaMensajes()) {
            hayMensajesNuevos = true;
            actualizarTituloMensajes();
        }
    }

    private String valorSeguro(String[] arr, int pos) {
        try {
            if (arr[pos] == null) {
                return "";
            }

            return arr[pos].trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String escaparSql(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replace("'", "''");
    }

    private void rehacerBibliografico() {
        if (ultimoResultadoRegistro == null) {
            agregarMensaje("No hay registro generado para pegar.");
            return;
        }

        if (alephAutomator == null) {
            agregarMensaje("AlephAutomator2 no está disponible. No se pudo pegar en Aleph.");
            return;
        }

        String sys = sysActual();

        try {
            /*
             * Flujo completo:
             * - enfoca Aleph
             * - abre SYS
             * - borra campos
             * - copia ALEPH_TAG
             * - enfoca Aleph
             * - pega Alt+T
             */
            alephAutomator.reemplazarRegistroCompletoCOM2(
                    sys,
                    ultimoResultadoRegistro.getAlephTagSinSys()
            );

            agregarMensaje("Registro " + sys + " reemplazado en Aleph con ALEPH_TAG.");
            areaBibliografico.appendText("\n\n[Ctrl+R] Registro " + sys + " reemplazado en Aleph.");

            pasarAItemsLuegoDeBibliografico();

        } catch (Exception ex) {
            agregarMensaje("Error reemplazando registro " + sys + " en Aleph: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void pasarAItemsLuegoDeBibliografico() {
        itemActual = 0;
        mostrarItemsActuales(itemsPendientesActuales);

        if (tabs != null && tabItems != null) {
            tabs.getSelectionModel().select(tabItems);
        }

        /*
         * Importante:
         * después de Ctrl+R dejamos el foco en Aleph,
         * porque el usuario puede necesitar revisar/validar antes de cargar ítems.
         *
         * NO llamar acá a com.enfocarVentana().
         */
    }
    
    private void pasarAVistaItemsDespuesDeBibliografico() {
        if (tabs != null && tabItems != null) {
            tabs.getSelectionModel().select(tabItems);
        }

        com.enfocarVentana();

        agregarMensaje("Bibliográfico reemplazado. Pasar a Ítems para cargar pendientes con Ctrl+I.");
    }

    private void cargarItemActual() {
        if (itemsPendientesActuales == null || itemsPendientesActuales.isEmpty()) {
            agregarMensaje("No hay ítems pendientes para este registro.");
            return;
        }

        if (itemActual >= itemsPendientesActuales.size()) {
            agregarMensaje("Ya se cargaron todos los ítems del registro " + sysActual() + ".");
            finalizarRegistroActual();
            return;
        }

        String[] item = itemsPendientesActuales.get(itemActual);
        String sys = sysActual();

        try {
            /*
             * Reutilizamos la lógica original de Keyboard.
             *
             * El segundo parámetro mantiene el comportamiento viejo:
             * true en el primer ítem del registro, false en los siguientes.
             */
            com.key.enfocaAleph();
            com.key.cargaItem(item, itemActual == 0);

            agregarMensaje(
                    "Ítem cargado en Aleph: "
                    + valorSeguro(item, 0)
                    + " / nroA "
                    + valorSeguro(item, 1)
                    + " / SYS "
                    + sys
                    + "."
            );

            itemActual++;
            mostrarItemsActuales(itemsPendientesActuales);

            if (itemActual >= itemsPendientesActuales.size()) {
                finalizarRegistroActual();
            } else {
                com.enfocarVentana();
            }

        } catch (Exception ex) {
            agregarMensaje("Error cargando ítem " + valorSeguro(item, 0) + ": " + ex.getMessage());
            ex.printStackTrace();
            com.enfocarVentana();
        }
    }
    
    private void finalizarRegistroActual() {
        String sysTerminado = sysActual();

        agregarMensaje("Ítems completos para SYS " + sysTerminado + ".");

        try {
            com.key.enfocaAleph();
            com.key.cierraRegistro();
            agregarMensaje("Registro " + sysTerminado + " cerrado en Aleph.");
        } catch (Exception ex) {
            agregarMensaje("No se pudo cerrar el registro " + sysTerminado + " en Aleph: " + ex.getMessage());
            ex.printStackTrace();

            /*
             * Si falla el cierre, volvemos a COM para que el usuario vea el error
             * y no avanzamos automáticamente.
             */
            com.enfocarVentana();
            return;
        }

        if (com.regActual < cantRegs - 1) {
            com.regActual++;
            itemActual = 0;

            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }

            /*
             * Acá SÍ vuelve el foco al COM:
             * ya cerró el registro anterior y queda listo para Ctrl+R del siguiente.
             */
            com.enfocarVentana();

            agregarMensaje("Listo para actualizar el siguiente registro.");
        } else {
            agregarMensaje("Proceso terminado: no quedan más registros para actualizar.");
            com.enfocarVentana();
        }
    }

    private void agregarMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().equals("")) {
            return;
        }

        mensajes.add(mensaje.trim());
        refrescarMensajes();

        if (!estaVistaMensajes()) {
            hayMensajesNuevos = true;
            actualizarTituloMensajes();
        }
    }

    private boolean estaVistaMensajes() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabMensajes;
    }

    private void refrescarMensajes() {
        if (mensajes.isEmpty()) {
            areaMensajes.setText("Sin mensajes.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (String mensaje : mensajes) {
            sb.append("• ").append(mensaje).append("\n");
        }

        areaMensajes.setText(sb.toString());
    }

    private void marcarMensajesComoLeidos() {
        hayMensajesNuevos = false;
        actualizarTituloMensajes();
    }

    private void actualizarTituloMensajes() {
        Platform.runLater(() -> {
            if (tabMensajes == null) {
                return;
            }

            if (hayMensajesNuevos) {
                tabMensajes.setText("● Mensajes");
            } else {
                tabMensajes.setText("Mensajes");
            }
        });
    }
    
    private void elegirItemInicial() {
        if (itemsPendientesActuales == null || itemsPendientesActuales.isEmpty()) {
            agregarMensaje("No hay ítems pendientes para este registro.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(itemActual + 1));
        dialog.setTitle("Elegir ítem inicial");
        dialog.setHeaderText("Ítems pendientes para SYS " + sysActual());
        dialog.setContentText("Empezar desde ítem número:");

        Optional<String> res = dialog.showAndWait();

        if (!res.isPresent()) {
            return;
        }

        String valor = res.get().trim();

        if (!valor.matches("\\d+")) {
            agregarMensaje("Número de ítem inválido: " + valor);
            return;
        }

        int elegido = Integer.parseInt(valor);

        if (elegido < 1 || elegido > itemsPendientesActuales.size()) {
            agregarMensaje(
                    "Número fuera de rango. Debe estar entre 1 y "
                    + itemsPendientesActuales.size()
                    + "."
            );
            return;
        }

        itemActual = elegido - 1;
        itemInicioSeleccionado = elegido - 1;

        mostrarItemsActuales(itemsPendientesActuales);

        agregarMensaje(
                "Carga de ítems reubicada. El próximo Ctrl+I empezará desde el ítem "
                + elegido
                + " de "
                + itemsPendientesActuales.size()
                + "."
        );

        com.enfocarVentana();
    }
}