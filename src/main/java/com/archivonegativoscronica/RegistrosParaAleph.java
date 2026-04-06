/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import vistas.Path;

/**
 *
 * @author francisco.ortiz
 */
public class RegistrosParaAleph {
    private BorderPane pane ;
    private ScrollPane cont ;
    private File inputFile ;
    private int count,outputFile ;
    private StringBuilder sb ;
    private BufferedReader br_inicio ;
    private Stream<String> regs ;
    private File folder ;
    private String numGrupo ;
    private RegistroGrupal reg ;
    private List<String> inventarios ;
    

    public RegistrosParaAleph() throws IOException {
        
        //recibe como entrada un archivo csv
        //separado por tabulaciones
        archivoEntrada();
        
        //verifica el archivo de entrada
        if (primeraLinea()) {
            System.out.println("Archivo correcto");
            //crea la pestaña de trabajo
            generaArchivoSalida();
        }
    }
    
    public RegistrosParaAleph(SingleSelectionModel<Tab> selectionModel, TabPane pestagnas,int num) throws IOException {
        
        //recibe como entrada un archivo csv
        //separado por tabulaciones
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Elija el archivo con los inventarios", ButtonType.OK) ;
        a.showAndWait() ;
        archivoEntrada();        
        br_inicio = new BufferedReader(new FileReader(inputFile));
        regs = br_inicio.lines() ;
        inventarios = new ArrayList<>() ;
        regs.forEach((t) -> {
            inventarios.add(t) ;
        });
        
        //carga el archivo y genera un lista de items
        a = new Alert(Alert.AlertType.INFORMATION, "Elija el archivo con los registros cargados", ButtonType.OK) ;
        a.showAndWait() ;
        Path path = new Path() ;
        String archivo = path.getPath() ;
        File i = new File(archivo);
        Registro r = new Registro(i) ;
        List<Registro> arrayRegs = r.getArrayRegs();
        arrayRegs.forEach((t) -> {
            List<Item> items = t.items;
            items.forEach((it) -> {
                String barcode = it.getBarcode();
                if (inventarios.contains(barcode)) {
                    //nada
                } else {
                    System.out.println(barcode);
                }
            });
        });
        System.out.println("Llegó al final");
    }
    
    private void generaArchivoSalida() throws FileNotFoundException{
        
        sb = new StringBuilder() ;
        br_inicio = new BufferedReader(new FileReader(inputFile));
        regs = br_inicio.lines() ;
        outputFile = 1 ;
        //selecciona el tipo de programa
        tipoPrograma();
    }
        
    private void escribeArchivoSalida(StringBuilder sb){
        try ( OutputStreamWriter char_output = new OutputStreamWriter(
                    new FileOutputStream(folder+"/"+outputFile),
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
        }
    }
    
    private void archivoEntrada(){
        Path path = new Path() ;
        String archivo = path.getPath() ;
        inputFile = new File(archivo) ;
    }
    
    private boolean primeraLinea() throws FileNotFoundException, IOException {
        //verifica la extension del archivo
        //si es incorrecta vuelve al Path
        if (verificaExtension(inputFile)) {
            //busca y almacena el primer sys
            br_inicio = new BufferedReader(new FileReader(inputFile));
            String primeraLinea = br_inicio.readLine() ;
            //verifica el separador
            if (verificaTabulaciones(primeraLinea)) {
                return true ;
            } else {                
                mensajeErrorDelimitadores();
                archivoEntrada();
            }
        //si es incorecto vuelve a empezar
        } else {            
            mensajeErrorDeArchivo();
            archivoEntrada();
        }
        return false ;
    }
    
    private void mensajeErrorDeArchivo() {
        Alert msg = new Alert(Alert.AlertType.ERROR) ;
        String text = "El archivo no tiene la extensión apropiada.\n"
                + "Por favor seleccione un archivo .csv" ;
        msg.setContentText(text);
        msg.showAndWait();
    }
    
    private void mensajeErrorDelimitadores() {
        Alert msg = new Alert(Alert.AlertType.ERROR) ;
        String text = "El archivo no esta delimitado de manera adecuada.\n"
                + "Por favor seleccione un archivo .csv delimitado por tabulaciones" ;
        msg.setContentText(text);
        msg.showAndWait();
    }
    
    private boolean verificaTabulaciones(String linea){
        return linea.contains("\t") ;
    }
    
    private boolean verificaExtension(File archivo){
        //String ext = FilenameUtils.getExtension(archivo.getPath());
        //return "csv".equals(ext);
        return true;
    }
    
    private String[] getColumnas(String linea) {
        return linea.split("\t") ;
    }

    private void tipoPrograma() {
        
        GridPane grid = new GridPane();        
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));        
        
