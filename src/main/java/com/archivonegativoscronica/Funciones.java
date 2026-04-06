/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author francisco.ortiz
 */
public final class Funciones  {
    
    //para la conexion
    public String hostname,port,url,username,password,database,comienzo ;
    public Connection conn = null;
    private List<String> lista, terminosParaEditar,inventarios ;
    private int cant_consultas,cant_con,contador ;
    private final BorderPane pane ;
    public final TabPane tabPane ;
    public PreparedStatement stmt ;
    File folder, inputFile ;
    int cargados,totales;
    private Usuario user ;
    boolean conectarA ;
    public Stage loadingStage;
    private Stage stage;
    
    public Funciones() throws SQLException, IOException, InterruptedException {        
        conn = conectarMySQL() ;
        
        //crea el tabPane
        tabPane = new TabPane() ;
        
        pane = new BorderPane() ;
        
        //digitalesFolder();
        folder = new File("U:\\Mapo-Cronica\\004-ordenados_DMFC") ;        
    }
    
    public List<String> generaLista() throws SQLException {
        String consulta = "SELECT termino FROM terminos ORDER BY termino" ;
        consultaSimple(consulta, 1) ;
        return consultaSimple(consulta, 1) ;
    }
    
    public Connection conectarMySQL() throws SQLException {
        /*Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conección");
        alert.setHeaderText("Seleccione una opción:");

        ButtonType buttonTypeLocal = new ButtonType("Conexión local");
        ButtonType buttonTypeVps = new ButtonType("Conexión VPS");

        alert.getButtonTypes().setAll(buttonTypeLocal, buttonTypeVps);
        
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeLocal) {
                hostname = "localhost";
                port = "3306";
                username = "root";
                password = "";
                database = "archivocronica" ;
                url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + 
                        "?useSSL=false";
                conectarA = true ;
            } else if (buttonType == buttonTypeVps) {
                hostname = "62.72.7.162";
                port = "3306";        
                username = "root";
                password = "Megachango10";        
                database = "archivocronica" ;
                url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + 
                        "?useSSL=false&allowPublicKeyRetrieval=true";
                conectarA = false ;
            }
        });*/
        hostname = "localhost";
        port = "3306";
        username = "root";
        password = "";
        database = "archivocronica" ;
        url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + 
                "?useSSL=false";
        conectarA = true ;
        url = "jdbc:mysql://localhost:3306/archivocronica?useUnicode=true"
                + "&characterEncoding=UTF-8&characterSetResults=UTF-8"
                + "&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";

        conn = DriverManager.getConnection(url, username, password);
        
        BackupBases bkp = new BackupBases(this) ;        
        return conn;
    }
    
