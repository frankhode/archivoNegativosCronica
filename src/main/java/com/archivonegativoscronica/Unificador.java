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
class Unificador {
    
    HashMap<String,List<String>> titulos ;
    String notaUnificacion ;
    boolean registroParaUnificar ;

    public Unificador(Funciones cron) {
        titulos = new HashMap<>() ;
        
        //obtiene titulos
        String consulta = "SELECT registro FROM registros" ;
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);
        consultaSimple.forEach((t) -> {
            Registro reg = new Registro(t) ;
            String titulo = reg.getTituloFormateado().replace(" [material gráfico].", "") ;
            titulo = titulo.replace(" [material gráfico]", "") ;
            titulo = titulo.replace("[sic]", "") ;
            titulo = titulo.replaceAll("\\[i\\.e\\. .*\\]", "") ;
            titulo = titulo.replaceAll("\\[i\\. e\\. .*\\]", "") ;
            titulo = titulo.replace("[", "") ;
            titulo = titulo.replace("]", "") ;
            String[] split = titulo.split("\\.");
            try {
                titulos.get(split[0]).add(reg.getSys());
            } catch (Exception e) {
                List<String> lista = new ArrayList<>() ;
                lista.add(reg.getSys()) ;
                titulos.put(split[0], lista) ;
            }
        });
        //imprime resultados
        titulos.forEach((k, v) -> {            
            if (v.size() > 1) {
                System.out.print(k+" (");
                v.forEach((t) -> {
                    System.out.print(t+" ");
                });
                System.out.println(")");
            }
        });
    }
    
    public String verificaTitulo(String titulo) {
        notaUnificacion = "" ;        
        titulos.forEach((k, v) -> {
            if (k.equals(titulo)) {
                notaUnificacion = notaUnificacion + " 500   L $$aPosible unificación: " ;
                notaUnificacion = notaUnificacion + "(" ;
                v.forEach((t) -> {
                    notaUnificacion = notaUnificacion + t ;                    
                });
                notaUnificacion = notaUnificacion + ")." ;
            } else {
            }
        });
        return notaUnificacion ;
    }
    
    public String getSysUnificador(String titulo) {
        notaUnificacion = "" ;        
        titulos.forEach((k, v) -> {
            if (k.equals(titulo)) {
                v.forEach((t) -> {
                    notaUnificacion =  t ;                    
                });
            } else {
            }
        });
        return notaUnificacion ;
    }
    
    public boolean posibleUnificacion(String titulo) {
        registroParaUnificar = false ;
        titulos.forEach((k, v) -> {            
            if (k.equals(titulo)) {
                registroParaUnificar = true ;
            }
        });
        return registroParaUnificar ;
    }

    String primeraParteDelTitulo(String titulo) {
        String primeraParteDelTitulo = titulo.replace(" [material gráfico].", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replace(" [material gráfico]", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replace("[sic]", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replaceAll("\\[i\\.e\\. .*\\]", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replaceAll("\\[i\\. e\\. .*\\]", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replace("[", "") ;
        primeraParteDelTitulo = primeraParteDelTitulo.replace("]", "") ;
        String[] split = primeraParteDelTitulo.split("\\.");        
        
        return split[0] ;
    }
}
