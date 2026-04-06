/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
class ExceleadorOMatic {
    private final Funciones cron ;
    private final File file ;

    ExceleadorOMatic(Funciones c) {
        cron = c ;
        Stage primaryStage = new Stage() ;
        // Crear el panel principal
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        
        //programas
        //SYS|BARCODE|UFI|1XX|245|260|264|300|490|5XX|6XX|7XX
        //SYS|BARCODE|UFI|1XX|245|260|264|300|490|5XX|7XX
        //SYS|BARCODE|UFI|1XX|245|26X|300|6XX
        
        //filtros
            //ufi like...
            //tipo de material...
            //subbiblio like
        
        
        // Configurar la escena y mostrarla
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Exceleador-o-Matic");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        Path path = new Path() ;
        String archivo = path.getPath() ;
        file = new File(archivo) ;
    }
    
}
