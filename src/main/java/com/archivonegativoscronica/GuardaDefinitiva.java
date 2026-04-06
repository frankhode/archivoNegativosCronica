/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.File;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
public class GuardaDefinitiva {
    private final Funciones cron ;
    private ReubicadorDigitalesOMatic reubicador ;
    private final String carpetaAltas ;
    private final String carpetaBajas ;

    public GuardaDefinitiva(Funciones cron) {
        this.cron = cron ;
        reubicador = new ReubicadorDigitalesOMatic(cron) ;
        carpetaAltas = "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Altas" ;
        carpetaBajas = "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Bajas" ;
        DirectoryChooser dir = new DirectoryChooser() ;
        dir.setTitle("Elegir carpeta para subir");
        File folder = dir.showDialog(new Stage()) ;
        if (folder != null) {
            folder.getAbsolutePath();
        }
        
        Alert al = new Alert(Alert.AlertType.NONE) ;
        al.setContentText("Subir altas o bajas?");
        al.getButtonTypes().add(new ButtonType("Altas")) ;
        al.getButtonTypes().add(new ButtonType("Bajas")) ;
        Optional<ButtonType> tipo = al.showAndWait();
        if (tipo.isPresent()) {
            if (tipo.get().getText().equals("Altas")) {
                mueveArchivos(folder,carpetaAltas) ;                
            } else {
                mueveArchivos(folder,carpetaBajas) ;
            }
        }
    }

    private void mueveArchivos(File folder, String carpeta) {
        for (File barcode : folder.listFiles()) {
            if (reubicador.correctUfis.containsKey(barcode.getName())) {
                String ufiAleph = carpeta+"\\"+reubicador.correctUfis.get(barcode.getName())
                    +"\\"+barcode.getName();
                
                if (verificaDuplicados(ufiAleph)) {
                    reubicador.moverCarpetaBarcode(barcode.getName(), barcode.getPath(), ufiAleph);
                }
            }else {
                System.out.println("Barcode no encontrado");
            }
        }
    }    

    private boolean verificaDuplicados(String ufiAleph) {
        boolean bool = false ;
        File file = new File(ufiAleph) ;
        File[] listFiles = file.getParentFile().listFiles();
        try {
            for (File listFile : listFiles) {
                if (listFile.getPath().equals(ufiAleph)) {
                    System.out.println("-----");
                    System.out.println("Ya existe la carpeta "+ufiAleph);
                    System.out.println("Verificar duplicado");
                    System.out.println("-----");
                    bool = false ;
                }
            }
        } catch (Exception e) {
            //Todo bien, no existe la carpeta
            bool = true ;
        }
        return bool ;
    }
}
