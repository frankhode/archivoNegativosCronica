/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 *
 * @author francisco.ortiz
 */
public final class Registro{
    private String sys ;
    private boolean tieneMaterias,tieneDimensiones ;
    private StringBuilder addRegistro ;
    private HashMap<String, List<String>> dicRegistro;
    public List<Item> items ;
    private List<Registro> arrayRegs ;
    private String[] areas ;
    private Registro reg ;
    
    public Registro(){ 
        this.dicRegistro = new HashMap<>();
        addRegistro = new StringBuilder();
        items = new ArrayList<>() ;
    }    

    public Registro(String registro){
        this.dicRegistro = new HashMap<>();
        addRegistro = new StringBuilder();
        items = new ArrayList<>() ;
        setSys(registro.substring(0,9));
        String [] divide = registro.split("\\r?\\n");
        for (String linea : divide) {
            addLine(linea);
        }
    }
    
    public Registro(File file) throws FileNotFoundException{
        arrayRegs = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        Stream<String> linea = br.lines() ;
        
        linea.forEach((t) -> {
            if (t.substring(10,13).equals("FMT")) {
                reg = new Registro() ;
                arrayRegs.add(reg) ;
                reg.addLine(t);
            } else {
                reg.addLine(t);
            }
        });
    }
    
    public void addLine(String N){        
        addRegistro.append(N) ;
        addRegistro.append("\n") ;
        String campo = N.substring(10,13) ;
        String indicadores = N.substring(13,15) ;
        String dato = N.substring(18) ;
        String dato_formateado = formatea_dato(dato,campo,N);
        
        ArrayList<String> list;
        if(dicRegistro.containsKey(campo)){
            // if the key has already been used,
            // we'll just grab the array list and add the value to it
            list = (ArrayList<String>) dicRegistro.get(campo);
            list.add(dato_formateado);
        } else {
            // if the key hasn't been used yet,
            // we'll create a new ArrayList<String> object, add the value
            // and put it in the array list with the new key
            list = new ArrayList<>();
            list.add(dato_formateado);
            dicRegistro.put(campo, list);
        }
    }
    
    private String formatea_dato(String dato, String campo,String N){
        String dato_formateado ;
        switch(campo){
            case "008":
                dato_formateado = dato ;
                break;
            case "080":
                if (dato.contains("$$2")) {
                    //String [] ed = new String [2] ;
                    String [] ed ;
                    ed = dato.split("\\$\\$2") ;
                    String ed_dato = ed[1] ;
                    String cdu = ed[0].replaceAll("\\$\\$.", "") ;
                    dato_formateado = cdu + " [ed. " + ed_dato + "]" ;                    
                } else {
                    dato_formateado = dato.replaceAll("\\$\\$.", "") ;
                }
                                
                break;
            case "043":
                areas = dato.split("\\$\\$a") ;
                dato_formateado = dato ;
                break;
            case "300":
                dato_formateado = dato.replaceAll("\\$\\$.", "") ;
                if(dato.contains("$$c")) {
                    tieneDimensiones = true ;
                }
                break;
            case "490": dato_formateado = dato ;
                break;
            case "600":
            case "610":
            case "611":
            case "630":
            case "650":
            case "651":
            case "653":
            case "655":
                dato_formateado = dato.replaceAll("\\$\\$a", "") ;
                dato_formateado = dato_formateado.replaceAll("\\$\\$[b|c|d|n|t]", " ") ;
                dato_formateado = dato_formateado.replaceAll("\\$\\$.", " -- ") ;
                tieneMaterias = true ;
                break;
            case "OWN":
                dato_formateado = dato.replaceAll("\\$\\$.", "") ;
                break;            
            case "CAT":
                String cat ;
                String fecha = dato.substring(dato.indexOf("$$c")+3,dato.indexOf("$$c")+11) ;
                try {
                    cat = dato.substring(dato.indexOf("$$a")+3,dato.indexOf("$$b")) ;
                } catch (Exception e) {
                    cat = "" ;
                }
                
                dato_formateado = cat + "|" + fecha ;
                break;
            case "Z30":
                dato_formateado = "" ;
                items.add(new Item(dato, N.substring(0,9))) ;
                break ;
            case "LKR":
                dato_formateado = dato.replaceAll("\\$\\$a", "") ;
                dato_formateado = dato_formateado.replaceAll("\\$\\$.", " ") ;                
                break ;
            default:
                dato_formateado = dato.replaceAll("\\$\\$a", "") ;
                dato_formateado = dato_formateado.replaceAll("\\$\\$.", " ") ;
                break;
        }
        return dato_formateado;
    }    
    
