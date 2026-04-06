/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author francisco.ortiz
 */
class COMregIndi {
    
    int status,cantRegsInd, regActual,tipoPrograma  ;
    List<String[]> regsIndiv ;
    Label registroActual, labCont ;
    Button siguiente ;
    boolean col ;
    BorderPane wiki ;
    CheckBox cb ;
    
    public COMregIndi(COM com) throws InterruptedException {
        col = false ;        
        regsIndiv = com.pendientes.getRegsIndiv();
        regsIndiv.sort(Comparator.comparing(arr -> arr[5]));
        cantRegsInd = regsIndiv.size() ;
        //com.nroProgr = 2 ;
        status = com.status ;
        correPrograma(com) ;        
        regActual = 0 ;  
        setBotoneraPlayer(com) ;
        siguiente = new Button("Cargar ítem");        
        
        com.scene.setOnKeyPressed(event -> {
            KeyCombination rg = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
            KeyCombination it = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
            if (it.match(event)) {
                siguiente.fire();
            }
            if (rg.match(event)) {
                com.botoneraPlayer.getBtnPlay().fire();
            }
            if (event.getCode() == KeyCode.PAGE_DOWN) {
                com.botoneraPlayer.getBtnPlus1().fire();
            }
            if (event.getCode() == KeyCode.PAGE_UP) {
                com.botoneraPlayer.getBtnMinus1().fire();
            }        
        });
    }
    
    private void correPrograma(COM com) throws InterruptedException {
        switch(status){
            case 0:                
                //pasosProgr2[0] Muestra la cantidad, si es mayor a cero continua, si no
                //vuelve al principio
                if (cantRegsInd > 0) {
                    com.botoneraPlayer.setVisible(true);                    
                    status++ ;
                    com.regActual = 0 ;
                    correPrograma(com);
                } else {
                    status = 0 ;
                }
                break;
            case 1:
                com.resumen.getChildren().clear();
                registroActual = new Label(regsIndiv.get(regActual)[5]) ;
                labCont = new Label("Registro "+(regActual+1)+" de "+cantRegsInd) ;
                cb = new CheckBox("Color") ;
                wiki = new WikiPane(regsIndiv.get(regActual)[5]) ;
                com.resumen.getChildren().addAll(labCont,cb,registroActual, wiki) ;
                break;
            case 2:
                //pasosProgr2[2] = "Carga bibliográfico" 
                com.key.enfocaAleph();
                com.running = true ;
                col = cb.isSelected();
                while(com.running){
                    enviaRegistroInd(com,regsIndiv.get(regActual));    
                }
                
                if (!com.resumen.getChildren().contains(siguiente)) {
                    com.resumen.getChildren().add(siguiente) ;
                }
                
                siguiente.setOnAction((t) -> {
                    try {
                        status = 3 ;
                        com.key.enfocaAleph();
                        correPrograma(com);                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                });
                break;
            case 3:
                // Cargar ítem
                com.key.cargaItem(regsIndiv.get(regActual),true) ;   
                com.key.cierraRegistro();
                com.scene.getWindow().requestFocus();
                break;
        }
    }
    
    private void enviaRegistroInd(COM com, String[] reg) {
        String autor = reg[4] ;
        String titulo = reg[5] ;
        String fechaISO = reg[6] ;
        
        com.key.abrePlantilla() ;
        //LDR -> pasa a la siguiente linea
        com.key.directType(KeyEvent.VK_DOWN) ;
        com.key.getRobot().delay(500);
        if (col) {
            com.key.actualiza007("c");
            com.key.directType(KeyEvent.VK_DOWN) ;
            com.key.getRobot().delay(500);
        } else {
            //007 -> pasa a la siguiente linea
            com.key.directType(KeyEvent.VK_DOWN) ;
            com.key.getRobot().delay(500);
        }
        //008
        if (fechaISO.isEmpty()) {            
            //com.key.carga008("197u") ;
            com.key.carga008("198u") ;
        } else {
            com.key.carga008(fechaISO) ;
        }        
        com.key.irAlFinalDelRegistro();
        //040
        com.key.cargaCampo040() ;
        //043
        switch(tipoPrograma) {
            case 1: com.key.cargaCampo043() ; break;
            case 2: /*omite*/ break; }
        //245
        com.key.cargaTitulo(titulo,fechaISO) ;
        //260
        com.key.cargaCampo260(fechaISO) ;
        //300
        com.key.cargaCampo300Ind(col) ;
        //500titulo
        com.key.cargaCampo500IndTit() ;
        //500autor
        com.key.cargaCampo500IndAut(autor) ;
        //540
        com.key.cargaCampo540() ;
        //561
        com.key.cargaCampo561() ;        
        switch(tipoPrograma) {
            case 1: //campos para indizacion
                com.key.cargaCampos6XX() ; break;                
            case 2: /*omite*/ break; }        
        //655
        com.key.cargaCampo655(col) ;        
        //773
        com.key.cargaCampo773() ;        
        //OWN
        com.key.cargaOWN() ;        
        com.running = false ;
    }

    private void setBotoneraPlayer(COM com) {        
        //boton play
        com.botoneraPlayer.getBtnPlay().setOnAction((t) -> {
            tipoPrograma = 1 ;                    
            status = 2 ;
            try {
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
        });                
        //boton -1
        com.botoneraPlayer.getBtnMinus1().setOnAction((t) -> {
            regActual-- ;
            labCont = new Label("Registro "+regActual+" de "+cantRegsInd) ;
            try {
                status = 1 ;
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //boton +1
        com.botoneraPlayer.getBtnPlus1().setOnAction((t) -> {
            regActual++ ;
            labCont = new Label("Registro "+regActual+" de "+cantRegsInd) ;
            try {
                status = 1 ;
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //boton rec
        com.botoneraPlayer.getBtn1().setOnAction((t) -> {
            try {
                com.key.enfocaAleph();
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
            com.key.guardarRegistro();
        });
        
        //boton rec
        com.botoneraPlayer.getBtnClose().setOnAction((t) -> {
            com.ventana.close();
            com.pendientes = null ;
        });
    }
}
