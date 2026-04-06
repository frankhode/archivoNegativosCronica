/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author francisco.ortiz
 */
public class RegistroGrupal {
    private List<String> fechas,autores ;
    private final List<String> digitales,titulos ;
    private final List<List<String>> items ;
    private int cantTiras ;
    private StringBuilder sb ;
    private int count ;
    private String numGrupo,titulo,sys ;
    private final Unificador unificador ;
    private List<String[]> arrayRegistros ;

    public RegistroGrupal(Unificador uni) {
        unificador = uni ;
        fechas = new ArrayList<>() ;
        items = new ArrayList<>() ;
        autores = new ArrayList<>() ;
        digitales = new ArrayList<>() ;
        titulos = new ArrayList<>() ;
        cantTiras = 0 ;
        sb = new StringBuilder() ;
        setCount(0) ;
        titulo = null ;
        arrayRegistros = new ArrayList<>() ;
    }
        
    public void agregaFecha(String fecha){
        try {
            if (!fecha.equals("")) {
                getFechas().add(fecha) ;
            }
        } catch (Exception e) {
            //NullPointerException
        }

    }
    public void agregaItems(String inv,String nro_a, String ufi){
        List<String> l = new ArrayList<>() ;
        l.add(inv) ;
        l.add(nro_a) ;
        l.add(ufi) ;
        getItems().add(l) ;
    }
    public void agregaAutores(String autor){
        try {
            if (!autor.equals("")) {
                getAutores().add(autor) ;   
            }
        } catch (Exception e) {
            //nullPointerException
        }

    }
    public void agregaDigitales(String digi){
        if (!digi.equals("")) {
            getDigitales().add(digi) ;
        }
    }        
    public void agregaTitulos(String titulo,String fecha){
        if (!titulo.equals("") ) {
            if (!fecha.equals("") ) {
                getTitulos().add(titulo+", "+fecha) ;
            } else {
                getTitulos().add(titulo) ;
            }                
        }
    }
    public void sumaTiras(int cant){            
        cantTiras = getCantTiras() + cant ;
    }
    public List<String> getFechas() {
        Collections.sort(fechas) ;
        fechas = fechas.stream().distinct().collect(Collectors.toList()) ;
        return fechas;
    }
    public List<List<String>> getItems() {
        return items;
    }
    public List<String> getAutores() {
        autores = autores.stream().distinct().collect(Collectors.toList()) ;
        return autores;
    }
    public List<String> getDigitales() {
        return digitales;
    }
    public List<String> getTitulos() {
        Collections.sort(titulos) ;
        return titulos;
    }        
    public String getTitulo() {            
        return titulo;
    }
    public int getCantTiras() {
        return cantTiras;
    }
    public void escribeSalida() {
        sb = new StringBuilder() ;
        sys = String.format("%09d", count) ;

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
        switch(getFechas().size()){
            case 0:
                sb.append(" 008   L ^^^^^^s197u^^^^ag^nnn^^^^^^^^^^^^knspa^^") ;
                break;
            case 1:
                sb.append(" 008   L ^^^^^^s") ;
                sb.append(getFechas().get(0).substring(0,4)) ;
                sb.append("^^^^ag^nnn^^^^^^^^^^^^knspa^^") ;
                break;                    
            default:
                sb.append(" 008   L ^^^^^^k") ;
                sb.append(getFechas().get(0).substring(0,4)) ;
                sb.append(getFechas().get(getFechas().size()-1).substring(0,4)) ;
                sb.append("ag^nnn^^^^^^^^^^^^knspa^^") ;
                break;
        }
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
        sb.append(" 24500 L $$a[") ;            
        if (titulo != null) {
            sb.append(titulo) ;
        } else {
            sb.append("Grupo ") ;
            sb.append(numGrupo) ;
        }            
        sb.append("]") ;
        sb.append("$$h[material gráfico].") ;
        sb.append("\n") ;

        //260 
        sb.append(sys) ;
        sb.append(" 260   L $$c ") ;
        switch(getFechas().size()){
            case 0:
                sb.append("[197-?]") ;
                break;
            case 1:
                sb.append(getFechas().get(0).substring(0,4)) ;
                break;                    
            default:
                sb.append(getFechas().get(0).substring(0,4)) ;
                sb.append("-") ;
                sb.append(getFechas().get(getFechas().size()-1).substring(0,4)) ;
                break;
        }
        sb.append("\n") ;

        //300
        sb.append(sys) ;
        sb.append(" 300   L $$a ") ;
        sb.append(getCantTiras()) ;
        sb.append(" sobres (negativos flexibles) :$$bbyn") ;
        sb.append("\n") ;

        //500 titulo
        sb.append(sys) ;
        sb.append(" 500   L $$a Título asignado por el personal de la Biblioteca.") ;
        sb.append("\n") ;        

        //500 autor
        if (getAutores().size() > 0) {
            sb.append(sys) ;
            sb.append(" 500   L $$aFotógrafo: ") ;
            getAutores().stream().forEach((t) -> {
                sb.append(t) ;
                sb.append(", ") ;
            });
            sb.append("\n") ;
        }

        //500 unifica
        try {
            if (unificador.posibleUnificacion(titulo)) {            
                sb.append(sys) ;
                sb.append(unificador.verificaTitulo(titulo)) ;
                sb.append("\n") ;
            }
        } catch (Exception e) {
            //null pointer
            System.out.println(" ->"+titulo+"<-");
            //System.out.println(Arrays.toString(lineaReg));
        }

        //505
        sb.append(sys) ;
        StringBuilder temp = new StringBuilder() ;
        String tem = "" ;
        temp.append(" 5058  L $$a") ;
        if (getTitulos().size() > 10) {
            for (int i = 0; i < getTitulos().size(); i++) {
                if (temp.toString().length() >= 1800) {
                    sb.append(temp.toString()) ;
                    temp = new StringBuilder() ;
                    temp.append("\n") ;
                    temp.append(sys) ;
                    temp.append(" 5058  L $$a") ;
                }
                temp.append(getTitulos().get(i)) ;
                temp.append(" -- ") ;
            }
            //carga los que faltan
            sb.append(temp.toString().substring(0,temp.length()-4)) ;
        } else {
            sb.append(" 5058  L $$a") ;                
            getTitulos().stream().forEach((t) -> {
                sb.append(t) ;
                sb.append(" -- ") ;
            });
            sb = new StringBuilder(sb.toString().substring(0,sb.toString().length()-4)) ;
        }
        sb.append(".\n") ;


        if (getDigitales().size() > 0) {
            //506
            sb.append(sys) ;
            sb.append(" 5061  L $$aDisponible solo en formato digital.") ;
            sb.append("\n") ;

            //530
            sb.append(sys) ;
            sb.append(" 530   L $$aDisponible en formato digital solo para consulta en sala (") ;
            getDigitales().stream().forEach((t) -> {
                sb.append("BNA_") ;
                sb.append(t) ;

                sb.append(", ") ;
            });
            sb = new StringBuilder(sb.toString().substring(0,sb.toString().length()-2)) ;
            sb.append(").") ;
            sb.append("\n") ;

            //520   L $$a
            sb.append(sys) ;
            sb.append(" 5208  L $$a") ;
            sb.append("\n") ;
        }

        //540            
        sb.append(sys) ;
        sb.append(" 540   L "
                + "$$aPuede presentar restricciones. "
                + "Consultar en el Departamento de "
                + "Materiales Cartográficos y Fotográficos.$$5AR-BaBN") ;
        sb.append("\n") ;

        //561
        sb.append(sys) ;
        sb.append(" 5611  L $$aForma parte del archivo fotográfico del diario Crónica.$$5AR-BaBN") ;
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
        getItems().stream().forEach((t) -> {
            sb.append(sys) ;
            sb.append(" 500   L $$a") ;
            sb.append(t.get(0)) ;
            sb.append(" ") ;
            sb.append(t.get(1)) ;
            sb.append(" ") ;
            sb.append(t.get(2)) ;
            sb.append("\n") ;
        });
    }
    public void setCount(int i) {
        count = i ;
    }
    public String getSbText() {
        return sb.toString() ;
    }
    public StringBuilder getSb() {
        return sb ;
    }
    void setTitulo(String k) {
        titulo = k ;
    }
    public String getSys() {
        return sys ;
    }

    public void addArrayRegistros(String[] sobre) {
        arrayRegistros.add(sobre) ;
    }
    
    public List<String[]> getArrayRegistros() {
        return arrayRegistros ;
    }
}