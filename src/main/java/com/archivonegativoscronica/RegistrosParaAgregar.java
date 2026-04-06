/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author francisco.ortiz
 */
class RegistrosParaAgregar {

    private final HashMap<String,List<String[]>> regs ;
    private final List<String> index ;
    private final HashMap<String,List<String>> titulos ;
    Funciones cron ;

    public RegistrosParaAgregar(Funciones cron) {
        this.cron = cron ;
        regs = new HashMap<>() ;
        index = new ArrayList<>() ;
        titulos = new HashMap<>() ;
        agregaRegistroPendientesdePreCatalogacion() ;
    }
    
    public void addReg(String sys,String[] regInventario){
        if (getRegs().containsKey(sys)) {
            // Si la clave ya existe en el HashMap, 
            //se agrega el nuevo valor a la lista correspondiente
            List<String[]> listaValores = getRegs().get(sys);
            listaValores.add(regInventario);
        } else {
            // Si la clave no existe en el HashMap, 
            //se crea una nueva lista con el nuevo valor y se agrega al HashMap
            List<String[]> listaValores = new ArrayList<>();
            listaValores.add(regInventario);
            getRegs().put(sys, listaValores);
        }
        if (!index.contains(sys)) {
            index.add(sys) ;
        }
    }
    
    public List<String[]> returnRegInv(String sys){
        return getRegs().get(sys) ;
    }
    
    public HashMap<String,List<String[]>> getRegs() {
        return regs;
    }
    
    public List<String[]> getReg(int ind) {
        return regs.get(index.get(ind));
    }
    
    public String getIndex(int ind) {
        return index.get(ind) ;
    }
    
    public void addTitulo(String sys,String titulo) {
        if (titulos.containsKey(sys)) {
            // Si la clave ya existe en el HashMap, 
            //se agrega el nuevo valor a la lista correspondiente
            List<String> get = titulos.get(sys);
            get.add(titulo);
        } else {
            // Si la clave no existe en el HashMap, 
            //se crea una nueva lista con el nuevo valor y se agrega al HashMap
            List<String> listaValores = new ArrayList<>();
            listaValores.add(titulo);
            titulos.put(sys, listaValores);
        }        
    }
    
    public String getTitulo(String sys) {
        try {
            return titulos.get(sys).get(0).split("\\. ")[1] ;
        } catch (Exception e) {
            return titulos.get(sys).get(0) ;
        }
    }
    
    public List<String> getTitulos(String sys){
        return titulos.get(sys) ;
    }

    private void agregaRegistroPendientesdePreCatalogacion() {
        String consultaConjuntos = "SELECT DISTINCT titulo FROM conjuntos WHERE status LIKE '2'"
                + " AND barcode NOT IN (SELECT barcode FROM items)" ;
        List<String> syss = cron.consultaSimple(consultaConjuntos,1);        
        syss.forEach((sys) -> {
            String consultaBar = "SELECT barcode FROM conjuntos WHERE titulo LIKE '"+sys+"' "
                    + "AND barcode NOT IN (SELECT barcode FROM items)" ;
            List<String> barcodes = cron.consultaSimple(consultaBar,1);
            barcodes.stream().map((barcode) ->                     
                    "SELECT * FROM inventario WHERE barcode LIKE '"+barcode+"' "
                            + "AND barcode NOT IN (SELECT barcode FROM items)")
                    .map((inv) -> cron.consultaCompleta(inv)).forEachOrdered((invs) -> {
                invs.forEach((t) -> {
                    this.addReg(sys, t);
                    this.addTitulo(sys, tituloCompleto(t));
                }) ;
            });
        });
    }
    
    private String tituloCompleto(String[] reg) {
        String titulo ;
        if (reg[1].equals("")) {
            titulo = "["+reg[0]+"]. "+reg[5] ;            
        } else {
            titulo = reg[1]+". "+reg[5] ;
        }
        if (!reg[6].equals("")) {
            titulo = titulo + ", " + RegistrosParaAleph.fechaFormateada(reg[6]) ;
        }
        return titulo ;
    }
}
