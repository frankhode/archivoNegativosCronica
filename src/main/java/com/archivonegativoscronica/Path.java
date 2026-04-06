/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
public class Path {
    String path = null ;

    public Path() {       
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir archivo");            
            File inputFile = fileChooser.showOpenDialog(new Stage());
            if (inputFile != null) {
                path = inputFile.getAbsolutePath();
            }
    }
    
    public String getPath() {
        return path ;
    }
}