    public void creaBaseyTablas() {
        Map<String,String> consultas = new HashMap<>();
        consultas.put("01 - BASE DE DATOS archivoCronica",
                "CREATE DATABASE archivoCronica CHARACTER SET utf8 COLLATE utf8_general_ci;") ;
        //tablas
        consultas.put("02 - TABLE Terminos",
            "CREATE TABLE archivoCronica.terminos (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, "
            + "termino varchar(100)) CHARACTER SET utf8 COLLATE utf8_general_ci");
        consultas.put("03 - TABLE Terminos",
            "ALTER TABLE archivoCronica.terminos ADD UNIQUE (termino);");
        consultas.put("04 - TABLE Relaciones",
            "CREATE TABLE archivoCronica.relaciones (id1 varchar(9), "
            + "relacion varchar(100), id2 varchar(9)) CHARACTER SET utf8 COLLATE utf8_general_ci");
        consultas.put("05 - TABLE registros",
            "CREATE TABLE archivoCronica.registros(sys varchar(9), "
            + "registro mediumtext,titulo245 varchar(500)) CHARACTER SET utf8 COLLATE utf8_general_ci");
        consultas.put("06 - TABLE items",
            "CREATE TABLE archivoCronica.items(sys varchar(9), dato varchar(500), "
            + "barcode varchar(100), ufi varchar(500), nroA varchar(500)) CHARACTER SET utf8 COLLATE utf8_general_ci");
        consultas.put("07 - TABLE Materias",
                "CREATE TABLE archivoCronica.materias (sys varchar(9), "
                + "campo varchar(3), materia varchar(1000), "
                + "linea varchar(1000)) CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("08 - TABLE titulos",
                "CREATE TABLE archivoCronica.titulos (sys varchar(9), "
                + "titulo varchar(1000), nroA varchar(100), barcode varchar(8), ufi varchar(100),"
                + "fecha varchar(8)) CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("09 - TABLE areas",
                "CREATE TABLE archivoCronica.areas (sys varchar(9), "
                + "area varchar(7)) CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("10 - TABLE MapaAreas",
                "CREATE TABLE archivoCronica.mapaAreas (cod varchar(7), "
                + "ingles varchar(100), espaniol varchar(100)) CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("11 - TABLE digitales",
                "CREATE TABLE archivoCronica.digitales ("
                        + "nombramiento varchar(50),inv varchar(8),cajon varchar(100),carpeta varchar(100)) "
                        + "CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("12 - TABLE inventario",
                "CREATE TABLE archivoCronica.inventario (barcode varchar(8) PRIMARY KEY,"
                        + "nroA varchar(100),nroNid varchar(100),nroAnm varchar(100),"
                        + "autor varchar(100),titulo varchar(250),"
                        + "fechaISO varchar(8),observaciones varchar(100),ufi varchar(100)) "
                        + "CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        consultas.put("13 - TABLE edicionImpresa",
                "CREATE TABLE archivoCronica.edicionImpresa (barcode varchar(50) PRIMARY KEY,"
                        + "fechaIso varchar(8),dia varchar(2),mes varchar(2),"
                        + "anio varchar(4),ed varchar(1), pag varchar(10), folder varchar(500)) "
                        + "CHARACTER SET utf8 COLLATE utf8_general_ci") ;
        /*niveles 1. ADM;2. CAT1, 3. CAT2, 4.INV, 5. USUARIO*/
        consultas.put("14 - TABLE usuarios",
                "CREATE TABLE archivoCronica.usuarios (`id` int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                        + "nombre varchar(50),nivel varchar(1),rol varchar(1), pass varchar(50)"
                        + ")CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        /*status 1. Pendiente, 0. */
        consultas.put("15 - TABLE conjuntos",
                "CREATE TABLE archivoCronica.conjuntos (idConj varchar(50) PRIMARY KEY,"
                        + "titulo varchar(100),barcode varchar(8),status varchar(1)"
                        + ")CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("16 - TABLE intervenciones",
                "CREATE TABLE archivoCronica.intervenciones (idUsuario varchar(50),"
                        + "idProceso varchar(100),barcode varchar(8),fecha varchar(14)"
                        + ")CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("17 - TABLE colecciones",
                "CREATE TABLE archivoCronica.colecciones (nombramiento varchar(250),"
                        + "coleccion varchar(250))CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("18 - TABLE indizImagenes",
                "CREATE TABLE archivoCronica.indizImagenes (nombramiento varchar(250),"
                        + "materia varchar(250), personaEnImagen VARCHAR(500),"
                        + "lugarEnImagen VARCHAR(500),objetoEnImagen VARCHAR(500),"
                        + "eventoEnImagen VARCHAR(500),institucionEnImagen VARCHAR(500)"
                        + ")CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("19 - TABLE descriptoresImagenes",
                "CREATE TABLE archivoCronica.descriptoresImagenes (nombramiento varchar(250),"
                        + "descriptor varchar(250))CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("20 - TABLE vistoImagenes",
                "CREATE TABLE archivoCronica.vistoImagenes (nombramiento varchar(250),"
                        + "vistoPor varchar(250), vistoFecha varchar(14))CHARACTER SET utf8 COLLATE utf8_general_ci ;") ;
        consultas.put("21 - TABLE conserDigi",
                "CREATE TABLE archivoCronica.conservacionDigi "
                        + "(fecha varchar(100),agente varchar(100),barcode varchar(8),"
                        + "estadodelsobre varchar(1),deterioros varchar(250),"
                        + "observaciones varchar(500),formato varchar(10),"
                        + "polaridad varchar(100),proceso varchar(100),"
                        + "cantidadtiras int(100),marca varchar(100),"
                        + "modelo varchar(100),cantidadfotogramas int(100)) "
                        + "CHARACTER SET utf8 COLLATE utf8_general_ci") ;

        //indices
        consultas.put("22 - INDEX Terminos -> Termino",
                "CREATE INDEX ter ON archivoCronica.terminos (termino)") ;
        consultas.put("23 - INDEX Termino -> id",
                "CREATE INDEX id ON archivoCronica.terminos (id)") ;
        consultas.put("24 - INDEX Relaciones -> Termino1",
                "CREATE INDEX rel1 ON archivoCronica.relaciones (id1)") ;
        consultas.put("25 - INDEX Relaciones -> Termino2",
                "CREATE INDEX rel2 ON archivoCronica.relaciones (id2)") ;
        consultas.put("26 - INDEX Relaciones -> Tipo de relacion",
                "CREATE INDEX tit ON archivoCronica.relaciones (relacion)") ;
        consultas.put("27 - INDEX BarCodes-> Códigos de barra",
                "CREATE INDEX bar ON archivoCronica.items (barcode)") ;
        consultas.put("28 - INDEX NroCronica-> Número original",
                "CREATE INDEX a ON archivoCronica.items (nroA)") ;
        consultas.put("29 - INDEX Materias -> Materia",
                "CREATE INDEX mat ON archivoCronica.materias (materia)") ;
        consultas.put("30 - INDEX Materias -> Sys",
                "CREATE INDEX mat_sys ON archivoCronica.materias (sys)") ;
        consultas.put("31 - INDEX Titulo -> titulo",
                "CREATE INDEX tit ON archivoCronica.titulos (titulo)") ;
        consultas.put("32 - INDEX Titulo -> nroA",
                "CREATE INDEX na ON archivoCronica.titulos (nroA)") ;
        consultas.put("33 - INDEX Titulo -> barcode",
                "CREATE INDEX tit_bar ON archivoCronica.titulos (barcode)") ;
        consultas.put("34 - INDEX Titulo -> ufi",
                "CREATE INDEX tit_ufi ON archivoCronica.titulos (ufi)") ;
        consultas.put("35 - INDEX Titulo -> fecha",
                "CREATE INDEX f ON archivoCronica.titulos (fecha)") ;
        consultas.put("36 - INDEX Areas -> area",
                "CREATE INDEX a ON archivoCronica.areas (area)") ;
        consultas.put("37 - INDEX MapaAreas -> codigo",
                "CREATE INDEX c ON archivoCronica.mapaAreas (cod)") ;
        consultas.put("38 - INDEX MapaAreas -> ingles",
                "CREATE INDEX i ON archivoCronica.mapaAreas (ingles)") ;        
        consultas.put("39 - INDEX MapaAreas -> espaniol",
                "CREATE INDEX e ON archivoCronica.mapaAreas (espaniol)") ;        
        consultas.put("40 - INDEX digitales -> inv",
                "CREATE INDEX cod ON archivoCronica.digitales (inv)") ;
        consultas.put("41 - INDEX inventario -> inv",
                "CREATE INDEX inv ON archivoCronica.inventario (barcode)") ;
        consultas.put("42 - INDEX edicionImpresa -> inv",
                "CREATE INDEX nomb ON archivoCronica.edicionImpresa (barcode)") ;
        consultas.put("43 - INDEX edicionImpresa -> fechaIso",
                "CREATE INDEX fiso ON archivoCronica.edicionImpresa (fechaIso)") ;
        consultas.put("44 - INDEX edicionImpresa -> ed",
                "CREATE INDEX ed ON archivoCronica.edicionImpresa (ed)") ;
        consultas.put("45 - INDEX registros -> tit245",
                "CREATE INDEX tit245 ON archivoCronica.registros (titulo245)") ;
        
        Statement st ;
        try {
            //conn = conectarMySQL() ;
            st = conn.createStatement();
            SortedSet<String> keys = new TreeSet<>(consultas.keySet());
            keys.forEach((key) -> {
                String value = consultas.get(key);                                
                try {
                    st.executeUpdate(value);                    
                } catch (SQLException ex) {
                    // handle any errors
                    mensajeSalida("SQLException: " + ex.getMessage() +"\n"
                                + "SQLState: " + ex.getSQLState() +"\n"
                                + "VendorError: " + ex.getErrorCode() +"\n") ;                    
                }
            });
            st.close();
            conn.close();
            MapaAreas mapa = new MapaAreas(conn) ;
            if (mapa.resultado()) {
                mensajeSalida("Base de datos creada con éxito!\n");
            } else {
                mensajeSalida("Base de datos creada con éxito!\n"
                        + "Falló al crear el mapa de áreas...");
            }
            
        } catch (SQLException ex) {
            // handle any errors
            mensajeSalida("SQLException: " + ex.getMessage() +"\n" 
                    + "SQLState: " + ex.getSQLState() +"\n" 
                    + "VendorError: " + ex.getErrorCode() +"\n") ;
        }
    }
    
    public void borraBaseyTablas() {
        if (envia("DROP DATABASE archivoCronica")) {
            mensajeSalida("Base de datos borrada con éxito!");
        }
    }    
    
    public void editorDeRelaciones(int tipo) throws SQLException {
        String titulo = "" ;
        switch(tipo){
            case 1:
                titulo = "Términos sin relaciones" ;
                break ;
            case 2:
                titulo = "Términos huérfanos" ;
                break ;
            case 3:
                titulo = "Términos relacionados" ;
                break ;
        }     
                
        Tab tab = new Tab(titulo) ;
        this.shortCutTab(tab);
        tabPane.getTabs().add(tab) ;
        tabPane.getSelectionModel().select(tab);
        
        BorderPane bp = new BorderPane() ;        
        tab.setContent(bp);
        
        //genera la lista de terminos completos y el combo
        generaLista() ;
        
        //crea una tabla de 4 col
        TableView table = new TableView();
        table.setStyle("-fx-font-size: 16;");
        
        TableColumn col1 = new TableColumn("Término");        
        TableColumn col2 = new TableColumn("Tipo de relación");
        TableColumn col3 = new TableColumn("Término 2");
        //TableColumn col4 = new TableColumn("Actualizar");
        TableColumn col5 = new TableColumn("Agregar padre");
        //TableColumn col6 = new TableColumn("Actualizar padre");
        TableColumn col7 = new TableColumn("Agregar termino raiz");
        //TableColumn col8 = new TableColumn("Actualizar termino raiz");
        col1.prefWidthProperty().bind(table.widthProperty().multiply(0.36));
        col2.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        col3.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        col5.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        col7.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        
        //table.getColumns().addAll(col1,col2,col3,col4,col5,col6,col7,col8) ;
        table.getColumns().addAll(col1,col2,col3,col5,col7) ;
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
       
        col1.setCellValueFactory(new PropertyValueFactory<>("termino"));
        col2.setCellValueFactory(new PropertyValueFactory<>("relaciones"));
        col3.setCellValueFactory(new PropertyValueFactory<>("textfield"));
        //col4.setCellValueFactory(new PropertyValueFactory<>("actualizar"));
        col5.setCellValueFactory(new PropertyValueFactory<>("padre"));
        //col6.setCellValueFactory(new PropertyValueFactory<>("agregarPadre"));
        col7.setCellValueFactory(new PropertyValueFactory<>("raiz"));
        //col8.setCellValueFactory(new PropertyValueFactory<>("agregarRaiz"));
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        terminosParaEditar.forEach((t) -> {
            TextField cb1 = creaDesplegableRelaciones() ;
            TextField textfield = new TextField() ;
            TextField padre = new TextField() ;
            TextField raiz = new TextField() ;
            try {            
                //TextFields.bindAutoCompletion(textfield,generaLista());
                AutoCompleteHelper.bindAutoCompletion(textfield, generaLista());
            } catch (SQLException ex) {
                Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Button borra = new Button("Borrar linea") ;
            Button agregar = new Button("Agregar") ;
            Button agregarPadre = new Button("Agregar") ;
            Button agregarRaiz = new Button("Agregar") ;
            
            agregar.setOnAction((event) -> {
                String trel = cb1.getText() ;
                String term = textfield.getText() ;
                String pad = padre.getText() ;
                try {
                    if (agregaRelacion(t,trel,term)) {
                        data.forEach((row) -> {
                            if (row.getTermino().equals(t)) {
                                Platform.runLater(() -> {
                                    data.remove(row) ;                                    
                                });                    
                            }
                        });   
                    } else {
                        mensajeSalida("Algo falló");
                    }
                } catch (SQLException ex) {
                    mensajeSalida("Algo falló...\n"+ex.getMessage());
                }
            });
            
            agregarPadre.setOnAction((event) -> {
                String pad = padre.getText() ;                
                try {
                    envia("INSERT IGNORE INTO terminos (termino) VALUES ('"+pad+"')") ;
                    if (agregaRelacion(t,"esHijoDe",pad)) {
                        data.forEach((row) -> {
                            if (row.getPadre().getText().equals(pad)) {
                                Platform.runLater(() -> {
                                    data.remove(row) ;                                    
                                });                    
                            }
                        });                        
                    } else {
                        mensajeSalida("Algo falló");
                    }
                    
                } catch (SQLException ex) {
                    mensajeSalida("Algo falló...\n"+ex.getMessage());
                }
            });
            
            agregarRaiz.setOnAction((event) -> {
                String r = raiz.getText() ;                
                try {
                    envia("INSERT IGNORE INTO terminos (termino) VALUES ('"+r+"')") ;
                    if (agregaRelacion(r,"tieneComoGentilicioA",t)) {
                        data.forEach((row) -> {
                            if (row.getRaiz().getText().equals(r)) {
                                Platform.runLater(() -> {
                                    data.remove(row) ;                                    
                                });                    
                            }
                        });                        
                    } else {
                        mensajeSalida("Algo falló");
                    }
                    
                } catch (SQLException ex) {
                    mensajeSalida("Algo falló...\n"+ex.getMessage());
                }
            });
            data.add(new Tabla(t,cb1,textfield,agregar,padre,agregarPadre,raiz,agregarRaiz)) ;
        });
        
        table.setItems(data);
        
        Button borra = new Button("Borrar seleccionados") ;
        borra.setStyle("-fx-font-size: 16;");
        borra.setOnAction(e -> {
            ObservableList<Tabla> selectedRows = table.getSelectionModel().getSelectedItems();
            // we don't want to iterate on same collection on with we remove items
            ArrayList<Tabla> rows = new ArrayList<>(selectedRows);
            rows.forEach(row -> table.getItems().remove(row));
        });
        
        //cierra la escena y carga de nuevo con las modificaciones
        Button actualizar = new Button("Actualizar lista") ;
        actualizar.setStyle("-fx-font-size: 16;");
        actualizar.setOnAction((event) -> {
            try {                
                switch(tipo){
                    case 1:
                        closeTab(tab) ;
                        editorInicial() ;
                        break ;
                    case 2:
                        closeTab(tab) ;
                        editorDeHuerfanos() ;
                        break ;
                    case 3:
                        closeTab(tab) ;
                        editorRelacionados();
                        break ;
                }                
            } catch (SQLException ex) {
                mensajeSalida("Algo falló...\n"+ex.getMessage());
            }
        });
        
        Button enviar = new Button("Enviar todos los trabajados") ;
        enviar.setStyle("-fx-font-size: 16;");
        enviar.setOnAction((event) -> {
            data.forEach((row) -> {
                String padre = row.getPadre().getText() ;
                String raiz = row.getRaiz().getText() ;
                String term = row.getTextfield().getText() ;
                String termino = row.getTermino() ;
                String rel = row.getRelaciones().getText() ;
                if (!rel.equals("") && !term.equals("")) {
                        try {
                            agregaRelacion(termino,rel,term) ;
                        } catch (SQLException ex) {
                            mensajeSalida("Algo falló...\n"+ex.getMessage());
                        }
                } else {
                    if (!"".equals(padre)) {
                        envia("INSERT IGNORE INTO terminos (termino) VALUES ('"+padre+"')") ;
                        try {
                            agregaRelacion(termino,"esHijoDe",padre) ;
                        } catch (SQLException ex) {
                            mensajeSalida("Algo falló...\n"+ex.getMessage());
                        }
                    }
                    if (!"".equals(raiz)) {
                        envia("INSERT IGNORE INTO terminos (termino) VALUES ('"+raiz+"')") ;
                        try {
                            agregaRelacion(raiz,"tieneComoGentilicioA",termino) ;
                        } catch (SQLException ex) {
                            mensajeSalida("Algo falló...\n"+ex.getMessage());
                        }
                    }
                }
            });            
        switch(tipo){
            case 1:
                try {
                    closeTab(tab) ;
                    editorInicial(); 
                } catch (SQLException ex) {
                    Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
                }
                break ;
            case 2:
                try {
                    closeTab(tab) ;
                    editorDeHuerfanos();
                } catch (SQLException ex) {
                    Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
                }
                break ;
            case 3:
                try {
                    closeTab(tab) ;
                    editorRelacionados();
                } catch (SQLException ex) {
                    Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
                }
                break ;
            }
        });
        HBox hb = new HBox(100,borra,actualizar,enviar) ;
        hb.setPadding(new Insets(15, 15, 15, 15));
        table.setPadding(new Insets(10, 10, 10, 10));
                
        bp.setTop(hb);
        bp.setCenter(table);
    }
    
    private TextField creaDesplegableRelaciones() {
        List<String> options = new ArrayList<>() ;
        options.add("esPadreDe") ;
        options.add("esHijoDe") ;
        options.add("estaRelacionadoCon") ;
        options.add("esGentilicioDe") ;
        options.add("tieneComoGentilicioA") ;
        
        TextField txf = new TextField() ;
        //TextFields.bindAutoCompletion(txf, options) ;
        AutoCompleteHelper.bindAutoCompletion(txf, options);
        return txf ;
    }

    private boolean agregaRelacion(String t1, String relacion, String t2) throws SQLException {
        //original
        String consulta = "INSERT INTO relaciones(id1,relacion,id2) "
            + "VALUES ("
            + "(SELECT id FROM terminos WHERE termino='"+t1+"'),"
            + "'"+relacion+"',"
            + "(SELECT id FROM terminos WHERE termino='"+t2+"'));" ;
        //inverso
        String relacionInversa = null ;
        switch(relacion){
            case "esPadreDe":
                relacionInversa = "esHijoDe" ;
                break;
            case "esHijoDe":
                relacionInversa = "esPadreDe" ;
                break;
            case "estaRelacionadoCon":
                relacionInversa = "estaRelacionadoCon" ;
                break;
            case "esGentilicioDe":
                relacionInversa = "tieneComoGentilicioA" ;
                break;
            case "tieneComoGentilicioA":
                relacionInversa = "esGentilicioDe" ;
                break;
            default:
                break;
        }
        try {
            envia(consulta) ;
            consulta = "INSERT INTO relaciones(id1,relacion,id2) "
            + "VALUES ("
            + "(SELECT id FROM terminos WHERE termino='"+t2+"'),"
            + "'"+relacionInversa+"',"
            + "(SELECT id FROM terminos WHERE termino='"+t1+"'));" ; 
            envia(consulta) ;
            return true ;
        } catch (Exception e) {
            return false ;
        }        
    }

    public boolean envia(String consulta) {
        try {            
            Statement st = conn.createStatement();
            st.execute(consulta) ; 
            return true ;
        } catch (SQLException ex) {
            mensajeSalida("Algo falló....\n" + ex);
            return false ;
        }
    }

    public void enviaMultiple(String consulta, List<String> lista) {
        try {
            Statement st = conn.createStatement();
            st.execute(consulta) ;
            ResultSet rs = st.getResultSet();
            while (rs.next()) {
                lista.add(rs.getString(1)) ;
            }
        } catch (SQLException ex) {
            mensajeSalida(ex.getMessage());
        }
    }

    public void muestraTesauro() {
        Tesauro tesauro = new Tesauro(this) ;
        tesauro.muestraTesauro();    
    }

    private void digitalesFolder() {
        String tx = "Elija la carpeta donde se encuentran los archivos digitales." ;
        Alert a = new Alert(Alert.AlertType.INFORMATION, tx, ButtonType.OK) ;
        a.setGraphic(null);
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("files/icon.png"));
        a.showAndWait() ;
        DirectoryChooser dir = new DirectoryChooser() ;
        dir.setTitle("Elegir carpeta donde se encuentran los digitales");
        folder = dir.showDialog(new Stage());
        if (folder != null) {
            folder.getAbsolutePath();
        }
    }
    
    private File selectFolder() {
        File carpeta = null ;
        String tx = "Elija la carpeta." ;
        Alert a = new Alert(Alert.AlertType.INFORMATION, tx, ButtonType.OK) ;
        a.setGraphic(null);
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("files/icon.png"));
        a.showAndWait() ;
        DirectoryChooser dir = new DirectoryChooser() ;
        dir.setTitle("Elegir carpeta");
        carpeta = dir.showDialog(new Stage());
        if (carpeta != null) {
            return carpeta ;
        } else {
            return carpeta ;
        }
    }
    
