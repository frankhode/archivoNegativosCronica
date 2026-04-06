package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author francisco.ortiz
 */
class InventarioPorCajon {

    private enum ModoCarga { FIJA, POR_RANGOS }

    private String ufi, tituloUnificador, sysUnificador;
    private final Funciones cron;
    private Cajon cajon;
    private BorderPane contenedor;
    private final Connection conn;
    CorregidorOMatic corr;
    Scene escena;

    // NUEVO: modo/rangos/ventana para controlar el flujo post-confirmación
    private ModoCarga modo = ModoCarga.FIJA;
    private List<RangeUfi> rangos; // solo si modo = POR_RANGOS
    private Stage ventana;

    /**
     * CONSTRUCTOR 1: (original) Pide UFI con diálogo simple de texto.
     */
    public InventarioPorCajon(Funciones cron) throws SQLException {
        this(cron, (String) null);
    }

    /**
     * CONSTRUCTOR 2: Recibe UFI directa. Si viene null o vacía, pide con diálogo.
     * Comportamiento: UFI fija; al terminar un sobre, el formulario se reinicia y continúa en la misma UFI.
     */
    public InventarioPorCajon(Funciones cron, String ufi) throws SQLException {
        this.cron = cron;
        this.conn = this.cron.conn;
        this.tituloUnificador = "";
        this.modo = ModoCarga.FIJA;
        this.rangos = null;

        if (ufi == null || ufi.isBlank()) {
            String ingresada = pedirUfiConDialogo();
            if (ingresada == null || ingresada.isBlank()) {
                // Usuario canceló
                return;
            }
            this.ufi = ingresada;
        } else {
            this.ufi = ufi;
        }

        procesa();
    }

    /**
     * CONSTRUCTOR 3 (nuevo): Selección de UFI desde un diálogo por rangos (Desde|Hasta|UFI).
     * Permite escribir número de inventario y resalta el rango que lo contiene.
     * Comportamiento: al terminar un sobre, se vuelve a mostrar el selector por rangos (puede cambiar UFI).
     */
    public InventarioPorCajon(Funciones cron, List<RangeUfi> rangos) throws SQLException {
        this.cron = cron;
        this.conn = this.cron.conn;
        this.tituloUnificador = "";
        this.modo = ModoCarga.POR_RANGOS;
        this.rangos = rangos != null ? new ArrayList<>(rangos) : new ArrayList<>();

        UfiPickerDialog dlg = new UfiPickerDialog(this.rangos);
        Optional<String> chosen = dlg.showAndWait();
        if (chosen.isEmpty() || chosen.get().isBlank()) {
            // Usuario canceló
            return;
        }
        this.ufi = chosen.get();

        procesa();
    }

    /** Diálogo simple para pedir UFI como texto. */
    private String pedirUfiConDialogo() {
        TextInputDialog paraUfi = new TextInputDialog();
        paraUfi.setTitle(null);
        paraUfi.setGraphic(null);
        paraUfi.setHeaderText("Ingrese la ubicación física\n del cajón");
        Optional<String> result = paraUfi.showAndWait();
        return result.orElse(null);
    }

    private void procesa() {
        cajon = new Cajon(ufi);

        // Contenedor de datos para la ventana
        contenedor = new BorderPane();
        contenedor.setPrefSize(400, 200);
        actualizaContenedor(contenedor);

        ventana = new Stage(StageStyle.UTILITY);
        escena = new Scene(contenedor);
        escena.setFill(Color.TRANSPARENT);

        ventana.setScene(escena);
        ventana.setTitle(ufi);
        ventana.show();
    }

    private void actualizaContenedor(BorderPane contenedor) {
        // Arriba
        HBox arriba = new HBox();
        BorderPane.setAlignment(arriba, Pos.CENTER);
        BorderPane.setMargin(arriba, new Insets(12, 12, 12, 12));

        Cargador cargaCentro = new Cargador(0, new Sobre(), cajon);

        Text txt = new Text();
        Region reg1 = new Region();
        reg1.setPrefWidth(20);

        arriba.getChildren().addAll(txt, reg1);
        contenedor.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
        contenedor.setTop(arriba);
        contenedor.setCenter(cargaCentro);
    }

