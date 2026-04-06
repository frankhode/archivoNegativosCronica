/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 *
 * @author francisco.ortiz
 */
class Tesauro {
    private final Funciones cron ;
    private final List<String[]> terminos,relaciones ;

    public Tesauro(Funciones cron) {
        this.cron = cron ;
        terminos = getTerminos() ;
        relaciones = getRelaciones() ;
    }
    
    // Method to fetch data from both tables and build thesaurus
    public void muestraTesauro() {
        //obtiene los terminos de primer nivel
        String consulta = ""
                + "SELECT DISTINCT terminos.termino "
                + "FROM terminos JOIN relaciones ON terminos.id=relaciones.id1 "
                + "WHERE relaciones.relacion LIKE 'esPadreDe'"
                + " AND relaciones.id1 NOT IN ("
                + "SELECT relaciones.id1 FROM relaciones WHERE relaciones.relacion LIKE 'esHijoDe')"
                + " ORDER BY terminos.termino" ;
        List<String> terminosRaiz = new ArrayList<>() ;
        cron.enviaMultiple(consulta,terminosRaiz) ;        
        List<TreeItem<Text>> raices = new ArrayList<>() ;
        
        terminosRaiz.forEach((t) -> {
            Text txt = new Text() ;
            txt.setFont(Font.font ("Verdana", 20));
            txt.setText(t);
            TreeItem<Text> item = new TreeItem<>(txt);
            item.setExpanded(true);
            raices.add(item) ;
            obtieneHijos(item,t) ;
        });
        
        // Sort the root items alphabetically
        Collections.sort(raices, Comparator.comparing(treeItem -> treeItem.getValue().getText()));

        // Sort the children of each root item alphabetically
        raices.forEach(root -> Collections.sort(root.getChildren(), Comparator.comparing(treeItem -> treeItem.getValue().getText())));
        
        TabPane sp = new TabPane() ;
        sp.setStyle("-fx-tab-min-height: 35px; -fx-tab-max-height: 80px;");
        
        raices.forEach((t) -> {
            TreeView<Text> tree = new TreeView<>(t) ;
            Tab tab = new Tab() ;
            tab.setGraphic(new Label(formateaLabel(t)));
            cron.shortCutTab(tab);
            tab.setContent(tree);
            sp.getTabs().add(tab) ;
            formateaTree(tree) ;
            tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, tema) -> {
                if (verificaMaterias(tema.getValue().getText())) {
                    try {
                        cron.muestraResultados(tema) ;
                    } catch (IOException | SQLException e) {
                    }
                } else {                    
                    tema.getValue().setStyle("-fx-text-fill: grey; -fx-opacity: 0.6;");  // Restablecer estilo para otros ítems
                }
            });
        });
        
        Tab tab = new Tab("Tesauro") ;
        cron.shortCutTab(tab);
        tab.setContent(sp); 
        if (!cron.tabPane.getTabs().contains(tab)) {
            cron.tabPane.getTabs().add(tab) ;
        }        
        cron.tabPane.getSelectionModel().select(tab);
    }
    
    private boolean obtieneHijos(TreeItem<Text> rootItem, String termino) {
        String id = getId(termino) ;
        relaciones.forEach((relacion) -> {
            String id1 = relacion[0] ;
            String id2 = relacion[2] ;
            String tipoRel = relacion[1] ;
            if (id1.equals(id) && tipoRel.equals("esPadreDe")) {
                if (getTermino(id2).equals("")) {
                    System.out.println("id 1: "+id1+" id 2: "+id2);
                }
                Text txt = new Text() ;
                txt.setFont(Font.font ("Verdana", 16));
                txt.setText(getTermino(id2));
                TreeItem<Text> item = new TreeItem<>(txt);
                
                rootItem.getChildren().add(item) ;
                //para evita el loop infinito?
                boolean bool = obtieneHijos(item, getTermino(id2)) ;
            }            
        });
        return true;
    }

    private List<String[]> getTerminos() {
        String consulta = "SELECT * FROM terminos" ;
        return cron.consultaCompleta(consulta) ;
    }

    private List<String[]> getRelaciones() {
        String consulta = "SELECT * FROM relaciones" ;
        return cron.consultaCompleta(consulta) ;
    }

    private String getId(String termino) {
        String id = "" ;
        for (String[] term : terminos) {
            if (term[1].equals(termino)) {
                id = term[0] ;
            }
        }
        return id ;
    }
    

    private String getTermino(String id) {
        String termino = "" ;
        for (String[] term : terminos) {
            if (term[0].equals(id)) {
                return term[1] ;
            }
        }
        return termino ;
    }

    private boolean verificaMaterias(String tema) {
        String consulta = "SELECT materia FROM materias WHERE materia LIKE '"+tema+"%'" ;
        return !cron.consultaSimple(consulta, 1).isEmpty() ;
    }

    private void formateaTree(TreeView<Text> tree) {
        // Customizar las celdas para aplicar el estilo de texto atenuado
        tree.setCellFactory((TreeView<Text> treeView) -> new TreeCell<Text>() {
            @Override
            protected void updateItem(Text item, boolean empty) {
                super.updateItem(item, empty);                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getText());                    
                    // Aplicar estilo de "deshabilitado" si es necesario
                    if (verificaMaterias(item.getText())) {  // Por ejemplo, aplicamos el estilo al Item 2
                        setStyle(null);  // Restablecer estilo para otros ítems
                    } else {
                        setStyle("-fx-text-fill: grey; -fx-opacity: 0.6;");                        
                    }
                }
            }
        });
    }

    private String formateaLabel(TreeItem<Text> t) {
        String label = t.getValue().getText();
        String labelFormateada = "" ;
        switch (label) {
            case "Características geográficas físicas":
                labelFormateada = "Características\ngeográficas físicas" ;
                break;
            case "Condiciones económicas y sociales":
                labelFormateada = "Condiciones económicas\ny sociales" ;
                break;
            default:
                /*
                Actividades
                Comportamiento
                Conceptos                
                Condiciones físicas
                Estados mentales
                Fenómenos naturales
                Materiales
                Objetos
                Organismos
                Organizaciones
                Partes del cuerpo
                Personas
                */
                labelFormateada = label ;
        }
        return labelFormateada ;
    }
    
}