    private boolean getHijos(String termino, List<String> hijos) {
        String consulta = ""
                + "SELECT DISTINCT terminos.termino "
                + "FROM terminos JOIN relaciones ON terminos.id=relaciones.id1 "
                + "WHERE ( "
                + "relaciones.relacion LIKE 'esHijoDe' OR "
                + "relaciones.relacion LIKE 'esGentilicioDe' )"
                + "AND relaciones.id2= (SELECT id FROM terminos WHERE termino='"+termino+"') "
                + "ORDER BY terminos.termino" ;
        List<String> hijos1 = new ArrayList<>() ;
        hijos.add(termino) ;
        enviaMultiple(consulta, hijos1);
        hijos1.forEach((hijo) -> {
            hijos.add(hijo) ;
            boolean bool = getHijos(hijo, hijos) ;
        });
        return !hijos.isEmpty();
    }

    public void muestraResultados(TreeItem<Text> tema) throws IOException, SQLException {
        TreeItem<Text> t = (TreeItem<Text>) tema ;
        List<String> arrayTemas = new ArrayList<>() ;
        String value = t.getValue().getText() ;
        if (t.isLeaf()) {
            arrayTemas.add(value) ;
        } else {
            getHijos(value,arrayTemas);
        }
        //primera vista
        //Resultados resultados = new Resultados(conn, arrayTemas, tabPane, folder) ;
        
        Resultados resultados = new Resultados(this, tema.getValue().getText(), tabPane,"tema") ;
    }

