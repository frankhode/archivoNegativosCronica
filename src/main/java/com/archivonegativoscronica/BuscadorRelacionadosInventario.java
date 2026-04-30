package com.archivonegativoscronica;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

class BuscadorRelacionadosInventario {

    private final Funciones cron;
    private final List<String[]> universo;
    private final String busquedaInicial;
    private final Consumer<List<String[]>> onAsignarSeleccionados;

    private final TableView<CandidatoInventario> tabla = new TableView<>();
    private final FilteredList<CandidatoInventario> filtrados;

    private final Map<CandidatoInventario, CheckBox> checkBoxesPorCandidato = new HashMap<>();
    private final Map<CandidatoInventario, TextField> autorFieldsPorCandidato = new HashMap<>();
    private final Map<CandidatoInventario, TextField> tituloFieldsPorCandidato = new HashMap<>();

    private Runnable actualizarEstadoCallback = () -> {};

    BuscadorRelacionadosInventario(
            Funciones cron,
            List<String[]> universo,
            String busquedaInicial,
            Consumer<List<String[]>> onAsignarSeleccionados
    ) {
        this.cron = cron;
        this.universo = universo == null ? new ArrayList<>() : universo;
        this.busquedaInicial = busquedaInicial == null ? "" : busquedaInicial;
        this.onAsignarSeleccionados = onAsignarSeleccionados;

        List<CandidatoInventario> candidatos = new ArrayList<>();
        for (String[] row : this.universo) {
            candidatos.add(new CandidatoInventario(row));
        }

        this.filtrados = new FilteredList<>(
                FXCollections.observableArrayList(candidatos),
                c -> true
        );
    }

