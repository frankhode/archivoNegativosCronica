/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;

/**
 *
 * @author francisco.ortiz
 */
class ListadorOMatic {
    Funciones cron ;
    RegistroCronica rc ;
    ComboBox comboCampos ;
    
    public ListadorOMatic(Funciones cron) throws IOException {
        this.cron = cron ;
        rc = new RegistroCronica() ;
        Alert al = new Alert(Alert.AlertType.CONFIRMATION) ;        
        comboCampos = new ComboBox() ;
        comboCampos.getItems().add("245") ;        
        comboCampos.getItems().add("260") ;
        comboCampos.getItems().add("490") ;
        /*comboCampos.getItems().add("611") ;
        comboCampos.getItems().add("650") ;
        comboCampos.getItems().add("651") ;
        comboCampos.getItems().add("655") ;
        comboCampos.getItems().add("6XX") ;        */
        //comboCampos.getItems().add("Barcodes-UFI") ;        
        al.getDialogPane().setContent(comboCampos);
        Optional<ButtonType> showAndWait = al.showAndWait();
        showAndWait.ifPresent((t) -> {
            if (t == ButtonType.OK) {
                procesa() ;
            }
        });
    }

    private void procesa() {
        System.out.println("SYS\tTITULO\tEDIT\tFECHA\tSERIE");
        rc.getArrayRegistros().forEach((reg) -> {
            List<String> campos = new ArrayList<>() ;
            campos.add("245") ;
            campos.add("260") ;
            campos.add("490") ;
            /*campos.add("630") ;
            campos.add("650") ;
            campos.add("651") ;
            campos.add("655") ;*/
            String campoSel = comboCampos.getValue().toString();
            reg.getItems().forEach((t) -> {
                StringBuilder sb = new StringBuilder() ;
                sb.append(reg.getSys()) ;
                sb.append("\t") ;
                sb.append(reg.getTituloFormateado()) ;
                sb.append("\t") ;
                reg.getCampo("260").forEach((w) -> {
                    sb.append(w) ;
                });
                sb.append("\t") ;
                sb.append(reg.getAño()) ;
                sb.append("\t") ;
                sb.append(t.getInventario()) ;
                sb.append("\t") ;
                sb.append(t.getUfi()) ;
                sb.append("\t") ;
                sb.append(t.getDescripcion()) ;
                System.out.println(sb.toString());
            });
            switch (comboCampos.getValue().toString()) {
                /*case "6XX":
                    campos.forEach((campo) -> {
                        List<String> materia = reg.getCampo(campo) ;
                        materia.forEach((mat) -> {
                            System.out.println(reg.getSys()+"\t"+mat+"\t"+campo);
                        }) ;
                    });                        
                    break;
                case "Barcodes-UFI":
                    reg.getItems().forEach((item) -> {
                        System.out.println(reg.getSys()+"\t"+item.getBarcode()+"\t"+item.getUfi());
                    });
                    break;
                case "245":
                    reg.getItems().forEach((item) -> {
                        System.out.println(reg.getSys()+"\t"+item.getBarcode()+"\t"+reg.getTituloFormateado());
                    });
                    break;
                default:
                    List<String> materia = reg.getCampo(campoSel) ;
                    materia.forEach((mat) -> {
                        System.out.println(reg.getSys()+"\t"+mat+"\t"+campoSel);
                    }) ;
                    break ;
*/
            }
        });
    }
    
}