    public TabPane getTabPane() {
        return tabPane ;
    }

    public void mensajeSalida(String txt) {
        Platform.runLater(() -> {
           Alert a = new Alert(Alert.AlertType.INFORMATION, txt, ButtonType.OK) ;
            a.setHeaderText(null);
            a.setGraphic(null);
            a.setTitle("Archivo Fotográfico del Diario Crónica");
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("files/icon.png"));
            a.show(); 
        });
    }

    public void editorInicial() throws SQLException {
        String consulta = "SELECT DISTINCT terminos.termino FROM terminos "
                + "WHERE NOT EXISTS (SELECT id1 FROM relaciones "
                + "     WHERE id1 = terminos.id OR id2 = terminos.id) "
                + "ORDER BY terminos.termino; " ;        
        terminosParaEditar = new ArrayList<>() ;
        enviaMultiple(consulta,terminosParaEditar) ;
        editorDeRelaciones(1);
    }

    public void editorDeHuerfanos() throws SQLException {
        //    PARA SELECCIONAR LOS TERMINOS QUE NO TIENEN PADRE Y NO SON GENTILICIOS
        String consulta = "SELECT DISTINCT terminos.termino FROM terminos "
                + "JOIN relaciones ON terminos.id = relaciones.id1 "
                + " WHERE relaciones.id1 NOT IN ("
                + " SELECT id1 FROM relaciones " 
                + " WHERE relacion LIKE 'esHijoDe' OR relacion LIKE 'esGentilicioDe') "
                + " AND terminos.termino NOT LIKE 'Actividades' "
                + " AND terminos.termino NOT LIKE 'Características geográficas físicas' "
                + " AND terminos.termino NOT LIKE 'Comportamiento' "
                + " AND terminos.termino NOT LIKE 'Conceptos' "
                + " AND terminos.termino NOT LIKE 'Condiciones económicas y sociales' "
                + " AND terminos.termino NOT LIKE 'Condiciones físicas' "
                + " AND terminos.termino NOT LIKE 'Estados mentales' "
                + " AND terminos.termino NOT LIKE 'Fenómenos naturales' "
                + " AND terminos.termino NOT LIKE 'Materiales' "
                + " AND terminos.termino NOT LIKE 'Objetos' "
                + " AND terminos.termino NOT LIKE 'Organismos' "
                + " AND terminos.termino NOT LIKE 'Organizaciones' "
                + " AND terminos.termino NOT LIKE 'Partes del cuerpo' "
                + " AND terminos.termino NOT LIKE 'Personas'"
                + " ORDER BY terminos.termino; " ;
        
        terminosParaEditar = new ArrayList<>() ;
        enviaMultiple(consulta,terminosParaEditar) ;   
        //prepara los datos para trabajar
        editorDeRelaciones(2);
    }
    
    public void editorRelacionados() throws SQLException {
        String consulta = "SELECT DISTINCT terminos.termino FROM terminos "
                + "WHERE terminos.id NOT IN (SELECT id1 FROM relaciones "
                + "     WHERE relaciones.relacion LIKE 'esGentilicioDe') "
                + "ORDER BY terminos.termino; " ;        
        terminosParaEditar = new ArrayList<>() ;
        enviaMultiple(consulta,terminosParaEditar) ;
        editorDeRelaciones(3);
    }
    
    private void closeTab(Tab tab) {
        EventHandler<Event> handler = tab.getOnClosed();
        if (null != handler) {
            handler.handle(null);
        } else {
            tab.getTabPane().getTabs().remove(tab);
        }
    }
    
    public void indiceTitulos(int inicio,int cantidad,String comienzo, Tab tab) {
        this.shortCutTab(tab);
        this.comienzo = comienzo ;
        int total = obtieneTotal("titulos") ;
        IndiceDeTitulos index = new IndiceDeTitulos(this,inicio,cantidad,comienzo,total,tab) ;
        tab.setText("Indice de títulos");        
        BorderPane bp = new BorderPane(index.getTable()) ;
        bp.setPadding(new Insets(15, 15, 15, 15));
        
        //tareas de la barra
        BarraDeNavegacion bn = funcionesBarra("titulo",index.getNavegacion(),tab) ;
                
        bp.setTop(bn);
        tab.setContent(bp);
        tabPane.getTabs().add(tab) ;
        tabPane.getSelectionModel().select(tab);
    }

    public void indiceTematico(String campo, int inicio,int cantidad,String comienzo, Tab tab) throws IOException, SQLException {
        this.shortCutTab(tab);
        this.comienzo = comienzo ;
        String titulo = null ;
        switch(campo) {
            case "600":
                titulo = "Indice de personas" ;
                totales = obtieneTotal("personas") ;
                break;
            case "610":
                titulo = "Indice de entidades" ;
                totales = obtieneTotal("entidades") ;
                break;
            case "611":
                titulo = "Indice de congresos, reuniones, etc." ;
                totales = obtieneTotal("congresos") ;
                break;
            case "630":
                titulo = "Indice de títulos" ;
                totales = obtieneTotal("tituloUniforme") ;
                break;
            case "650":
                titulo = "Indice temático" ;
                totales = obtieneTotal("temas") ;
                break;
            case "651":
                titulo = "Indice de lugares" ;
                totales = obtieneTotal("lugares") ;
                break;
            case "043":
                titulo = "Indice de areas geográficas" ;
                break;
        }
        IndiceTematico index = new IndiceTematico(campo,this,folder,inicio,cantidad,comienzo,tabPane,totales) ;
        tab.setText(titulo);
        BorderPane bp = new BorderPane(index.getTable()) ;
        bp.setPadding(new Insets(15, 15, 15, 15));
        
        //tareas de la barra
        BarraDeNavegacion bn = funcionesBarra(campo,index.getNavegacion(),tab) ;
        bn.ocultaComboBox();
        bp.setTop(bn);
        tab.setContent(bp);
        if (!tabPane.getTabs().contains(tab)) {
            tabPane.getTabs().add(tab) ;
        }
        tabPane.getSelectionModel().select(tab);
    }

    public String formateaFecha(String fecha) {
        //borra el punto final si hay
        try {
            if (".".equals(fecha.substring(fecha.length()-1))) {
                fecha = fecha.substring(0,fecha.length()-1) ;
            }
        } catch (Exception e) {
            System.out.println(fecha);
        }
        
        //borra la palabra año u otros textos
        fecha = fecha.replaceAll("[^\\d|^/]", "") ;
        String[] arr = fecha.split("/") ;
                
        switch(fecha.length()){
            case 0:
                fecha = "" ;
                break;
            case 4:
                fecha = fecha + "0000" ;
                break;
            case 7:
                fecha = fecha.substring(3,7) + fecha.substring(0,2) + "00" ;
                break;
            case 10:
                fecha = fecha.substring(6,10) + fecha.substring(3,5) + fecha.substring(0,2) ;
                break;
            default:
                fecha = "" ;
                break;
        }
        return fecha ;
    }

    public void verFichero() {
        Fichas f = new Fichas() ;
        Tab tab = f.getFicheroTab();
        this.shortCutTab(tab);
        tabPane.getTabs().add(tab) ;
        tabPane.getSelectionModel().select(tab);
    }

    public void indiceAreas(int inicio, int cantidad, String comienzo) {
        this.comienzo = comienzo ;
        IndiceDeAreas index = new IndiceDeAreas(this,tabPane,folder) ;
        Tab tab = new Tab("Indice de áreas") ;
        this.shortCutTab(tab);        
        BorderPane bp = new BorderPane(index.getTable()) ;
        bp.setPadding(new Insets(15, 15, 15, 15));
                
        tab.setContent(bp);
        tabPane.getTabs().add(tab) ;
        tabPane.getSelectionModel().select(tab);
    }

    public void cargaDigitales() throws SQLException, IOException {
        // Conecta y prepara el esquema de las consultas
        conn.setAutoCommit(false);        
        stmt = conn.prepareStatement("INSERT INTO digitales(nombramiento,inv,cajon, carpeta) "
                + "VALUES (?,?,?,?);");

        try (Stream<java.nio.file.Path> paths = Files.walk(Paths.get(folder.getPath()))) {
            paths.forEach((jpg) -> {
                try {
                    if (!Files.isDirectory(jpg) 
                            && !jpg.getParent().toString().contains("FICHERO CROAF")
                            && !jpg.getParent().toString().contains("Edicion impresa")
                            && !jpg.getParent().toString().contains("Bajas CMD")) {
                        agregaDigi(jpg.toString()) ;

                        if (cant_consultas == 1000) {
                            int[] results = stmt.executeBatch();
                            conn.commit();
                            cant_con = cant_con + results.length;
                            stmt = conn.prepareStatement("INSERT INTO digitales (nombramiento,inv,cajon,carpeta) VALUES (?,?,?,?);");
                            cant_consultas = 0;
                        }
                        cant_consultas++;
                    }
                } catch (SQLException | RuntimeException e) {
                    // Relanzar cualquier excepción para propagarla
                    throw new RuntimeException("Error en la carga de archivos digitales: " + e.getMessage(), e);
                }
            });

            // Cargar los que faltan
            int[] results = stmt.executeBatch();
            conn.commit();
            mensajeSalida("Tabla digitales cargada con éxito!");

        } catch (IOException | SQLException | RuntimeException ex) {
            // Relanzar excepción para que el Task falle
            throw new RuntimeException("Error en la ejecución de cargaDigitales: " + ex.getMessage(), ex);
        }
    }
    
    public void cargaDigitalesExterno() throws SQLException, IOException {
        // Conecta y prepara el esquema de las consultas
        conn.setAutoCommit(false);        
        stmt = conn.prepareStatement("INSERT INTO digitales(nombramiento,inv,cajon, carpeta) "
                + "VALUES (?,?,?,?);");
        File carpetaRaiz = selectFolder();        
        try (Stream<java.nio.file.Path> paths = Files.walk(Paths.get(carpetaRaiz.getPath()))) {
            paths.forEach((jpg) -> {
                try {
                    if (!Files.isDirectory(jpg)) {
                        agregaDigi(jpg.toString()) ;
                        if (cant_consultas == 1000) {
                            int[] results = stmt.executeBatch();
                            conn.commit();
                            cant_con = cant_con + results.length;
                            stmt = conn.prepareStatement("INSERT INTO digitales (nombramiento,inv,cajon,carpeta) VALUES (?,?,?,?);");
                            cant_consultas = 0;
                        }
                        cant_consultas++;
                    }
                } catch (SQLException | RuntimeException e) {
                    // Relanzar cualquier excepción para propagarla
                    throw new RuntimeException("Error en la carga de archivos digitales: " + e.getMessage(), e);
                }
            });

            // Cargar los que faltan
            int[] results = stmt.executeBatch();
            conn.commit();
            mensajeSalida("Tabla digitales cargada con éxito!");

        } catch (IOException | SQLException | RuntimeException ex) {
            // Relanzar excepción para que el Task falle
            throw new RuntimeException("Error en la ejecución de cargaDigitales: " + ex.getMessage(), ex);
        }
    }


    public void cargaEdImpresa() throws SQLException, IOException {
        conn.setAutoCommit(false);  
        stmt = conn.prepareStatement("INSERT INTO edicionimpresa(barcode, fechaiso, dia, mes, anio, ed, pag, folder) "
                                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

        File carpetaRaiz = selectFolder();
        if (carpetaRaiz == null) {
            return;  // Si no se selecciona carpeta, simplemente sale.
        }

        // Llamar a la función recursiva
        Platform.runLater(() -> {
            try {
                procesarDirectorio(carpetaRaiz, carpetaRaiz.getPath());
            } catch (SQLException ex) {
                Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // Ejecutar cualquier batch pendiente
        ejecutarBatchFinal();

        mensajeSalida("Tabla edicionImpresa cargada con éxito!");
    }

    // Función recursiva para recorrer directorios
    private void procesarDirectorio(File carpeta, String raiz) throws SQLException {
        if (!carpeta.isDirectory()) {
            return;  // No es un directorio, no hay nada que hacer
        }

        String nombreCarpeta = carpeta.getPath().replace(raiz, "").replaceFirst("^/", "");  
        File[] archivos = carpeta.listFiles();

        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    // Llamada recursiva para subdirectorios
                    procesarDirectorio(archivo, raiz);
                } else {
                    // Procesar archivo
                    procesarArchivo(archivo, nombreCarpeta);
                }
            }
        }
    }

    // Función para procesar archivos y añadirlos al batch
    private void procesarArchivo(File file, String carpeta) throws SQLException {
        String archivo = file.getName().replace(".jpg", "");
        String[] exp = archivo.split("_");

        if (exp.length < 5) { // Validar que el nombre tenga las partes necesarias
            System.err.println("Error: Formato de nombre incorrecto en archivo: " + archivo);
            return;
        }

        try {
            stmt.setString(1, archivo);
            stmt.setString(2, exp[2]);
            stmt.setString(3, exp[2].substring(6));   // Día
            stmt.setString(4, exp[2].substring(4, 6)); // Mes
            stmt.setString(5, exp[2].substring(0, 4)); // Año
            stmt.setString(6, exp[3]); // Edición
            stmt.setString(7, exp[4]); // Página
            stmt.setString(8, carpeta); // Carpeta

            stmt.addBatch();
            stmt.clearParameters();

            if (++cant_consultas >= 1000) {
                ejecutarBatch();
            }
        } catch (SQLException e) {
            //mensajeSalida("Error al cargar la tabla edicionImpresa...\n" + e.getMessage());
            System.out.println("* -> "+archivo);
            System.out.println(e.getMessage());
        } catch (StringIndexOutOfBoundsException e) {
            //System.err.println("Error en los índices de substring en archivo: " + archivo);
            System.out.println("* -> "+archivo);
        }
    }

    // Ejecutar batch cuando se llega a 1000 consultas
    private void ejecutarBatch() throws SQLException {
        int[] results = stmt.executeBatch();
        conn.commit();
        cant_con += results.length;
        stmt.clearBatch();  // En lugar de recrear el PreparedStatement, solo limpiamos el batch.
        cant_consultas = 0;
    }

    // Ejecutar el último batch pendiente
    private void ejecutarBatchFinal() throws SQLException {
        if (cant_consultas > 0) {
            ejecutarBatch();
        }
    }


    void vaciaTabla(String tabla) {
        if (envia("TRUNCATE "+tabla)) {
            mensajeSalida("Tabla "+tabla+" vaciada con éxito!");
        } else {
            
        } 
    }

    public List<String> consultaSimple(String consulta,int indice){
        List<String> resultados = new ArrayList<>() ;        
        try {
            stmt = conn.prepareStatement(consulta) ;            
            if (stmt.execute(consulta)) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {   
                    String sys = rs.getString(indice);
                    resultados.add(sys) ;
                }                
            }                
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return resultados ;
    }
    
    public List<String[]> consultaCompleta(String consulta){
        List<String[]> resultados = new ArrayList<>() ;
        //abre cuenta de tiempo
        long startTime = System.nanoTime();
        
        try {
            Statement stmt = conn.createStatement();
            if (stmt.execute(consulta)) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                int colNum = rsmd.getColumnCount();                
                int count = 0 ;
                while (rs.next()) {
                    String[] array = new String[colNum] ;
                    for (int i = 1; i <= colNum; i++) {
                        array[i-1] = rs.getString(i);
                    }
                    resultados.add(array) ;
                    count++;
                }
            }                
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        long endTime = System.nanoTime();
        double duration = ((double)endTime - startTime) / 1000000000;             
        int nro_redondo = (int) duration ;
        if (nro_redondo > 60) {
            nro_redondo = nro_redondo / 60 ;
            System.out.println("Tiempo: "+nro_redondo+" minutos.");
        } else {
            System.out.println("Tiempo: "+nro_redondo+" segundos.");
        }
        
        return resultados ;
    }
    
    private int obtieneTotal(String tabla) {
        String consulta = null ;
        switch(tabla){
            case "titulos":
                consulta = "SELECT COUNT( DISTINCT titulo ) FROM titulos" ;
                break;
            case "personas":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '600'" ;
                break;
            case "entidades":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '610'" ;
                break;
            case "congresos":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '611'" ;
                break;
            case "tituloUniforme":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '630'" ;
                break;
            case "temas":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '650'" ;
                break;
            case "lugares":
                consulta = "SELECT COUNT( DISTINCT materia ) FROM materias WHERE campo = '651'" ;
                break;
            case "inventario":
                consulta = "SELECT COUNT( DISTINCT barcode ) FROM inventario" ;
                break;
        }
        List<String> cantidades = consultaSimple(consulta, 1);
        return Integer.valueOf(cantidades.get(0)) ;
    }

    public String formateaMateria(String linea) {
        String campo = linea.substring(10,13) ;
        String materiaFormateada = "" ;
        switch(campo){
            case "600":
            case "610":
            case "611":
                materiaFormateada = linea.substring(18) ;                
                materiaFormateada = materiaFormateada.replace("$$vFotografías","") ;
                materiaFormateada = materiaFormateada.replaceAll("(\\$\\$x.*)(?=\\$\\$.|\\b)", "") ;
                materiaFormateada = materiaFormateada.replaceAll("\\$\\$.", " ") ;                
                break;
            case "630":
            case "650":
            case "651":
                String[] subcampos = linea.substring(18).split("\\$\\$.") ;
                materiaFormateada = subcampos[1] ;
                break;
            case "655":
                subcampos = linea.substring(18).split("\\$\\$.") ;
                materiaFormateada = subcampos[1] ;
                break;            
        }        
        return materiaFormateada.trim() ;
    }

    private BarraDeNavegacion funcionesBarra(String index, BarraDeNavegacion bn, Tab tab) {
        this.shortCutTab(tab);
        final int ini = bn.getInicio() ;
        final int cant = bn.getCantidad() ;
        
        ComboBox cb = bn.getComboBox() ;
        cb.valueProperty().addListener((obs, oldItem, newItem) -> {
            procesaBN(index,ini,Integer.valueOf(newItem.toString()),comienzo,tab) ;
        });
        
        Button btn = bn.getIni() ;
        btn.setOnAction((event) -> {
            procesaBN(index,0,cant,bn.getComienzo(),tab) ;
        });
        
        btn = bn.getNext() ;
        btn.setOnAction((event) -> {
            procesaBN(index,ini+cant,cant,comienzo,tab) ;
        });
        
        btn = bn.getPrev() ;
        btn.setOnAction((event) -> {
            if (ini-cant > 0) {
                procesaBN(index,ini-cant,cant,comienzo,tab) ;
            } else {
                procesaBN(index,0,cant,comienzo,tab) ;
            }
        });
        
        bn.setPadding(new Insets(15, 15, 15, 15));
        
        return bn ;
    }

    private void procesaBN(String index, int ini, int cant, String comienzo, Tab tab) {        
        this.shortCutTab(tab);
        if (index.equals("titulo")) {
            indiceTitulos (ini, cant,comienzo,tab);
        } else {
            try {
                indiceTematico(index, ini, cant,comienzo,tab) ;                
            } catch (IOException | SQLException ex) {
                System.out.println(ex);
                Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void cargaSobres(List<InventarioPorCajon.Sobre> sobres, Connection con) throws SQLException {
        //conecta y prepara el esquema de las consultas
        conn = con ;
        conn.setAutoCommit(false);        
        stmt = conn.prepareStatement("INSERT IGNORE INTO inventario"
                + "(barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi) "
                + "VALUES (?,?,?,?,?,?,?,?,?);");
        sobres.forEach((sobre) -> {
            totales++;
            try {  
                String inv = sobre.getBarcode() ;
                String nroA = sobre.getNroA();
                String autor = sobre.getFotografo();
                String titulo = sobre.getTitulo();
                String fechaISO = sobre.getFecha();
                String observaciones = sobre.getObservaciones();
                String ufi = sobre.getUfi();
                
                try {
                    //barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi
                    stmt.setString(1,inv);
                    stmt.setString(2,nroA);
                    stmt.setString(3,"");
                    stmt.setString(4,"");
                    stmt.setString(5,autor);
                    stmt.setString(6,titulo);
                    stmt.setString(7,fechaISO);
                    stmt.setString(8,observaciones);
                    stmt.setString(9,ufi);

                    stmt.addBatch();
                    stmt.clearParameters();
                    if (cant_consultas == 1000) {
                        int [] results = stmt.executeBatch();
                        conn.commit();
                        cant_con = cant_con+results.length ;
                        stmt = conn.prepareStatement("INSERT IGNORE INTO inventario"
            + "(barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi) "
            + "VALUES (?,?,?,?,?,?,?,?,?);");
                        cant_consultas = 0 ;
                    }
                    cant_consultas++;
                    cargados++;
                } catch (SQLException e) {                            
                    mensajeSalida("Algo falló al cargar los sobres...\n"+e);
                }                
            } catch (Exception e) {
                System.out.println(sobre.getTitulo());
            }

        });
        try {
            int [] results = stmt.executeBatch();
            conn.commit();
            mensajeSalida("Sobres cargados con éxito!") ;
        } catch (Exception e) {                
            System.out.println(e);
        }
    }

    public List<Usuario> obtenerUsuarios() {
        String consulta = "SELECT * from usuarios" ;
        List<Usuario> usuarios = new ArrayList<>() ;
        List<String[]> consultaCompleta = this.consultaCompleta(consulta);
        consultaCompleta.forEach((t) -> {
           Usuario u = new Usuario() ;
           u.setId(t[0]);
           u.setNombre(t[1]);
           u.setNivel(t[2]);           
           u.setRol(t[3]);
           u.setPass(t[4]);
           usuarios.add(u) ;
        });
        return usuarios ;
    }

    public boolean tieneDigital(Registro reg) {
        String consulta = "SELECT barcode FROM items WHERE sys LIKE '"+reg.getSys()+"'" ;
        List<String> consultaSimple = consultaSimple(consulta, 1);
        for (String barcode : consultaSimple) {
            String con = "SELECT nombramiento FROM digitales WHERE inv LIKE '"+barcode+"'" ;
            if (!consultaSimple(con, 1).isEmpty()) {
                return true ;
            }
        }
        return false ;
    }

    public void muestraSobre(String barcode) throws SQLException {
        Alert a = new Alert(Alert.AlertType.NONE,null,ButtonType.CLOSE);
        a.setResizable(true);
        a.setGraphic(null);
        a.setHeaderText(null);
        a.setTitle(null);
        a.getButtonTypes().clear();                
        a.initStyle(StageStyle.UTILITY);
        Window window = a.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> a.hide());
        BorderPane bp = new BorderPane() ;        
        bp.setCenter(muestraDataSobre(barcode)); 
        ScrollPane sp = new ScrollPane(bp) ;
        a.getDialogPane().setContent(sp) ;
        
        a.showAndWait() ; 
    }

    public boolean verificaDigitalizado(String barcode) {
        String consulta = "SELECT nombramiento FROM digitales WHERE inv LIKE '"+barcode+"'" ;
        return !consultaSimple(consulta, 1).isEmpty() ;
    }

    private VBox muestraDataSobre(String barcode) {
        String consulta = "SELECT * FROM inventario WHERE barcode LIKE '"+barcode+"'" ;
        List<String[]> dataSobre = consultaCompleta(consulta);
        VBox contenedor = new VBox() ;
        if (!dataSobre.isEmpty()) {
            contenedor.getChildren().add(new Label("Barcode: "+dataSobre.get(0)[0])) ;
            contenedor.getChildren().add(new Label("nroA: "+dataSobre.get(0)[1])) ;
            contenedor.getChildren().add(new Label("nroNid: "+dataSobre.get(0)[2])) ;
            contenedor.getChildren().add(new Label("nroAnm: "+dataSobre.get(0)[3])) ;
            contenedor.getChildren().add(new Label("autor: "+dataSobre.get(0)[4])) ;
            contenedor.getChildren().add(new Label("titulo: "+dataSobre.get(0)[5])) ;
            contenedor.getChildren().add(new Label("fechaISO: "+dataSobre.get(0)[6])) ;
            contenedor.getChildren().add(new Label("observaciones: "+dataSobre.get(0)[7])) ;
            contenedor.getChildren().add(new Label("ufi: "+dataSobre.get(0)[8])) ;
        } else {
            contenedor.getChildren().add(new Label("El sobre no esta en la tabla de inventario.")) ;
        }
        return contenedor ;
    }
    
    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }
    
    public void muestraRegistro(Registro reg) throws SQLException {
        Alert a = new Alert(Alert.AlertType.NONE,null,ButtonType.CLOSE);
        a.setResizable(true);
        a.setGraphic(null);
        a.setHeaderText(null);
        a.setTitle(null);
        a.getButtonTypes().clear();                
        a.initStyle(StageStyle.UTILITY);
        Window window = a.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> a.hide());
        BorderPane bp = new BorderPane() ;
        if (tieneDigital(reg)) {            
            List<String> digital = getDigital(reg) ;            
            creaBotoneraDigital(digital,bp) ;
        }
        bp.setCenter(new PanelRegistroVistas(reg).getCont()); 
        ScrollPane sp = new ScrollPane(bp) ;
        a.getDialogPane().setContent(sp) ;
        
        a.showAndWait() ; 
    }
    
    private List<String> getDigital(Registro reg) {
        List<String> digitales = new ArrayList<>() ;
        List<Item> items = reg.items;
        items.forEach((item) -> {
            String barcode = item.getBarcode();
            String consulta = "SELECT DISTINCT nombramiento FROM digitales WHERE (inv ='" + barcode + "') " ;
            System.out.println(consulta);
            try {
                String consultaSimple = consultaSimple(consulta,1).get(0);
                if (consultaSimple != null) {
                    digitales.add(barcode) ;
                }
            } catch (Exception e) {/*no hay digital*/}
            
        });
        return digitales ;
    }
    
    private void creaBotoneraDigital(List<String> digital, BorderPane bp) throws SQLException {
        List<String[]> consultaCompleta;
        String consulta = "SELECT carpeta,nombramiento FROM digitales WHERE (inv ='"  ;
        if (digital.size() > 0) {
            for (String digi : digital) {
                consulta = consulta + digi +"' OR inv='" + digi + "'" ;
            }
        } else {
            consulta = consulta + digital.get(0) +"' " ;
        }        
        consulta = consulta + ") ;" ;
        consultaCompleta = consultaCompleta(consulta) ;
        consultaCompleta.forEach((t) -> {
            t[0] = t[0].replace("\\","\\\\") ;
        });
        
        Button btnDigi = new Button("Ver digital");
        bp.setTop(btnDigi);
        switch(consultaCompleta.size()){
            case 0:
                break;
            case 1 :
                btnDigi.setOnAction((event) -> {
                    try {
                        String arch = folder.getAbsolutePath()+"\\" +
                                consultaCompleta.get(0)[0] + "\\\\" 
                                + consultaCompleta.get(0)[1] ;
                        //System.out.println(arch);
                        openFile(new File(arch));
                    } catch (Exception ex) {
                        //algun error
                    }
                });
                break;
            default:
                btnDigi.setOnAction((event) -> {
                    abreConjunto(consultaCompleta) ;
                }); 
                break;
        }
    }
    
    public void openFile(File file) throws Exception {
        if (Desktop.isDesktopSupported()) {            
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    
                }
            }).start();
        }
    }
    
    public void abreConjunto(List<String[]> consultaSimple) {
        NavegadorDeImagenes ndi = new NavegadorDeImagenes(this,consultaSimple) ;        
    }
    
    public List<String[]> getDigitales(String barcode) throws SQLException {
        String consulta = "SELECT inv, nombramiento, cajon, carpeta FROM digitales WHERE "
                + "(inv ='" + barcode + "') AND carpeta LIKE 'Bajas';";
        List<String[]> consultaCompleta = consultaCompleta(consulta);
        if (consultaCompleta.isEmpty()) {
            generaYEnviaBajas(barcode);
        }
        return consultaCompleta;
    }

    public void enviaRecorte(String pdf, String barcode, ByteArrayInputStream byteArrayInputStream) throws SQLException {
        String consulta = "INSERT INTO recortes (barcode, recortadoDe, image) VALUES (?,?,?)" ;
        conn.setAutoCommit(false);
        stmt = conn.prepareStatement(consulta) ;
        stmt.setString(1, barcode);
        stmt.setString(2, pdf);        
        stmt.setBinaryStream(3, byteArrayInputStream);
        stmt.addBatch();
        stmt.clearParameters();
        int [] results = stmt.executeBatch();
        conn.commit();
    }

    List<byte[]> consultaSimpleBinaria(String consulta, int i) {
        List<byte[]> resultados = new ArrayList<>() ;        
        try {
            stmt = conn.prepareStatement(consulta) ;            
            if (stmt.execute(consulta)) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {   
                    byte[] imageData = rs.getBytes("image");
                    resultados.add(imageData) ;
                }                
            }                
        }
        catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return resultados ;
    }
    
    public void shortCutTab(Tab tab) {
        Scene scene = tabPane.getScene();
        // Definir el método abreviado Ctrl+W
        KeyCombination ctrlW = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlW, () -> {
            tabPane.getTabs().remove(tab);
        });
    }

    public void agregaDigi(String url) throws SQLException {
        String[] split = url.split("\\\\");
        String archivo = split[split.length-1];
        String[] exp = archivo.split("_");
        if (exp.length < 2) {
        System.out.println("Error: Nombre de archivo no contiene suficiente información");
        System.out.println(url);
        }
        String inv = exp[1];
        if (split.length < 3) {
        System.out.println("Error: Ruta de archivo no contiene suficientes directorios");
        }
        if (inv.length() != 8) {
        System.out.println("Error de largo de archivo: "+archivo +
            " -> " + url);
        }
        if (!split[split.length - 4].equals("Altas") || 
                !split[split.length - 4].equals("Bajas")) {
            System.out.println(url);
        }
        stmt.setString(1, archivo);
        stmt.setString(2, inv);
        stmt.setString(3, split[split.length - 3]);
        stmt.setString(4, split[split.length - 4]);                        
        stmt.addBatch();
    }
    
    public void ejecutarEnSegundoPlano(Callable<Void> tarea, Runnable cuandoTermine, Consumer<Throwable> cuandoFalla) {        
        // Crear una tarea que se ejecuta en segundo plano
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                return tarea.call();  // Ejecutar el Callable que puede lanzar excepciones
            }
        };

        // Si la tarea termina exitosamente, actualizar la UI
        task.setOnSucceeded(event -> {
            cuandoTermine.run();  // Ejecuta la acción después de la tarea
        });

        // Si la tarea falla, manejar el error y actualizar la UI
        task.setOnFailed(event -> {
            Throwable ex = task.getException();  // Captura la excepción lanzada
            cuandoFalla.accept(ex);  // Pasa la excepción a la acción de error
        });

        // Ejecutar la tarea en un hilo separado
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    public List<String> generaBajas(List<String[]> altas) throws SQLException {
        List<String> rutasTif = new ArrayList<>() ;
        altas.forEach((alta) -> {
            //consulta = "SELECT inv, nombramiento, cajon, carpeta FROM digitales WHERE "
            String archivo = "U:\\Mapo-Cronica\\004-ordenados_DMFC\\"+
                    alta[3]+File.separator+alta[2]+File.separator+
                    alta[0]+File.separator+alta[1] ;
            rutasTif.add(archivo) ;
            System.out.println(archivo);
        });        
        return ejecutarPythonScript(rutasTif);        
    }
    
    public void mostrarVentanaCarga() {
        Platform.runLater(() -> {
            loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.setTitle("Procesando...");

            Label mensaje = new Label("Generando miniaturas...");
            ProgressIndicator indicador = new ProgressIndicator();        

            VBox layout = new VBox(10, mensaje, indicador);
            layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

            loadingStage.setScene(new Scene(layout));
            loadingStage.setResizable(false);
            loadingStage.show();
        });
    }
    
    public static List<String> ejecutarPythonScript(List<String> rutasTif) {
        List<String> rutasJpg = new ArrayList<>();
        List<String> command = new ArrayList<>();        
        String directorioBase = System.getProperty("user.dir");
        String pythonEmbebido = 
                        directorioBase 
                        + File.separator + "src" 
                        + File.separator + "main" 
                        + File.separator + "resources" 
                        + File.separator + "python" 
                        + File.separator + "python.exe";
                String scriptPython = 
                        directorioBase 
                        + File.separator + "src" 
                        + File.separator + "main" 
                        + File.separator + "resources" 
                        + File.separator + "python" 
                        + File.separator + "convertir.py";
        command.add(pythonEmbebido);
        command.add(scriptPython);  // Ruta del script Python

        // Reemplazar la ruta de entrada por la de salida
        for (String ruta : rutasTif) {
            String rutaSalida = ruta.replace(
                    "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Altas\\",
                    "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Bajas\\"
                    //"C:\\Users\\francisco.ortiz\\Desktop\\bajas\\"
            );
            File outputDir = new File(rutaSalida).getParentFile();
            command.add(ruta);
            command.add(outputDir.getAbsolutePath());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process proceso = pb.start();

            // Leer salida del script
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));                        
            String linea;            
            while ((linea = reader.readLine()) != null) {
                System.out.println(linea);
                rutasJpg.add(linea.replace("OK:", "")) ;
            }

            // Leer errores si ocurren
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
            while ((linea = errorReader.readLine()) != null) {
                System.err.println("Error: " + linea);
            }
            proceso.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al ejecutar el script Python: " + e.getMessage());
        }
        return rutasJpg ;
    }

    public void enviaConsultas() throws SQLException {
        stmt.executeBatch();
        conn.commit();
    }

    void generaYEnviaBajas(String barcode) throws SQLException {
        conn.setAutoCommit(false);
        stmt = conn.prepareStatement("INSERT INTO digitales(nombramiento,inv,cajon, carpeta) "
                + "VALUES (?,?,?,?);");
        String consulta2 = "SELECT inv, nombramiento, cajon, carpeta FROM digitales WHERE "
                + "(inv ='" + barcode + "');";
        List<String> generaBajas = generaBajas(consultaCompleta(consulta2));
                generaBajas.forEach((t) -> {
            try {
                agregaDigi(t);
            } catch (SQLException ex) {
                System.out.println("Algo fallo en la carga de las bajas en mysql "+ex);
            }
        });
        enviaConsultas();
    }

    public void setStage(Stage stage) {
        this.stage = stage ;        
    }
    
    public Stage getStage() {
        return stage;        
    }
    
    public static class Tabla {
        private SimpleStringProperty termino ;
        private TextField textfield,relaciones,padre,raiz ;
        private Button actualizar,agregarPadre,agregarRaiz ;
        
        private Tabla(String termino,TextField relaciones,
                TextField textfield,Button actualizar,
                TextField padre,Button agregarPadre,
                TextField raiz,Button agregarRaiz){
            this.termino = new SimpleStringProperty(termino) ;
            this.relaciones = relaciones ;
            this.textfield = textfield ;
            this.actualizar = actualizar ;
            this.padre = padre ;
            this.agregarPadre = agregarPadre ;
            this.raiz = raiz ;
            this.agregarRaiz = agregarRaiz ;
        }

        public String getTermino() {
            return termino.get();
        }

        public void setTermino(SimpleStringProperty termino) {
            this.termino = termino;
        }

        public TextField getRelaciones() {
            return relaciones;
        }

        public void setRelaciones(TextField relaciones) {
            this.relaciones = relaciones;
        }

        public Button getActualizar() {
            return actualizar;
        }

        public void setActualizar(Button actualizar) {
            this.actualizar = actualizar;
        }

        public TextField getTextfield() {
            return textfield;
        }

        public void setTextfield(TextField textfield) {
            this.textfield = textfield;
        }

        public TextField getPadre() {
            return padre;
        }

        public void setPadre(TextField padre) {
            this.padre = padre;
        }

        public Button getAgregarPadre() {
            return agregarPadre;
        }

        public void setAgregarPadre(Button agregarPadre) {
            this.agregarPadre = agregarPadre;
        }

        public TextField getRaiz() {
            return raiz;
        }

        public void setRaiz(TextField raiz) {
            this.raiz = raiz;
        }

        public Button getAgregarRaiz() {
            return agregarRaiz;
        }

        public void setAgregarRaiz(Button agregarRaiz) {
            this.agregarRaiz = agregarRaiz;
        }
    }
}
