/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Maru
 */
public final class PanelRegistroVistas extends Object {
    private final Registro registro ;
    private VBox salida ;
    private final String sys ;
    private final BorderPane cont ;
    
    public PanelRegistroVistas(Registro reg) {
        registro = reg ;
        cont = new BorderPane() ;
        sys = registro.getSys() ;
        
        cont.setMinHeight(400);
        
        //cabecera con botones para las distintas vistas
        HBox botonera = new HBox() ;
        botonera.setStyle("-fx-padding: 10,10,10,10;");
        Button btnOpac = new Button("Opac Web") ;
        btnOpac.setOnAction((event) -> {
            Platform.runLater(() -> {
                setVista("opac") ;
            });
        });
        Button btnSec = new Button("ALEPH secuencial") ;
        btnSec.setOnAction((event) -> {
            Platform.runLater(() -> {
                setVista("secuencial") ;
            });
        });
        Region filler = new Region(); HBox.setHgrow(filler, Priority.ALWAYS);
        botonera.getChildren().addAll(btnOpac,filler,btnSec) ;        
        
        cont.setTop(botonera);
        setVista("inicio") ;
    }
    
    private VBox setVista(String vista) {        
        switch(vista){
            case "secuencial":
                salida = registro.getFormatoSecuencial() ;
                cont.setCenter(salida);
                break;
            case "opac":
                salida = registro.getFormatoOpac() ;
                cont.setCenter(salida);
                break ;
            default:
                salida = registro.getFormatoOpac() ;
                cont.setCenter(salida);
                break;
        }
        return salida ;
    }
    
    public String getSys() {
        return sys ;
    }
    
    public BorderPane getCont() {
        return cont ;
    }
    
    public Registro getRegistro(){
        return registro ;
    }
}
