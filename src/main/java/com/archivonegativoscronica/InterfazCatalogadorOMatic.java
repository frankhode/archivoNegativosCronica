/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Comparator;


/**
 *
 * @author francisco.ortiz
 */
class InterfazCatalogadorOMatic {

    
    public int getStatus() {
        return status;
    }
    private final ConjuntosParaAleph regs ;
    Stage ventana ;
    private final int cantRegsInd,cantRegsGrupos;
    private int status,nroProgr,regActual,itemActual,tipoPrograma ;
    private final Button iniciar,detener,siguiente,cierra,progr1,progr2,progr3 ;
    private final Keyboard key ;
    private final VBox pasos;
    private VBox contenedorPasos;
    private final HBox botoneraProcesa;
    private HBox botoneraRegInd;
    boolean running ;
    private Label registroActual ;
    private final RegistrosParaAgregar regsParaAgregar ;
    private final Funciones cron ;
    private double xOffset ;
    private double yOffset ;

    InterfazCatalogadorOMatic(Funciones cron,Keyboard key) throws IOException {
        this.cron = cron ;
        this.key = key ;
        nroProgr = 0 ;
        running = false ;
        regs = new ConjuntosParaAleph(cron, 1, true, true) ;
        regActual = 0 ;
        itemActual = 0 ;
        cantRegsInd = regs.getRegIndi().size() ;
        cantRegsGrupos = regs.getRegGrupos().size() ;
        regsParaAgregar = new RegistrosParaAgregar(cron) ;
        status = 0 ;
        
        BorderPane contenedorGeneral = new BorderPane() ;
        
        //Botonera para tipos de programa
        HBox botonera = new HBox(10) ;
        botonera.setAlignment(Pos.CENTER);
        botonera.setPadding(new Insets(10)); // Agregar un margen de 10 píxeles
        botonera.setSpacing(10);
        progr1 = new Button("Agregar a reg. exist.") ;
        progr2 = new Button("Nuevo individual") ;
        progr3 = new Button("Nuevo grupal") ;
        
        progr1.setOnAction((t) -> {
            nroProgr = 1 ;
            //anula los botones de los otros programas
            progr2.setDisable(true);
            progr3.setDisable(true);            
            try {
                correPrograma1() ;
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });        
        progr2.setOnAction((t) -> {
            try {
                nroProgr = 2 ;
                //anula los botones de los otros programas
                progr1.setDisable(true);
                progr3.setDisable(true);            
                correPrograma2() ;
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });        
        progr3.setOnAction((t) -> {
            try {
                nroProgr = 3 ;
                //anula los botones de los otros programas
                progr1.setDisable(true);
                progr2.setDisable(true) ;
                correPrograma3() ;
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        botonera.getChildren().addAll(progr1,progr2,progr3) ;
        contenedorGeneral.setTop(botonera);
        
        //centro donde muestra los pasos
        pasos = new VBox(30) ;
        pasos.setAlignment(Pos.CENTER);
        //contenedorGeneral.setCenter(new ScrollPane(pasos));
        contenedorGeneral.setCenter(pasos);
        
        //botonera funcionamiento
        botoneraProcesa = new HBox(10) ;
        botoneraProcesa.setAlignment(Pos.CENTER);
        botoneraProcesa.setPadding(new Insets(10)); // Agregar un margen de 10 píxeles
        botoneraProcesa.setSpacing(10);
        iniciar = new Button("Iniciar") ;
        
        detener = new Button("Detener") ;
        detener.setOnAction((t) -> {
            running = false ;
        });
        siguiente = new Button("Siguiente") ;
        cierra = new Button("Cerrar COM") ;
        cierra.setOnAction((t) -> {
            ventana.close();
        });
        cierra.setAlignment(Pos.CENTER_RIGHT);
        botoneraProcesa.getChildren().addAll(iniciar,detener,siguiente,cierra) ;
        contenedorGeneral.setBottom(botoneraProcesa);
        botoneraProcesa.setVisible(false);
        
        // Crear la escena y asignarla al escenario
        ventana = new Stage() ;
        // Agregar un efecto de sombra para simular un borde
        BorderStroke bordeStroke = new BorderStroke(
            Color.BLACK,
            BorderStrokeStyle.SOLID,
            null,
            new javafx.scene.layout.BorderWidths(2)
        );
        Border borde = new Border(bordeStroke);
        contenedorGeneral.setBorder(borde);
        Scene scene = new Scene(contenedorGeneral, 400, 300);
        ventana.setScene(scene);
        
        // Agregar un controlador de eventos de arrastre a la escena
        scene.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            ventana.setX(event.getScreenX() - xOffset);
            ventana.setY(event.getScreenY() - yOffset);
        });
        
        // Configurar la ventana para que siempre esté al frente y no tenga decoraciones
        ventana.initStyle(StageStyle.UNDECORATED);
        ventana.setAlwaysOnTop(true);
        
        // Obtener las dimensiones de la pantalla
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Establecer la posición y tamaño de la ventana
        double anchoVentana = screenBounds.getWidth() / 3;
        double altoVentana = screenBounds.getHeight();
        double xVentana = screenBounds.getMaxX() - anchoVentana;
        double yVentana = 40;
        ventana.setX(xVentana+40);
        ventana.setY(yVentana);
        ventana.setWidth(anchoVentana*0.8);
        ventana.setHeight(altoVentana*0.8);
        
        // Mostrar la ventana
        ventana.show();
    }

    private void pasosPrograma1() {
        String[] pasosProgr1 = new String[7] ;
        pasosProgr1[0] = "0. Cantidad de registros grupales \n"
                + "para agregar: "+regsGrupalesParaAgregar().size() ;
        pasosProgr1[1] = "1. Cantidad de registros individuales \n" 
                + "para agregar: "+regsIndParaAgregar().size() ;
        pasosProgr1[2] = "2. Carga bibliográfico" ;
        pasosProgr1[3] = "5. Cargar ítems" ;
        pasosProgr1[4] = "6. Siguiente registro" ;
                
        contenedorPasos = new VBox() ;
        
        pasos.getChildren().add(contenedorPasos) ;
        for (String paso : pasosProgr1) {
            Label textoPaso = new Label(paso) ;
            textoPaso.setStyle("-fx-font-size: 16px; -fx-font-family: "
                    + "'Segoe UI', Verdana, sans-serif;");            
            contenedorPasos.getChildren().add(textoPaso) ;
        }
    }
    
    private void pasosPrograma3() {
        String[] pasosProgr3 = new String[7] ;
        pasosProgr3[0] = "0. Cantidad de registros grupales \n"
                + "pendientes: "+cantRegsGrupos ;
        //pasosProgr2[1] = "1. Muestra el ALEPH" ;
        pasosProgr3[1] = "1. Tipo de carga" ;
        pasosProgr3[2] = "2. Carga bibliográfico" ;
        pasosProgr3[3] = "5. Cargar ítems" ;
        pasosProgr3[4] = "6. Siguiente registro" ;
                
        contenedorPasos = new VBox() ;
        
        pasos.getChildren().add(contenedorPasos) ;
        for (String paso : pasosProgr3) {
            Label textoPaso = new Label(paso) ;
            textoPaso.setStyle("-fx-font-size: 16px; -fx-font-family: "
                    + "'Segoe UI', Verdana, sans-serif;");            
            contenedorPasos.getChildren().add(textoPaso) ;
        }
    }

    private void pasosPrograma2() {
        String[] pasosProgr2 = new String[7] ;
        pasosProgr2[0] = "0. Cantidad de registros individuales \n"
                + "pendientes: "+cantRegsInd ;
        pasosProgr2[1] = "1. Tipo de carga" ;
        pasosProgr2[2] = "2. Carga bibliográfico" ;
        pasosProgr2[3] = "5. Cargar ítem" ;
        pasosProgr2[4] = "6. Siguiente registro" ;
                
        contenedorPasos = new VBox() ;
        
        pasos.getChildren().add(contenedorPasos) ;
        for (String paso : pasosProgr2) {
            Label textoPaso = new Label(paso) ;
            textoPaso.setStyle("-fx-font-size: 16px; -fx-font-family: "
                    + "'Segoe UI', Verdana, sans-serif;");            
            contenedorPasos.getChildren().add(textoPaso) ;
        }
    }

    private void abrePlantilla() {
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_A) ;
        key.getRobot().delay(50);
        key.directType(KeyEvent.VK_Z) ;
        key.getRobot().delay(50);
        key.directType(KeyEvent.VK_ENTER) ;
        key.getRobot().delay(50);
    }
    
    private void expandePlantilla() {
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_E) ;
        key.getRobot().delay(50);
        key.directType(KeyEvent.VK_Z) ;
        key.getRobot().delay(50);
        key.directType(KeyEvent.VK_ENTER) ;
        key.getRobot().delay(50);
    }
    
    private void irAlFinalDelRegistro() {
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_END) ;
        key.getRobot().delay(50);
    }
    
