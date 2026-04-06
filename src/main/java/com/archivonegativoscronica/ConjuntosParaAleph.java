/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
class ConjuntosParaAleph {

    List<String[]> listaPendientes ;
    List<String[]> listaUnicos ;    
    List<List<String[]>> listaGrupos ;
    HashMap<String,List<String[]>> unificador ;
    private RegistroGrupal reg ;
    private File folder ;
    private int count,outputFile ;
    private RegistrosIndividuales ind ;
    private final Unificador uni ;
    Funciones cron ;
    private List<String[]> regIndi ;
    private List<RegistroGrupal> regGrupos ;
    
    
    //contructor para catalogacion normal
    public ConjuntosParaAleph(Funciones cron) throws IOException {
        String consulta = "SELECT * FROM inventario WHERE barcode NOT IN "
                + "(SELECT barcode FROM items) AND barcode NOT IN " 
                + "(SELECT barcode FROM conjuntos)" ;
        this.cron = cron ;
        uni = new Unificador(cron) ;
        
        TextInputDialog filter = new TextInputDialog() ;
        filter.setTitle("Filtro: ");
        filter.setHeaderText("Ejemplo de filtro: titulo LIKE 'O%'");
        ((Button) filter.getDialogPane().lookupButton(ButtonType.OK)).setText("Aplicar filtro");
        ((Button) filter.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No usar filtro");
        
        Optional<String> result = filter.showAndWait() ;
        if (result.isPresent() && !result.get().equals("")) {
            consulta = consulta + " AND (" + result.get() +")";
        }
        
        listaPendientes = cron.consultaCompleta(consulta);
        listaUnicos = new ArrayList<>() ;
        listaGrupos = new ArrayList<>() ;
        unificador = new HashMap<>() ;        
        outputFile = 1 ;
        ind = new RegistrosIndividuales(getUni()) ;
        salida() ;
        
        listaPendientes.forEach((t) -> {
            String titulo = "" ;
            try {
                titulo = t[5].replace(" [material gráfico].", "") ;
                titulo = titulo.replace(" [material gráfico]", "") ;
                titulo = titulo.replace("[sic]", "") ;
                titulo = titulo.replaceAll("\\[i\\.e\\. .*\\]", "") ;
                titulo = titulo.replaceAll("\\[i\\. e\\. .*\\]", "") ;
                titulo = titulo.replace("[", "") ;
                titulo = titulo.replace("]", "") ;            
            } catch (Exception e) {
                System.out.println(Arrays.toString(t));
            }
            String[] split = titulo.split("\\.");
            try {
                unificador.get(split[0]).add(t);
            } catch (Exception e) {
                List<String[]> lista = new ArrayList<>() ;
                lista.add(t) ;
                unificador.put(split[0], lista) ;
            }            
        });
        System.out.println("Lista pendientes: "+listaPendientes.size());
        System.out.println("Unificador: "+unificador.size());
        
        //para registros grupales
        unificador.forEach((k, v) -> {
            if (v.size() != 1) {
                //registros grupales
                //muestra el listado, si esta ok cargaGrupo()
                //si no agrega el sobre a la lista de individuales
                if (revisaGrupo(v,k) && !v.isEmpty()) {
                    reg = new RegistroGrupal(getUni()) ;
                    reg.setTitulo(k) ;
                    for (String[] sobre : v) {
                        cargaGrupo(sobre);
                    }
                    reg.escribeSalida();
                    if (verificaUnificacion(reg.getTitulo())) {
                        reg.getSb().append(reg.getSys()) ;
                        reg.getSb().append(posibleUnificacion(reg.getTitulo())) ;
                    }                    
                    escribeArchivoSalida(reg.getSb(),"GRUPOS") ;
                }                                
            } else {
                ind.addArrayRegistro(v.get(0));                
            }
        });
        
        //para registros individuales
        ind.setCount(0) ;
        ind.getArrayRegistros().forEach((t) -> {
            ind.addRegistro(t) ;            
            if (ind.getCount() == 10) {
                escribeArchivoSalida(ind.getSb(),"IND") ; 
                ind.setCount(0) ;
                ind.resetSb() ;
            }
        });
        escribeArchivoSalida(ind.getSb(),"IND") ; 
        renombraArchivos() ;
        
    }
    
    //constructor para el catalogador-o-matic
    public ConjuntosParaAleph(Funciones cron, int cantReg,
            boolean indiv, boolean grupos) throws IOException {
        String consulta = "SELECT * FROM inventario WHERE barcode "
                + "NOT IN (SELECT barcode FROM items) AND barcode "
                + "NOT IN (SELECT barcode FROM conjuntos)" ;
        this.cron = cron ;
        uni = new Unificador(cron) ;
        
        
        TextInputDialog filter = new TextInputDialog() ;
        filter.setTitle("Filtro: ");
        filter.setHeaderText("Ejemplo de filtro: titulo LIKE 'O%'");
        ((Button) filter.getDialogPane().lookupButton(ButtonType.OK)).setText("Aplicar filtro");
        ((Button) filter.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No usar filtro");
        
        Optional<String> result = filter.showAndWait() ;
        if (result.isPresent() && !result.get().equals("")) {
            consulta = consulta + " AND (" + result.get() +")";
        }
        
        listaPendientes = cron.consultaCompleta(consulta);
        listaUnicos = new ArrayList<>() ;
        listaGrupos = new ArrayList<>() ;
        unificador = new HashMap<>() ;        
        ind = new RegistrosIndividuales(getUni()) ;
        
        listaPendientes.forEach((t) -> {
            String titulo = "" ;
            try {
                titulo = t[5].replace(" [material gráfico].", "") ;
                titulo = titulo.replace(" [material gráfico]", "") ;
                titulo = titulo.replace("[sic]", "") ;
                titulo = titulo.replaceAll("\\[i\\.e\\. .*\\]", "") ;
                titulo = titulo.replaceAll("\\[i\\. e\\. .*\\]", "") ;
                titulo = titulo.replace("[", "") ;
                titulo = titulo.replace("]", "") ;            
            } catch (Exception e) {
                System.out.println(Arrays.toString(t));
            }
            String[] split = titulo.split("\\.");
            try {
                unificador.get(split[0]).add(t);
            } catch (Exception e) {
                List<String[]> lista = new ArrayList<>() ;
                lista.add(t) ;
                unificador.put(split[0], lista) ;
            }            
        });
        regGrupos = new ArrayList<>() ;
        
        unificador.forEach((k, v) -> {
            if (v.size() != 1) {
                //para registros grupales
                if (grupos) {
                    if (revisaGrupo(v,k) && !v.isEmpty()) {
                        reg = new RegistroGrupal(getUni()) ;                        
                        reg.setTitulo(k) ;
                        for (String[] sobre : v) {
                            cargaGrupo(sobre);
                            reg.addArrayRegistros(sobre) ;
                        }                                         
                       regGrupos.add(reg) ;
                    }                                
                }                
            } else {
                ind.addArrayRegistro(v.get(0));                
            }
        });
                        
        //para registros grupales
        
        
        //registros individuales
        if (indiv) {
            regIndi = new ArrayList<>() ;
            ind.getArrayRegistros().forEach((t) -> {
                regIndi.add(t) ;                
            });            
        }
    }
    
    public ConjuntosParaAleph(Funciones cron, int cantReg,
            boolean indiv, boolean grupos, boolean precatalog) throws IOException {
        String consulta = "SELECT * FROM inventario WHERE barcode "
                + "NOT IN (SELECT barcode FROM items) AND barcode "
                + "NOT IN (SELECT barcode FROM conjuntos)" ;
        this.cron = cron ;
        uni = new Unificador(cron) ;
                
        listaPendientes = cron.consultaCompleta(consulta);
        regIndi = new ArrayList<>() ;
        listaPendientes.forEach((t) -> {
            regIndi.add(t) ; 
        });        
    }
    
    public void cargaGrupo(String[] lineaReg) {
        //autor
        reg.agregaAutores(lineaReg[4]);
        //fecha
        reg.agregaFecha(lineaReg[6]);
        //para inventario
        String inv,nro_a,ufi ;
        try {inv = lineaReg[0] ;} catch (Exception e) {
            inv = "ERROR DE CODIGO DE BARRAS" ;}
        //numero original
        try {
            if (!lineaReg[1].equals("")) {
                nro_a = lineaReg[1] ;
            } else {
                nro_a = "["+lineaReg[0]+"]" ;
            }
            
        } catch (Exception e) {
            nro_a = "ERROR DE NUMERO ORIGINAL" ;}
        //ubicacion fisica
        try {ufi = lineaReg[8] ;} catch (Exception e) {
            ufi = "ERROR DE CODIGO DE UBICACION FISICA" ;}
        reg.agregaItems(inv, nro_a, ufi);
        //titulo
        reg.agregaTitulos(nro_a+". "+lineaReg[5],RegistrosParaAleph.fechaFormateada(lineaReg[6]));
        reg.sumaTiras(1);
    }
    
    private void escribeArchivoSalida(StringBuilder sb,String carpeta){
        try {
            boolean mkdirs = new File(folder+"/"+carpeta+"/").mkdirs();
        } catch (Exception e) {
            System.out.println(e);
        }
        try ( OutputStreamWriter char_output = new OutputStreamWriter(
                    new FileOutputStream(folder+"/"+carpeta+"/_"+outputFile),
                    Charset.forName("UTF-8").newEncoder() 
                );) {
            outputFile++ ;
            final int aLength = sb.length();
            final int aChunk = 1024;// 1 kb buffer to read data from 
            final char[] aChars = new char[aChunk];            

            for (int aPosStart = 0; aPosStart < aLength; aPosStart += aChunk) {
                final int aPosEnd = Math.min(aPosStart + aChunk, aLength);
                sb.getChars(aPosStart, aPosEnd, aChars, 0); // Create no new buffer
                char_output.write(aChars, 0, aPosEnd - aPosStart);// This is faster than just copying one byte at the time
                }
            char_output.flush();            
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    private void salida(){        
        DirectoryChooser dir = new DirectoryChooser() ;
        dir.setTitle("Elegir carpeta para guardar los archivos");
        folder = dir.showDialog(new Stage()) ;
        if (folder != null) {
            folder.getAbsolutePath();
        }
    }

    private void renombraArchivos() throws IOException {
        //grupos de registros
        try {
            File grupales = new File(folder+"/GRUPOS") ;
            File[] listGrupos = grupales.listFiles();
            outputFile = 0 ;
            for (File archivoGrupo : listGrupos) {
                renameFile(archivoGrupo, Integer.toString(outputFile));
                outputFile++ ;
            }        
        } catch (Exception e) {
            //no hay grupos
        }
        //registros individuales        
        try {
            File individuales = new File(folder+"/IND") ;
            File[] listInd = individuales.listFiles();
            outputFile = 0 ;
            for (File archivoInd : listInd) {
                renameFile(archivoInd, Integer.toString(outputFile));
                outputFile++ ;
            }
        } catch (Exception e) {
            //no hay individuales
        }
        
    }
    public static void renameFile(File toBeRenamed, String new_name)
        throws IOException {
        //need to be in the same path
        File fileWithNewName = new File(toBeRenamed.getParent(), new_name);
        if (fileWithNewName.exists()) {
            throw new IOException("file exists");
        }
        // Rename file (or directory)
        boolean success = toBeRenamed.renameTo(fileWithNewName);
        if (!success) {
            // File was not successfully renamed
        }
    }

    private boolean revisaGrupo(List<String[]> sobres, String titulo) {
        Alert muestra = new Alert(Alert.AlertType.CONFIRMATION) ;
        
        // Obtener la lista de botones predeterminados
        ButtonType buttonTypeOK = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        ButtonType separar = new ButtonType("Separar selecc.", ButtonBar.ButtonData.LEFT);
        
        muestra.getButtonTypes().setAll(separar, buttonTypeOK, buttonTypeCancel);
        muestra.setTitle("Verificar conjunto");
        muestra.setHeaderText(titulo+"\n"+
                "Para quitar el sobre hacerle doble click");
        ListView listaSobres = new ListView();
        listaSobres.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sobres.forEach((sobre) -> {
            listaSobres.getItems().add(sobre[5]) ;
        });
        listaSobres.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2) {
                Object selectedItem = listaSobres.getSelectionModel().getSelectedItem();
                try {
                    sobres.stream().filter((sobre) -> (sobre[5].equals(selectedItem))).forEachOrdered((sobre) -> {
                        ind.addArrayRegistro(sobre);
                        sobres.remove(sobre) ;
                        listaSobres.getItems().remove(selectedItem) ;
                    });
                } catch (Exception e) {
                    //array vacio
                }                
            }
        });        
        Button separarButton = (Button) muestra.getDialogPane().lookupButton(separar);
        separarButton.setOnAction(event -> {
            System.out.println("Se ha pulsado el botón personalizado.");
            ObservableList<Integer> selectedIndices = listaSobres.getSelectionModel().getSelectedIndices();
            List<Integer> indicesToRemove = new ArrayList<>(selectedIndices);

            indicesToRemove.forEach(index -> {
                Object item = listaSobres.getItems().get(index);
                System.out.println(item);

                try {
                    sobres.stream()
                            .filter(sobre -> sobre[5].equals(item))
                            .forEach(sobre -> {
                                ind.addArrayRegistro(sobre);
                                sobres.remove(sobre);
                            });
                } catch (Exception e) {
                    // Array vacío
                }
            });

            listaSobres.getItems().removeAll(selectedIndices);
        });
        
        muestra.getDialogPane().setContent(listaSobres);
        Optional<ButtonType> result = muestra.showAndWait() ;
        return result.isPresent();
    }

    public String posibleUnificacion(String titulo) {
        return getUni().verificaTitulo(titulo) ;
    }

    public boolean verificaUnificacion(String titulo) {
        return getUni().posibleUnificacion(titulo) ;
    }
    
    /**
     * @return the regIndi
     */
    public List<String[]> getRegIndi() {
        return regIndi;
    }

    /**
     * @return the regGrupos
     */
    public List<RegistroGrupal> getRegGrupos() {
        return regGrupos;
    }
    
    public Unificador getUni() {
        return uni;
    }

    List<String[]> getRegIND() {
        String consultaInd = "SELECT * FROM inventario WHERE barcode IN " 
                + "(SELECT barcode FROM conjuntos WHERE titulo LIKE 'IND')"
                + " and barcode NOT IN (SELECT barcode FROM items)" ;
        regIndi =  cron.consultaCompleta(consultaInd);
        return regIndi ;
    }
   
}
