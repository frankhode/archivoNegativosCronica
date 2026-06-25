package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Helper visual para Catalogador-O-Matic 2.
 *
 * No modifica el COM viejo.
 */
class COM2VistaCompacta {

    private static final String FONT_MONO =
            "-fx-font-family: 'Consolas'; -fx-font-size: 11px;";

    private static final String FONT_MONO_BOLD =
            "-fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-font-weight: bold;";

    private COM2VistaCompacta() {
    }

    static String compactarAlephTag(String textoAleph) {
        if (textoAleph == null) {
            return "";
        }

        String[] lineas = textoAleph.split("\\r?\\n");
        StringBuilder out = new StringBuilder();
        String sys = "";

        for (String lineaOriginal : lineas) {
            CampoAleph c = parsearLineaAleph(lineaOriginal);
            if (c == null) {
                continue;
            }

            if (sys.length() == 0 && c.sys.length() > 0) {
                sys = c.sys;
                //out.append("SYS ").append(sys).append("\n");
            }

            String contenido = limpiarSubcampos(c.contenido);
            if (contenido.length() == 0) {
                contenido = limpiarEspacios(c.contenido);
            }

            if (contenido.length() == 0) {
                continue;
            }

            if (
                    c.tag.equals("LDR") || c.tag.equals("FMT") || c.tag.equals("SYS") 
                    || c.tag.equals("001") || c.tag.equals("005") || c.tag.equals("007") 
                    || c.tag.equals("008") || c.tag.equals("040") || c.tag.equals("043") 
                    || c.tag.equals("500") || c.tag.equals("505") || c.tag.equals("540") 
                    || c.tag.equals("561") || c.tag.equals("773")|| c.tag.equals("OWN")
                    || c.tag.equals("Z39")) {
                //out.append(c.tag).append(" ").append(contenido).append("\n");
            } else {
                out.append(c.tagIndicadores()).append(" ").append(contenido).append("\n");
            }
        }

        return out.toString().trim();
    }

    static void aplicarVistaBibliograficaCompacta(TextArea area, String textoAleph) {
        if (area == null) {
            return;
        }

        area.setText(compactarAlephTag(textoAleph));
        area.setEditable(false);
        area.setWrapText(true);
        area.setStyle(FONT_MONO);
        area.setFocusTraversable(false);
    }

    static TextArea crearTextAreaBibliografico(String textoAleph) {
        TextArea area = new TextArea();
        aplicarVistaBibliograficaCompacta(area, textoAleph);
        area.setPrefRowCount(9);
        return area;
    }

    static ScrollPane crearScrollBibliografico(String textoAleph) {
        ScrollPane sp = new ScrollPane(crearTextAreaBibliografico(textoAleph));
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        return sp;
    }

    static Node vistaItemsArray(List<String[]> items, int itemActual) {
        return vistaItemsArray(items, itemActual, 0);
    }

    static Node vistaItemsArray(List<String[]> items, int itemActual, int itemInicioSeleccionado) {
        List<ItemVista> normalizados = new ArrayList<>();

        if (items != null) {
            for (String[] item : items) {
                normalizados.add(new ItemVista(
                        valor(item, 0),
                        valor(item, 1),
                        valor(item, 8),
                        valor(item, 5),
                        valor(item, 6)
                ));
            }
        }

        return vistaItems(normalizados, itemActual, itemInicioSeleccionado);
    }

    static Node vistaItemsLista(List<List<String>> items, int itemActual) {
        return vistaItemsLista(items, itemActual, 0);
    }

    static Node vistaItemsLista(List<List<String>> items, int itemActual, int itemInicioSeleccionado) {
        List<ItemVista> normalizados = new ArrayList<>();

        if (items != null) {
            for (List<String> row : items) {
                normalizados.add(new ItemVista(
                        valor(row, 0),
                        valor(row, 1),
                        valor(row, 2),
                        valor(row, 3),
                        valor(row, 4)
                ));
            }
        }

        return vistaItems(normalizados, itemActual, itemInicioSeleccionado);
    }

