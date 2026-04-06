/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author francisco.ortiz
 */
public class Item {
    
    public HashMap itm ;
    public String sys,dato ;

    public Item(String z30,String sys) {
        this.sys = sys ;
        this.dato = z30 ;
        String separador = "$$" ;
        String [] divideItem = z30.substring(2).split(Pattern.quote(separador)) ;
        itm = new HashMap() ;
        for (String divideItem1 : divideItem) {
            String indice = divideItem1.substring(0, 1);
            String valor = divideItem1.substring(1);
            itm.put(indice, valor) ;
        }
    }
    /*
        l   BNA01                       BASE
        L   BNA01                       BASE
        2   LIBRO                       COD COLECCION
        B   Colección Libros            DESC COLECCION
        3   FOES031335                  UBICACION FISICA
        4   Brodsky, 20050713           NOTA INTERNA
        7   B                           NOTA OPAC
        8   20110912                    FECHA DE INVENTARIO
        f   15                          COD ESTATUS DEL ITEM
        F   Préstamo común en sala      DESC ESTATUS DEL ITEM
        */
    public String imprimeItem() {
        StringBuilder item = new StringBuilder() ;
        this.itm.forEach((k,v)->item.append(k).append("->").append(v));
        return item.toString() ;
    }
         
    public String getInventario() {
        String inv ;
        try {
            inv = this.itm.get("6").toString() ;
        } catch (Exception e) {
            inv = "" ;
        }
        return inv ;
    }
    
    public String getBarcode() {
        String barcode ;
        try {
            barcode = this.itm.get("5").toString() ;
        } catch (Exception e) {
            barcode = "" ;
        }
        return barcode ;
    }
    
    public String getTipoDeMaterial() {
        String tipo ;
        try {
            tipo = this.itm.get("m").toString() ;
        } catch (Exception e) {
            tipo = "" ;
        }
        return tipo ;
    }
    public String getCodSubBiblio() {
        String barcode ;
        try {
            barcode = this.itm.get("1").toString() ;
        } catch (Exception e) {
            barcode = "" ;
        }        
        return barcode ;
    }
    public String getDescSubBiblio() {
        String sub ;
        try {
            sub = this.itm.get("A").toString() ;
        } catch (Exception e) {
            sub = "" ;
        }        
        return sub ;
    }
    public String getUfi() {
        String ufi ;
        try {
             ufi = this.itm.get("3").toString() ;
        } catch (Exception e) {
            ufi = "" ;
        }        
        return ufi ;
    }
    
    public String getDescripcion() {
        String desc ;
        try {
             desc = this.itm.get("h").toString() ;
        } catch (Exception e) {
            desc = "" ;
        }        
        return desc ;
    }
    
}
