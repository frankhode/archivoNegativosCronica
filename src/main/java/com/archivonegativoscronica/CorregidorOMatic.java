/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
/**
 *
 * @author francisco.ortiz
 */
class CorregidorOMatic {
    TextField cat,fecha ;
    Funciones cron ;
    int cont ;
    List<Registro> regs ;
    Label cants ;
    TextArea content ;
    ScrollPane vistaReg ;
    Keyboard key ;
    boolean enInventario,bool,col ;
    String sysUnificador,barcodeData="",nroAData="",nroNIDData="",nroANMData="",autorData="",
                tituloData="",dateData="",obsData="",ufiData="",catDato="",fechaDato="",consulta ;
    ListView<String> listReg ;
    Button corregir,unificar,verificarUnificacion ;
    Stage primaryStage ;
    List<String[]> digitales ;
    
    CorregidorOMatic(Funciones cron, boolean bool) {
        //para poder usar las funciones desde afuera
        this.cron = cron ;
        unificar = new Button ("Unificar") ;        
    }

    CorregidorOMatic(Funciones cron) throws AWTException {
        this.cron = cron ;
        bool = true ;
        key = new Keyboard(new Robot()) ;        
        
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Ingresar cat y período");
        alert.setHeaderText(null);
        alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        DialogPane dialogPane = alert.getDialogPane();

        // Create TextFields
        cat = new TextField();        
        cat.setPromptText("Nombre");
        fecha = new TextField();        
        fecha.setPromptText("Periodo YYYYMMDD");

        // Create a GridPane layout for the TextFields
        GridPane grid = new GridPane();         
        grid.add(cat, 0, 0);
        grid.add(fecha, 1, 0);

        dialogPane.setContent(grid);

        alert.showAndWait().ifPresent(result -> {
            catDato = cat.getText() ;
            fechaDato = fecha.getText() ;

            if (catDato.isEmpty() || fechaDato.isEmpty()) {
                Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                validationAlert.setTitle("Error");
                validationAlert.setHeaderText("Both fields are mandatory.");
                validationAlert.showAndWait();
            } else {
                // Handle the user's input here
                cargaRegistros(catDato,fechaDato) ;
                panelCorregidor() ;                
            }
        });
    }
    
    CorregidorOMatic(Funciones cron, String tipo) throws AWTException {
        this.cron = cron ;
        bool = false ;
        key = new Keyboard(new Robot()) ;
        col = false ;
        
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Ingresar UFI");
        alert.setHeaderText(null);
        alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        DialogPane dialogPane = alert.getDialogPane();
        CheckBox color = new CheckBox("Positivo color") ;
        
        // Create TextFields
        cat = new TextField();        
        cat.setPromptText("Cajón");
        alert.setOnShown(evt -> cat.requestFocus()) ;
        // Create a GridPane layout for the TextFields
        GridPane grid = new GridPane();         
        grid.add(cat, 0, 0);
        grid.add(new Separator(), 1, 0);
        grid.add(color, 2, 0);

        dialogPane.setContent(grid);

        alert.showAndWait().ifPresent(result -> {
            catDato = cat.getText() ;

            if (catDato.isEmpty()) {
                Alert validationAlert = new Alert(Alert.AlertType.ERROR);
                validationAlert.setTitle("Error");
                validationAlert.setHeaderText("Ingrese la UFI");
                validationAlert.showAndWait();
            } else {
                // Handle the user's input here
                if (color.isSelected()) {
                    col = true ;
                }
                cargaRegistros(catDato) ;
                panelCorregidor() ;                
            }
        });
    }

    private void cargaRegistros(String cat, String fecha) {
        consulta = "SELECT registro FROM registros" ;
        List<String> consulta1 = cron.consultaSimple(consulta, 1);
        cont = 0 ;
        regs = new ArrayList<>() ;
        consulta1.forEach((r) -> {
            Registro reg = new Registro(r) ;
            String primeraIntervencion = reg.getPrimeraIntervencion();
            String primerCat = reg.getPrimerCatalogador() ;
            String ultimoCatalogador = reg.getUltimoCatalogador();
            if (primerCat.equals(cat) && verificaFecha(primeraIntervencion,fecha)) {
                //if (!reg.intervenidoPor("FORTIZ")) {
                if (!ultimoCatalogador.equals("FORTIZ")) {
                    if (reg.getMaterias().isEmpty()) {
                        cont ++ ;
                        regs.add(reg) ;
                    }
                }
            }
        });        
        System.out.println("Registros de "+cat+" para el periodo "+fecha+": "+cont);
    }
    
