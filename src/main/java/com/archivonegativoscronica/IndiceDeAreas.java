/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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
class IndiceDeAreas {
    private final TableView table ;
    private final Funciones cron ;

    IndiceDeAreas(Funciones cron, TabPane tabPane,File folder) {
        table = new TableView() ;
        this.cron = cron ;
        
        //consulta
        String consulta = "SELECT DISTINCT mapaareas.espaniol " +
                "FROM mapaareas JOIN areas ON mapaareas.cod = areas.area " +
                "WHERE areas.area IN (SELECT cod FROM areas) ORDER BY mapaareas.espaniol" ;
        
        List<String> areas = new ArrayList<>() ;
        enviaMultiple(consulta,areas) ;
        
        TableColumn col1 = new TableColumn("Area geográfica");        
        TableColumn col2 = new TableColumn("");
        
        table.getColumns().addAll(col1,col2);
        
        col1.setCellValueFactory(new PropertyValueFactory<>("area"));
        col2.setCellValueFactory(new PropertyValueFactory<>("verReg"));
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        areas.forEach((area) -> {
            Button verReg = new Button("Ver sobres") ;
            verReg.setOnAction((event) -> {
                ResultadosArea resultado = new ResultadosArea(cron, area, tabPane) ;
            });            
            
            data.add(new Tabla(area,verReg)) ;
        });
        
        table.setItems(data);
    }
    
    private void enviaMultiple(String consulta, List<String> lista) {
        try {
            Statement st = cron.conn.createStatement();
            st.execute(consulta) ;
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                lista.add(rs.getString(1)) ;
            }
        } catch (SQLException ex) {
            mensajeSalida(ex.getMessage());
        }
    }
    
    private void mensajeSalida(String txt) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, txt, ButtonType.OK) ;
        a.setHeaderText(null);
        a.setGraphic(null);
        a.setTitle("Archivo Fotográfico del Diario Crónica");
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("files/icon.png"));
        a.show();
    }

    Node getTable() {
        return table ;
    }

    public static class Tabla {
        private SimpleStringProperty area ;
        private Button verReg ;
        
        private Tabla(String area,Button verReg){
            this.area = new SimpleStringProperty(area) ;
            this.verReg = verReg ;
        }

        public String getArea() {
            return area.get() ;
        }

        public void setArea(SimpleStringProperty area) {
            this.area = area ;
        }

        public Button getVerReg() {
            return verReg ;
        }

        public void setVerReg(Button verReg) {
            this.verReg = verReg ;
        }
    }
}
