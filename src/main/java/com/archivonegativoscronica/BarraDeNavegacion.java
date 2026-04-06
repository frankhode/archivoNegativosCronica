package com.archivonegativoscronica;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Barra de navegación reutilizable para índices.
 * 
 * Estructura:
 * [ < ] [ combo cantidad ] [ > ] [ "Comenzar desde:" (texto + botón) ] [ Secciones (opcional) ]
 */
class BarraDeNavegacion extends HBox {

    private final Button ini;
    private final Button next;
    private final Button prev;
    private final TextField textField;
    private final ComboBox<String> comboBox;
    private final ComboBox<String> secciones;
    private final int inicio;
    private final int total;
    private final int cantidad;

    public BarraDeNavegacion(int inicio, int cantidad, String comienzo, int total) {
        this.inicio = inicio;
        this.cantidad = cantidad;
        this.total = total;

        setSpacing(5);
        setPadding(new Insets(5, 5, 5, 5));

        // Botones prev/next
        prev = new Button();
        next = new Button();

        // Combo de "registros por página"
        ObservableList<String> options =
                FXCollections.observableArrayList("10", "50", "100", "500", "1000");
        comboBox = new ComboBox<String>(options);
        comboBox.setPrefWidth(80);
        // FIX: antes hacías setValue(cantidad) (int), ahora lo casteamos a String
        comboBox.setValue(String.valueOf(cantidad));

        // Combo de secciones (por si después lo usás)
        secciones = new ComboBox<String>(FXCollections.observableArrayList());

        // Prev solo si hay página anterior
        if (inicio >= cantidad) {
            Image ia = new Image(getClass().getResourceAsStream("/files/anterior.png"),
                                 17, 17, false, false);
            prev.setGraphic(new ImageView(ia));
            agrega(prev);
        } else {
            prev.setDisable(true);
        }

        // Combo "cantidad"
        agrega(comboBox);

        // Next solo si hay una página siguiente
        if (inicio + cantidad < total) {
            Image ip = new Image(getClass().getResourceAsStream("/files/posterior.png"),
                                 17, 17, false, false);
            next.setGraphic(new ImageView(ip));
            agrega(next);
        } else {
            next.setDisable(true);
        }

        // Campo "comenzar desde"
        Label label = new Label("Comenzar desde:");
        textField = new TextField();
        if (comienzo != null && !comienzo.isEmpty()) {
            textField.setText(comienzo);
        }
        textField.setPrefColumnCount(20);

        ini = new Button("→");
        // el onAction lo configurás desde afuera con getIni()

        HBox hbBuscar = new HBox(5, label, textField, ini);
        hbBuscar.setPadding(new Insets(0, 5, 0, 5));
        this.getChildren().add(hbBuscar);
    }

    /** Texto del campo "Comenzar desde:" */
    public String getComienzo() {
        return textField.getText();
    }

    private void agrega(Node n) {
        HBox hb = new HBox(n);
        hb.setPadding(new Insets(0, 5, 0, 5));
        this.getChildren().add(hb);
    }


    public ComboBox<String> getComboBox() {
        return comboBox;
    }

    public void ocultaComboBox() {
        comboBox.setVisible(false);
        comboBox.setManaged(false);
    }

    public ComboBox<String> getSecciones() {
        return secciones;
    }

    public Button getIni() {
        return ini;
    }

    public Button getNext() {
        return next;
    }

    public Button getPrev() {
        return prev;
    }

    public int getInicio() {
        return inicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getTotal() {
        return total;
    }

    /**
     * Agrega combo de secciones al final de la barra.
     */
    public void verSecciones(List<String> sec) {
        secciones.getItems().clear();
        secciones.getItems().addAll(sec);
        Label label = new Label("Secciones:");
        HBox hb1 = new HBox(5, label, secciones);
        hb1.setPadding(new Insets(0, 5, 0, 5));
        this.getChildren().add(hb1);
    }
}