    private void cargaRegistros(String catDato) {
        consulta = "SELECT registro FROM registros" ;
        List<String> consulta1 = cron.consultaSimple(consulta, 1);
        cont = 0 ;
        regs = new ArrayList<>() ;
        consulta1.forEach((r) -> {
            Registro reg = new Registro(r) ;            
            String ultimoCatalogador = reg.getUltimoCatalogador();
            if (verificaUfi(catDato,reg.getItems())) {
                //if (!reg.intervenidoPor("FORTIZ")) {
                if (!ultimoCatalogador.equals("FORTIZ")) {
                    cont ++ ;
                    regs.add(reg) ;
                }
            }
        });
        HashMap hm = new HashMap() ;
        regs.forEach((reg) -> {
            String nroA = reg.getItems().get(0).getDescripcion() ;
            hm.put(nroA, reg) ;
        });
        // Convertir el HashMap a una lista de entradas
        List<Map.Entry<String, Registro>> list = new ArrayList<>(hm.entrySet());

        // Ordenar la lista por claves
        Collections.sort(list, (
                Map.Entry<String, Registro> o1, 
                Map.Entry<String, Registro> o2) -> o1.getKey().compareTo(o2.getKey()));

        // Crear un LinkedHashMap para mantener el orden de las entradas
        LinkedHashMap<String, Registro> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Registro> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        regs = new ArrayList<>() ;
        // Imprimir el mapa ordenado
        for (Map.Entry<String, Registro> entry : sortedMap.entrySet()) {
            //System.out.println(entry.getKey() + ": " + entry.getValue());
            regs.add(entry.getValue()) ;
        }
    }

    public boolean verificaFecha(String ultimaIntervencion, String fecha) {
    // Obtiene los primeros 4 caracteres (año) de ambas cadenas.
    String añoUltimaIntervencion = ultimaIntervencion.substring(0, 4);
    String añoFecha = fecha.substring(0, 4);
    
    // Si el año es 0000 -> todos
    if (añoFecha.equals("0000")) {
        return true ;
    }

    // Compara los años.
    if (!añoUltimaIntervencion.equals(añoFecha)) {
        return false; // Los años no son iguales.
    }

    // Obtiene los siguientes 2 caracteres (mes) de ambas cadenas.
    String mesUltimaIntervencion = ultimaIntervencion.substring(4, 6);
    String mesFecha = fecha.substring(4, 6);

    // Si el mes en "fecha" es "00", entonces no importa el mes en "ultimaIntervencion".
    if (!mesFecha.equals("00") && !mesUltimaIntervencion.equals(mesFecha)) {
        return false; // Los meses no son iguales.
    }

    // Obtiene los últimos 2 caracteres (día) de ambas cadenas.
    String diaUltimaIntervencion = ultimaIntervencion.substring(6, 8);
    String diaFecha = fecha.substring(6, 8);

    // Si el día en "fecha" es "00", entonces no importa el día en "ultimaIntervencion".
    if (!diaFecha.equals("00") && !diaUltimaIntervencion.equals(diaFecha)) {
        return false; // Los días no son iguales.
    }

    // Si llegamos hasta aquí, los años coinciden y, en caso de que haya un "00" en mes o día en "fecha", no se requiere que coincida en "ultimaIntervencion".
    return true;
}

