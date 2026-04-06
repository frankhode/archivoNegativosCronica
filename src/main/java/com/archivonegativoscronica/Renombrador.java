/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public final class Renombrador {
    
    public Renombrador() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());        
        renameFiles(selectedDirectory);
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION, "Fin renombre", ButtonType.CLOSE) ;
        mensaje.showAndWait() ;
    }
    
    public void renameFiles(File folder) {
        int count = 0;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                renameFiles(file);
            } else {
                String parentFolderName = folder.getName();
                String extension = getFileExtension(file);
                String newName = "BNA_" + parentFolderName + "_" + String.format("%03d", count) + "." + extension;
                File newFile = new File(file.getParentFile().getAbsolutePath() + "\\" + newName);
                file.renameTo(newFile);
                count++;
            }
        }        
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOfDot = name.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return "";
        }
        return name.substring(lastIndexOfDot + 1);
    }
}