    public void ImprimeRegistro() {
        // Print the Map Contents
        System.out.println(getSys());
        
        //IMPRIME ORDENADO POR CAMPO
        SortedSet<String> keys = new TreeSet<>(dicRegistro.keySet());
        keys.forEach((String key) -> {
            List<String> value = dicRegistro.get(key);
            // do something
            value.forEach((str) -> {
                System.out.println(key +" -> "+ str);
            });
        }); /*
        //IMPRIME POR ORDEN NATURAL
        for (Map.Entry<String, List<String>> entry : dicRegistro.entrySet()) {
        String x = entry.getKey();
        entry.getValue().forEach((temp) -> {
        System.out.println(x + ": " + temp);
        });
        }*/
    }
    
    public void vaciaRegistro() {
        sys = "" ;
        dicRegistro = new HashMap<>();
        addRegistro = null ;
    }

    /**
     * @return the sys
     */
    public String getSys() {
        return sys;
    }

    /**
     * @param sys the sys to set
     */
    public void setSys(String sys) {
        this.sys = sys;
    }
    
    public List<Item> getItems() {
        return items ;
    }
    
    public void eliminaCat(String [] N){
        List<String> lista_cat = this.dicRegistro.get("CAT") ;
                
        for (Iterator<String> iterator = lista_cat.iterator(); iterator.hasNext();) {
            String next = iterator.next();
            String [] divide = next.split("\\|") ;
            for (String cat : N) {
                if (cat.equals(divide[0])) {
                    iterator.remove();
                }
            }            
        }
    }
    
    public boolean verificaRangoFecha(String fechaInicial, String fechaFinal){
        List<String> lista_cat = this.dicRegistro.get("CAT") ;
        boolean fecha_en_rango = false ;
        
        for (String next : lista_cat) {
            String [] divide = next.split("\\|") ;
            int fecha = Integer.parseInt(divide[1]) ;
            int fechaIni = Integer.parseInt(fechaInicial) ;
            int fechaFin = Integer.parseInt(fechaFinal) ;
            if (fecha <= fechaFin && fecha >= fechaIni) {
                //System.out.println(registro.sys+" | "+divide[0]+" ->"+fecha);
                fecha_en_rango = true ;
            }
        }
        return fecha_en_rango ;
    }
    
    public boolean verificaRangoFechaCatalogador(String cat, String fechaInicial, String fechaFinal){
        List<String> lista_cat = this.dicRegistro.get("CAT") ;
        boolean fecha_en_rango = false ;
        
        for (String next : lista_cat) {
            String [] divide = next.split("\\|") ;
            
            int fecha = Integer.parseInt(divide[1]) ;
            String catalogador = divide[0] ;
            int fechaIni = Integer.parseInt(fechaInicial) ;
            int fechaFin = Integer.parseInt(fechaFinal) ;
            if (fecha <= fechaFin && fecha >= fechaIni && catalogador.equals(cat)) {
                //System.out.println(registro.sys+" | "+divide[0]+" ->"+fecha);
                fecha_en_rango = true ;
            }
        }
        return fecha_en_rango ;
    }
    
    public String getTitulo() {
        List<String> we = this.dicRegistro.get("245") ;
        String titulo = "" ;
        for (String s : we) {
            titulo = s ;
        }        
        return titulo ;
    }
    
    public String getRegistro() {
        return this.addRegistro.toString() ;
    }
    
    public String getTituloFormateado() {
        List<String> we = this.dicRegistro.get("245") ;
        String titulo = "" ;
        for (String s : we) {
            String s_formateado = s.replaceAll("\\$\\$a", "") ;
            s_formateado = s_formateado.replaceAll("\\$\\$.", " ") ;
            titulo = s_formateado ;
        }        
        return titulo.replace("[material gráfico]", "") ;
    }
    
