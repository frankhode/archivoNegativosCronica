/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author francisco.ortiz
 */
class ColeccionadorOMatic {
    Funciones cron ;
    List<String> colecciones ;
    Alert alert ;
    ButtonType verButton, cerrarButton ;
    ComboBox comboColecciones ;
    public String coleccion ;

    public ColeccionadorOMatic(Funciones cron) {        
        this.cron = cron ;        
        elegirColeccion() ;
        // Mostrar la alerta y esperar a que el usuario haga clic en un botón
        alert.showAndWait().ifPresent(response -> {
            if (response == verButton) {
                verColecciones(comboColecciones.getValue()) ;
            } else if (response == cerrarButton) {
                alert.close();
            }
        });
    }
    
    public ColeccionadorOMatic(Funciones cron, boolean bool) {        
        this.cron = cron ;        
        elegirColeccion() ;
        // Mostrar la alerta y esperar a que el usuario haga clic en un botón
        alert.showAndWait().ifPresent(response -> {
            if (response == verButton) {
                coleccion = comboColecciones.getValue().toString() ;
            } else if (response == cerrarButton) {
                alert.close();
            }
        });
    }

    private void verColecciones(Object coleccion) {
        String digi = "SELECT nombramiento FROM colecciones WHERE coleccion LIKE '"+coleccion.toString()+"'" ;
            List<String> digital = cron.consultaSimple(digi, 1) ;
            List<String[]> consultaCompleta;
            String consulta ;
            if (digital == null || digital.isEmpty()) {
                // define el comportamiento sin filtros
                consulta = "SELECT carpeta, nombramiento FROM digitales WHERE 1=0"; // devuelve vacío
            } else {
                StringBuilder sb = new StringBuilder("SELECT inv,nombramiento,cajon,carpeta FROM digitales WHERE (");
                for (int i = 0; i < digital.size(); i++) {
                    if (i > 0) sb.append(" OR ");
                    // OJO: si insistís en concatenar, escapá comillas simples
                    String val = digital.get(i).replace("'", "''");
                    sb.append("nombramiento='").append(val).append("'");
                }
                sb.append(")");
                consulta = sb.toString();
            }
            consultaCompleta = cron.consultaCompleta(consulta) ;
            consultaCompleta.forEach((cons) -> {
                cons[0] = cons[0].replace("\\","\\\\") ;
            });
            
            cron.abreConjunto(consultaCompleta) ;
    }

    private void elegirColeccion() {
        alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Colecciones");
        alert.setHeaderText(null);  // Para quitar el encabezado predeterminado

        // Agregar botones personalizados
        verButton = new ButtonType("Ver");
        cerrarButton = new ButtonType("Cerrar");        

        alert.getButtonTypes().setAll(verButton, cerrarButton);        
        
        String consultaColecciones = "SELECT DISTINCT coleccion FROM colecciones" ;
        colecciones = cron.consultaSimple(consultaColecciones, 1);
        
        comboColecciones = new ComboBox() ;
        colecciones.forEach((t) -> {
            comboColecciones.getItems().add(t) ;
        });
        
        // Crear un GridPane para el diseño
        GridPane gridPane = new GridPane();        
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        
        // Agregar elementos al GridPane
        gridPane.add(new Label("Seleccione la colección:"), 0, 0);
        gridPane.add(comboColecciones, 1, 0);
        // Establecer el contenido de la alerta como el GridPane
        alert.getDialogPane().setContent(gridPane);
        
        
    }
    
}
