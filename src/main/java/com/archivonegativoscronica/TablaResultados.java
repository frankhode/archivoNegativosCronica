/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */
import java.io.IOException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TablaResultados {

    private final TableView<Resultado> tableView;

    public TablaResultados() {
        // Configuración de la TableView
        tableView = new TableView<>();
        TableColumn<Resultado, String> columnaResultado = new TableColumn<>("Resultado");
        columnaResultado.setCellValueFactory(cellData -> cellData.getValue().getTextoResultado());
        columnaResultado.setPrefWidth(450);
        tableView.setPrefHeight(1000);

        TableColumn<Resultado, Button> columnaVerSobre = new TableColumn<>("Ver Sobres");
        columnaVerSobre.setCellValueFactory(cellData -> cellData.getValue().getBotonVerSobre());

        TableColumn<Resultado, String> columnaVerRegistro = new TableColumn<>("Cant. sobres");
        columnaVerRegistro.setCellValueFactory(cellData -> cellData.getValue().getVerRegistro());

        tableView.getColumns().addAll(columnaResultado, columnaVerSobre, columnaVerRegistro);
        
    }
    
    public void mostrarTabla(boolean mostrar) {
        tableView.setVisible(mostrar);
        tableView.setManaged(mostrar);
    }

    public TableView<Resultado> getTableView() {
        return tableView;
    }

    public void limpiarResultados() {
        tableView.getItems().clear();
    }

    public void agregarResultado(Resultado resultado) {
        tableView.getItems().add(resultado);
    }

    public static class Resultado {
        private final SimpleStringProperty textoResultado;
        private final SimpleObjectProperty<Button> botonVerSobre;
        private final SimpleStringProperty verRegistro;
        String reg ;

        public Resultado(String textoResultado, String index, Funciones cron) {
            Button verSobre = new Button("Ver sobres") ;            
            
            verSobre.setOnAction((event) -> {
                List<String> resu = new ArrayList<>() ;
                resu.add(textoResultado) ;
                try {
                    Resultados resultado = new Resultados(cron, resu.get(0), cron.tabPane,index) ;
                } catch (IOException | SQLException ex) {
                    Logger.getLogger(IndiceTematico.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            String consulta ;
            switch(index){
                case "245":
                    consulta = "SELECT COUNT(barcode) FROM titulos WHERE ("
                            + "titulos.titulo LIKE '"+textoResultado+"') ;" ;
                    break;
                case "043":
                    consulta = "SELECT COUNT( i.barcode ) FROM items i "
                        + "JOIN areas a ON a.sys = i.sys "
                        + "JOIN mapaareas ma ON a.area = ma.cod WHERE ("
                        + "ma.espaniol LIKE '"+textoResultado+"') ;" ;
                    break;
                default:
                    consulta = "SELECT COUNT(titulos.barcode) FROM titulos JOIN "
                            + " materias ON materias.sys=titulos.sys WHERE ("
                            + "materias.campo LIKE '"+index+"' AND materias.materia LIKE '"+
                            textoResultado+"')" ;                
                    break;
            }
            reg = cron.consultaSimple(consulta,1).get(0);

            this.textoResultado = new SimpleStringProperty(textoResultado);
            this.botonVerSobre = new SimpleObjectProperty<>(verSobre);
            this.verRegistro = new SimpleStringProperty(reg);
        }

        public SimpleStringProperty getTextoResultado() {
            return textoResultado;
        }

        public SimpleObjectProperty<Button> getBotonVerSobre() {
            return botonVerSobre;
        }

        public SimpleStringProperty getVerRegistro() {
            return verRegistro;
        }
    }    
}
