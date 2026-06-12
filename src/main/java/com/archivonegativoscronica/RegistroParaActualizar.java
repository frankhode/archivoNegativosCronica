/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author francisco.ortiz
 */
class RegistroParaActualizar {

    private final List<String> titulos,fechas,fotografos ;
    private final List<String[]> campo600,campo610,campo611,campo630,
            campo650,campo651,campo655 ;
    private int cantItems ;
    private String titulo245;
    private String campo043;
    public String sys ;
    boolean tieneDiapo, tieneNega ;

    public RegistroParaActualizar(Funciones cron, String sys, RegistrosParaAgregar regsParaAgregar) {
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '"+sys+"';" ;
        String reg = cron.consultaSimple(consulta, 1).get(0);
        
        this.sys = sys ;
        
        boolean regInd = false ;
        
        //titulos
        titulos = new ArrayList<>(regsParaAgregar.getTitulos(sys));
        
        System.out.println("Consulta:"+consulta);
        System.out.println("Registro original:");
        System.out.println(reg);
        System.out.println("Items:");
        regsParaAgregar.returnRegInv(sys).forEach((t) -> {
            System.out.println(Arrays.toString(t));
        }) ;        
        System.out.println("Títulos regsParaAgregar.getTitulos(sys):");
        titulos.forEach((t) -> {
            System.out.println(t);
        });
        
        //fechas extremas
        fechas = new ArrayList<>() ;
        //obtiene las fechas de los titulos a actualizar
        titulos.forEach((t) -> {
            if (!extraerAnio(t).equals("")) {
                if (!fechas.contains(extraerAnio(t))) {
                    fechas.add(extraerAnio(t)) ;                    
                }
            }
        });
        
        //cantidad de items total
        cantItems = regsParaAgregar.getTitulos(sys).size() ;
        //fotografos
        fotografos = new ArrayList<>() ;
        campo600 = new ArrayList<>() ;
        campo610 = new ArrayList<>() ;
        campo611 = new ArrayList<>() ;
        campo630 = new ArrayList<>() ;
        campo650 = new ArrayList<>() ;
        campo651 = new ArrayList<>() ;
        campo655 = new ArrayList<>() ;
        regsParaAgregar.returnRegInv(sys).forEach((t) -> {
            String aut = t[4] ;
            if (!fotografos.contains(aut)) {
                fotografos.add(aut) ;
            }
        }) ;        
        
        String[] lineas = reg.split("\\n");
        for (String linea : lineas) {
            String campo = linea.substring(10,13) ;
            String indicadores = linea.substring(13,15) ;
            String valorH = null ;
            switch(campo){
                case "008":
                    //fecha 1
                    if (!linea.substring(25,29).equals("^^^^")) {
                        if (!linea.substring(25,29).equals("197^")) {
                            if (!fechas.contains(linea.substring(25,29))) {
                                fechas.add(linea.substring(25,29)) ;
                            }                        
                        }
                    }
                    if (!linea.substring(29,33).equals("^^^^")) {
                        //fecha 2
                        if (!fechas.contains(linea.substring(29,33))) {
                            fechas.add(linea.substring(29,33)) ;
                        }
                    }
                    break;
                case "043":                    
                    campo043 = linea.substring(21) ;
                    break;
                case "245":
                    titulo245 = linea.substring(21) ;
                    break;
                case "300":
                    if (linea.substring(21).startsWith("1 sobre ")) {
                        regInd = true ;
                    }
                    break;
                case "500":
                    if (linea.substring(21).contains("Fotógrafo: ")) {
                        String[] split = linea.substring(21).
                                replace("Fotógrafo: ","").split(", ");
                        for (String fot : split) {
                            if (fot.endsWith(".")) {
                                fot = fot.substring(0,fot.length()-1) ;
                            } else {}
                            if (!fotografos.contains(fot)) {
                                fotografos.add(fot) ;
                            }
                        }
                    }
                    break;
                case "505":
                    String[] tits = linea.substring(21).split(" -- ") ;
                    for (String tit : tits) {
                        if (tit.endsWith(".")) {
                            titulos.add(tit.substring(0,tit.length()-1)) ;
                        } else {
                            titulos.add(tit) ;
                        }
                    }
                    break;
                case "600":
                    campo600.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "610":
                    campo610.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "611":
                    campo611.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "630":
                    campo630.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "650":
                    campo650.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "651":
                    campo651.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "655":
                    campo655.add(new String[]{indicadores,linea.substring(21)}) ;
                    break;
                case "Z30":
                    cantItems++ ;
                    if (regInd) {
                        valorH = buscaValorH(linea) ;                        
                        titulos.add(valorH+". "+titulo245.replace("$$h[material gráfico].","")) ;
                        titulo245 = "["+titulo245.replace("$$h[material gráfico].","").
                                split("\\.")[0]+"]" ;
                        titulo245 = titulo245 + "$$h[material gráfico]." ;
                    }
                    break;
            }
        }
    }
    
    public String extraerAnio(String s) {
        Pattern patron = Pattern.compile(",.*/?(\\d{4})");
        Matcher matcher = patron.matcher(s);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Si no se encontró una coma seguida de un espacio o no se encontró un patrón de fecha válido, devolver la cadena original
        return "";
    }
    
    public List<String> getTitulos() {
        return titulos;
    }
    public List<String> getFechas() {
        return fechas;
    }
    public List<String> getFotografos() {
        return fotografos;
    }
    public List<String[]> getCampo600() {
        return campo600;
    }
    public List<String[]> getCampo610() {
        return campo610;
    }
    public List<String[]> getCampo611() {
        return campo611;
    }
    public List<String[]> getCampo630() {
        return campo630;
    }
    public List<String[]> getCampo650() {
        return campo650;
    }
    public List<String[]> getCampo651() {
        return campo651;
    }
    public List<String[]> getCampo655() {
        return campo655;
    }
    public int getCantItems() {
        return cantItems;
    }
    public String getTitulo245() {
        return titulo245;
    }
    public String getCampo043() {
        return campo043;
    }
    
    public boolean tieneDiapo() {
        tieneDiapo = false ;
        getCampo655().forEach((String[] t) -> {
            System.out.println(Arrays.toString(t));
            if (t[1].equals("Fotografía en color")) {
                tieneDiapo = true ;
            }
        });
        return tieneDiapo;
    }
    
    public boolean tieneNega() {
        tieneNega = false ;
        getCampo655().forEach((t) -> {
            System.out.println(Arrays.toString(t));
            if (t[1].equals("Negativos flexibles")) {
                tieneNega = true ;
            }
        });
        return tieneNega;
    }

    private String buscaValorH(String linea) {
        String valorH = null;                        
        // dividir la cadena en campos para encontrar $$h
        String[] subcampos = linea.substring(21).split("\\$\\$"); 
        for (String subcampo : subcampos) {
            if (subcampo.startsWith("h")) {
                valorH = subcampo.substring(1); // obtener el valor de h
                break;
            }
        }
        if (valorH == null) {
            // dividir la cadena en campos para encontrar $$6
            for (String subcampo : subcampos) {
                if (subcampo.startsWith("6")) {
                    valorH = subcampo.substring(1); // obtener el valor de 6
                    valorH = "["+valorH+"]" ;
                    break;
                }
            }
        }
        if (valorH == null) {
            // dividir la cadena en campos para encontrar $$6
            for (String subcampo : subcampos) {
                if (subcampo.startsWith("5")) {
                    valorH = subcampo.substring(1); // obtener el valor de 5
                    valorH = "["+valorH+"]" ;
                    break;
                }
            }
        }
        return valorH ;
    }
}