    static Node vistaItemsBarcodes(List<String> barcodes, int itemActual) {
        List<ItemVista> normalizados = new ArrayList<>();

        if (barcodes != null) {
            for (String barcode : barcodes) {
                normalizados.add(new ItemVista(barcode, "", "", "", ""));
            }
        }

        return vistaItems(normalizados, itemActual, 0);
    }

    static Node vistaItems(List<ItemVista> items, int itemActual, int itemInicioSeleccionado) {
        VBox box = new VBox(3);
        box.setPadding(new Insets(2));

        int total = items == null ? 0 : items.size();
        int skip = Math.max(0, Math.min(itemInicioSeleccionado, total));
        int ok = Math.max(0, Math.min(itemActual, total) - skip);
        int faltan = Math.max(0, total - Math.max(itemActual, skip));
        int sig = total == 0 ? 0 : Math.min(itemActual + 1, total);

        Label resumen = new Label(
                "ITEMS " + total
                + " | SKIP " + skip
                + " | OK " + ok
                + " | FALTAN " + faltan
                + " | SIG " + String.format("%03d", sig)
        );
        resumen.setStyle(FONT_MONO_BOLD);

        ListView<String> lista = new ListView<>();
        lista.setFixedCellSize(20);
        lista.setStyle(FONT_MONO);
        lista.setFocusTraversable(false);

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ItemVista item = items.get(i);
                lista.getItems().add(lineaItem(i, itemActual, itemInicioSeleccionado, item));
            }
        }

        aplicarCellFactory(lista);

        box.getChildren().addAll(resumen, lista);
        VBox.setVgrow(lista, Priority.ALWAYS);

        final int scrollTo = Math.max(0, itemActual - 3);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (total > 0) {
                    lista.getSelectionModel().select(Math.min(itemActual, total - 1));
                    lista.scrollTo(scrollTo);
                }
            }
        });

        return box;
    }

    static String resumenItemsTexto(int total, int itemActual, int itemInicioSeleccionado) {
        int skip = Math.max(0, Math.min(itemInicioSeleccionado, total));
        int ok = Math.max(0, Math.min(itemActual, total) - skip);
        int faltan = Math.max(0, total - Math.max(itemActual, skip));
        int sig = total == 0 ? 0 : Math.min(itemActual + 1, total);

        return "ITEMS " + total
                + " | SKIP " + skip
                + " | OK " + ok
                + " | FALTAN " + faltan
                + " | SIG " + String.format("%03d", sig);
    }

    private static String lineaItem(int index, int itemActual, int itemInicioSeleccionado, ItemVista item) {
        String marca;
        if (index < itemInicioSeleccionado) {
            marca = "S";
        } else if (index < itemActual) {
            marca = "✓";
        } else if (index == itemActual) {
            marca = "→";
        } else {
            marca = " ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(marca).append(" ");
        sb.append(String.format("%03d", index + 1));

        if (item.barcode.length() > 0) {
            sb.append(" ").append(item.barcode);
        }
        if (item.nroA.length() > 0) {
            sb.append(" | ").append(item.nroA);
        }
        if (item.ufi.length() > 0) {
            sb.append(" | ").append(item.ufi);
        }
        if (item.fecha.length() >= 4) {
            sb.append(" | ").append(item.fecha.substring(0, 4));
        }
        if (item.titulo.length() > 0) {
            sb.append(" | ").append(recortar(item.titulo, 45));
        }

        return sb.toString();
    }

    private static void aplicarCellFactory(ListView<String> lista) {
        lista.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                if (item.startsWith("✓")) {
                    setStyle(FONT_MONO + " -fx-background-color: #d8f5d0;");
                } else if (item.startsWith("→")) {
                    setStyle(FONT_MONO + " -fx-background-color: #fff3b0; -fx-font-weight: bold;");
                } else if (item.startsWith("S")) {
                    setStyle(FONT_MONO + " -fx-text-fill: #777777;");
                } else {
                    setStyle(FONT_MONO);
                }
            }
        });
    }

    private static CampoAleph parsearLineaAleph(String linea) {
        if (linea == null) {
            return null;
        }

        linea = linea.trim();
        if (linea.length() == 0) {
            return null;
        }

        String sys = "";
        String tag = "";
        String ind = "";
        String contenido = "";

        if (linea.length() >= 13 && linea.substring(0, Math.min(9, linea.length())).matches("\\d+")) {
            sys = linea.substring(0, 9).trim();
            tag = linea.substring(10, 13).trim();

            if (linea.length() >= 15) {
                ind = linea.substring(13, 15);
            }

            int idxL = linea.indexOf(" L ");
            if (idxL >= 0) {
                contenido = linea.substring(idxL + 3).trim();
            } else if (linea.length() > 17) {
                contenido = linea.substring(17).trim();
            }
        } else {
            if (linea.length() >= 3) {
                tag = linea.substring(0, 3).trim();
            }
            if (linea.length() >= 5) {
                ind = linea.substring(3, Math.min(5, linea.length()));
            }

            int idxL = linea.indexOf(" L ");
            if (idxL >= 0) {
                contenido = linea.substring(idxL + 3).trim();
            } else if (linea.length() > 5) {
                contenido = linea.substring(5).trim();
            }
        }

        if (tag.length() == 0) {
            return null;
        }

        return new CampoAleph(sys, tag, ind, contenido);
    }

    private static String limpiarSubcampos(String s) {
        if (s == null) {
            return "";
        }

        String out = s;

        out = out.replace("$$a", "");
        out = out.replace("$$b", " ");
        out = out.replace("$$c", " ");
        out = out.replace("$$d", " ");
        out = out.replace("$$e", " ");
        out = out.replace("$$f", " ");
        out = out.replace("$$g", " ");
        out = out.replace("$$h", " ");
        out = out.replace("$$i", " ");
        out = out.replace("$$j", " ");
        out = out.replace("$$k", " ");
        out = out.replace("$$l", " ");
        out = out.replace("$$m", " ");
        out = out.replace("$$n", " ");
        out = out.replace("$$o", " ");
        out = out.replace("$$p", " ");
        out = out.replace("$$q", " ");
        out = out.replace("$$t", " ");
        out = out.replace("$$u", " ");

        out = out.replace("$$v", " -- ");
        out = out.replace("$$x", " -- ");
        out = out.replace("$$y", " -- ");
        out = out.replace("$$z", " -- ");

        out = out.replaceAll("\\$\\$[0-9a-zA-Z]", " ");

        return limpiarEspacios(out);
    }

    private static String limpiarEspacios(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s+", " ").trim();
    }

    private static String recortar(String s, int max) {
        s = limpiarEspacios(s);
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String valor(String[] arr, int pos) {
        if (arr == null || pos < 0 || pos >= arr.length || arr[pos] == null) {
            return "";
        }
        return limpiarEspacios(arr[pos]);
    }

    private static String valor(List<String> arr, int pos) {
        if (arr == null || pos < 0 || pos >= arr.size() || arr.get(pos) == null) {
            return "";
        }
        return limpiarEspacios(arr.get(pos));
    }

    static class ItemVista {
        final String barcode;
        final String nroA;
        final String ufi;
        final String titulo;
        final String fecha;

        ItemVista(String barcode, String nroA, String ufi, String titulo, String fecha) {
            this.barcode = limpiarEspacios(barcode);
            this.nroA = limpiarEspacios(nroA);
            this.ufi = limpiarEspacios(ufi);
            this.titulo = limpiarEspacios(titulo);
            this.fecha = limpiarEspacios(fecha);
        }
    }

    private static class CampoAleph {
        final String sys;
        final String tag;
        final String indicadores;
        final String contenido;

        CampoAleph(String sys, String tag, String indicadores, String contenido) {
            this.sys = sys == null ? "" : sys;
            this.tag = tag == null ? "" : tag;
            this.indicadores = indicadores == null ? "" : indicadores;
            this.contenido = contenido == null ? "" : contenido;
        }

        String tagIndicadores() {
            String ind = indicadores.trim();
            if (ind.length() == 0) {
                return tag;
            }
            return tag + ind;
        }
    }
}