    private void enviaRegistroInd(String[] reg) {
        String autor = reg[4] ;
        String titulo = reg[5] ;
        String fechaISO = reg[6] ;
        
        abrePlantilla() ;
        //LDR -> pasa a la siguiente linea
        key.directType(KeyEvent.VK_DOWN) ;
        key.getRobot().delay(500);
        //007 -> pasa a la siguiente linea
        key.directType(KeyEvent.VK_DOWN) ;
        key.getRobot().delay(500);
        //008
        carga008(fechaISO) ;
        irAlFinalDelRegistro();
        //040
        cargaCampo040() ;
        //043
        switch(tipoPrograma) {
            case 1: cargaCampo043() ; break;
            case 2: /*omite*/ break; }
        //245
        cargaTitulo(titulo,fechaISO) ;
        //260
        cargaCampo260(fechaISO) ;
        //300
        cargaCampo300Ind() ;
        //500titulo
        cargaCampo500IndTit() ;
        //500autor
        cargaCampo500IndAut(autor) ;
        //540
        cargaCampo540() ;
        //561
        cargaCampo561() ;
        switch(tipoPrograma) {
            case 1: //campos para indizacion
                cargaCampos6XX() ; break;
            case 2: /*omite*/ break; }        
        //655
        cargaCampo655() ;
        //773
        cargaCampo773() ;
        //OWN
        cargaOWN() ;
        running = false ;
    }
    
