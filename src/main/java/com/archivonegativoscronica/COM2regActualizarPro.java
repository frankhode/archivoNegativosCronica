package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class COM2regActualizarPro {

    private final COM2pro com;
    private final Funciones cron;

    private RegistrosParaAgregar regsParaActualizar;
    private int cantRegs;

    private TabPane tabs;
    private Tab tabBibliografico;
    private Tab tabItems;
    private Tab tabMensajes;

    private TextArea areaBibliografico;
    private TextArea areaItems;
    private TextArea areaMensajes;

    private final List<String> mensajes = new ArrayList<>();
    private boolean hayMensajesNuevos = false;

    private RegistroCompletoCOM2Builder builderRegistro;
    private ResultadoRegistroCOM2 ultimoResultadoRegistro;
    private AlephAutomator2 alephAutomator;

    private List<String[]> itemsPendientesActuales;
    private int itemActual = 0;
    private int itemInicioSeleccionado = 0;

    /*
     * Igual que COM original:
     * false = los ítems nuevos se tratan como B/N.
     * true  = los ítems nuevos se tratan como color.
     *
     * El tipo final se calcula cruzando esto con el 300 original.
     */
    private CheckBox cbColorNuevos;

    public COM2regActualizarPro(COM2pro com, Funciones cron) {
        this.com = com;
        this.cron = cron;

        iniciar();
    }

    private void iniciar() {
        regsParaActualizar = com.regsParaActualizar;

        if (regsParaActualizar == null) {
            mostrarErrorInicial("COM2: regsParaActualizar no fue inicializado.");
            return;
        }

        cantRegs = regsParaActualizar.getRegs().size();

        if (cantRegs == 0) {
            mostrarErrorInicial("COM2: no hay registros para actualizar.");
            return;
        }

        builderRegistro = new RegistroCompletoCOM2Builder(cron);

        try {
            alephAutomator = new AlephAutomator2();
        } catch (Exception ex) {
            alephAutomator = null;
            agregarMensaje("No se pudo iniciar AlephAutomator2: " + ex.getMessage());
            ex.printStackTrace();
        }

        prepararVista();
        prepararAtajos();
        mostrarRegistroActual();

        com.enfocarVentana();
    }

    private void mostrarErrorInicial(String mensaje) {
        com.resumen.getChildren().clear();

        TextArea area = new TextArea(mensaje);
        area.setEditable(false);
        area.setWrapText(true);

        com.resumen.getChildren().add(area);
        VBox.setVgrow(area, Priority.ALWAYS);
    }

    private void prepararVista() {
        tabs = new TabPane();

        areaBibliografico = crearArea();
        areaItems = crearArea();
        areaMensajes = crearArea();

        cbColorNuevos = new CheckBox("Ítems nuevos color");
        cbColorNuevos.setSelected(false);
        cbColorNuevos.setOnAction(e -> {
            if (builderRegistro != null && regsParaActualizar != null && cantRegs > 0) {
                mostrarRegistroActualManteniendoEstadoItems();
            }
        });

        tabBibliografico = new Tab("Bibliográfico");
        tabItems = new Tab("Ítems");
        tabMensajes = new Tab("Mensajes");

        tabBibliografico.setClosable(false);
        tabItems.setClosable(false);
        tabMensajes.setClosable(false);

        tabBibliografico.setContent(crearPanelBibliografico());
        tabItems.setContent(crearPanel("Ítems pendientes", areaItems));
        tabMensajes.setContent(crearPanel("Mensajes", areaMensajes));

        tabs.getTabs().addAll(tabBibliografico, tabItems, tabMensajes);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabMensajes) {
                hayMensajesNuevos = false;
                actualizarTituloMensajes();
            }
        });

        tabs.setMaxWidth(Double.MAX_VALUE);
        tabs.setMaxHeight(Double.MAX_VALUE);

        com.resumen.getChildren().clear();
        com.resumen.getChildren().add(tabs);

        VBox.setVgrow(tabs, Priority.ALWAYS);
    }

    private TextArea crearArea() {
        TextArea area = new TextArea();

        area.setEditable(false);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);

        return area;
    }

    private VBox crearPanel(String titulo, TextArea area) {
        Label label = new Label(titulo);

        VBox box = new VBox(4);
        box.getChildren().addAll(label, area);

        VBox.setVgrow(area, Priority.ALWAYS);

        return box;
    }

    private VBox crearPanelBibliografico() {
        Label titulo = new Label("Vista bibliográfica compacta");

        HBox barra = new HBox(8);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getChildren().addAll(titulo, cbColorNuevos);

        VBox box = new VBox(4);
        box.getChildren().addAll(barra, areaBibliografico);

        VBox.setVgrow(areaBibliografico, Priority.ALWAYS);

        return box;
    }

    private void prepararAtajos() {
        com.scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.PAGE_DOWN) {
                registroSiguiente();
                e.consume();
                return;
            }

            if (e.getCode() == KeyCode.PAGE_UP) {
                registroAnterior();
                e.consume();
                return;
            }

            if (e.isControlDown() && e.getCode() == KeyCode.R) {
                if (estaVistaBibliografica()) {
                    rehacerBibliografico();
                } else {
                    agregarMensaje("Ctrl+R solo está habilitado en la pestaña Bibliográfico.");
                }

                e.consume();
                return;
            }

            if (e.isControlDown() && e.getCode() == KeyCode.I) {
                if (estaVistaItems()) {
                    cargarItemActual();
                } else {
                    agregarMensaje("Ctrl+I solo está habilitado en la pestaña Ítems.");
                }

                e.consume();
                return;
            }

            if (e.isControlDown() && e.getCode() == KeyCode.G) {
                if (estaVistaItems()) {
                    elegirItemInicial();
                } else {
                    agregarMensaje("Ctrl+G solo está habilitado en la pestaña Ítems.");
                }

                e.consume();
            }
        });
    }

    private boolean estaVistaBibliografica() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabBibliografico;
    }

    private boolean estaVistaItems() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabItems;
    }

    private boolean estaVistaMensajes() {
        return tabs != null && tabs.getSelectionModel().getSelectedItem() == tabMensajes;
    }

    private String sysActual() {
        return regsParaActualizar.getIndex(com.regActual);
    }

    private void mostrarRegistroActual() {
        itemActual = 0;
        itemInicioSeleccionado = 0;
        construirYMostrarRegistro(sysActual(), true);
    }

    private void mostrarRegistroActualManteniendoEstadoItems() {
        construirYMostrarRegistro(sysActual(), false);
    }

    private void construirYMostrarRegistro(String sys, boolean resetearItems) {
        itemsPendientesActuales = regsParaActualizar.returnRegInv(sys);

        if (resetearItems) {
            itemActual = 0;
            itemInicioSeleccionado = 0;
        }

        boolean nuevosColor = cbColorNuevos != null && cbColorNuevos.isSelected();

        ultimoResultadoRegistro = builderRegistro.construir(
                sys,
                regsParaActualizar,
                nuevosColor
        );

        StringBuilder bib = new StringBuilder();

        bib.append("UPD ").append(com.regActual + 1).append("/").append(cantRegs);
        bib.append(" | SYS ").append(sys).append("\n");
        bib.append("Ítems nuevos color: ").append(nuevosColor ? "sí" : "no").append("\n\n");

        bib.append(resumenCompactoRegistro(ultimoResultadoRegistro.getPreviewConSys()));
        bib.append("\n");
        bib.append("Ctrl+R: abrir SYS, borrar campos y pegar ALEPH_TAG.\n");

        areaBibliografico.setText(bib.toString());

        mostrarItemsActuales(itemsPendientesActuales);
        mostrarMensajesResultado(ultimoResultadoRegistro);
    }

    private String resumenCompactoRegistro(String previewConSys) {
        if (previewConSys == null || previewConSys.trim().equals("")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String[] lineas = previewConSys.split("\\r?\\n");

        for (String linea : lineas) {
            String sinSys = quitarSysPreview(linea);

            if (sinSys.startsWith("003 ")
                    || sinSys.startsWith("245")
                    || sinSys.startsWith("260")
                    || sinSys.startsWith("300")
                    || sinSys.startsWith("500")
                    || sinSys.startsWith("505")
                    || sinSys.startsWith("650")
                    || sinSys.startsWith("655")) {

                sb.append(sinSys).append("\n");
            }
        }

        return sb.toString();
    }

    private String quitarSysPreview(String linea) {
        if (linea == null) {
            return "";
        }

        if (linea.length() > 10
                && linea.substring(0, 9).matches("\\d{9}")
                && linea.charAt(9) == ' ') {
            return linea.substring(10);
        }

        return linea;
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
                sb.append(valorSeguro(r, 6)).append("\n");
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

    private void refrescarMensajes() {
        if (areaMensajes == null) {
            return;
        }

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

    private void actualizarTituloMensajes() {
        if (tabMensajes == null) {
            return;
        }

        if (hayMensajesNuevos) {
            tabMensajes.setText("● Mensajes");
        } else {
            tabMensajes.setText("Mensajes");
        }
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
            alephAutomator.reemplazarRegistroCompletoCOM2(
                    sys,
                    ultimoResultadoRegistro.getAlephTagSinSys()
            );

            agregarMensaje("Registro " + sys + " reemplazado en Aleph con ALEPH_TAG.");
            areaBibliografico.appendText("\n[Ctrl+R] Registro " + sys + " reemplazado en Aleph.");

            pasarAItemsLuegoDeBibliografico();

        } catch (Exception ex) {
            agregarMensaje("Error reemplazando registro " + sys + " en Aleph: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void pasarAItemsLuegoDeBibliografico() {
        itemActual = 0;
        itemInicioSeleccionado = 0;

        mostrarItemsActuales(itemsPendientesActuales);

        if (tabs != null && tabItems != null) {
            tabs.getSelectionModel().select(tabItems);
        }

        /*
         * No enfocamos COM acá.
         * Después de Ctrl+R el foco queda en Aleph.
         */
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
            com.enfocarVentana();
            return;
        }

        String valor = res.get().trim();

        if (!valor.matches("\\d+")) {
            agregarMensaje("Número de ítem inválido: " + valor);
            com.enfocarVentana();
            return;
        }

        int elegido = Integer.parseInt(valor);

        if (elegido < 1 || elegido > itemsPendientesActuales.size()) {
            agregarMensaje(
                    "Número fuera de rango. Debe estar entre 1 y "
                    + itemsPendientesActuales.size()
                    + "."
            );
            com.enfocarVentana();
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

            com.enfocarVentana();
            return;
        }

        if (com.regActual < cantRegs - 1) {
            com.regActual++;
            itemActual = 0;
            itemInicioSeleccionado = 0;

            /*
             * Igual que COM original:
             * cada registro nuevo arranca con ítems nuevos B/N.
             * Si son color, el usuario lo tilda.
             */
            resetColorNuevos();

            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }

            com.enfocarVentana();

            agregarMensaje("Listo para actualizar el siguiente registro.");
        } else {
            agregarMensaje("Proceso terminado: no quedan más registros para actualizar.");
            com.enfocarVentana();
        }
    }

    private void registroSiguiente() {
        if (com.regActual < cantRegs - 1) {
            com.regActual++;
            itemActual = 0;
            itemInicioSeleccionado = 0;
            resetColorNuevos();

            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }

            com.enfocarVentana();
        }
    }

    private void registroAnterior() {
        if (com.regActual > 0) {
            com.regActual--;
            itemActual = 0;
            itemInicioSeleccionado = 0;
            resetColorNuevos();

            mostrarRegistroActual();

            if (tabs != null && tabBibliografico != null) {
                tabs.getSelectionModel().select(tabBibliografico);
            }

            com.enfocarVentana();
        }
    }

    private void resetColorNuevos() {
        if (cbColorNuevos != null) {
            cbColorNuevos.setSelected(false);
        }
    }

    private String valorSeguro(String[] fila, int index) {
        if (fila == null || index < 0 || index >= fila.length || fila[index] == null) {
            return "";
        }

        return fila[index];
    }
}