/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
public class IndiceTematico {
    private final TableView table ;
    private final Funciones con ;
    private BarraDeNavegacion bn ;

    public IndiceTematico(String campo, Funciones con, File folder,
            int inicio,int cantidad,String comienzo,TabPane tabPane,int total) 
            throws IOException, SQLException {
        
        table = new TableView() ;
        this.con = con ;
        String consulta = "SELECT DISTINCT materia FROM materias "
                + "WHERE materia >= '"+comienzo+"' AND campo = '"+campo+"'"
                + " ORDER BY materia LIMIT "+inicio+", " +cantidad+" ;" ;

        bn = new BarraDeNavegacion(inicio,cantidad,comienzo,total) ;
        
        List<String> terminos = con.consultaSimple(consulta, 1) ;
        
        String indice = null ;
        
        switch(campo) {
            case "600":
                indice = "Personas" ;
                break;
            case "610":
                indice = "Entidades" ;
                break;
            case "611":
                indice = "Congresos, reuniones, etc." ;
                break;
            case "630":
                indice = "Títulos" ;
                break;
            case "650":
                indice = "Temas" ;
                break;
            case "651":
                indice = "Lugares" ;
                break;
            case "043":
                indice = "Areas geográficas" ;
                break;
        }
        
        TableColumn col1 = new TableColumn(indice);
        TableColumn col2 = new TableColumn("");
        TableColumn col3 = new TableColumn("Tiene digital");
        
        table.getColumns().addAll(col1,col2,col3);
        
        col1.setCellValueFactory(new PropertyValueFactory<>("tema"));
        col2.setCellValueFactory(new PropertyValueFactory<>("verReg"));
        col3.setCellValueFactory(new PropertyValueFactory<>("digital"));
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        terminos.forEach((tema) -> {
            Button verReg = new Button("Ver sobres") ;
            verReg.setOnAction((event) -> {
                List<String> resu = new ArrayList<>() ;
                resu.add(tema) ;
                try {
                    Resultados resultado = new Resultados(con, resu.get(0), tabPane,"tema") ;
                } catch (IOException | SQLException ex) {
                    Logger.getLogger(IndiceTematico.class.getName()).log(Level.SEVERE, null, ex);
                }
            });            
            String dig = verificaDFigital(tema) ;
            data.add(new Tabla(tema,verReg,dig)) ;
        });
        
        table.setItems(data);
                
    }
    
    private void mensajeSalida(String txt) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, txt, ButtonType.OK) ;
        a.setHeaderText(null);
        a.setGraphic(null);
        a.setTitle("Archivo Fotográfico del Diario Crónica");
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("files/icon.png").toString()));
        a.show();
    }

    public TableView getTable() {        
        return table;
    }

    public BarraDeNavegacion getNavegacion() {
        return bn ;
    }

    private String verificaDFigital(String tema) {
        String consulta1,consulta2;
        consulta1 = "SELECT DISTINCT sys FROM items WHERE barcode IN "
                + "(SELECT inv FROM digitales)" ;
        consulta2 = "SELECT DISTINCT sys FROM materias WHERE materia LIKE '"+tema+"'" ;
        List<String> con1 = con.consultaSimple(consulta1, 1);
        List<String> con2 = con.consultaSimple(consulta2, 1);
        Set<String> elementosRepetidos = new HashSet<>(con1);        
        elementosRepetidos.retainAll(con2);
        if (elementosRepetidos.isEmpty()) {
            return "X" ;
        } else {
            return "✓" ;
        }
    }
    
    public static class Tabla {
        private SimpleStringProperty tema,digital,anio ;
        private Button verReg ;
        
        public Tabla(String tema,Button verReg,String digital){
            this.tema = new SimpleStringProperty(tema) ;            
            this.verReg = verReg ;
            this.digital = new SimpleStringProperty(digital) ;
        }
        
        public Tabla(String tema,Button verReg,String digital,String anio){
            this.tema = new SimpleStringProperty(tema) ;            
            this.verReg = verReg ;
            this.digital = new SimpleStringProperty(digital) ;
            this.anio = new SimpleStringProperty(anio) ;
        }

        public String getTema() {
            return tema.get() ;
        }

        public void setTema(SimpleStringProperty tema) {
            this.tema = tema ;
        }
        
        public String getDigital() {
            return digital.get() ;
        }

        public void setDigital(SimpleStringProperty digital) {
            this.digital = digital ;
        }

        public Button getVerReg() {
            return verReg ;
        }

        public void setVerReg(Button verReg) {
            this.verReg = verReg ;
        }
        
        public String getAnio() {
            return anio.get() ;
        }

        public void setAnio(SimpleStringProperty anio) {
            this.anio = anio ;
        }
    }
}
