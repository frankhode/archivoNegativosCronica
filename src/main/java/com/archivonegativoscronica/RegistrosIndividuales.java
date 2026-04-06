/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author francisco.ortiz
 */
public class RegistrosIndividuales {
    private StringBuilder sb ;
    private int count ;
    private final List<String[]> arrayRegistros ;
    Unificador unificador ;
    
    public RegistrosIndividuales(Unificador uni) {
        unificador = uni ;
        sb = new StringBuilder() ;
        count =  0 ;
        arrayRegistros = new ArrayList<>() ;
    }
    
    private void creaRegistrosInd(String[] lineaReg) {
        
        String sys = String.format("%09d", count) ;
        
        //*********  campos  *********//
        
        //FMT
        sb.append(sys) ;
        sb.append(" FMT   L VM") ;
        sb.append("\n") ;
        
        //LDR
        sb.append(sys) ;
        sb.append(" LDR   L ^^^^^akd^a22^^^^^^a^4500") ;
        sb.append("\n") ;
        
        //007
        sb.append(sys) ;
        sb.append(" 007   L kg^be^") ;
        sb.append("\n") ;
        
        //008
        sb.append(sys) ;
        sb.append(" 008   L ^^^^^^") ;
        String fecha ;
        try {
            fecha = "s"+lineaReg[6].substring(0,4)+"^^^^" ;
        } catch (Exception e) {
            fecha = "s197u^^^^" ;            
        }
        sb.append(fecha) ;
        sb.append("ag^nnn^^^^^^^^^^^^knspa^^") ;
        sb.append("\n") ;
        
        //040
        sb.append(sys) ;
        sb.append(" 040   L $$aAR-BaBN$$bspa$$cAR-BaBN$$eaacr") ;
        sb.append("\n") ;
        
        //043
        sb.append(sys) ;
        sb.append(" 043   L $$a") ;
        sb.append("\n") ;
        
        //245
        sb.append(sys) ;
        sb.append(" 24500 L $$a") ;
        
        try {
            String titulo = lineaReg[5] ;
            sb.append(titulo) ;
            String fechaFormateada = RegistrosParaAleph.fechaFormateada(lineaReg[6]);
            if (!"".equals(fechaFormateada)) {
                sb.append(", ") ;
                sb.append(fechaFormateada) ;
            }
        } 
        catch (Exception e) {sb.append("ERROR DE TITULO (verificar archivo)") ;}
        sb.append("$$h[material gráfico].") ;
        sb.append("\n") ;
        
        //260 
        sb.append(sys) ;
        sb.append(" 260   L $$c ") ;
        try {sb.append(lineaReg[6].substring(0,4)) ;} 
        catch (Exception e) {sb.append("[197-?]") ;}            
        sb.append("\n") ;
        
        //300
        sb.append(sys) ;
        sb.append(" 300   L $$a 1 sobre (negativos flexibles) :$$bbyn") ;
        sb.append("\n") ;
        
        //500 titulo
        sb.append(sys) ;
        sb.append(" 500   L $$aTítulo tomado del sobre.") ;
        sb.append("\n") ;        
        
        //500 autor
        try {
            if (!lineaReg[4].equals("")) {            
                sb.append(sys) ;
                sb.append(" 500   L $$aFotógrafo: ") ;
                sb.append(lineaReg[4]) ;
                sb.append("\n") ;
            }
        } catch (Exception e) {
            //null pointer
            System.out.println(" ->"+lineaReg[4]+"<-");
            System.out.println(Arrays.toString(lineaReg));
        }
        
        //500 unifica
        try {
            if (unificador.posibleUnificacion(unificador.primeraParteDelTitulo(lineaReg[5]))) {            
                sb.append(sys) ;
                sb.append(unificador.verificaTitulo(unificador.primeraParteDelTitulo(lineaReg[5]))) ;
                sb.append("\n") ;
            }
        } catch (Exception e) {
            //null pointer
            System.out.println(" ->"+lineaReg[5]+"<-");
            System.out.println(Arrays.toString(lineaReg));
        }
        
        
        //540
        sb.append(sys) ;
        sb.append(" 540   L "
                + "$$aPuede presentar restricciones. "
                + "Consultar en el Departamento de "
                + "Materiales Cartográficos y Fotográficos.") ;
        sb.append("\n") ;
        
        //561
        sb.append(sys) ;
        sb.append(" 5611  L $$aForma parte del archivo fotográfico del diario Crónica.") ;
        sb.append("\n") ;
        
        //600
        sb.append(sys) ;
        sb.append(" 60014 L $$a $$d $$x $$v") ;
        sb.append("\n") ;
        
        //610
        sb.append(sys) ;
        sb.append(" 61024 L $$a $$v") ;
        sb.append("\n") ;
        
        //611
        sb.append(sys) ;
        sb.append(" 61124 L $$a $$n $$d $$c $$v") ;
        sb.append("\n") ;
        
        //630
        sb.append(sys) ;
        sb.append(" 63004 L $$a $$v") ;
        sb.append("\n") ;
        
        //650
        sb.append(sys) ;
        sb.append(" 650 4 L $$a $$x $$y $$v") ;
        sb.append("\n") ;
        
        //651
        sb.append(sys) ;
        sb.append(" 651 4 L $$a $$x $$y $$v") ;
        sb.append("\n") ;
        
        //655
        sb.append(sys) ;
        sb.append(" 655 4 L $$a Negativos flexibles") ;
        sb.append("\n") ;
        
        //773
        sb.append(sys) ;
        sb.append(" 77318 L $$tSección Archivo fotográfico $$w(AR-BaBN)001412736") ;
        sb.append("\n") ;
        
        //OWN
        sb.append(sys) ;
        sb.append(" OWN   L $$aCAT_FOTO") ;
        sb.append("\n") ;
        

        //NOTA PARA CARGAR EL ITEM
        sb.append(sys) ;
        sb.append(" 500   L $$a") ;
        //inventario
        try {sb.append(lineaReg[0]) ;} 
        catch (Exception e) {sb.append("ERROR DE CODIGO DE BARRAS") ;}
        sb.append(" ") ;
        //numero original
        try {
            if (!lineaReg[1].equals("")) {
                sb.append(lineaReg[1]) ;
            } else {
                sb.append("[").append(lineaReg[0]).append("]") ;
            }
        } 
        catch (Exception e) {sb.append("ERROR DE NUMERO ORIGINAL") ;}
        sb.append(" ") ;
        //ubicacion fisica
        try {sb.append(lineaReg[8]) ;} 
        catch (Exception e) {sb.append("ERROR DE UBICACION FISICA") ;}
        sb.append("\n") ;
    }

    public void addRegistro(String[] v) {
        creaRegistrosInd(v);
        count++;        
    }
    
    public String getRegistro(String[] v) {
        creaRegistrosInd(v);
        String reg = sb.toString() ;
        sb = new StringBuilder() ;
        //formatea para catalogacion-o-matic
        reg = reg.replace(" 500   L $$aFO"," 999   L $$aFO") ;
        reg = reg.replace(" L ","") ;
        reg = reg.replace("$$","$") ;
        reg = reg.replace("000000000 ","") ;
        return reg ;
    }
    
    public void addArrayRegistro(String[] v) {
        arrayRegistros.add(v) ;        
    }
    
    public List<String[]> getArrayRegistros() {
        return arrayRegistros ;
    }

    public int getCount() {
        return count ;
    }

    public StringBuilder getSb() {
        return sb ;
    }

    void setCount(int i) {
        count = i ;
    }

    void resetSb() {
        sb = new StringBuilder() ;
    }
}
