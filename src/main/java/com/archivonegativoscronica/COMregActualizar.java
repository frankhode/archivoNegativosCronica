/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;

/**
 *
 * @author francisco.ortiz
 */
class COMregActualizar {
    int status,regActual,itemActual,cantRegs ;
    Button siguiente ;
    RegistrosParaAgregar regsParaActualizar ;
    Label registroActual,labCont ;
    Funciones cron ;
    boolean col ;
    CheckBox cb ;
    
    COMregActualizar(COM com, Funciones cron) throws InterruptedException {
        col = false ;
        this.cron = cron ;
        regsParaActualizar = com.regsParaActualizar ;
        cantRegs = regsParaActualizar.getRegs().size();
        status = com.status ;
        siguiente = new Button("Cargar ítems");
        regActual = 0 ;  
        setBotoneraPlayer(com) ;        
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
                if (regsParaActualizar.getRegs().size() > 0) {
                    muestraRegistro(com);                    
                    com.botoneraPlayer.setVisible(true);
                } else {
                    status = 0 ;
                }
                break;
            case 1:
                com.key.enfocaAleph();                
                com.running = true ;
                if (cb.isSelected()) {
                    col = true ;
                } else {
                    col = false ;
                }
                while (com.running) {
                    agregaAlRegistro(com,regsParaActualizar.getIndex(regActual)) ;
                    status = 2 ;
                    try {
                        correPrograma(com);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(COMregActualizar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                break;                
            case 2 :
                itemActual = 0 ;
                List<String[]> items = regsParaActualizar.returnRegInv(
                        regsParaActualizar.getIndex(regActual));
                siguiente.setText("Cargar ítems "+itemActual+"/"+items.size());
                siguiente.setVisible(true);
                siguiente.setOnAction((t) -> {
                    try {
                        com.key.enfocaAleph();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    com.key.cargaItem(items.get(itemActual), itemActual == 0);
                    itemActual++;                    
                    // Guard clause: si ya terminamos, no intentar acceder a la lista
                    if (itemActual >= items.size()) {                        
                        itemActual = 0;
                        regActual++;
                        status = 0;
                        muestraRegistro(com);
                        com.key.cierraRegistro();
                        try {
                            correPrograma(com);
                            com.scene.getWindow().requestFocus();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return;
                    }
                    siguiente.setText("Cargar ítem " + (itemActual + 1) + "/" + items.size());
                    com.scene.getWindow().requestFocus();
                });
            break;
        }
    }
    
    private void setBotoneraPlayer(COM com) {
        //boton play
        com.botoneraPlayer.getBtnPlay().setOnAction((t) -> {
            try {
                status = 1 ;
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });                
        //boton -1
        com.botoneraPlayer.getBtnMinus1().setOnAction((t) -> {
            regActual-- ;
            com.resumen.getChildren().clear();
            try {
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        //boton +1
        com.botoneraPlayer.getBtnPlus1().setOnAction((t) -> {
            regActual++ ;
            com.resumen.getChildren().clear();
            muestraRegistro(com);
            try {
                correPrograma(com);
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregIndi.class.getName()).log(Level.SEVERE, null, ex);
            }            
        });
        
        //boton rec
        com.botoneraPlayer.getBtn1().setOnAction((t) -> {
            status = 2 ;
            try {
                com.key.enfocaAleph();
            } catch (InterruptedException ex) {
                Logger.getLogger(COMregActualizar.class.getName()).log(Level.SEVERE, null, ex);
            }
            com.key.guardarRegistro();
        });
        
        //boton rec
        com.botoneraPlayer.getBtnClose().setOnAction((t) -> {
            com.ventana.close();
            com.pendientes = null ;
        });
    }
    
    private void muestraRegistro(COM com) {
        String titulo = regsParaActualizar.getTitulo(
                regsParaActualizar.getIndex(regActual)) ;
        labCont = new Label("Registro "+(regActual+1)+" de "+cantRegs) ;
        String dataRegs = "Unificación para " + titulo + 
                " (" + regsParaActualizar.getReg(regActual).size() + " items)"  ;
        ScrollPane sp = muestraDataRegistro(regsParaActualizar.getReg(regActual),regsParaActualizar.getIndex(regActual)) ;
        cb = new CheckBox("Color") ;
        registroActual = new Label(dataRegs) ;
        com.resumen.getChildren().clear();
        com.resumen.getChildren().addAll(labCont,cb,registroActual,sp,siguiente) ;
        com.botoneraPlayer.setVisible(true);
    }
    
    private void agregaAlRegistro(COM com, String sys) throws InterruptedException {        
        RegistroParaActualizar rpa = new RegistroParaActualizar(com.cron,sys,regsParaActualizar) ;
        agregaAlRegistro(com,rpa);
    }    
    
    private void agregaAlRegistro(COM com, RegistroParaActualizar rpa) throws InterruptedException {
        //actualizaciones para registros de color mixto
        boolean actualiza = false ;
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '"+rpa.sys+"'" ;
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);
        Registro registro = new Registro(consultaSimple.get(0)) ;
        String c300 = registro.getCampo("300").get(0) ;
        if (col) {            
            if (!c300.contains("diapositiva")) {                
                actualiza = true ;
            }
            if (c300.contains("diapositiva") && c300.contains("negativos")) {
                //es mixto actualiza
                actualiza = true ;
            }
        } else {
            if (c300.contains("diapositiva")) {
                actualiza = true ;
            }
        }
        //enfocaAleph();
        com.key.abreBibliografico(rpa.sys) ;
        com.key.getRobot().delay(1000);
        com.key.borraCampos() ;
        com.key.getRobot().delay(2000);
        com.key.expandePlantilla() ;
        com.key.getRobot().delay(800);
        com.key.irAlFinalDelRegistro();
        com.key.getRobot().delay(800);
        //007
        if (actualiza) {
            com.key.actualiza007("m");
        }
        com.key.irAlFinalDelRegistro();
        //008
        com.key.carga008Grupal(rpa.getFechas());
        com.key.getRobot().delay(500);
        com.key.irAlFinalDelRegistro();
        //040
        com.key.cargaCampo040() ;
        //043
        com.key.agregaCampo043(rpa.getCampo043()) ;
        //245
        com.key.cargaTitulo245(rpa.getTitulo245()) ;
        //260
        com.key.cargaCampo260Grupal(rpa.getFechas());
        //300
        if (actualiza) {
            com.key.actualizaCampo300(rpa.getCantItems(),"mixto");
        } else {
            com.key.cargaCampo300Grupal(rpa.getCantItems(),col);
        }
        
        //500titulo
        com.key.agregaCampo500Tit(rpa.getTitulo245()) ;
        //500autor
        com.key.cargaCampo500GrupAut(rpa.getFotografos());
        //505
        com.key.cargaTitulos505(rpa.getTitulos());
        //540
        com.key.cargaCampo540() ;
        //561
        com.key.cargaCampo561() ;
        //6XX
        com.key.agregaCampo6XX("600",rpa.getCampo600()) ;
        com.key.agregaCampo6XX("610",rpa.getCampo610()) ;        
        com.key.agregaCampo6XX("611",rpa.getCampo611()) ;        
        com.key.agregaCampo6XX("630",rpa.getCampo630()) ;
        com.key.agregaCampo6XX("650",rpa.getCampo650()) ;
        com.key.agregaCampo6XX("651",rpa.getCampo651()) ;
        com.key.agregaCampo655(rpa);        
        //773
        com.key.cargaCampo773() ;
        //OWN
        com.key.cargaOWN() ;
        com.running = false ;
    }

    private ScrollPane muestraDataRegistro(List<String[]> reg, String sys) {
        ScrollPane sp = new ScrollPane() ;
        StringBuilder sb = new StringBuilder() ;
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '"+sys+"'" ;
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);
        try {
            Registro registro = new Registro(consultaSimple.get(0)) ;
        sb.append("SYS: ") ;
        sb.append(sys) ;
        sb.append("\n") ;
        sb.append("Título original: ") ;
        sb.append(registro.getTituloFormateado()) ;
        sb.append("\n") ;
        sb.append("Fecha original: ") ;
        sb.append(registro.getFecha()) ;
        sb.append("\n") ;
        sb.append("300 original: ") ;
        sb.append(registro.getCampo("300")) ;
        sb.append("\n") ;
        sb.append("6XX cant.: ") ;
        sb.append(registro.getMaterias().size()) ;
        sb.append("\n") ;
        sb.append("\n") ;
        sb.append("Registros para agregar: ") ;
        sb.append("\n") ;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(consulta);
        }
        
        reg.forEach((t) -> {
            sb.append(t[0]) ;
            sb.append(". ") ;
            sb.append(t[5]) ;
            sb.append("\n") ;
        });
        
        sp.setContent(new Text(sb.toString()));
        return sp ;
    }
    
}