    public class Cajon {
        private String ufi;
        private final List<Sobre> sobres;

        public Cajon(String ufi) {
            updateUfi(ufi);
            sobres = new ArrayList<>();
        }

        // AHORA público para poder cambiar UFI cuando el modo es POR_RANGOS
        public void updateUfi(String ufi) {
            this.ufi = ufi;
        }

        public String getUfi() {
            return ufi;
        }

        private int getCantSobres() {
            return sobres.size();
        }

        private void addSobre(Sobre sobre) {
            sobres.add(sobre);
        }

        private boolean cargaCajon(Funciones cron) throws IOException, SQLException {
            cron.cargaSobres(sobres, conn);
            sobres.clear();
            return true;
        }
    }

    public final class Sobre {

        private String barcode, ufi, titulo, fecha, nroA, nroANM, fotografo, observaciones;

        public Sobre() { }

        public Sobre(String ufi) {
            setUfi(ufi);
        }

        public String getNroANM() {
            return nroANM;
        }

        public void setNroANM(String nroANM) {
            this.nroANM = nroANM;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getUfi() {
            return ufi;
        }

        public void setUfi(String ufi) {
            if (ufi == null) {
                ufi = "";
            }
            this.ufi = ufi;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            if (fecha == null) {
                fecha = "";
            }
            this.fecha = fecha;
        }

        public String getNroA() {
            return nroA;
        }

        public void setNroA(String nroA) {
            if (nroA == null) {
                nroA = "";
            }
            this.nroA = nroA;
        }

        public String getFotografo() {
            return fotografo;
        }

        public void setFotografo(String fotografo) {
            if (fotografo == null) {
                fotografo = "";
            }
            this.fotografo = fotografo;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }

        private void cargaSobre() throws SQLException {
            String sql = "INSERT IGNORE INTO inventario "
                    + "(barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi) "
                    + "VALUES (?,?,?,?,?,?,?,?,?)";

            boolean prevAuto = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    // barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi
                    stmt.setString(1, barcode);
                    stmt.setString(2, nroA);
                    stmt.setString(3, "");
                    stmt.setString(4, getNroANM());
                    stmt.setString(5, fotografo);
                    stmt.setString(6, titulo);
                    stmt.setString(7, fecha);
                    stmt.setString(8, observaciones);
                    stmt.setString(9, ufi);

                    int rows = stmt.executeUpdate();
                    if (rows == 0) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setContentText("Algo falló al cargar los sobres...\n"
                                + "Posiblemente código de barras duplicado");
                        a.show();
                    }
                    conn.commit();
                }
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignore) { }
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Algo falló al cargar los sobres...\n" + e);
                a.show();
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(prevAuto);
                } catch (SQLException ignore) { }
            }
        }
    }

    private final class Cargador extends VBox {
        private int nivel;
        private TextField dato;
        private Sobre sobre;
        private Button derecha, izquierda;
        private Text encabezado;

        public Cargador(int nivel, Sobre sobre, Cajon cajon) {
            sobre.setUfi(cajon.getUfi());
            this.sobre = sobre;
            setNivel(nivel);
            this.setAlignment(Pos.CENTER);
            encabezado = new Text();
            encabezado.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));

            dato = new TextField();
            dato.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
            dato.setMaxWidth(250);
            dato.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    derecha.fire();
                }
            });

            izquierda = new Button("Cerrar");
            izquierda.setOnAction((event) -> {
                if (getNivel() == 0) {
                    this.getParent().getScene().getWindow().hide();
                } else {
                    setNivel(getNivel() - 1);
                    if (getNivel() != 5) {
                        this.getChildren().set(2, dato);
                    }
                    verificaNivel();
                }

            });
            derecha = new Button("Siguiente");
            derecha.setOnAction((event) -> {
                verificaNivel();
            });

            Region reg1 = new Region();
            reg1.setPrefWidth(30);
            Region reg2 = new Region();
            reg2.setPrefWidth(30);
            Region reg3 = new Region();
            reg3.setPrefWidth(30);
            HBox botonera = new HBox(reg1, izquierda, reg2, derecha, reg3);

            Region reg4 = new Region();
            reg4.setPrefHeight(30);
            Region reg5 = new Region();
            reg5.setPrefHeight(30);
            Region reg6 = new Region();
            reg6.setPrefHeight(30);

            this.getChildren().addAll(encabezado, reg4, dato, reg5, botonera, reg6);
            Platform.runLater(() -> dato.requestFocus());
        }

        private void verificaNivel() {
            escena.setOnKeyPressed(event -> {
                KeyCombination izq = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
                KeyCombination der = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
                if (izq.match(event)) {
                    izquierda.fire();
                }
                if (der.match(event)) {
                    derecha.fire();
                }
            });
            Platform.runLater(() -> dato.requestFocus());
            Platform.runLater(dato::end);
            switch (nivel) {
                case 0:
                    encabezado.setText("Código de barras");
                    dato.setText(sobre.getBarcode());
                    izquierda.setText("Cerrar");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(1);
                        sobre.setBarcode(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    break;

                case 1:
                    // verifica barcode en la base de inventarios
                    if (verificaBarcode(sobre.getBarcode())) {
                        // mensaje de alerta
                        Alert al = new Alert(Alert.AlertType.ERROR, "El inventario "
                                + sobre.getBarcode() + " ya figura en la base, verificar por favor", ButtonType.OK);
                        Optional<ButtonType> result = al.showAndWait();
                        if (result.isPresent()) {
                            setNivel(0);
                            sobre.setBarcode(null);
                            verificaNivel();
                        }
                    }
                    encabezado.setText("Número original");
                    if (sobre.getNroA() != null) {
                        dato.setText(sobre.getNroA());
                    } else {
                        dato.setText("");
                    }
                    izquierda.setText("Volver");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(2);
                        sobre.setNroA(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    if (sobre.getBarcode() == null) {
                        setNivel(0);
                        verificaNivel();
                    }
                    break;

                case 2:
                    encabezado.setText("Número AMN");
                    if (sobre.getNroANM() != null) {
                        dato.setText(sobre.getNroANM());
                    } else {
                        dato.setText("");
                    }
                    izquierda.setText("Volver");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(3);
                        sobre.setNroANM(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    break;

                case 3:
                    encabezado.setText("Título del sobre");
                    dato.setText(sobre.getTitulo());
                    izquierda.setText("Volver");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(4);
                        sobre.setTitulo(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    break;

                case 4:
                    encabezado.setText("Fecha del sobre");
                    dato.setText(sobre.getFecha());
                    izquierda.setText("Volver");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(5);
                        sobre.setFecha(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    break;

                case 5:
                    encabezado.setText("Fotógrafo del sobre");
                    dato.setText(sobre.getFotografo());
                    izquierda.setText("Volver");
                    derecha.setText("Siguiente");
                    derecha.setOnAction((event) -> {
                        setNivel(6);
                        sobre.setFotografo(getDato());
                        resetDato();
                        verificaNivel();
                    });
                    break;

                case 6:
                    encabezado.setText("Observaciones");
                    ComboBox combo = new ComboBox();
                    combo.getItems().add("");
                    combo.getItems().add("Sobre vacío");
                    combo.getItems().add("Incluye ficha de referencia");
                    combo.getItems().add("Sin negativos, solo ficha de referencia");
                    combo.getSelectionModel().select(0);
                    Platform.runLater(() -> combo.requestFocus());
                    combo.setOnKeyReleased(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            derecha.fire();
                        }
                    });
                    CheckBox chb = new CheckBox(tituloUnificador);
                    chb.setOnKeyReleased(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            derecha.fire();
                        }
                    });
                    if (!tituloUnificador.equals("")) {
                        this.getChildren().add(chb);
                        chb.setSelected(true);
                    }
                    this.getChildren().set(2, combo);

                    izquierda.setText("Volver");
                    derecha.setText("Agregar sobre al cajón");
                    derecha.setOnAction((event) -> {
                        if (getDato() == null) {
                            sobre.setObservaciones(combo.getSelectionModel().getSelectedItem().toString());
                        } else {
                            sobre.setObservaciones(getDato());
                        }
                        Alert fin = new Alert(Alert.AlertType.CONFIRMATION);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Ubicación física: ").append(sobre.getUfi()).append("\n");
                        sb.append("Código de barras: ").append(sobre.getBarcode()).append("\n");
                        sb.append("Número original: ").append(sobre.getNroA()).append("\n");
                        sb.append("Número ANM: ").append(sobre.getNroANM()).append("\n");
                        sb.append("Título: ").append(sobre.getTitulo()).append("\n");
                        sb.append("Fecha: ").append(sobre.getFecha()).append("\n");
                        sb.append("Fotógrafo: ").append(sobre.getFotografo()).append("\n");
                        sb.append("Observaciones: ").append(sobre.getObservaciones()).append("\n");
                        fin.setContentText(sb.toString());
                        if (!chb.isSelected()) {
                            agregarAConjunto(fin);
                        } else {
                            if (sysUnificador != null) {
                                corr.unifica(sobre.getBarcode(), sysUnificador);
                            }
                        }
                        Optional<ButtonType> result = fin.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            fin.close();
                            try {
                                sobre.cargaSobre();
                            } catch (SQLException ex) {
                                Logger.getLogger(InventarioPorCajon.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            // ←— AQUÍ SE BIFURCA EL FLUJO SEGÚN EL CONSTRUCTOR USADO
                            if (modo == ModoCarga.FIJA) {
                                // UFI fija: continuar cargando en la misma UFI
                                setNivel(0);
                                this.getChildren().remove(chb);
                                this.getChildren().set(2, dato);
                                cajon.addSobre(sobre);
                                sobre = new Sobre(cajon.getUfi());
                                verificaNivel();
                            } else {
                                // POR_RANGOS: relanzar selector de UFI (puede cambiar)
                                UfiPickerDialog dlg = new UfiPickerDialog(rangos);
                                Optional<String> chosen = dlg.showAndWait();
                                if (chosen.isPresent() && !chosen.get().isBlank()) {
                                    String nuevaUfi = chosen.get();
                                    cajon.updateUfi(nuevaUfi);
                                    ventana.setTitle(nuevaUfi);
                                    // reset UI al nivel 0 con nueva UFI
                                    setNivel(0);
                                    this.getChildren().remove(chb);
                                    this.getChildren().set(2, dato);
                                    sobre = new Sobre(cajon.getUfi());
                                    verificaNivel();
                                } else {
                                    // Si cancelás el selector, se cierra la ventana
                                    ventana.close();
                                }
                            }
                        } else {
                            fin.close();
                        }
                    });
                    break;

            }
        }

        public String getDato() {
            return dato.getText();
        }

        public void resetDato() {
            dato.setText(null);
        }

        public int getNivel() {
            return nivel;
        }

        public void setNivel(int nivel) {
            this.nivel = nivel;
        }

        private boolean verificaBarcode(String barcode) {
            String consulta = "SELECT * FROM inventario WHERE barcode LIKE '" + barcode + "';";
            List<String> consultaSimple = cron.consultaSimple(consulta, 1);
            return !consultaSimple.isEmpty();
        }

        private void agregarAConjunto(Alert fin) {
            Button verConjuntos = new Button("Unificador");
            verConjuntos.setOnAction((t) -> {
                corr = new CorregidorOMatic(cron, true);
                corr.regExistentes();
                corr.verificador();
                sysUnificador = corr.sysUnificador;
                corr.unifica(sobre.getBarcode(), sysUnificador);
                guardaUltimoUnificador(corr.sysUnificador);
            });
            fin.getDialogPane().setContent(verConjuntos);
        }

        private void guardaUltimoUnificador(String sysUnificador) {
            String consulta = "SELECT titulo245 FROM registros WHERE sys LIKE'"
                    + sysUnificador + "'";
            tituloUnificador = cron.consultaSimple(consulta, 1).get(0);
        }
    }

    // ========================================================================
    // =================== Helpers para la nueva función =======================
    // ========================================================================

    /** POJO del rango Desde-Hasta → UFI */
    static class RangeUfi {
        final int desde;
        final int hasta;
        final String ufi;

        RangeUfi(int desde, int hasta, String ufi) {
            if (hasta != 0 && desde > hasta) {
                int t = desde;
                desde = hasta;
                hasta = t;
            }
            this.desde = desde;
            this.hasta = hasta;
            this.ufi = ufi;
        }

        boolean contains(int n) {
            if (hasta == 0) return false;
            return n >= desde && n <= hasta;
        }
    }

    /** Parser de lista multilínea "desde  hasta  ufi" (tabs o espacios) */
    static class RangeUfiParser {
        static List<RangeUfi> parse(String multiline) {
            List<RangeUfi> out = new ArrayList<>();
            if (multiline == null) return out;

            String[] lines = multiline.split("\\R+");
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split("\\s+");
                if (cols.length < 3) continue;

                Integer d = tryInt(cols[0]);
                Integer h = tryInt(cols[1]);
                String u = cols[2].trim();

                if (d == null || h == null || u.isEmpty()) continue;
                out.add(new RangeUfi(d, h, u));
            }
            out.sort(Comparator.comparingInt(r -> r.desde));
            return out;
        }

        private static Integer tryInt(String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (Exception e) {
                return null;
            }
        }
    }

    /** Diálogo JavaFX con tabla (Desde|Hasta|UFI) y búsqueda por número de inventario. */
    static class UfiPickerDialog extends Dialog<String> {
        private final TextField txtNumero = new TextField();
        private final TableView<RangeUfi> table = new TableView<>();
        private final FilteredList<RangeUfi> filtered;

        UfiPickerDialog(List<RangeUfi> rangos) {
            setTitle("Seleccionar UFI por rango");
            setHeaderText("Ingresá un nroA o elegí un rango. ENTER acepta.");

            rangos.sort(Comparator.comparingInt(r -> r.desde));
            filtered = new FilteredList<>(FXCollections.observableArrayList(rangos), r -> true);

            TableColumn<RangeUfi, Number> colDesde = new TableColumn<>("Desde");
            colDesde.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().desde));
            colDesde.setPrefWidth(90);

            TableColumn<RangeUfi, Number> colHasta = new TableColumn<>("Hasta");
            colHasta.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().hasta));
            colHasta.setPrefWidth(90);

            TableColumn<RangeUfi, String> colUfi = new TableColumn<>("UFI");
            colUfi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ufi));
            colUfi.setPrefWidth(140);

            table.getColumns().addAll(colDesde, colHasta, colUfi);
            table.setItems(filtered);
            table.setPrefHeight(360);

            txtNumero.setPromptText("nroA…");
            txtNumero.setMaxWidth(220);
            txtNumero.textProperty().addListener((obs, oldV, newV) -> highlightByNumber(newV));

            txtNumero.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    if (!table.getSelectionModel().isEmpty()) {
                        setResult(table.getSelectionModel().getSelectedItem().ufi);
                        close();
                    }
                }
            });
            table.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER && !table.getSelectionModel().isEmpty()) {
                    setResult(table.getSelectionModel().getSelectedItem().ufi);
                    close();
                }
            });
            table.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !table.getSelectionModel().isEmpty()) {
                    setResult(table.getSelectionModel().getSelectedItem().ufi);
                    close();
                }
            });

            HBox searchLine = new HBox(new Label("nroA:"), txtNumero);
            searchLine.setSpacing(12);
            searchLine.setAlignment(Pos.CENTER_LEFT);

            VBox body = new VBox(10, searchLine, table);
            body.setPadding(new Insets(12));

            BorderPane root = new BorderPane();
            root.setCenter(body);
            getDialogPane().setContent(root);

            ButtonType ok = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().addAll(ok, cancel);

            Node okBtn = getDialogPane().lookupButton(ok);
            okBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

            setResultConverter(bt -> {
                if (bt == ok) {
                    RangeUfi sel = table.getSelectionModel().getSelectedItem();
                    return sel != null ? sel.ufi : null;
                }
                return null;
            });

            Platform.runLater(txtNumero::requestFocus);
        }

        private void highlightByNumber(String text) {
            int n;
            try {
                n = Integer.parseInt(text.trim());
            } catch (Exception ex) {
                table.getSelectionModel().clearSelection();
                return;
            }
            RangeUfi first = table.getItems().stream()
                    .filter(r -> r.contains(n))
                    .findFirst()
                    .orElse(null);
            if (first != null) {
                table.getSelectionModel().select(first);
                table.scrollTo(first);
            } else {
                table.getSelectionModel().clearSelection();
            }
        }
    }
}