    public VBox getFormatoOpac() {
        VBox salida = new VBox() ;
        Text etiqueta ;
        Text dato ;
        Font negrita = Font.font("Verdana", FontWeight.BOLD, 12) ;
        Font normal = Font.font("Verdana", 12) ;
        
        ///////////**************** SYS *************************//////
        etiqueta = new Text("No. de sistema: ") ;
        etiqueta.setFont(negrita);
        dato = new Text(this.sys) ;
        dato.setFont(normal);
        salida.getChildren().add(new HBox(etiqueta,dato)) ;
        
        ///////////**************** FMT *************************//////
        try {
            List<String> cFMT = this.dicRegistro.get("FMT") ;
            for (String s : cFMT) {
                
                etiqueta = new Text("Formato: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }
        } catch (Exception e) {/*no existe el campo*/}
                
        ///////////**************** CDU - CDD *************************//////
        try {
            List<String> c080 = this.dicRegistro.get("080") ;
            for (String s : c080) {                
                etiqueta = new Text("CDU: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                dato.setWrappingWidth(450);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }
            List<String> c082 = this.dicRegistro.get("082") ;
            for (String s : c082) {
                etiqueta = new Text("CDD: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                dato.setWrappingWidth(450);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }
        } catch (Exception e) {/*no existe el campo*/}
        
        ///////////********** ENTRADA PRINCIPAL *********************//////        
        List<String> c1XX = new ArrayList<>() ;
        c1XX.add("100") ;
        c1XX.add("110") ;
        c1XX.add("111") ;
        c1XX.add("130") ;
        for (String i : c1XX) {
            try {
                List<String> c = this.dicRegistro.get(String.valueOf(i)) ;                
                for (String s : c) {
                    etiqueta = new Text("Entrada principal: ") ;
                    etiqueta.setFont(negrita);
                    dato = new Text(s) ;
                    dato.setFont(normal);
                    dato.setWrappingWidth(450);
                    salida.getChildren().add(new HBox(etiqueta,dato)) ;
                }                        
            } catch (Exception e) {/*no existe el campo*/}
        }
        
        ///////////**************** TITULO *************************//////
        try {
            List<String> c245 = this.dicRegistro.get("245") ;        
            
            for (String s : c245) {
                etiqueta = new Text("Título: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                dato.setWrappingWidth(450);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }            
        } catch (Exception e) {/*no existe el campo*/}
        
        ///////////**************** PUBLICACION, ETC *************************//////
        List<String> pie = new ArrayList<>() ;
        pie.add("260") ;
        pie.add("264") ;        
        for (String a : pie) {
            try {
                List<String> c = this.dicRegistro.get(a) ;
                for (String s : c) {
                    etiqueta = new Text("Pie de imprenta: ") ;
                    etiqueta.setFont(negrita);
                    dato = new Text(s) ;
                    dato.setFont(normal);
                    dato.setWrappingWidth(450);
                    salida.getChildren().add(new HBox(etiqueta,dato)) ;
                }
            } catch (Exception e) {/*no existe el campo*/}
        }
        
        ///////////**************** DESCRIPCION FISICA *************************//////
        try {
            List<String> df = this.dicRegistro.get("300") ; 
            for (String s : df) {
                etiqueta = new Text("Descrip. física: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                dato.setWrappingWidth(450);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }                    
        } catch (Exception e) {/*no existe el campo*/}
        
        ///////////**************** SERIE *************************//////
        try {
            List<String> c490 = this.dicRegistro.get("490") ; 
            for (String s : c490) {
                etiqueta = new Text("Serie: ") ;
                etiqueta.setFont(negrita);
                dato = new Text(s) ;
                dato.setFont(normal);
                dato.setWrappingWidth(450);
                salida.getChildren().add(new HBox(etiqueta,dato)) ;
            }                    
        } catch (Exception e) {/*no existe el campo*/}
        
        ///////////**************** NOTAS *************************//////
        for (int i = 500; i <= 599; i++) {
            try {
                List<String> c5XX = this.dicRegistro.get(String.valueOf(i)) ; 
                for (String s : c5XX) {
                    etiqueta = new Text("Notas: ") ;
                    etiqueta.setFont(negrita);
                    dato = new Text(s) ;
                    dato.setFont(normal);
                    dato.setWrappingWidth(450);
                    salida.getChildren().add(new HBox(etiqueta,dato)) ;
                }   
            } catch (Exception e) {/*no existe el campo*/}            
        }
        
        ///////////**************** MATERIAS *************************//////
        List<String> materias = new ArrayList<>() ;
        materias.add("600") ;
        materias.add("610") ;
        materias.add("611") ;
        materias.add("630") ;
        materias.add("650") ;
        materias.add("651") ;
        materias.add("653") ;
        materias.add("655") ;
        
        for (String campo : materias) {            
            try {                
                List<String> c6XX = this.dicRegistro.get(campo) ;
                for (String s : c6XX) {
                    switch(campo) {
                        case("600"):
                            etiqueta = new Text("Materia persona: ") ;
                            break ;
                        case("610"):
                            etiqueta = new Text("Materia entidad: ") ;
                            break ;
                        case("611"):
                            etiqueta = new Text("Materia congreso: ") ;
                            break ;
                        case("630"):
                            etiqueta = new Text("Materia titulo: ") ;
                            break ;
                        case("650"):
                            etiqueta = new Text("Materia tema: ") ;
                            break ;
                        case("651"):
                            etiqueta = new Text("Materia lugar: ") ;
                            break ;
                        case("653"):
                            etiqueta = new Text("Materia descriptores: ") ;
                            break ;
                        case("655"):
                            etiqueta = new Text("Género/Forma: ") ;
                            break ;
                    }
                    etiqueta.setFont(negrita);
                    dato = new Text(s) ;
                    dato.setFont(normal);
                    dato.setWrappingWidth(450);
                    salida.getChildren().add(new HBox(etiqueta,dato)) ;
                }
            } catch (Exception e) {/*no existe el campo*/}
        }
        
        ///////////**************** SECUNDARIAS *************************//////
        for (int i = 700; i <= 730; i++) {
            try {
                List<String> c7XX = this.dicRegistro.get(String.valueOf(i)) ;                 
                for (String s : c7XX) {
                    etiqueta = new Text("Entrada secundaria: ") ;
                    etiqueta.setFont(negrita);
                    dato = new Text(s) ;
                    dato.setFont(normal);
                    dato.setWrappingWidth(450);
                    salida.getChildren().add(new HBox(etiqueta,dato)) ;
                }
            } catch (Exception e) {/*no existe el campo*/}
        }
        
        //return registroSalida ;        
        return salida ;
    }
    
    public VBox getFormatoSecuencial() {
        VBox salida = new VBox() ;
        Text dato = new Text(this.addRegistro.toString()) ;        
        salida.getChildren().add(new HBox(dato)) ;
        dato.setWrappingWidth(450);        
        return salida ;
    }
    
    public String nivelDeCatalogacion () {
        String sta = "Catalogación mínima";
        if (tieneDimensiones && tieneMaterias) {
            sta = "Completo" ;
        } else {
            if (tieneDimensiones) {
                sta = "Catalogación mínima" ;
            }
            if (tieneMaterias) {
                sta = "Indizado" ;
            }
        }
        return sta ;
    }
    
    public List<String> getMaterias () {
        List<String> materias = new ArrayList<>() ;
        String[] mat = new String[]{"600","610","611","630","650","651","653"} ;
        
        for (String m : mat) {
            try {
                List<String> getm = this.dicRegistro.get(m);
                getm.forEach((mat6XX) -> {
                    materias.add(mat6XX) ;
                });
            } catch (Exception e) {
                //no existe el campo en el registro
            }
        }
        return materias ;
    }
    
    public List<String> getCampos (String[] campos) {
        List<String> resultados = new ArrayList<>() ;        
        for (String m : campos) {
            try {
                List<String> getm = this.dicRegistro.get(m);
                getm.forEach((res) -> {
                    resultados.add(res) ;
                });
            } catch (Exception e) {
                //no existe el campo en el registro
            }
        }
        return resultados ;
    }
    
    public List<String> getMateriaPersona () {
        List<String> mp = new ArrayList<>() ;        
        try {
            List<String> getm = this.dicRegistro.get("600");
            getm.forEach((mat600) -> {
                mp.add(mat600) ;
            });
        } catch (Exception e) {
            //no existe el campo en el registro
        }
        return mp ;
    }
    
    public List<String> getGeneroForma () {
        List<String> gf = new ArrayList<>() ;
        
        try {
            List<String> getm = this.dicRegistro.get("655");
            getm.forEach((mat655) -> {
                gf.add(mat655) ;
            });
        } catch (Exception e) {
            //no existe el campo en el registro
        }
        return gf ;
    }
    
    public List<String> getAutores () {
        List<String> autores = new ArrayList<>() ;
        //600 610 611 630 650 651 653 655
        String[] campo = new String[]{"100","110","111","130"} ;
        
        for (String m : campo) {
            try {
                List<String> getm = this.dicRegistro.get(m);
                getm.forEach((mat1XX) -> {
                    autores.add(mat1XX) ;
                });
            } catch (Exception e) {
                //no existe el campo en el registro
            }
        }
        return autores ;
    }
    
    public List<String> getCDU () {
        List<String> clasificaciones = new ArrayList<>() ;        
        try {
            List<String> getm = this.dicRegistro.get("080");
            getm.forEach((mat6XX) -> {
                clasificaciones.add(mat6XX) ;
            });
        } catch (Exception e) {
            //no existe el campo en el registro
        }
        return clasificaciones ;
    }
    
    public List<String> getCampo (String campo) {
        List<String> datos = new ArrayList<>() ;        
        try {
            List<String> get = this.dicRegistro.get(campo);
            get.forEach((result) -> {
                datos.add(result) ;
            });
        } catch (Exception e) {
            //no existe el campo en el registro
        }
        return datos ;
    }        
    
    public boolean verificaMateria(String mat){
        List<String> materias = this.getMaterias();
        return materias.contains(mat);
    }
    
    public boolean verificaAutor(String aut){
        List<String> autor = this.getAutores();
        return autor.contains(aut);
    }
    
    public boolean verificaGF(String gf){
        List<String> getGF = getCampo("655") ;
        return getGF.contains(gf);
    }
    
    public String getFormatoLiteral() {       
        String ldr = dicRegistro.get("LDR").get(0) ;
        String ldr_06 = ldr.substring(6,1) ;
        String formato = "" ;
        switch(ldr_06){
            case "a":
                formato ="Material textual" ;
                break;
            case "c":
                  formato ="Música notada" ;
                  break;
            case "d":
                  formato ="Música notada manuscrita" ;
                  break;
            case "e":
                  formato ="Material cartográfico" ;
                  break;
            case "f":
                  formato ="Material cartográfico manuscrito" ;
                  break;
            case "g":
                  formato ="Material gráfico proyectable" ;
                  break;
            case "i":
                  formato ="Grabación sonora no musical" ;
                  break;
            case "j":
                  formato ="Grabación sonora musical" ;
                  break;
            case "k":
                  formato ="Material gráfico bidimensional, no proyectable" ;
                  break;
            case "m":
                  formato ="Archivo de computadora" ;
                  break;
            case "o":
                  formato ="Kit" ;
                  break;
            case "p":
                  formato ="Material mixto" ;
                  break;
            case "r":
                  formato ="Objeto tridimensional artificial o natural" ;
                  break;
            case "t":
                  formato ="Material textual manuscrito" ;
                  break;
        }
        return formato ;
    }
    
    public String getFormatoCod() {
        String ldr = dicRegistro.get("LDR").get(0) ;
        String ldr_06 = ldr.substring(6,7) ;
        return ldr_06 ;
    }
    
    public boolean tieneDigital() {
        try {
            String d = dicRegistro.get("530").get(0) ;
            return true ;
        } catch (Exception e) {
            return false ;
        }
    }
    
    public String getDigital() {
        String d = dicRegistro.get("530").get(0) ;
        int bnaInicio = d.indexOf("(") + 1 ;
        int bnaFin = d.indexOf(")") ;
        String digital = d.substring(bnaInicio,bnaFin) ;
        return digital ;
    }

    String getFecha() {
        return dicRegistro.get("260").get(0) ;
    }
    
    String getFechaCompleta() {
        String titulo = getTituloFormateado().replace("[material gráfico]", "") ;
        String fecha = "" ;
        String[] split = titulo.split(",");
        if (split.length > 1) {
            fecha = split[split.length-1].trim();
        }
        return fecha ;
    }

    String getPrimerCatalogador() {
        List<String> cats = dicRegistro.get("CAT") ;
        String[] exp = cats.get(0).split("\\|") ;        
        return exp[0] ;
    }
    
    String getPrimeraIntervencion() {
        List<String> cats = dicRegistro.get("CAT") ;
        String[] exp = cats.get(0).split("\\|") ;        
        return exp[1] ;
    }
    
    public List<Registro> getArrayRegs(){
        return arrayRegs ;
    }

    List<String> getAreas() {
        if (areas == null) {
            areas = new String[0] ;
        }
        
        
        List<String> a = new ArrayList<>() ;
        a.addAll(Arrays.asList(areas));
        //elimina los vacios
        Arrays.asList(areas).forEach((t) -> {
            if ("".equals(t)) {
                a.remove(t) ;
            }
        });
        return a ;
    }

    String getUltimoCatalogador() {
        List<String> cats = this.getCampo("CAT");
        String cat = null ;
        for (String c : cats) {
            cat = c.split("\\|")[0] ;            
        }
        return cat ;
    }
    
    String getUltimaIntervencion() {
        List<String> cats = dicRegistro.get("CAT") ;
        String cat = null ;
        for (String c : cats) {            
            cat = c.split("\\|")[1] ;
        }
        return cat ;
    }
    
    boolean intervenidoPor(String cat){
        boolean bool = false ;
        List<String> cats = dicRegistro.get("CAT") ;
        for (String c : cats) {
            String intervencionDe = c.split("\\|")[0] ;
            if (intervencionDe.equals(cat)) {
                bool = true ;
            }
        }
        return bool ;
    }

    public String get500Fotografo() {
        String autor = "";
        List<String> aut = getCampo("500") ;
        for (String string : aut) {            
            if (string.toLowerCase().contains("fotografo") || 
                    string.toLowerCase().contains("fotógrafo")) {
                autor = string.substring(string.indexOf(":")+2) ;
                if (autor.endsWith(".")) {
                    autor = autor.substring(0, autor.length() - 1);
                }
                autor = autor.trim() ;
            }
        }
        return autor ;
    }

    public String getFechaISO() {
        String inputDate = getFechaCompleta().replace(" ", "").replace(".", "");
        
        // Formateador de salida a formato ISO
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate date = null;
        String fechaISO = "";

        try {
            // Si el formato es solo año (YYYY)
            if (inputDate.matches("\\d{4}")) {
                // Completa con "-00-00" cuando solo se tiene el año
                fechaISO = inputDate + "0000";
            }
            // Si el formato es mes y año (MM/YYYY)
            else if (inputDate.matches("\\d{2}/\\d{4}")) {
                date = LocalDate.parse("01/" + inputDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                fechaISO = date.format(isoFormatter);
            }
            // Si el formato es fecha completa (DD/MM/YYYY)
            else if (inputDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
                DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                date = LocalDate.parse(inputDate, fullDateFormatter);
                fechaISO = date.format(isoFormatter);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Error al parsear la fecha: " + e.getMessage());
        }
        
        // Retorna la fecha en formato ISO o vacío si no coincide el formato
        return fechaISO.replace("-", "");
    }

    public String getTituloFormateadoSinFecha() {
        String titulo = getTituloFormateado() ;
        if (titulo.contains(", "+getFechaCompleta())) {
            titulo = titulo.replace(titulo.substring(titulo.indexOf(
                    ", "+getFechaCompleta())), "") ;
        } else if (titulo.contains(","+getFechaCompleta())) {
            titulo = titulo.replace(titulo.substring(titulo.indexOf(
                    ","+getFechaCompleta())), "") ;
        }
        return titulo ;
    }
    
    public String getSerie() {        
        StringBuilder serie = new StringBuilder() ;
        try {
            List<String> get490 = this.dicRegistro.get("490");
            get490.forEach((t) -> {
                if (t.contains("$$v")) {
                    serie.append(t.substring(3, t.indexOf("$$v")+3));
                    serie.append("\t");
                    serie.append(t.substring(t.indexOf("$$v")+3));
                }
            });
        } catch (Exception e) {
            //no hay 490
        }
        return serie.toString() ;
    }
    
    public String getAño() {        
        String anio = "" ;
        try {
            List<String> get260 = this.dicRegistro.get("260");
            for (String string : get260) {
                if (string.contains("$$c")) {
                    anio = string.substring(string.indexOf("$$c")+3); 
                } else {
                    System.out.println("");
                }
            }
        } catch (Exception e) {
            //no hay 260
            System.out.println("");
        }        
        return anio ;
    }

    public String getColeccion() {        
        try {
            List<String> get = this.dicRegistro.get("561");
            if (!get.isEmpty()) {
                return getCampo("561").get(0) ;
            } else {
                return "" ;
            }
        } catch (Exception e) {
            return "" ;
        }
    }    
}