    private void carga008(String fecha) {
        if (fecha.isEmpty()) {
            key.directType(KeyEvent.VK_DOWN) ;
        } else {
            key.getRobot().delay(500);
            key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
            String anio = fecha.substring(0,4) ;
            key.getRobot().delay(500);
            key.directType(KeyEvent.VK_TAB) ;
            key.getRobot().delay(500);
            key.type(anio);
            key.getRobot().delay(500);
            key.directType(KeyEvent.VK_ENTER) ;
            key.getRobot().delay(500);
        }        
    }
    
    private void actualizaPaso() throws InterruptedException {
        iniciar.setDisable(true);
        for (int i = 0; i < status; i++) {
            ((Label) contenedorPasos.getChildren().get(i)).setTextFill(Color.GREEN);
        }
        switch(nroProgr){
            case 1:
                correPrograma1();
                break;
            case 2:
                correPrograma2();
                break;
            case 3:
                correPrograma3();
                break;
        }        
    }

    private void correPrograma2() throws InterruptedException {
        switch(status){
            case 0:                
                //pasosProgr2[0] Muestra la cantidad, si es mayor a cero continua, si no
                //vuelve al principio
                if (regs.getRegIndi().size() > 0) {
                    botoneraProcesa.setVisible(true);
                    //muestra los pasos
                    pasosPrograma2();
                    status++ ;
                    regActual = 0 ;
                    actualizaPaso(); 
                } else {
                    status = 0 ;
                    nroProgr = 0 ;
                    botoneraProcesa.setVisible(false);
                }
                break;
            case 1:
                botoneraRegInd = new HBox() ;
                Button omitir = new Button("Omitir") ;
                Button cargaNormal = new Button("Cargar + IND") ;
                Button cargaBreve = new Button("Cargar - IND") ;
                botoneraRegInd.getChildren().addAll(omitir,cargaNormal,cargaBreve) ;
                registroActual = new Label(regs.getRegIndi().get(regActual)[5]) ;
                pasos.getChildren().add(registroActual) ;
                pasos.getChildren().add(botoneraRegInd) ;
                cargaNormal.setOnAction((t) -> {
                    tipoPrograma = 1 ;                    
                    try {
                        status = 2 ;
                        actualizaPaso();                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic
                            .class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                cargaBreve.setOnAction((t) -> {                    
                    tipoPrograma = 2 ;
                    try {
                        status = 2 ;
                        actualizaPaso();                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic
                            .class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                omitir.setOnAction((t) -> {
                    pasos.getChildren().remove(registroActual) ;
                    pasos.getChildren().remove(botoneraRegInd) ;
                    regActual++ ;
                    registroActual = new Label(Arrays.toString(regs.getRegIndi().get(regActual))) ;
                    pasos.getChildren().add(registroActual) ;   
                    pasos.getChildren().add(botoneraRegInd) ;
                });
                break;
            case 2:
                //pasosProgr2[2] = "Carga bibliográfico" 
                enfocaAleph();
                running = true ;
                while(running){
                    cargaRegistro(regs.getRegIndi().get(regActual)) ;                    
                }
                siguiente.setText("Cargar ítem");
                siguiente.setOnAction((t) -> {
                    try {
                        status++ ;
                        actualizaPaso() ;
                        enfocaAleph();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                });
                break;
            case 3:
                // Cargar ítem
                status++ ;
                actualizaPaso() ;
                cargaItem(regs.getRegIndi().get(regActual),true) ;
                break;
            case 4:
                status = 1 ;
                actualizaPaso() ;
                // Siguiente registro
                break;
        }
    }

    private void cargaRegistro(String[] reg) {
        botoneraRegInd.setVisible(false);
        enviaRegistroInd(reg);    
    }

    private void enfocaAleph() throws InterruptedException {
        // Obtener todas las ventanas del sistema
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        // Buscar la ventana deseada por título
        String windowTitle = "ALEPH";
        DesktopWindow targetWindow = null;
        for (DesktopWindow window : windows) {
            if (window.getTitle().contains(windowTitle)) {
                targetWindow = window;
                break;
            }
        }
        if (targetWindow != null) {
            HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
            HWND hwnd = targetWindow.getHWND();
            if (foregroundWindow.equals(hwnd)) {                
                //System.out.println("La ventana ya está en primer plano");
            } else {
                // Establecer la ventana de destino como la ventana activa            
                User32.INSTANCE.SetForegroundWindow(targetWindow.getHWND()) ;
                Thread.sleep(500);
            }            
        }
    }

    private void cargaCampo040() {
        //$$aAR-BaBN$$bspa$$cAR-BaBN$$eaacr
        key.directType(KeyEvent.VK_F6) ;
        key.type("040");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("AR-BaBN$$bspa$$cAR-BaBN$$eaacr");
        /*key.type("AR-BaBN");
        key.directType(KeyEvent.VK_F7) ;
        key.type("bspa");
        key.directType(KeyEvent.VK_F7) ;
        key.type("cAR-BaBN");
        key.directType(KeyEvent.VK_F7) ;
        key.type("eaacr");*/
    }

    private void cargaCampo043() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("043");
    }

    private void cargaTitulo(String titulo, String fechaISO) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("24500");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type(titulo);
        String fechaFormateada = RegistrosParaAleph.fechaFormateada(fechaISO);
        if (!"".equals(fechaFormateada)) {
            key.type(", ") ;
            key.type(fechaFormateada) ;            
        }
        key.directType(KeyEvent.VK_F7) ;
        key.type("h[material gráfico].") ;        
    }

    private void cargaCampo260(String fechaISO) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("260");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("c");
        try {
            String anio = fechaISO.substring(0,4);
            key.type(anio);
        } catch (Exception e) {
            key.type("[197-?]") ;
        }         
    }

    private void cargaCampo300Ind() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("300");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("1 sobre (negativos flexibles) :");
        key.directType(KeyEvent.VK_F7) ;
        key.type("bbyn");
    }

    private void cargaCampo500IndTit() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("500");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("Título tomado del sobre.");        
    }

    private void cargaCampo540() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("540");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("Puede presentar restricciones. "
                + "Consultar en el Departamento de Materiales "
                + "Cartográficos y Fotográficos.");
        key.directType(KeyEvent.VK_F7) ;
        key.type("5AR-BaBN");
    }

    private void cargaCampo561() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("561");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("Forma parte del archivo fotográfico del diario Crónica.");
        key.directType(KeyEvent.VK_F7) ;
        key.type("5AR-BaBN");
    }