    void showAndWait(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Buscar relacionados");
        stage.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label lblBuscar = new Label("Buscar:");
        TextField txtBuscar = new TextField(busquedaInicial);
        txtBuscar.setPromptText("Título, fotógrafo, barcode, nroA, fecha...");
        txtBuscar.setPrefWidth(520);

        Button btnSeleccionarTodos = new Button("Seleccionar visibles");
        Button btnLimpiar = new Button("Limpiar selección");

        HBox top = new HBox(lblBuscar, txtBuscar, btnSeleccionarTodos, btnLimpiar);
        top.setSpacing(8);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        configurarTabla();

        tabla.setItems(filtrados);
        tabla.setEditable(false);
        tabla.setFocusTraversable(false);

        Label lblEstado = new Label();

        Button btnAsignar = new Button("Asignar seleccionados");
        Button btnCerrar = new Button("Cerrar");

        HBox bottom = new HBox(lblEstado, btnAsignar, btnCerrar);
        bottom.setSpacing(10);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(lblEstado, Priority.ALWAYS);

        VBox center = new VBox(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        root.setTop(top);
        root.setCenter(center);
        root.setBottom(bottom);

        BorderPane.setMargin(top, new Insets(0, 0, 10, 0));
        BorderPane.setMargin(bottom, new Insets(10, 0, 0, 0));

        Runnable actualizarEstado = () -> {
            long sel = filtrados.stream()
                    .filter(CandidatoInventario::isSeleccionado)
                    .count();

            lblEstado.setText("Visibles: " + filtrados.size() + " | Seleccionados: " + sel);
        };

        actualizarEstadoCallback = actualizarEstado;

        txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> {
            aplicarFiltro(newValue);
            actualizarEstado.run();
        });

        txtBuscar.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                enfocarCeldaLogica(0, 0);
                e.consume();
            }
        });

        btnSeleccionarTodos.setOnAction(e -> {
            for (CandidatoInventario c : filtrados) {
                c.setSeleccionado(true);
            }
            tabla.refresh();
            actualizarEstado.run();
        });

        btnLimpiar.setOnAction(e -> {
            for (CandidatoInventario c : filtrados) {
                c.setSeleccionado(false);
            }
            tabla.refresh();
            actualizarEstado.run();
        });

        btnAsignar.setOnAction(e -> {
            List<String[]> seleccionados = new ArrayList<>();

            for (CandidatoInventario c : filtrados) {
                if (c.isSeleccionado()) {
                    seleccionados.add(c.getRow());
                }
            }

            if (!seleccionados.isEmpty() && onAsignarSeleccionados != null) {
                onAsignarSeleccionados.accept(seleccionados);
                stage.close();
            }
        });

        btnCerrar.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 980, 560);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
                e.consume();
                return;
            }

            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                btnAsignar.fire();
                e.consume();
            }
        });

        stage.setScene(scene);

        aplicarFiltro(busquedaInicial);
        actualizarEstado.run();

        Platform.runLater(() -> {
            txtBuscar.requestFocus();
            txtBuscar.positionCaret(txtBuscar.getText() == null ? 0 : txtBuscar.getText().length());
        });

        stage.showAndWait();
    }

    private void configurarTabla() {
        tabla.getColumns().clear();

        TableColumn<CandidatoInventario, Boolean> colSel = new TableColumn<>("");
        colSel.setCellValueFactory(data -> data.getValue().seleccionadoProperty());
        colSel.setCellFactory(col -> new CheckBoxInventarioCell());
        colSel.setPrefWidth(45);
        colSel.setEditable(false);

        TableColumn<CandidatoInventario, String> colBarcode = new TableColumn<>("Barcode");
        colBarcode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        colBarcode.setPrefWidth(120);
        colBarcode.setEditable(false);

        TableColumn<CandidatoInventario, String> colNroA = new TableColumn<>("nroA");
        colNroA.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNroA()));
        colNroA.setPrefWidth(100);
        colNroA.setEditable(false);

        TableColumn<CandidatoInventario, String> colAutor = new TableColumn<>("Fotógrafo");
        colAutor.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAutor()));
        colAutor.setCellFactory(col -> new EditableInventarioCell(4));
        colAutor.setPrefWidth(190);
        colAutor.setEditable(false);

        TableColumn<CandidatoInventario, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitulo()));
        colTitulo.setCellFactory(col -> new EditableInventarioCell(5));
        colTitulo.setPrefWidth(360);
        colTitulo.setEditable(false);

        TableColumn<CandidatoInventario, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFecha()));
        colFecha.setPrefWidth(100);
        colFecha.setEditable(false);

        tabla.getColumns().addAll(colSel, colBarcode, colNroA, colAutor, colTitulo, colFecha);
    }

    private void aplicarFiltro(String texto) {
        String q = normalizarParaBuscar(texto);

        checkBoxesPorCandidato.clear();
        autorFieldsPorCandidato.clear();
        tituloFieldsPorCandidato.clear();

        if (q.isEmpty()) {
            filtrados.setPredicate(c -> true);
        } else {
            filtrados.setPredicate(c -> normalizarParaBuscar(c.textoBuscable()).contains(q));
        }

        tabla.refresh();
    }

    private static String normalizarParaBuscar(String s) {
        if (s == null) {
            return "";
        }

        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{P}\\p{S}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String safe(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index];
    }

    private static String sqlEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "''");
    }

    private void enfocarSiguiente(int rowIndex, int logicalColumn) {
        int nextRow = rowIndex;
        int nextColumn = logicalColumn + 1;

        if (nextColumn > 2) {
            nextColumn = 0;
            nextRow++;
        }

        enfocarCeldaLogica(nextRow, nextColumn);
    }

    private void enfocarAnterior(int rowIndex, int logicalColumn) {
        int prevRow = rowIndex;
        int prevColumn = logicalColumn - 1;

        if (prevColumn < 0) {
            prevColumn = 2;
            prevRow--;
        }

        enfocarCeldaLogica(prevRow, prevColumn);
    }

    private void enfocarCeldaLogica(int rowIndex, int logicalColumn) {
        if (filtrados == null || filtrados.isEmpty()) {
            return;
        }

        if (rowIndex < 0) {
            rowIndex = 0;
        }

        if (rowIndex >= filtrados.size()) {
            rowIndex = filtrados.size() - 1;
        }

        CandidatoInventario candidato = filtrados.get(rowIndex);

        tabla.scrollTo(rowIndex);
        tabla.getSelectionModel().clearAndSelect(rowIndex);

        final int fila = rowIndex;
        final int columna = logicalColumn;

        Platform.runLater(() -> {
            tabla.scrollTo(fila);
            tabla.layout();

            Platform.runLater(() -> {
                Node node = null;

                if (columna == 0) {
                    node = checkBoxesPorCandidato.get(candidato);
                } else if (columna == 1) {
                    node = autorFieldsPorCandidato.get(candidato);
                } else if (columna == 2) {
                    node = tituloFieldsPorCandidato.get(candidato);
                }

                if (node != null) {
                    node.requestFocus();

                    if (node instanceof TextInputControl) {
                        TextInputControl input = (TextInputControl) node;
                        input.positionCaret(input.getText() == null ? 0 : input.getText().length());
                    }
                }
            });
        });
    }

    class EditableInventarioCell extends TableCell<CandidatoInventario, String> {

        private final int inventarioIndex;
        private final TextField textField = new TextField();

        EditableInventarioCell(int inventarioIndex) {
            this.inventarioIndex = inventarioIndex;
            textField.setFocusTraversable(true);

            textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    guardarCambio();
                    e.consume();
                    return;
                }

                if (e.getCode() == KeyCode.TAB) {
                    guardarCambio();

                    CandidatoInventario candidato = getTableRow() == null ? null : getTableRow().getItem();
                    if (candidato != null) {
                        int rowIndex = filtrados.indexOf(candidato);
                        int logicalColumn = inventarioIndex == 4 ? 1 : 2;

                        if (e.isShiftDown()) {
                            enfocarAnterior(rowIndex, logicalColumn);
                        } else {
                            enfocarSiguiente(rowIndex, logicalColumn);
                        }
                    }

                    e.consume();
                    return;
                }

                if (e.getCode() == KeyCode.ESCAPE) {
                    CandidatoInventario item = getTableRow() == null ? null : getTableRow().getItem();
                    if (item != null) {
                        textField.setText(item.getValue(inventarioIndex));
                    }
                    e.consume();
                }
            });

            textField.focusedProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue) {
                    guardarCambio();
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            CandidatoInventario candidato = getTableRow().getItem();
            textField.setText(candidato.getValue(inventarioIndex));

            if (inventarioIndex == 4) {
                autorFieldsPorCandidato.put(candidato, textField);
            } else if (inventarioIndex == 5) {
                tituloFieldsPorCandidato.put(candidato, textField);
            }

            setGraphic(textField);
            setText(null);
        }

        private void guardarCambio() {
            CandidatoInventario candidato = getTableRow() == null ? null : getTableRow().getItem();

            if (candidato == null) {
                return;
            }

            String valorNuevo = textField.getText() == null ? "" : textField.getText();
            String valorAnterior = candidato.getValue(inventarioIndex);

            if (valorNuevo.equals(valorAnterior)) {
                return;
            }

            String campo;
            if (inventarioIndex == 4) {
                campo = "autor";
            } else if (inventarioIndex == 5) {
                campo = "titulo";
            } else {
                return;
            }

            String barcode = candidato.getBarcode();
            if (barcode == null || barcode.trim().isEmpty()) {
                return;
            }

            String sql = "UPDATE inventario SET "
                    + campo + "='" + sqlEscape(valorNuevo) + "' "
                    + "WHERE barcode='" + sqlEscape(barcode) + "';";

            cron.consultaSimple(sql, 1);

            candidato.setValue(inventarioIndex, valorNuevo);
        }
    }

    class CheckBoxInventarioCell extends TableCell<CandidatoInventario, Boolean> {

        private final CheckBox checkBox = new CheckBox();

        CheckBoxInventarioCell() {
            checkBox.setFocusTraversable(true);

            checkBox.setOnAction(e -> {
                CandidatoInventario candidato = getTableRow() == null ? null : getTableRow().getItem();

                if (candidato != null) {
                    candidato.setSeleccionado(checkBox.isSelected());
                    actualizarEstadoCallback.run();
                }
            });

            checkBox.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                CandidatoInventario candidato = getTableRow() == null ? null : getTableRow().getItem();

                if (candidato == null) {
                    return;
                }

                int rowIndex = filtrados.indexOf(candidato);

                if (e.getCode() == KeyCode.SPACE) {
                    checkBox.setSelected(!checkBox.isSelected());
                    candidato.setSeleccionado(checkBox.isSelected());
                    actualizarEstadoCallback.run();
                    e.consume();
                    return;
                }

                if (e.getCode() == KeyCode.TAB) {
                    if (e.isShiftDown()) {
                        enfocarAnterior(rowIndex, 0);
                    } else {
                        enfocarSiguiente(rowIndex, 0);
                    }
                    e.consume();
                }
            });
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            CandidatoInventario candidato = getTableRow().getItem();
            checkBox.setSelected(candidato.isSeleccionado());
            checkBoxesPorCandidato.put(candidato, checkBox);

            setGraphic(checkBox);
            setText(null);
        }
    }

    static class CandidatoInventario {

        private final String[] row;
        private final BooleanProperty seleccionado = new SimpleBooleanProperty(false);

        CandidatoInventario(String[] row) {
            this.row = row;
        }

        BooleanProperty seleccionadoProperty() {
            return seleccionado;
        }

        boolean isSeleccionado() {
            return seleccionado.get();
        }

        void setSeleccionado(boolean value) {
            seleccionado.set(value);
        }

        String[] getRow() {
            return row;
        }

        String getValue(int index) {
            return safe(row, index);
        }

        void setValue(int index, String value) {
            if (row != null && index >= 0 && index < row.length) {
                row[index] = value == null ? "" : value;
            }
        }

        String getBarcode() {
            return getValue(0);
        }

        String getNroA() {
            return getValue(1);
        }

        String getAutor() {
            return getValue(4);
        }

        String getTitulo() {
            return getValue(5);
        }

        String getFecha() {
            return getValue(6);
        }

        String textoBuscable() {
            return getBarcode() + " "
                    + getNroA() + " "
                    + getAutor() + " "
                    + getTitulo() + " "
                    + getFecha() + " "
                    + getValue(7) + " "
                    + getValue(8);
        }
    }
}