/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author francisco.ortiz
 */
class COMregGrupal {
    List<RegistroGrupal> regsGrupos ;
    int cantRegsGrupo,status,regActual,tipoPrograma,itemActual ;
    Button siguiente ;
    Label registroActual, labCont ;
    boolean col ;
    CheckBox cb ;
    
    COMregGrupal(COM com) throws InterruptedException {
        col = false ;
        regsGrupos = com.pendientes.getRegsGrupos();
        cantRegsGrupo = regsGrupos.size() ;
        status = com.status ;                
        regActual = 0 ;  
        setBotoneraPlayer(com) ;
        siguiente = new Button("Cargar ítem");
        itemActual = com.itemActual ;
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
        correPrograma(com) ;
    }

    private void correPrograma(COM com) throws InterruptedException {
        switch(status){
            case 0:                
                //pasosProgr2[0] Muestra la cantidad, si es mayor a cero continua, si no
                //vuelve al principio
                if (cantRegsGrupo > 0) {
                    status++ ;
                    correPrograma(com);
} else {
                    status = 0 ;
                }
                break;
            case 1:
                com.resumen.getChildren().clear();
                registroActual = new Label(regsGrupos.get(regActual).getTitulo()+" ("+
                        regsGrupos.get(regActual).getArrayRegistros().size()+"ítems)") ;                
                labCont = new Label("Registro "+(regActual+1)+" de "+cantRegsGrupo) ;
                cb = new CheckBox("Color") ;
                com.resumen.getChildren().addAll(labCont,cb,registroActual) ;
                com.botoneraPlayer.setVisible(true);
                break;
            case 2:
                com.running = true ;
                com.key.enfocaAleph();
                if (cb.isSelected()) {
                    col = true ;
                } else {
                    col = false ;
                }
                while (com.running) {                    
                    enviaRegistroGrupal(com,regsGrupos.get(regActual)) ;
                    itemActual = 0 ;
                    status = 3 ;
                }
                correPrograma(com);
                break;
            case 3:
                // items
                List<List<String>> items1 = regsGrupos.get(regActual).getItems();

                // Si no hay ítems, cerramos el registro directamente
                if (items1 == null || items1.isEmpty()) {
                    status = 4;
                    correPrograma(com);
                    break;
                }

                // texto inicial
                siguiente.setText("Cargar ítem " + (itemActual + 1) + "/" + items1.size());
                siguiente.setVisible(true);

                siguiente.setOnAction((t) -> {
                    // Guard clause: si ya terminamos, no intentar acceder a la lista
                    if (itemActual >= items1.size()) {
                        status = 4;
                        siguiente.setVisible(false);
                        try {
                            correPrograma(com);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(COMregGrupal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                    }

                    // Cargar ítem actual (robusto ante filas incompletas)
                    List<String> row = items1.get(itemActual);
                    String[] it = new String[9];
                    it[0] = row.size() > 0 ? row.get(0) : "";
                    it[1] = row.size() > 1 ? row.get(1) : "";
                    it[8] = row.size() > 2 ? row.get(2) : "";

                    com.key.cargaItem(it, itemActual == 0);
                    itemActual++;
                    com.scene.getWindow().requestFocus();
                    // Si fue el último ítem, cerrar registro directo (paso 4)
                    if (itemActual >= items1.size()) {
                        status = 4;
                        siguiente.setVisible(false);
                        try {
                            correPrograma(com);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(COMregGrupal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                    }

                    // Si quedan ítems, actualizar texto
                    siguiente.setText("Cargar ítem " + (itemActual + 1) + "/" + items1.size());
                });

                if (!com.resumen.getChildren().contains(siguiente)) {
                    com.resumen.getChildren().add(siguiente);
                }
                break;

            case 4:
                // Cierra el registro actual y avanza al siguiente
                com.key.cierraRegistro();

                // Preparar siguiente registro
                regActual++;
                itemActual = 0;
                siguiente.setText("Cargar");
                status = 0;
                correPrograma(com);
                com.scene.getWindow().requestFocus();
                break;
        }
    }
    
    private void setBotoneraPlayer(COM com) {
        //boton play
        com.botoneraPlayer.getBtnPlay().setOnAction((t) -> {
            try {
                // Play solo tiene sentido en la pantalla de "mostrar registro"
                if (status == 1) {
                    status = 2;
                    correPrograma(com);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //boton -1
        com.botoneraPlayer.getBtnMinus1().setOnAction((t) -> {
            regActual-- ;
            labCont = new Label("Registro "+regActual+" de "+cantRegsGrupo) ;
            try {
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //boton +1
        com.botoneraPlayer.getBtnPlus1().setOnAction((t) -> {
            regActual++ ;
            registroActual = new Label(regsGrupos.get(regActual).getTitulo()) ;
            labCont = new Label("Registro "+regActual+" de "+cantRegsGrupo) ;
            try {
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
            status = 1 ;  
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

    private void enviaRegistroGrupal(COM com,RegistroGrupal reg) {        
        com.key.abrePlantilla() ;
        //LDR -> pasa a la siguiente linea
        com.key.directType(KeyEvent.VK_DOWN) ;
        com.key.getRobot().delay(500);
        //007 -> pasa a la siguiente linea
        com.key.directType(KeyEvent.VK_DOWN) ;
        com.key.getRobot().delay(500);
        //008
        com.key.carga008Grupal(reg.getFechas()) ;
        com.key.irAlFinalDelRegistro();
        //040
        com.key.cargaCampo040() ;
        //043
        com.key.cargaCampo043() ;
        //245
        com.key.cargaTituloGrupal(reg) ;
        //260
        com.key.cargaCampo260Grupal(reg.getFechas()) ;
        //300
        com.key.cargaCampo300Grupal(reg.getArrayRegistros().size(),col) ;        
        //500titulo
        com.key.cargaCampo500GrupTit() ;
        //500autor
        com.key.cargaCampo500GrupAut(reg.getAutores()) ;
        //505
        com.key.cargaTitulos505(reg.getTitulos()) ;                
        //540
        com.key.cargaCampo540() ;
        //561
        com.key.cargaCampo561() ;
        //6XX
        com.key.cargaCampos6XX() ;
        //655
        com.key.cargaCampo655(col) ;
        //773
        com.key.cargaCampo773() ;
        //OWN
        com.key.cargaOWN() ;
        com.running = false ;
    }
}
