/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author francisco.ortiz
 */
class Campeonatos extends Tab{
    private final Funciones cron ;
    private final TableView table ;
    private final TabPane tabPane ;
    
    public Campeonatos(Funciones c, TabPane tabpane) {
        this.cron = c ;
        this.tabPane = tabpane ;        
        String consulta = "SELECT DISTINCT tituloReg FROM partidos" ;
        List<String> campeonatos = cron.consultaSimple(consulta,1);
        table = new TableView() ;        
        
        TableColumn col1 = new TableColumn("Campeonatos");
        TableColumn col2 = new TableColumn("");
        TableColumn col3 = new TableColumn("Tiene digital");
        TableColumn col4 = new TableColumn("Año");
        
        table.getColumns().addAll(col1,col2,col3,col4);
        
        col1.setCellValueFactory(new PropertyValueFactory<>("tema"));
        col2.setCellValueFactory(new PropertyValueFactory<>("verReg"));
        col3.setCellValueFactory(new PropertyValueFactory<>("digital"));
        col4.setCellValueFactory(new PropertyValueFactory<>("anio"));
        
        ObservableList<IndiceTematico.Tabla> data = FXCollections.observableArrayList();
        campeonatos.forEach((campeonato) -> {
            Button verReg = new Button("Ver sobres") ;
            Pattern patron = Pattern.compile("\\b\\d{4}\\b");
            Matcher matcher = patron.matcher(campeonato);
            String anio = "" ;
            if (matcher.find()) {
                anio = matcher.group();
            }
            verReg.setOnAction((event) -> {
                try {
                    Resultados resultado = new Resultados(cron, campeonato, 
                            tabPane,"campeonato") ;
                } catch (IOException | SQLException ex) {
                    Logger.getLogger(IndiceTematico.class.getName()).log(Level.SEVERE, null, ex);
                }
            });            
            String dig = verificaDFigital(campeonato) ;
            data.add(new IndiceTematico.Tabla(campeonato,verReg,dig,anio)) ;
        });
        
        table.setItems(data);
        setContent(table);
        setText("Campeonatos");
        cron.shortCutTab(this);
    }
    
    private String verificaDFigital(String tema) {
        String consulta1,consulta2;
        consulta1 = "SELECT DISTINCT sys FROM items WHERE barcode IN "
                + "(SELECT inv FROM digitales)" ;
        consulta2 = "SELECT DISTINCT sys FROM materias WHERE materia LIKE '"+tema+"'" ;
        List<String> con1 = cron.consultaSimple(consulta1, 1);
        List<String> con2 = cron.consultaSimple(consulta2, 1);
        Set<String> elementosRepetidos = new HashSet<>(con1);        
        elementosRepetidos.retainAll(con2);
        if (elementosRepetidos.isEmpty()) {
            return "X" ;
        } else {
            return "✓" ;
        }
    }
    
}