        DialogPane d = new DialogPane() ;
        d.setContent(grid);
        Alert a = new Alert(Alert.AlertType.INFORMATION) ;
        a.setDialogPane(d);
        a.setGraphic(null);
        a.setTitle("Archivo de negativos Crónica.\n"
                + "Archivos para importar a ALEPH");
        
        Text scenetitle = new Text("ALEPH - Crónica");        
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));        
        grid.add(scenetitle, 0, 0, 2, 1);
        
        Button digiGrupo = new Button("Registros grupales");
        grid.add(digiGrupo, 0, 1);
        digiGrupo.setOnAction((event) -> {
            a.hide();
            salida() ;
            numGrupo = "1" ;
            try {
                reg = new RegistroGrupal(new Unificador(new Funciones())) ;
            } catch (SQLException | IOException | InterruptedException ex) {
                Logger.getLogger(RegistrosParaAleph.class.getName()).log(Level.SEVERE, null, ex);
            }
            regs.forEach((t) -> {            
                String[] lineaReg = getColumnas(t);
                if (!"".equals(lineaReg[0])) {
                    creaRegistrosGrupales(lineaReg) ;
                }
            });
            reg.escribeSalida() ;
            escribeArchivoSalida(sb) ;
            verificaGruposCreados() ;
        });
        
        Button digiInd = new Button("Registros individuales");
        grid.add(digiInd, 1, 1);
        digiInd.setOnAction((event) -> {
            a.hide();
            salida() ;
            regs.forEach((t) -> {            
                String[] lineaReg = getColumnas(t);
                if (lineaReg.length == 15) {
                    creaRegistrosInd(lineaReg) ;
                    count++;
                    if (count % 10 == 0) {
                        escribeArchivoSalida(sb);
                        sb = new StringBuilder() ;
                    }                    
                } else {
                    errorDeCargaDeInventario(lineaReg[0]) ;
                }
            });
            if (sb.length() != 0) {
                escribeArchivoSalida(sb);
            }
            verificaArchivosCreados() ;
        });
        
        Button digiRefe = new Button("Registros vacios/referencias");
        grid.add(digiRefe, 2, 1);
        digiRefe.setOnAction((event) -> {
            a.hide();
            salida() ;
            numGrupo = "1" ;
            try {
                reg = new RegistroGrupal(new Unificador(new Funciones())) ;
            } catch (SQLException | IOException | InterruptedException ex) {
                Logger.getLogger(RegistrosParaAleph.class.getName()).log(Level.SEVERE, null, ex);
            }
            regs.forEach((t) -> {            
                String[] lineaReg = getColumnas(t);
                if (!"".equals(lineaReg[0])) {
                    creaRegistrosRefe(lineaReg) ;
                }
            });
            reg.escribeSalida() ;
            escribeArchivoSalida(sb) ;
            verificaGruposCreados() ;
        });
        
        a.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        a.show() ;        
    }

    private void creaRegistrosGrupales(String[] lineaReg) {        
        try {
            String grupoNum = lineaReg[16] ;
            if (!grupoNum.equals(numGrupo)) {            
                reg.escribeSalida() ;
                escribeArchivoSalida(sb) ;
                count++;
                reg = new RegistroGrupal(new Unificador(new Funciones())) ;
                numGrupo = lineaReg[16] ;
            }
            cargaGrupo(lineaReg);
        } catch (Exception e) {
            System.out.println("Algo falló en el registro: ");
            System.out.println(Arrays.toString(lineaReg));
        }
        
    }
    
    private void creaRegistrosRefe(String[] lineaReg) {        
        try {
            String grupoNum = lineaReg[16] ;
            if (!grupoNum.equals(numGrupo)) {            
                reg.escribeSalida() ;
                escribeArchivoSalida(sb) ;
                count++;
                reg = new RegistroGrupal(new Unificador(new Funciones())) ;
                numGrupo = lineaReg[16] ;
            }            
            cargaRefe(lineaReg);
        } catch (Exception e) {
            System.out.println("Algo falló en el registro: ");
            System.out.println(Arrays.toString(lineaReg));
        }
        
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
            String fechaFormateada = fechaFormateada(lineaReg[6]);
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
        if (!"".equals(lineaReg[4])) {
            sb.append(sys) ;
            sb.append(" 500   L $$aFotógrafo: ") ;
            sb.append(lineaReg[4]) ;
            sb.append("\n") ;
        }
        
        if (lineaReg[11].equals("si")) {
            //506
            sb.append(sys) ;
            sb.append(" 5061  L $$aDisponible solo en formato digital.") ;
            sb.append("\n") ;

            //530
            sb.append(sys) ;
            sb.append(" 530   L $$aDisponible en formato digital solo para consulta en sala (BNA_") ;
            sb.append(lineaReg[0]) ;
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
        try {sb.append(lineaReg[9]) ;} 
        catch (Exception e) {sb.append("ERROR DE UBICACION FISICA") ;}
        sb.append("\n") ;
    }
    
    private void salida(){        
        DirectoryChooser dir = new DirectoryChooser() ;
        dir.setTitle("Elegir carpeta para guardar los archivos");
        folder = dir.showDialog(new Stage()) ;
        if (folder != null) {
            folder.getAbsolutePath();
        }
    }

    private void verificaArchivosCreados() {
        File[] fs = folder.listFiles();
        int verif ;
        if (count % 10 == 0) {
            verif = count/10 ;
        } else {
            verif = (count/10)+1 ;
        }
        if (fs.length == verif) {
            escribeArchivoSalida(sb);
            Alert msg = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Archivos creado con éxito!", ButtonType.OK) ;
            msg.setGraphic(null);
            msg.setHeaderText(null);
            msg.showAndWait() ;
        }
    }
    
    private void verificaGruposCreados() {
        File[] fs = folder.listFiles();
        if (fs.length == count+1) {
            //escribeArchivoSalida(sb);
            Alert msg = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Archivos creado con éxito!", ButtonType.OK) ;
            msg.setGraphic(null);
            msg.setHeaderText(null);
            msg.showAndWait() ;
        }
    }

    private void cargaGrupo(String[] lineaReg) {
        //autor
        reg.agregaAutores(lineaReg[4]);
        //digital
        if (lineaReg[11].equals("si")) {
            reg.agregaDigitales(lineaReg[0]);
        }        
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
        try {ufi = lineaReg[9] ;} catch (Exception e) {
            ufi = "ERROR DE CODIGO DE UBICACION FISICA" ;}
        reg.agregaItems(inv, nro_a, ufi);
        //titulo
        reg.agregaTitulos(nro_a+". "+lineaReg[5],fechaFormateada(lineaReg[6]));
        reg.sumaTiras(1);
    }
    
    private void cargaRefe(String[] lineaReg) {
        //autor
        reg.agregaAutores(lineaReg[4]);
        //digital
        if (lineaReg[11].equals("si")) {
            reg.agregaDigitales(lineaReg[0]);
        }        
        //fecha
        reg.agregaFecha(lineaReg[6]);
        //para inventario
        String inv,nro_a,ufi ;
        try {inv = lineaReg[0] ;} catch (Exception e) {
            inv = "ERROR DE CODIGO DE BARRAS" ;}
        //numero original
        try {nro_a = lineaReg[1] ;} catch (Exception e) {
            nro_a = "ERROR DE NUMERO ORIGINAL" ;}
        //ubicacion fisica
        try {ufi = lineaReg[9] ;} catch (Exception e) {
            ufi = "ERROR DE CODIGO DE UBICACION FISICA" ;}
        reg.agregaItems(inv, nro_a, ufi);
        //titulo
        reg.agregaTitulos(lineaReg[1]+". "+lineaReg[5],fechaFormateada(lineaReg[6])+" ("+lineaReg[10]+")");
        reg.sumaTiras(1);
    }

    private void errorDeCargaDeInventario(String string) {
        String msg = "Error en el inventario "+string ;
        Alert error = new Alert(Alert.AlertType.ERROR, msg, ButtonType.CLOSE) ;
        error.showAndWait() ;
    }

    public static String fechaFormateada(String fecha) {
        String fechaFormateada = "" ;
        try {
            switch(fecha.length()){
                case 8 :
                    String dia = fecha.substring(6,8) ;
                    String mes = fecha.substring(4,6) ;
                    String anio = fecha.substring(0,4) ;
                    if (!"0000".equals(anio)) {
                        fechaFormateada = anio ;
                    }
                    if (!"00".equals(mes)) {
                        fechaFormateada = mes + "/" + fechaFormateada ;
                    }                
                    if (!"00".equals(dia)) {
                        fechaFormateada = dia + "/" + fechaFormateada ;
                    }                
                    break ;
                default :
                    break;
            }
        } catch (Exception e) {
            //NullPointerException
        }
        
        return fechaFormateada ;
    }
}
