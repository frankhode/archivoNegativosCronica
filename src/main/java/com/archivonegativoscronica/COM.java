/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class COM {
    int nroVista,status,player,regActual,itemActual,tipoPrograma ;
    Scene scene ;
    Stage ventana ;
    final Funciones cron ;
    final Keyboard key ;
    boolean running ;
    private double xOffset ;
    private double yOffset ;
    CatalogadorOMatic.InventariosPendientes pendientes ;
    HBox botoneraProgramas ;
    BotoneraPlayer botoneraPlayer ; 
    VBox resumen ;
    public RegistrosParaAgregar regsParaActualizar ;

    COM(CatalogadorOMatic.InventariosPendientes pendientes,Funciones cron,Keyboard key) {
        //variables iniciales
        this.cron = cron ;
        this.key = key ;
        running = false ;
        regActual = 0 ;
        itemActual = 0 ;
        status = 0 ;
        this.pendientes = pendientes ;
        
        // Crear cuatro paneles de botones
        botoneraProgramas = creaBotonesProgramas() ;
        botoneraPlayer = new BotoneraPlayer() ;    
        botoneraPlayer.setVisible(false);
        resumen = new VBox();
        
        //centra los contenedores y su contenido
        botoneraProgramas.setAlignment(Pos.CENTER);
        botoneraPlayer.setAlignment(Pos.CENTER);
        resumen.setAlignment(Pos.CENTER);
        BackgroundFill backgroundFill = new BackgroundFill(Color.GAINSBORO, null, null);
        Background background = new Background(backgroundFill);
        resumen.setBackground(background);

        // Crear el layout principal y agregar los cuatro paneles de botones
        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(botoneraProgramas, resumen, botoneraPlayer);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));        
        
        //boton para cambiar a la vista reducida
        Button reduceBtn = new Button("Reducir");
        botoneraPlayer.getChildren().add(reduceBtn) ;
        reduceBtn.setOnAction(e -> {
            if (mainLayout.getChildren().contains(resumen)) {
                mainLayout.getChildren().removeAll(botoneraProgramas, resumen);
                ventana.setWidth(botoneraPlayer.getWidth()+20);
                ventana.setHeight(botoneraPlayer.getHeight()+20);
                reduceBtn.setText("Restaurar");
            } else {
                mainLayout.getChildren().remove(botoneraPlayer);
                mainLayout.getChildren().addAll(botoneraProgramas, resumen, botoneraPlayer);
                ventanaCompleta();
                reduceBtn.setText("Reducir");
            }
        });

        // Crear la escena y mostrar la ventana
        //scene = new Scene(mainLayout, 600, 150, Color.WHITE);
        scene = new Scene(mainLayout) ;
        
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
        mainLayout.setBorder(borde);
        
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
        //ventana.initStyle(StageStyle.UNDECORATED);
        ventana.setAlwaysOnTop(true);
        ventana.setResizable(true);
        
        ventanaCompleta() ;
        
        // Mostrar la ventana
        ventana.setScene(scene);
        ventana.setTitle("Catalogador-O-Matic");
        ventana.show();
    }
    
    // Crear un ScrollPane con contenido de ejemplo
    public ScrollPane createScrollPane(String contentText) {
        TextArea content = new TextArea(contentText);
        content.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        return scrollPane;
    }

    private HBox creaBotonesProgramas() {
        HBox buttonPanel = new HBox();        
        BackgroundFill backgroundFill = new BackgroundFill(Color.DARKGREY, null, null);
        Background background = new Background(backgroundFill);
        buttonPanel.setBackground(background);
        buttonPanel.setSpacing(30);
        buttonPanel.setPadding(new Insets(5));
                
        ToggleGroup group = new ToggleGroup();        
        ToggleButton btn1 = new ToggleButton("REG. \nIND");
        ToggleButton btn2 = new ToggleButton("REG. \nGRU");
        ToggleButton btn3 = new ToggleButton("UPD \nAUTO");
        ToggleButton btn4 = new ToggleButton("UPD \nMANUAL");
        
        Background bkgBtn1 = new Background(new BackgroundFill(Color.AZURE,null,null));
        btn1.setBackground(bkgBtn1);
        Background bkgBtn2 = new Background(new BackgroundFill(Color.CADETBLUE,null,null));
        btn2.setBackground(bkgBtn2);
        Background bkgBtn3 = new Background(new BackgroundFill(Color.LIGHTBLUE,null,null));
        btn3.setBackground(bkgBtn3);
        Background bkgBtn4 = new Background(new BackgroundFill(Color.LIGHTSTEELBLUE,null,null));
        btn4.setBackground(bkgBtn4);
        
        btn1.setFont(Font.font("Arial Rounded MT", FontWeight.THIN, FontPosture.REGULAR, 10));
        btn2.setFont(Font.font("Arial Rounded MT", FontWeight.THIN, FontPosture.REGULAR, 10));
        btn3.setFont(Font.font("Arial Rounded MT", FontWeight.THIN, FontPosture.REGULAR, 10));
        btn4.setFont(Font.font("Arial Rounded MT", FontWeight.THIN, FontPosture.REGULAR, 10));
        
        btn1.setPrefWidth(120);
        btn2.setPrefWidth(120);
        btn3.setPrefWidth(120);
        btn4.setPrefWidth(120);
        
        btn1.setToggleGroup(group);
        btn2.setToggleGroup(group);
        btn3.setToggleGroup(group);
        btn4.setToggleGroup(group);
        
        btn1.setOnAction((t) -> {
            //btn2.setDisable(true);
            //btn3.setDisable(true);
            //btn4.setDisable(true);
            try {
                //corre programa 1 reg ind
                COMregIndi cri = new COMregIndi(this) ;
            } catch (InterruptedException ex) {
                Logger.getLogger(COM.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btn2.setOnAction((t) -> {
            //btn1.setDisable(true);
            //btn3.setDisable(true);
            //btn4.setDisable(true);
            try {
                //corre programa 2 reg grupo
                COMregGrupal crg = new COMregGrupal(this) ;                
            } catch (InterruptedException ex) {
                Logger.getLogger(COM.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        btn3.setOnAction((t) -> {
            //btn1.setDisable(true);
            //btn2.setDisable(true);
            //btn4.setDisable(true);
            try {
                //corre programa 3 regs para actualizar
                COMregActualizar cra = new COMregActualizar(this,cron) ;
            } catch (InterruptedException ex) {                
                Logger.getLogger(COM.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        btn4.setOnAction((t) -> {
            //btn1.setDisable(true);
            //btn2.setDisable(true);
            //btn3.setDisable(true);
            //corre programa 4
        });
        
        btn2.setPrefWidth(120);
        btn3.setPrefWidth(120);
        btn4.setPrefWidth(120);
        
        buttonPanel.getChildren().addAll(btn1,btn2,btn3,btn4);
        
        return buttonPanel ;
    }

    private void ventanaCompleta() {
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
    }
    
    public void setRegistrosParaAgregar(RegistrosParaAgregar regsParaActualizar) {
        this.regsParaActualizar = regsParaActualizar ;
    }

    class BotoneraPlayer extends HBox {
        
        /**
         * @return the btn1
         */
        public ToggleButton getBtn1() {
            return btn1;
        }

        /**
         * @return the btn2
         */
        public ToggleButton getBtnMinus1() {
            return btn2;
        }

        /**
         * @return the btn3
         */
        public ToggleButton getBtnPlay() {
            return btn3;
        }

        /**
         * @return the btn4
         */
        public ToggleButton getBtnPlus1() {
            return btn4;
        }

        /**
         * @return the btn5
         */
        public ToggleButton getBtnClose() {
            return btn5;
        }
        private final ToggleButton btn1,btn2,btn3,btn4,btn5;

        public BotoneraPlayer() {
            ToggleGroup group = new ToggleGroup();
            this.setSpacing(20);
            btn1 = new ToggleButton("*");
            btn2 = new ToggleButton("<<");
            btn3 = new ToggleButton(">");
            btn4 = new ToggleButton(">>");
            btn5 = new ToggleButton("X");

            btn1.setPrefSize(35,35);
            btn2.setPrefSize(35,35);
            btn3.setPrefSize(35,35);
            btn4.setPrefSize(35,35);
            btn5.setPrefSize(35,35);

            btn1.setFont(Font.font("Arial Rounded MT", FontWeight.BOLD, FontPosture.REGULAR, 10));
            btn2.setFont(Font.font("Arial Rounded MT", FontWeight.BOLD, FontPosture.REGULAR, 10));
            btn3.setFont(Font.font("Arial Rounded MT", FontWeight.BOLD, FontPosture.REGULAR, 10));
            btn4.setFont(Font.font("Arial Rounded MT", FontWeight.BOLD, FontPosture.REGULAR, 10));
            btn5.setFont(Font.font("Arial Rounded MT", FontWeight.BOLD, FontPosture.REGULAR, 10));

            btn1.setToggleGroup(group);
            btn2.setToggleGroup(group);
            btn3.setToggleGroup(group);
            btn4.setToggleGroup(group);
            btn5.setToggleGroup(group);

            this.getChildren().addAll(btn1,btn2,btn3,btn4,btn5);
        }
    }
    
}