    private void cargaCampo500IndAut(String autor) {
        if (!autor.equals("")) {
            key.directType(KeyEvent.VK_F6) ;
            key.type("500");
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            key.type("Fotógrafo: ");        
            key.type(autor);        
            key.type(".");        
        }
    }

    private void cargaCampos6XX() {
        //60014 L $$a $$d $$x $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("60014");
        key.directType(KeyEvent.VK_F7) ;
        key.type("d");
        key.directType(KeyEvent.VK_F7) ;
        key.type("x");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");
        //61024 L $$a $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("61024");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");
        //61124 L $$a $$n $$d $$c $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("61124");
        key.directType(KeyEvent.VK_F7) ;
        key.type("n");
        key.directType(KeyEvent.VK_F7) ;
        key.type("d");
        key.directType(KeyEvent.VK_F7) ;
        key.type("c");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");
        //63004 L $$a $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("63004");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");
        //650 4 L $$a $$x $$y $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("650 4");
        key.directType(KeyEvent.VK_F7) ;
        key.type("x");
        key.directType(KeyEvent.VK_F7) ;
        key.type("y");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");        
        //651 4 L $$a $$x $$y $$v
        key.directType(KeyEvent.VK_F6) ;
        key.type("651 4");
        key.directType(KeyEvent.VK_F7) ;
        key.type("x");
        key.directType(KeyEvent.VK_F7) ;
        key.type("y");
        key.directType(KeyEvent.VK_F7) ;
        key.type("v");        
    }

