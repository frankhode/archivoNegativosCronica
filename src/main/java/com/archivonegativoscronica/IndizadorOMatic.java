
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */

import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class IndizadorOMatic extends Tab {

    private final Funciones cron;
    private final SplitPane splitPane;
    private  VBox rightBox ;

    public IndizadorOMatic(Funciones cron) {
        this.cron = cron;
        setText("Indizador-O-Matic");
        splitPane = new SplitPane();
        rightBox = new VBox() ;
        rightBox.setAlignment(Pos.CENTER);
        setContent(splitPane);
        loadData();
    }

    private void loadData() {
        LoadingDialog loadingDialog = new LoadingDialog("Haciendo magia...");
        loadingDialog.show();

        new Thread(() -> {
            List<String[]> conjuntos = getConjuntos();
            List<Accordion> accordions = createAccordions(conjuntos);

            Platform.runLater(() -> {
                ListView<Accordion> listView = new ListView<>();
                listView.getItems().addAll(accordions);
                listView.setPrefWidth(300);
                
                TextField filterTextField = new TextField();
                filterTextField.setPromptText("Buscar...");

                // Filter the accordion items based on user input
                filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    String filter = newValue.trim().toLowerCase();
                    listView.getItems().clear();
                    for (Accordion accordion : accordions) {
                        for (TitledPane titledPane : accordion.getPanes()) {
                            if (titledPane.getText().toLowerCase().contains(filter)) {
                                listView.getItems().add(accordion);
                                break; // Once an accordion is added, move to the next one
                            }
                        }
                    }
                });

                VBox filterBox = new VBox(filterTextField, listView);
                filterBox.setSpacing(10);
                VBox.setVgrow(listView, Priority.ALWAYS);                

                splitPane.getItems().addAll(filterBox, rightBox);
                loadingDialog.close();
            });
        }).start();
    }

    private List<String[]> getConjuntos() {
        String consulta = "SELECT registros.titulo245, registros.sys FROM registros "
                + "JOIN items ON items.sys = registros.sys "
                + "WHERE items.barcode IN ("
                + "SELECT DISTINCT inv FROM digitales WHERE inv NOT IN ("
                + "SELECT barcode FROM indizimagenes)) ORDER BY RAND()";
        return cron.consultaCompleta(consulta);
    }

    private List<Accordion> createAccordions(List<String[]> conjuntos) {
        List<Accordion> accordions = new ArrayList<>();
        for (String[] conjunto : conjuntos) {
            accordions.add(createAccordion(conjunto));
        }
        return accordions;
    }

    private Accordion createAccordion(String[] conjunto) {
        Accordion accordion = new Accordion();        
        TitledPane titledPane = new TitledPane();
        titledPane.setText(conjunto[0]);
        titledPane.setWrapText(true);
        titledPane.setPrefWidth(300); // Set preferred width for the titled pane
        VBox content = new VBox(); // Content container for children
        List<String[]> children = getChildrenForSys(conjunto[1]);

        ListView<String[]> childrenListView = new ListView<>();
        childrenListView.setFixedCellSize(25); // Set the fixed height for each cell

        // Set a custom cell factory for the ListView
        childrenListView.setCellFactory(param -> new ListCell<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); // Clear text if item is empty
                } else {
                    if (children.size() > 1) {
                        setText(item[0]);
                    } else {
                        setText(item[1]);
                    }                    
                }
            }
        });

        // Add each child to the ListView
        for (String[] child : children) {
            childrenListView.getItems().add(child);
        }

        // Set the preferred height of the ListView to accommodate the existing items
        childrenListView.setPrefHeight(children.size() * childrenListView.getFixedCellSize());

        // Handle item clicks
        childrenListView.setOnMouseClicked(event -> {
            String selectedItem = childrenListView.getSelectionModel().getSelectedItem()[1];
            if (selectedItem != null) {
                muestraImagenes(selectedItem);
            }
        });

        content.getChildren().add(childrenListView); // Add children ListView to content
        titledPane.setContent(content);
        accordion.getPanes().add(titledPane); // Add titled pane to accordion
        return accordion;
    }

    private List<String[]> getChildrenForSys(String sys) {
        String consulta = "SELECT titulo,barcode FROM titulos WHERE sys LIKE '" + sys + "'";
        return cron.consultaCompleta(consulta);
    }

    private void muestraImagenes(String data) {        
        String consulta = "SELECT carpeta,nombramiento FROM digitales WHERE (inv ='" + data + "')";
        System.out.println(consulta);
        List<String[]> consultaSimple = cron.consultaCompleta(consulta);
        rightBox.getChildren().clear();
        if (!consultaSimple.isEmpty()) {            
            NavegadorDeImagenes nav = new NavegadorDeImagenes(cron, consultaSimple);
            //rightBox.getChildren().add(nav.getRoot());
        } else {
            Text txt = new Text("Todavía no disponible") ;
            txt.setFont(Font.font("Arial", FontWeight.THIN, 30));
            rightBox.getChildren().add(txt);
        }
    }
}
