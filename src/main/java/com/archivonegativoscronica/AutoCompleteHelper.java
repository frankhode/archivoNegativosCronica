package com.archivonegativoscronica;

import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import java.util.List;
import java.util.stream.Collectors;

public class AutoCompleteHelper {

    // Máximo alto de la lista de sugerencias (en píxeles)
    private static final double MAX_LIST_HEIGHT = 200;

    public static void bindAutoCompletion(TextField textField, List<String> suggestions) {
        // Crea un ListView para mostrar las sugerencias
        ListView<String> listView = new ListView<>();
        listView.setMaxHeight(MAX_LIST_HEIGHT);
        listView.setPrefHeight(MAX_LIST_HEIGHT);
        
        // Crea un Popup que contendrá el ListView
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(listView);

        // Actualiza la lista de sugerencias según lo que escribe el usuario
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                popup.hide();
            } else {
                List<String> filteredEntries = suggestions.stream()
                        .filter(e -> e.toLowerCase().contains(newText.toLowerCase()))
                        .collect(Collectors.toList());
                if (!filteredEntries.isEmpty()) {
                    listView.setItems(FXCollections.observableArrayList(filteredEntries));
                    listView.getSelectionModel().selectFirst(); // Selecciona el primero automáticamente
                    // Ajusta el ancho del ListView al del TextField
                    listView.setPrefWidth(textField.getWidth());
                    if (!popup.isShowing()) {
                        // Muestra el Popup justo debajo del TextField
                        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
                        popup.show(textField, bounds.getMinX(), bounds.getMaxY());
                    }
                } else {
                    popup.hide();
                }
            }
        });
        
        // Al hacer click en una opción, se asigna el texto al TextField
        listView.setOnMouseClicked(event -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                textField.setText(selected);
                popup.hide();
            }
        });
        
        // Manejo de eventos de teclado en el TextField
        textField.setOnKeyPressed(event -> {
            if (popup.isShowing()) {
                if (event.getCode() == KeyCode.DOWN) {
                    listView.requestFocus();
                    listView.getSelectionModel().selectFirst();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER) {
                    String selected = listView.getSelectionModel().getSelectedItem();
                    if (selected == null && !listView.getItems().isEmpty()) {
                        selected = listView.getItems().get(0); // Forzar el primero
                    }
                    if (selected != null) {
                        textField.setText(selected);
                        popup.hide();
                        event.consume();
                    }
                }
            }
        });

        
        // Manejo de eventos de teclado en el ListView
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Al presionar ENTER, asigna el elemento seleccionado al TextField
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    textField.setText(selected);
                    popup.hide();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.UP && listView.getSelectionModel().getSelectedIndex() == 0) {
                // Si se presiona UP en el primer elemento, retorna el foco al TextField
                textField.requestFocus();
                event.consume();
            }
        });
        
        // Oculta el Popup al perder el foco el TextField
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                popup.hide();
            }
        });
    }
}