    private void cargaCampo655() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("655 4");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("Negativos flexibles");        
    }

    private void cargaCampo773() {
        //tSección Archivo fotográfico $$w(AR-BaBN)001412736
        key.directType(KeyEvent.VK_F6) ;
        key.type("77318");
        key.type("tSección Archivo fotográfico");        
        key.directType(KeyEvent.VK_F7) ;
        key.type("w(AR-BaBN)001412736");        
    }

    private void cargaItem(String[] reg, boolean primerItem) {
        String barcode = reg[0] ;
        String nroA = reg[1] ;
        String ufi = reg[8] ;
        if (primerItem) {
            key.getRobot().mouseMove(30, 368);
            key.getRobot().delay(500);
            key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().delay(50);
            key.getRobot().mouseMove(90, 384);
            key.getRobot().delay(500);
            key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().delay(50);
            key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            key.getRobot().delay(500);
        }
        //nuevo
        key.getRobot().mouseMove(1321, 169);
        key.getRobot().delay(500);
        key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        key.getRobot().delay(500);
        //barcode
        key.type(barcode);
        key.getRobot().delay(500);
        //ufi
        for (int i = 0; i < 9; i++) {
            key.directType(KeyEvent.VK_TAB) ;            
        }
        key.getRobot().delay(50);
        key.type(ufi);
        //nro A
        for (int i = 0; i < 3; i++) {
            key.directType(KeyEvent.VK_TAB) ;            
        }
        key.getRobot().delay(50);
        key.type(nroA);
        //pestaña 2
        key.getRobot().mouseMove(530, 437);
        key.getRobot().delay(500);
        key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        //inventario
        key.type(barcode);
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_ENTER) ;        
    }

    private void correPrograma3() throws InterruptedException {
        switch(status){
            case 0:                
                //pasosProgr2[0] Muestra la cantidad, si es mayor a cero continua, si no
                //vuelve al principio
                if (regs.getRegGrupos().size() > 0) {
                    botoneraProcesa.setVisible(true);
                    //muestra los pasos
                    pasosPrograma3();
                    status++ ;
                    regActual = 0 ;
                    actualizaPaso(); 
                } else {
                    status = 0 ;
                    nroProgr = 0 ;
                    botoneraProcesa.setVisible(false);
                }
                break;
            case 1:
                botoneraRegInd = new HBox() ;
                Button omitir = new Button("Siguiente") ;
                omitir.setOnAction((t) -> {
                    pasos.getChildren().remove(registroActual) ;
                    pasos.getChildren().remove(botoneraRegInd) ;
                    regActual++ ;
                    registroActual = new Label(regs.getRegGrupos().get(regActual).getTitulo()) ;
                    pasos.getChildren().add(registroActual) ;   
                    pasos.getChildren().add(botoneraRegInd) ;
                    status = 1 ;                    
                });
                Button carga = new Button("Cargar") ;
                carga.setOnAction((t) -> {
                    try {
                        status++ ;
                        actualizaPaso();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                botoneraRegInd.getChildren().addAll(omitir,carga) ;                
                registroActual = new Label(regs.getRegGrupos().get(regActual).getTitulo()) ;
                pasos.getChildren().add(registroActual) ;
                pasos.getChildren().add(botoneraRegInd) ;
                break;
            case 2:
                running = true ;
                //envia el registro
                detener.setOnAction((t) -> {
                    running = false ;
                });
                enfocaAleph();
                while (running) {                    
                    enviaRegistroGrupal(regs.getRegGrupos().get(regActual)) ;
                    itemActual = 0 ;
                    status = 3 ;
                }
                actualizaPaso() ;
                break;
            case 3:
                // items
                siguiente.setText("Cargar items");
                List<List<String>> items1 = regs.getRegGrupos().get(regActual).getItems();
                siguiente.setOnAction((t) -> {
                    String[] it = new String[9] ;
                    it[0] = items1.get(itemActual).get(0) ;
                    it[1] = items1.get(itemActual).get(1) ;
                    it[8] = items1.get(itemActual).get(2) ;
                    if (itemActual == 0) {
                        cargaItem(it,true) ;
                        siguiente.setText("Siguiente item");
                    } else {
                        if (itemActual == items1.size()) {
                            siguiente.setText("Siguiente registro");
                            regActual++ ;
                            try {
                                actualizaPaso();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        siguiente.setText("Siguiente item");
                        cargaItem(it,false) ;
                    }
                    itemActual++;
                });
                break;
            case 4:
                // Guardar registro                
                break;
        }
    }

    private void enviaRegistroGrupal(RegistroGrupal reg) {
        
        abrePlantilla() ;
        //LDR -> pasa a la siguiente linea
        key.directType(KeyEvent.VK_DOWN) ;
        key.getRobot().delay(500);
        //007 -> pasa a la siguiente linea
        key.directType(KeyEvent.VK_DOWN) ;
        key.getRobot().delay(500);
        //008
        carga008Grupal(reg.getFechas()) ;
        irAlFinalDelRegistro();
        //040
        cargaCampo040() ;
        //043
        cargaCampo043() ;
        //245
        cargaTituloGrupal(reg) ;
        //260
        cargaCampo260Grupal(reg.getFechas()) ;
        //300
        cargaCampo300Grupal(reg.getItems().size()) ;        
        //500titulo
        cargaCampo500GrupTit() ;
        //500autor
        cargaCampo500GrupAut(reg.getAutores()) ;
        //505
        cargaTitulos505(reg.getTitulos()) ;        
        //540
        cargaCampo540() ;
        //561
        cargaCampo561() ;
        //6XX
        cargaCampos6XX() ;
        //655
        cargaCampo655() ;
        //773
        cargaCampo773() ;
        //OWN
        cargaOWN() ;
        running = false ;
    }

    private void cargaTituloGrupal(RegistroGrupal reg) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("24500");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("[");
        key.type(reg.getTitulo());
        key.type("]");
        key.directType(KeyEvent.VK_F7) ;
        key.type("h[material gráfico].") ;        
    }

    private void cargaCampo500GrupTit() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("500");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("Título asignado por el personal de la Biblioteca.");        
    }

    private void cargaCampo260Grupal(List<String> fechas) {
        String fecha ;
        int menorAnio = Integer.MAX_VALUE;
        int mayorAnio = Integer.MIN_VALUE;
        boolean todosIguales = true;
        
        if (fechas.isEmpty()) {
            fecha = "[197-?]";
        } else if (fechas.size() == 1) {
            fecha = fechas.get(0).substring(0,4);
        } else {
            for (String anioStr : fechas) {
                int anio = Integer.parseInt(anioStr.substring(0,4));
                if (anio < menorAnio) {
                    menorAnio = anio;
                }
                if (anio > mayorAnio) {
                    mayorAnio = anio;
                }
                if (!anioStr.equals(fechas.get(0).substring(0,4))) {
                    todosIguales = false;
                }
            }
            if (todosIguales) {
                fecha = fechas.get(0).substring(0,4);
            } else {
                fecha = menorAnio + "-" + mayorAnio ;
            }
        }
        key.directType(KeyEvent.VK_F6) ;
        key.type("260");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("c[");
        key.type(fecha);
        key.type("]");
    }

    private void carga008Grupal(List<String> fechas) {
        int menorAnio = Integer.MAX_VALUE;
        int mayorAnio = Integer.MIN_VALUE;
        boolean todosIguales = true;
        
        if (fechas.isEmpty()) {
            key.directType(KeyEvent.VK_DOWN) ;
        } else if (fechas.size() == 1) {            
            cargaFecha1(fechas.get(0)) ;
        } else {
            for (String anioStr : fechas) {
                int anio = Integer.parseInt(anioStr);
                if (anio < menorAnio) {
                    menorAnio = anio;
                }
                if (anio > mayorAnio) {
                    mayorAnio = anio;
                }
                if (!anioStr.equals(fechas.get(0))) {
                    todosIguales = false;
                }
            }
            if (todosIguales) {                
                cargaFecha1(fechas.get(0));
            } else {
                cargaFechaRango(menorAnio,mayorAnio) ;                
            }
        }        
    }

    private void cargaFecha1(String fecha1) {
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_TAB) ;
        key.getRobot().delay(500);
        key.type(fecha1);
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_ENTER) ;
        key.getRobot().delay(500);
    }

    private void cargaFechaRango(int menorAnio, int mayorAnio) {
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
        key.getRobot().delay(500);
        key.type("k");
        key.directType(KeyEvent.VK_TAB) ;
        key.getRobot().delay(500);
        key.type(Integer.toString(menorAnio));
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_TAB) ;
        key.getRobot().delay(500);
        key.type(Integer.toString(mayorAnio));
        key.getRobot().delay(500);
        key.directType(KeyEvent.VK_ENTER) ;
        key.getRobot().delay(500);
    }

    private void cargaCampo300Grupal(int cantItems) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("300");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type(Integer.toString(cantItems));
        key.type(" sobres (negativos flexibles) :");
        key.directType(KeyEvent.VK_F7) ;
        key.type("bbyn");
    }

    private void cargaCampo500GrupAut(List<String> autores) {
        Collections.sort(autores) ;
        if (!autores.isEmpty()) {
            key.directType(KeyEvent.VK_F6) ;
            key.type("500");
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            key.type("Fotógrafo: ");
            autores.forEach((fotografo) -> {
                key.type(fotografo);
                int indice = autores.indexOf(fotografo);
                if (indice == autores.size() - 1) {
                    //no hace nada
                } else {
                    key.type(", ");        
                }                
            });
            key.type(".");
        }
    }

    private void cargaTitulos505(List<String> titulos) {        
        StringBuilder temp = new StringBuilder() ;        
        Collections.sort(titulos, new MyComparator()) ;        
        if (titulos.size() > 10) {
            key.directType(KeyEvent.VK_F6) ;
            key.type("5050");
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            for (int i = 0; i < titulos.size(); i++) {
                temp.append(titulos.get(i)) ;                
                if (i == titulos.size() - 1) {
                    //no hace nada
                } else {
                    temp.append(" -- ") ;
                }
                if (temp.toString().length() >= 1300) {
                    //pegaTexto(temp.toString()) ;                    
                    key.getRobot().delay(2000);
                    key.type(temp.toString());
                    temp = new StringBuilder() ;                    
                    key.directType(KeyEvent.VK_F6) ;
                    key.type("5050");
                    key.directType(KeyEvent.VK_RIGHT) ;
                    key.directType(KeyEvent.VK_RIGHT) ;                    
                }
            }
            //carga los que faltan
            pegaTexto(temp.toString());
            //key.type(temp.toString());
            key.type(".");                    
        } else {
            key.directType(KeyEvent.VK_F6) ;
            key.type("5050");
            key.directType(KeyEvent.VK_RIGHT) ;
            key.directType(KeyEvent.VK_RIGHT) ;
            temp = new StringBuilder() ;
            for (int i = 0; i < titulos.size(); i++) {
                temp.append(titulos.get(i));
                if (i == titulos.size() - 1) {
                    //no hace nada
                } else {
                    temp.append(" -- ") ;
                }
            }
            temp.append(".");
            pegaTexto(temp.toString());
        }
    }

    private void cargaOWN() {
        key.directType(KeyEvent.VK_F6) ;
        key.type("OWN  ");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type("CAT_FOTO");        
    }

    private void correPrograma1() throws InterruptedException {
        switch(status){
            case 0:
                pasosPrograma1() ;
                status++;
                regActual = 0 ;
                actualizaPaso() ;
                break;
            case 1:
                status++;
                actualizaPaso() ;
                break;
            case 2:
                if (regsParaAgregar.getRegs().size() > 0) {
                    muestraRegistroProgr1() ;                    
                } else {
                    status = 0 ;
                    nroProgr = 1 ;
                    botoneraProcesa.setVisible(false);
                }
                break;
            case 3 :
               enfocaAleph();
               running = true ;
               while (running) {
                   agregaAlRegistro(regsParaAgregar.getIndex(regActual)) ;
               }
               siguiente.setText("Cargar ítem");
                siguiente.setOnAction((t) -> {
                    try {
                        status++ ;
                        actualizaPaso() ;
                        enfocaAleph();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                });
               break;
            case 4 :
                List<String[]> items = regsParaAgregar.returnRegInv(regsParaAgregar.getIndex(regActual));
                siguiente.setOnAction((t) -> {                    
                if (itemActual == 0) {
                    cargaItem(items.get(itemActual),true) ;
                    siguiente.setText("Siguiente item");
                    itemActual++;
                } else {
                    cargaItem(items.get(itemActual),false) ;
                    siguiente.setText("Siguiente item");
                    itemActual++;
                    if (itemActual == items.size()) {
                        siguiente.setText("Siguiente registro");
                        itemActual = 0 ;
                        regActual++ ;
                        status = 2 ;
                        muestraRegistroProgr1();
                        try {
                            actualizaPaso();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(InterfazCatalogadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                });
                
                
               break;
        }
    }
    
    private List<String> regsGrupalesParaAgregar() {
        List<String> sys = new ArrayList<>() ;
        regs.getRegGrupos().forEach((regGrupo) -> {
            String tituloRegGrupo = regGrupo.getTitulo();
            if (regs.verificaUnificacion(tituloRegGrupo)) {
                String sysUnificador = regs.getUni().getSysUnificador(tituloRegGrupo);
                sys.add(sysUnificador) ;
                List<List<String>> items1 = regGrupo.getItems();
                for (List<String> list : items1) {                    
                    String[] it = new String[9] ;
                    it[0] = list.get(0) ;
                    it[1] = list.get(1) ;
                    it[8] = list.get(2) ;
                    regsParaAgregar.addReg(sysUnificador, it);
                    regGrupo.getTitulos().forEach((t) -> {
                        if (t.split("\\. ")[0].equals(list.get(1))) {
                            regsParaAgregar.addTitulo(sysUnificador, t);
                        }
                    });
                    
                }
            } else {
                //System.out.println("No hay posible unificacion para "+tituloRegGrupo);
            }
        });
        return sys;
    }

    private List<String> regsIndParaAgregar() {
        List<String> sys = new ArrayList<>() ;
        regs.getRegIndi().forEach((reg) -> { 
            if (regs.verificaUnificacion(reg[5])) {
                String sysUnificador = regs.getUni().getSysUnificador(reg[5]);
                sys.add(sysUnificador) ;
                regsParaAgregar.addReg(sysUnificador, reg);
                regsParaAgregar.addTitulo(sysUnificador, reg[5]);
            } else {
                //System.out.println("No hay posible unificacion para "+reg[5]);
            }
        });
        return sys;
    }

    private void agregaAlRegistro(String sys) throws InterruptedException {        
        //items para agregar regsParaAgregar.returnRegInv(sys) ;
        //titulos para agregar regsParaAgregar.getTitulos(sys) ;
        RegistroParaActualizar rpa = new RegistroParaActualizar(cron,sys,regsParaAgregar) ;
        agregaAlRegistro(rpa);
    }

    private void abreBibliografico(String sys) {
        key.getRobot().delay(500);
        key.getRobot().mouseMove(68, 54);
        key.getRobot().delay(500);
        key.getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        key.getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        key.type(sys);
        key.directType(KeyEvent.VK_ENTER);
    }
    
    private void agregaAlRegistro(RegistroParaActualizar rpa) throws InterruptedException {
        //enfocaAleph();
        abreBibliografico(rpa.sys) ;
        key.getRobot().delay(1000);
        borraCampos() ;
        key.getRobot().delay(2000);
        expandePlantilla() ;
        key.getRobot().delay(800);
        irAlFinalDelRegistro();
        key.getRobot().delay(800);
        //008
        carga008Grupal(rpa.getFechas());
        key.getRobot().delay(500);
        irAlFinalDelRegistro();
        //040
        cargaCampo040() ;
        //043
        agregaCampo043(rpa.getCampo043()) ;
        //245
        cargaTitulo245(rpa.getTitulo245()) ;
        //260
        cargaCampo260Grupal(rpa.getFechas());
        //300
        cargaCampo300Grupal(rpa.getCantItems());
        //500titulo
        agregaCampo500Tit(rpa.getTitulo245()) ;
        //500autor
        cargaCampo500GrupAut(rpa.getFotografos());
        //505
        cargaTitulos505(rpa.getTitulos());
        //540
        cargaCampo540() ;
        //561
        cargaCampo561() ;
        //6XX
        agregaCampo6XX("600",rpa.getCampo600()) ;
        agregaCampo6XX("610",rpa.getCampo610()) ;        
        agregaCampo6XX("611",rpa.getCampo611()) ;        
        agregaCampo6XX("630",rpa.getCampo630()) ;
        agregaCampo6XX("650",rpa.getCampo650()) ;
        agregaCampo6XX("651",rpa.getCampo651()) ;
        agregaCampo6XX("655",rpa.getCampo655()) ;
        //773
        cargaCampo773() ;
        //OWN
        cargaOWN() ;
        running = false ;
    }

    private void cargaTitulo245(String titulo245) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("24500");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type(titulo245);        
    }

    private void agregaCampo500Tit(String titulo245) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("500  ");
        key.directType(KeyEvent.VK_RIGHT) ;
        if (titulo245.substring(0,1).equals("[")) {            
            key.type("Título asignado por el personal de la Biblioteca.");
        } else {
            key.type("Título tomado del sobre.");
        }
    }

    private void agregaCampo043(String campo043) {
        key.directType(KeyEvent.VK_F6) ;
        key.type("043");
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.directType(KeyEvent.VK_RIGHT) ;
        key.type(campo043);
    }

    private void borraCampos() {
        key.getRobot().delay(500);
        irAlFinalDelRegistro();
        key.getRobot().delay(500);
        for (int i = 0; i < 50; i++) {
            key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F5) ;            
        }
        key.getRobot().delay(500);
    }

    private void agregaCampo6XX(String campo, List<String[]> campo6XX) {
        campo6XX.forEach((t) -> {
            key.getRobot().delay(1000);
            key.directType(KeyEvent.VK_F6) ;
            key.type(campo);
            key.type(t[0]);
            key.directType(KeyEvent.VK_RIGHT) ;            
            key.type(t[1]);
            key.getRobot().delay(1000);
        });
    }

    private void pegaTexto(String texto) {
        key.getRobot().delay(500);
        StringSelection seleccion = new StringSelection(texto);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(seleccion, null);
        key.directType(KeyEvent.VK_CONTROL,KeyEvent.VK_V) ;
        key.getRobot().delay(500);
    }

    private void muestraRegistroProgr1() {
        try {
            pasos.getChildren().remove(registroActual) ;
            pasos.getChildren().remove(botoneraRegInd) ;
        } catch (Exception e) {
        }
        botoneraProcesa.setVisible(true);
        botoneraRegInd = new HBox() ;
        Button omitir = new Button("Omitir") ;
        Button agregar = new Button("Agregar a ALEPH") ;
        botoneraRegInd.getChildren().addAll(omitir,agregar) ;                    
        String titulo = regsParaAgregar.getTitulo(regsParaAgregar.getIndex(regActual)) ;
        String dataRegs = "Unificación para " + titulo + 
                " (" + regsParaAgregar.getReg(regActual).size() + " items)"  ;

        registroActual = new Label(dataRegs) ;
        pasos.getChildren().add(registroActual) ;
        pasos.getChildren().add(botoneraRegInd) ;
        agregar.setOnAction((t) -> {
            try {
                status++ ;
                actualizaPaso();                        
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic
                    .class.getName()).log(Level.SEVERE, null, ex);
            }
        }); 
        omitir.setOnAction((t) -> {
            try {
                if (regActual < regsParaAgregar.getRegs().size()) {
                    pasos.getChildren().remove(registroActual) ;
                    pasos.getChildren().remove(botoneraRegInd) ;
                    regActual++ ;
                    actualizaPaso();       
                } else {
                    status = 0 ;
                    nroProgr = 0 ;
                }                                                 
            } catch (InterruptedException ex) {
                Logger.getLogger(InterfazCatalogadorOMatic
                    .class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}

/*class MyComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        String[] parts1 = str1.split("\\. | ");
        String[] parts2 = str2.split("\\. | ");
        boolean isA1 = parts1[0].startsWith("A");
        boolean isA2 = parts2[0].startsWith("A");
        if (isA1 && isA2) {
            // Ambas cadenas comienzan con "A", comparamos los números después de la "A"
            String numStr1 = parts1[0].substring(1);
            double num1 = Double.parseDouble(numStr1);
            String numStr2 = parts2[0].substring(1);
            double num2 = Double.parseDouble(numStr2);
            int cmp = Double.compare(num1, num2);
            if (cmp != 0) {
                return cmp;
            }
            // Si los números son iguales, comparamos los caracteres restantes
            return parts1[1].compareTo(parts2[1]);
        } else if (isA1) {
            // La primera cadena comienza con "A", va primero
            return -1;
        } else if (isA2) {
            // La segunda cadena comienza con "A", va primero
            return 1;
        } else {
            // Ninguna cadena comienza con "A", comparamos los números directamente
            String numStr1 = parts1[0];
            double num1 = Double.parseDouble(numStr1);
            String numStr2 = parts2[0];
            double num2 = Double.parseDouble(numStr2);
            int cmp = Double.compare(num1, num2);
            if (cmp != 0) {
                return cmp;
            }
            // Si los números son iguales, comparamos los caracteres restantes
            return parts1[1].compareTo(parts2[1]);
        }
    }
}*/

class MyComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        
        String prefix1 = "";
        String prefix2 = "";
        double num1 = -1;
        double num2 = -1;
        
        // Buscamos el prefijo "A" y los números siguientes en la primera cadena
        String[] parts1 = str1.split(" ");
        for (int i = 0; i < parts1.length; i++) {
            if (parts1[i].startsWith("A")) {
                prefix1 = "A";
                try {
                    num1 = Double.parseDouble(parts1[i].substring(1));
                } catch (NumberFormatException e) {
                    // Si no se puede parsear el número, lo dejamos en -1
                }
                break;
            }
        }
        
        // Buscamos el prefijo "A" y los números siguientes en la segunda cadena
        String[] parts2 = str2.split(" ");
        for (int i = 0; i < parts2.length; i++) {
            if (parts2[i].startsWith("A")) {
                prefix2 = "A";
                try {
                    num2 = Double.parseDouble(parts2[i].substring(1));
                } catch (NumberFormatException e) {
                    // Si no se puede parsear el número, lo dejamos en -1
                }
                break;
            }
        }
        
        // Si alguna de las cadenas no comienza con "A", las comparamos directamente
        if (prefix1.isEmpty() || prefix2.isEmpty()) {
            return str1.compareTo(str2);
        }
        
        // Si ambas cadenas comienzan con "A", comparamos los números
        if (prefix1.equals("A") && prefix2.equals("A")) {
            return Double.compare(num1, num2);
        }
        
        // Si solo una de las cadenas comienza con "A", la otra va primero
        return prefix1.isEmpty() ? 1 : -1;
    }
}

