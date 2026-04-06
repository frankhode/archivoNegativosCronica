/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
public class TerminoTesauro {
    private final String termino ;
    private final Funciones cron ;
    private Tab tab1,tab2,tab3,tab4 ;
    TabPane tabPane ;
    

    public TerminoTesauro(String tema,Funciones cron) throws SQLException {
        this.cron = cron ;
        termino = tema ;
        abrirVentanaModal();
    }

    private void abrirVentanaModal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Editor de términos");

        // Crear la label del título
        Label titleLabel = new Label(termino);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Crear los botones
        Button verImagenes = new Button("Ver imágenes");
        verImagenes.setOnAction((t) -> {
        });

        // Crear el TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Crear las pestañas
        tab1 = generaTab1();
        tab2 = generaTab("Variantes - UF","esVarianteDe");
        tab3 = generaTab("Términos específicos","esPadreDe");
        tab4 = generaTab("Términos relacionados","estaRelacionadoCon");

        // Agregar las pestañas al TabPane
        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4);
        tabPane.getTabs().forEach((t) -> {
            cron.shortCutTab(t);
        });

        // Crear el contenedor principal de la ventana modal
        VBox modalRoot = new VBox(10);
        modalRoot.setPadding(new Insets(10));
        HBox cabecera = new HBox(titleLabel,verImagenes) ;
        cabecera.setAlignment(Pos.CENTER_LEFT);        
        cabecera.setSpacing(100);
        
        modalRoot.getChildren().addAll(cabecera, tabPane);

        // Configurar la escena de la ventana modal
        Scene modalScene = new Scene(modalRoot, 800, 600);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    private Tab generaTab(String titulo, String relacion) {
        Tab tab = new Tab(titulo);
        
        // Crear el VBox para el contenido de la pestaña
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // Crear la lista tipo zebra
        ListView<String> termList = new ListView<>();
        termList.setFixedCellSize(30);
        termList.setStyle("-fx-control-inner-background: derive(-fx-base,80%); -fx-background-insets: 0;");

        // Obtener los términos mediante una función
        // Aquí puedes reemplazar el contenido de la lista con los términos que obtengas
        termList.getItems().addAll(getTerminos(relacion));

        // Crear el botón en el margen derecho de cada línea de término
        termList.setCellFactory(param -> {
            ListCell<String> cell = new ListCell<String>() {
                private final Button button = new Button("Acción");

                {
                    button.setOnAction(event -> {
                        String termino = getItem();
                        System.out.println("Realizar acción con el término: " + termino);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setGraphic(empty ? null : button);
                }
            };
            return cell;
        });

        // Agregar la lista al VBox
        tabContent.getChildren().add(termList);

        // Configurar el contenido de la pestaña "tab4"
        tab.setContent(tabContent);   
        return tab ;
    }
    
     // Función para obtener los términos
    private  List<String> getTerminos(String relacion) {
        String consulta = "SELECT terminos.termino FROM terminos "
                + "JOIN relaciones ON relaciones.id2 = terminos.id WHERE relacion "
                + "LIKE '"+relacion+"' AND id1 LIKE "
                + "(SELECT id FROM terminos WHERE termino LIKE '"+termino+"')" ;
        List<String> relacionados = cron.consultaSimple(consulta, 1);        
        return relacionados;
    }

    private Tab generaTab1() {
        return new Tab() ;
    }
}
