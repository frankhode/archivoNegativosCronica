/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author francisco.ortiz
 */
class VerificadorDeTitulos {
    private final Funciones cron ;

    public VerificadorDeTitulos(Funciones cron) {
        this.cron = cron ;
        String consulta = "SELECT registro FROM registros" ;
        List<String> registros = cron.consultaSimple(consulta, 1);
        registros.forEach((reg) -> {
            Registro registro = new Registro(reg) ;
            String sys = registro.getSys() ;
            List<String> c505 = registro.getCampo("505");            
            if (!c505.isEmpty()) {
                List<String> titulos = new ArrayList<>() ;
                List<Item> items = registro.getItems();
                List<String> itemDescList = new ArrayList<>() ;
                items.forEach((t) -> {
                    itemDescList.add(t.getDescripcion()) ;
                }) ;
                for (String campo : c505) {
                    String[] tit = campo.split(" -- ") ;
                    String nroA ;
                    for (String titulo : tit) {
                        titulos.add(titulo) ;
                        try {
                            nroA = titulo.split("\\.")[0] ;                            
                            if (!itemDescList.contains(nroA)) {
                                System.out.print("Ningun item con nroA -> "+nroA+" -> "+sys+" -> tit: "+titulo);
                            }
                        } catch (Exception e) {
                            nroA = "" ;
                            System.out.print("Error de nroA -> "+sys+" -> tit: "+titulo);
                        }
                    }
                }
                if (titulos.size() != registro.getItems().size()) {
                    System.out.println("Error de diferencia ->" + sys);
                    System.out.println("Titulos 505: "+titulos.size());
                    System.out.println("Items: "+registro.getItems().size());
                }
                items.forEach((item) -> {
                    if (item.getDescripcion().equals("")) {
                        System.out.println("El item "+item.getBarcode()+
                                " no tiene nroA en el campo descripcion ->"+ sys);
                    }
                });
                itemDescList.forEach((nroOriginal) -> {
                    boolean tieneTitulo = false ;
                    for (String titulo : titulos) {
                        String nroA ;
                        try {
                            nroA = titulo.split("\\.")[0] ;                            
                            if (nroOriginal.equals(nroA)) {
                                tieneTitulo = true ;
                            }
                        } catch (Exception e) {
                            //nada
                        }
                    }
                    if (!tieneTitulo) {
                        System.out.println("Falta el titulo para el sobre "+nroOriginal+" -> "+sys);
                    }
                });
            }
        });
    }
    
}