    private void panelCorregidor() {
        //para vueltas siguientes
        try {
            primaryStage.close();
        } catch (Exception e) {
            //es la primera vez que se instancia
        }
        cont = 0 ;
        BorderPane root = new BorderPane() ;
        content = new TextArea(regs.get(cont).getRegistro());        
        content.setWrapText(true);        
        vistaReg = new ScrollPane(content) ;
        vistaReg.setFitToWidth(true);
        vistaReg.setFitToHeight(true);
        root.setCenter(vistaReg);
        
        cants = new Label("Registro "+ cont +" de "+regs.size()) ;
        root.setTop(cants);
        
        HBox botonera = new HBox() ;        
        Button ok = new Button("Ok!") ;
        Button miv = new Button("Revisar") ;
        Button siguiente = new Button("Siguiente reg.") ;
        Button anterior = new Button("Anterior reg.") ;
        botonera.getChildren().addAll(anterior,ok,miv,siguiente) ;
        
        ok.setOnAction((ActionEvent event) -> {            
            siguiente.fire() ;
        });
        
        miv.setOnAction((ActionEvent event) -> {            
            try {
                mandaAlInventario() ;
            } catch (InterruptedException ex) {
                Logger.getLogger(CorregidorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        siguiente.setOnAction((ActionEvent event) -> {
            cont++ ;
            actualizaVistaReg() ;            
        });
        
        anterior.setOnAction((ActionEvent event) -> {
            cont-- ;
            actualizaVistaReg() ;            
        });
        
        root.setBottom(botonera) ;
        
        // Creamos una escena
        Scene scene = new Scene(root, 800, 500);        
        primaryStage = new Stage() ;
        KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
        KeyCombination arriba = new KeyCodeCombination(KeyCode.PAGE_DOWN, KeyCombination.CONTROL_DOWN);
        KeyCombination abajo = new KeyCodeCombination(KeyCode.PAGE_UP, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrU = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(event -> {
            if (enter.match(event)) {
                miv.fire();
            }
            if (abajo.match(event)) {
                anterior.fire() ;
            }
            if (arriba.match(event)) {
                siguiente.fire() ;
            }
        });
        
        // Configuramos la ventana principal        
        primaryStage.setTitle("Corregidor-O-Matic");
        primaryStage.setScene(scene);
        
        // Mostramos la ventana principal
        primaryStage.show();
    }    

    private void mandaAlInventario() throws InterruptedException {
        
        //toma los datos para cargar en el inventario
        String sys = regs.get(cont).getSys() ;
        String inv = regs.get(cont).getItems().get(0).getBarcode() ;
        
        enInventario = verificaEnInventario(inv) ;
        if (enInventario) {
            consulta = "SELECT * FROM inventario WHERE barcode LIKE '"+inv+"'" ;
            List<String[]> data = cron.consultaCompleta(consulta);
            barcodeData = data.get(0)[0] ;
            nroAData = data.get(0)[1] ;
            nroNIDData = data.get(0)[2] ;
            nroANMData = data.get(0)[3];
            autorData = data.get(0)[4] ;
            tituloData = data.get(0)[5] ;
            dateData = data.get(0)[6] ;
            obsData = data.get(0)[7] ;
            ufiData = data.get(0)[8] ;
        } else {
            barcodeData = regs.get(cont).getItems().get(0).getBarcode() ;
            nroAData = regs.get(cont).getItems().get(0).getDescripcion() ;
            tituloData = regs.get(cont).getTituloFormateadoSinFecha() ;
            dateData = regs.get(cont).getFechaISO() ;
            autorData = regs.get(cont).get500Fotografo() ;
            //borra la fecha del titulo
            tituloData = tituloData.replace(", "+dateData, "") ;
            ufiData = regs.get(cont).getItems().get(0).getUfi() ;
        }
        //barcode
        TextField barcode = new TextField(barcodeData);
        barcode.setPrefWidth(400);
        Label barcodeLabel = new Label("Barcode");
        //nroA
        TextField nroA = new TextField(nroAData);
        nroA.setPrefWidth(400);
        Label nroALabel = new Label("NroA");
        //nroNID
        TextField nroNID = new TextField(nroNIDData);
        nroNID.setPrefWidth(400);
        Label nroNidLabel = new Label("NroNID");
        //nroANM
        TextField nroANM = new TextField(nroANMData);
        nroANM.setPrefWidth(400);
        Label nroANMLabel = new Label("NroANM");
        //Autor
        TextField autor = new TextField(autorData);
        autor.setPrefWidth(400);
        Label autorLabel = new Label("Autor");
        //Titulo
        TextField titulo = new TextField(tituloData);
        titulo.setPrefWidth(400);
        Label tituloLabel = new Label("Titulo");
        //Fecha
        TextField date = new TextField(dateData);
        date.setPrefWidth(400);
        Label dateLabel = new Label("Fecha");
        //Observaciones
        TextField obs = new TextField(obsData);
        obs.setPrefWidth(400);
        Label obsLabel = new Label("Observaciones");
        //ufi
        TextField ufi = new TextField(ufiData);
        Label ufiLabel = new Label("Ufi");
        
        GridPane grid = new GridPane();    
        grid.add(barcode, 1, 0);
        grid.add(nroA, 1,1 );
        grid.add(nroNID, 1,2);
        grid.add(nroANM, 1,3);
        grid.add(autor, 1,4);
        grid.add(titulo, 1,5);
        grid.add(date, 1,6);
        grid.add(obs, 1,7);
        grid.add(ufi, 1,8);
        
        grid.add(barcodeLabel, 0, 0);
        grid.add(nroALabel, 0,1 );
        grid.add(nroNidLabel, 0,2);
        grid.add(nroANMLabel, 0,3);
        grid.add(autorLabel, 0,4);
        grid.add(tituloLabel, 0,5);
        grid.add(dateLabel, 0,6);
        grid.add(obsLabel, 0,7);
        grid.add(ufiLabel, 0,8);
        
        Stage dialogStage = new Stage();
        dialogStage.initOwner(new Stage());
        dialogStage.setTitle("Mi Diálogo");
        
        VBox dialogLayout = new VBox(10);        
        dialogLayout.setPadding(new Insets(20));
        HBox botonera = new HBox() ;
        dialogLayout.getChildren().addAll(grid, botonera);
        Scene dialogScene = new Scene(dialogLayout, 250, 150);
        
        // Establecer la ubicación personalizada
        corregir = new Button ("Corregir") ;
        unificar = new Button ("Unificar") ;
        verificarUnificacion = new Button ("Verificar unif.") ;
        Button verDigi = new Button("Ver digital");
        verDigi.setDisable(true);
        if (getDigi(barcodeData)) {
            verDigi.setDisable(false);
            verDigi.setOnAction((event) -> {
                cron.ejecutarEnSegundoPlano(
                    () -> {
                        digitales = cron.getDigitales(barcodeData);
                        return null;
                    },
                    () -> {
                        cron.abreConjunto(digitales);
                    },
                    e -> {
                        System.out.println(e.getMessage());
                    }
                );
            });
        }
        unificar.setDisable(true);
        Button cerrar = new Button("Cerrar") ;
        botonera.getChildren().addAll(corregir,unificar,verDigi,
                verificarUnificacion,cerrar) ;
        
        sysUnificador = "" ;
        corregir.setOnAction((t) -> {
            try {
                dialogStage.close();
                corrigeRegistro(autor.getText(),titulo.getText(),date.getText(),sys) ;
                procesaInventario(barcode.getText(),nroA.getText(),nroNID.getText(),
                        nroANM.getText(),autor.getText(),titulo.getText(),date.getText(),
                        obs.getText(),ufi.getText(),sys) ;
                actualizaLaBase(barcode.getText());
            } catch (InterruptedException | SQLException ex) {
                Logger.getLogger(CorregidorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        unificar.setOnAction((t) -> {
            try {
                //unifica
                dialogStage.close();
                procesaInventario(barcode.getText(),nroA.getText(),nroNID.getText(),
                        nroANM.getText(),autor.getText(),titulo.getText(),date.getText(),
                        obs.getText(),ufi.getText(),sys) ;
                unifica(barcode.getText(),sysUnificador) ;
                actualizaLaBase(barcode.getText());
            } catch (InterruptedException | SQLException ex) {
                Logger.getLogger(CorregidorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                borrarRegistro(sys, barcodeData);
            } catch (InterruptedException | SQLException ex) {
                Logger.getLogger(CorregidorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        verificarUnificacion.setOnAction((t) -> {
            //cuadro para verificar unificacion
            verificador() ;
        });
        
        cerrar.setOnAction((t) -> {
            dialogStage.close();
        });        
        dialogStage.setScene(dialogScene);
        dialogStage.setWidth(600);
        dialogStage.setHeight(600);
        dialogStage.show();
        KeyCombination altU = new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN);
        KeyCombination altC = new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN);
        KeyCombination altV = new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN);
        dialogScene.setOnKeyPressed(event -> {
            if (altU.match(event)) {
                unificar.fire() ;
            }
            if (altC.match(event)) {
                corregir.fire();
            }
            if (altV.match(event)) {
                verificarUnificacion.fire() ;
            }
        });
    }

    private void actualizaVistaReg() {
        //cants = new Label("Registro "+ cont +" de "+regs.size()) ;
        cants.setText("Registro "+ cont +" de "+regs.size());
        content = new TextArea(regs.get(cont).getRegistro());
        vistaReg.setContent(content);
    }
    
    private void procesaInventario(String barcode,String nroA,String nroNID,
            String nroANM,String autor,String titulo,String date,String obs,String ufi, String sys) throws InterruptedException {
        //verifica si ya existe en el inventario
        if(enInventario){
            //si existe actualiza con los datos
            consulta = "UPDATE inventario SET "
                    + "barcode='"+barcode+"',"
                    + "nroA='"+nroA+"',"
                    + "nroNid='"+nroNID+"',"
                    + "nroAnm='"+nroANM+"',"
                    + "autor='"+autor+"',"
                    + "titulo='"+titulo+"',"
                    + "fechaISO='"+date+"',"
                    + "observaciones='"+obs+"',"
                    + "ufi='"+ufi+"' "
                    + "WHERE barcode LIKE '"+barcode+"'" ;
            cron.consultaSimple(consulta,1) ;
            System.out.println(consulta);
        } else {
            //si no esta lo carga
            consulta = "INSERT INTO inventario(barcode, nroA, nroNid, nroAnm, autor, "
                    + "titulo, fechaISO, observaciones, ufi) VALUES ("+
                    "'"+barcode+"','"+nroA+"','"+nroNID+"','"+nroANM+"','"+autor+"',"
                    + "'"+titulo+"','"+date+"','"+obs+"','"+ufi+"')" ;
            cron.consultaSimple(consulta,1) ;
            System.out.println(consulta);
        }        
        
    }

    private void actualizaLaBase(String barcode) throws SQLException {
        consulta = "SELECT sys FROM items WHERE barcode LIKE'"+barcode+"'" ;
        if (!cron.consultaSimple(consulta, 1).isEmpty()) {
            String sys = cron.consultaSimple(consulta, 1).get(0) ;
            //borra item
            consulta = "DELETE FROM items WHERE barcode LIKE '"+barcode+"'" ;
            System.out.println(consulta);            
            cron.consultaSimple(consulta, 1) ;
            //borra registro
            consulta = "DELETE FROM registros WHERE sys LIKE '"+sys+"'" ;
            System.out.println(consulta);            
            cron.consultaSimple(consulta, 1) ;
        }
        cron.conn.close();
        cron.conectarMySQL() ;
        if (bool) { //programa por catalogador
            cargaRegistros(catDato,fechaDato) ;
        } else {
            //programa por ufi
            cargaRegistros(catDato) ;
        }
        
        panelCorregidor() ;        
    }

    private boolean verificaEnInventario(String barcode) {
        consulta = "SELECT * FROM inventario WHERE barcode LIKE '"+barcode+"'" ;
        return !cron.consultaSimple(consulta, 1).isEmpty() ;
    }
    
    public HBox regExistentes() {
        // Creamos un TextField y un ListView
        TextField textField = new TextField();
        textField.setPrefWidth(200);
        listReg = new ListView<>();

        // Configuramos el ListView
        listReg.setPrefSize(600, 400);
        
        // Agregamos los componentes al contenedor programas
        HBox cont = new HBox(new Label("Reg. exist:"), textField, listReg) ;
        cont.setPadding(new Insets(20));
        cont.setPrefWidth(800);

        // Configuramos el TextField para que muestre las sugerencias
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Obtenemos los registros de la base de datos que coincidan con la búsqueda
            List<String> registros;
            registros = obtenerRegistrosDeLaBaseDeDatos(newValue);
            // Actualizamos el ListView con los registros obtenidos
            listReg.getItems().clear();
            listReg.getItems().addAll(registros);
        });
        return cont ;
    }

    public void verificador() {
        Alert ver = new Alert(Alert.AlertType.CONFIRMATION) ;
        HBox regExistentes = regExistentes();
        ver.getDialogPane().setContent(regExistentes);
        //ver.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CLOSE);
        
        Optional<ButtonType> result = ver.showAndWait();        
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                String tit = listReg.getSelectionModel().getSelectedItem().replace("'", "''") ;
                consulta = "SELECT sys FROM registros WHERE titulo245 LIKE '"+tit+"'" ;                
                List<String> lista = cron.consultaSimple(consulta, 1) ;
                sysUnificador = lista.get(0) ;
                unificar.setDisable(false);
                try {
                    verificarUnificacion.setDisable(true);
                } catch (Exception e) {
                }
            }  else if (result.get() == ButtonType.CLOSE) {
                //cierra
                ver.close();
            }
        }
    }
    
    private void borrarRegistro(String sys, String barcode) throws InterruptedException, SQLException {
        //borra el registro en Aleph
        key.enfocaAleph();
        key.getRobot().delay(500);
        key.borraRegistroAleph(sys) ;
        Alert msg = new Alert(Alert.AlertType.CONFIRMATION) ;
        msg.setGraphic(null);
        msg.setHeaderText("Borrar ítem?");
        Optional<ButtonType> result = msg.showAndWait() ;
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                key.enfocaAleph();
                key.getRobot().delay(500);                
                key.borraItem(barcode);
                key.getRobot().delay(500);
                actualizaLaBase(barcode) ;
            } else if (result.get() == ButtonType.CANCEL) {
                //nada
            }
        }
    }

    private void corrigeRegistro(String autor, String titulo, String date, String sys) throws InterruptedException {
        key.enfocaAleph();
        key.getRobot().delay(500);
        key.abreBibliografico(sys);
        key.getRobot().delay(500);
        key.irAlFinalDelRegistro();
        key.getRobot().delay(500);
        key.borraCampos();
        key.getRobot().delay(500);        
        key.expandePlantilla();
        key.getRobot().delay(500);
        if (col) {
            key.actualiza007("c");
            key.directType(KeyEvent.VK_DOWN) ;
            key.getRobot().delay(500);
        }
        key.getRobot().delay(500);
        key.irAlFinalDelRegistro();
        key.getRobot().delay(500);
        key.carga008(date);
        key.getRobot().delay(500);
        key.cargaCampo260(date);
        key.cargaCampo500IndAut(autor);
        key.cargaTitulo(titulo, date);
        key.getRobot().delay(500);
        key.expandePlantilla("ind");
        key.getRobot().delay(500);
        key.reemplazaCampo655(col);
        key.reemplazaCampo300(col);
    }
    
    public List<String> obtenerRegistrosDeLaBaseDeDatos(String busqueda) {
        List<String> listaRegistros = new ArrayList<>();
        consulta = "SELECT titulo245 FROM registros WHERE titulo245 LIKE '%"+busqueda+"%';" ;
        List<String> registros = cron.consultaSimple(consulta, 1);
        registros.stream().filter((registro) -> (
                registro.toLowerCase().contains(busqueda.toLowerCase()))).forEachOrdered((registro) -> {
            listaRegistros.add(registro);
        });
        return listaRegistros;
    }

    public void unifica(String barcode, String sys) {
        consulta = "INSERT INTO conjuntos(titulo, barcode, status) "
                        + "VALUES ('"+sys+"','"+barcode+"','2')" ;
        cron.consultaSimple(consulta, 1) ;        
    }

    private boolean verificaUfi(String ufi, List<Item> items) {
        boolean bool = false ;
        for (Item item : items) {
            if (item.getUfi().equals(ufi)) {
                bool = true ;
            }
        }
        return bool ;
    }
    
    private boolean getDigi(String barcode) {        
        consulta = "SELECT inv FROM digitales WHERE inv ='" + barcode + "'";
        return !cron.consultaSimple(consulta, 1).isEmpty();
    } 
}
