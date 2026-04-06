/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.image.BufferedImage;
import javafx.geometry.Pos;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;


/**
 *
 * @author francisco.ortiz
 */
class NavegadorDeImagenes {
    private List<String> imagePaths;
    private int currentIndex;
    private Label label ;
    private static final double IMAGE_WIDTH_PERCENTAGE = 0.7;
    private final Funciones cron ;
    private Stage stage ;
    BorderPane root ;
    String nombramiento ;

    public NavegadorDeImagenes(Funciones cron, List<String[]> consultaSimple) {
        this.cron = cron ;
        ImageIO.scanForPlugins();
        imagePaths = obtenerRutasImagenes(consultaSimple,cron.folder);
        currentIndex = 0;

        stage = new Stage() ;
        root = new BorderPane();        
        label = new Label(muestraLabel());
        label.setStyle("-fx-font-weight: bold;-fx-font-size: 16;");
        HBox buttonBar = createButtonBar();
                
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);        
        
        Button previousButton = new Button();
        Image ia = new Image(getClass().getResourceAsStream("/files/anterior.png"), 30, 30, false, false);
        previousButton.setGraphic(new ImageView(ia));
        previousButton.setOnAction(event -> mostrarImagenAnterior(imageView));

        Button nextButton = new Button();
        Image ip = new Image(getClass().getResourceAsStream("/files/posterior.png"), 30, 30, false, false);
        nextButton.setGraphic(new ImageView(ip));
        nextButton.setOnAction(event -> mostrarSiguienteImagen(imageView));

        HBox buttonBox = new HBox(previousButton, nextButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        
        VBox vb = new VBox(label,buttonBar) ;
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(10);
        BorderPane.setAlignment(vb, Pos.CENTER);
        BorderPane.setAlignment(previousButton, Pos.CENTER_LEFT);
        BorderPane.setAlignment(nextButton, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        root.setTop(vb);
        root.setLeft(previousButton);
        root.setCenter(imageView);
        root.setRight(nextButton);

        Scene scene = new Scene(root, 600, 700);
        // Adaptar el tamaño de root al cambiar el tamaño de la ventana
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        stage.setScene(scene);
        stage.setTitle("Archivo Fotográfico del Diario Crónica");
        stage.show();
        imageView.setFitWidth(stage.getWidth()*IMAGE_WIDTH_PERCENTAGE);

        // Mostrar la primera imagen
        mostrarImagen(imageView, currentIndex);
        
        // Ajustar el ancho del ImageView al 70% del ancho de la ventana
        stage.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth) -> {
            double imageViewWidth = newWidth.doubleValue() * IMAGE_WIDTH_PERCENTAGE;
            double imageViewHeight = imageViewWidth / imageView.getImage().getWidth() * imageView.getImage().getHeight();

            if (imageViewHeight > stage.getHeight()) {
                // Si la imagen es más alta que la ventana, ajustar el alto en consecuencia
                imageViewHeight = stage.getHeight()*IMAGE_WIDTH_PERCENTAGE;
                imageViewWidth = imageViewHeight / imageView.getImage().getHeight() * imageView.getImage().getWidth();
            }

            imageView.setFitWidth(imageViewWidth);
            imageView.setFitHeight(imageViewHeight);

            // Centrar la imagen horizontal y verticalmente
            imageView.setX((stage.getWidth() - imageView.getBoundsInParent().getWidth()) / 2);
            imageView.setY((stage.getHeight() - imageView.getBoundsInParent().getHeight()) / 2);
        });
        
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                previousButton.fire();
            }
            if (event.getCode() == KeyCode.RIGHT) {
                nextButton.fire();
            }        
        });
    }
    public NavegadorDeImagenes(Funciones cron, List<String[]> consultaSimple, VBox detailsPane) {
        this.cron = cron ;
        imagePaths = obtenerRutasImagenes(consultaSimple,cron.folder);
        currentIndex = 0;

        root = new BorderPane();        
        label = new Label(muestraLabel());
        label.setStyle("-fx-font-weight: bold;-fx-font-size: 16;");
        HBox buttonBar = createButtonBar();
                
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true); // Maintain aspect ratio 
        imageView.setFitWidth(detailsPane.getWidth()/1.35); // Set the width to fit detailsPane
        imageView.setFitHeight(detailsPane.getHeight()/1.35); // Set the height to fit detailsPane        
        
        Button previousButton = new Button();
        Image ia = new Image(getClass().getResourceAsStream("/files/anterior.png"), 30, 30, false, false);
        previousButton.setGraphic(new ImageView(ia));
        previousButton.setOnAction(event -> mostrarImagenAnterior(imageView));

        Button nextButton = new Button();
        Image ip = new Image(getClass().getResourceAsStream("/files/posterior.png"), 30, 30, false, false);
        nextButton.setGraphic(new ImageView(ip));
        nextButton.setOnAction(event -> mostrarSiguienteImagen(imageView));

        HBox buttonBox = new HBox(previousButton, nextButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        
        VBox vb = new VBox(label,buttonBar) ;
        vb.setAlignment(Pos.CENTER);
        vb.setSpacing(10);
        BorderPane.setAlignment(vb, Pos.CENTER);
        BorderPane.setAlignment(previousButton, Pos.CENTER_LEFT);
        BorderPane.setAlignment(nextButton, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(imageView, Pos.CENTER);
        BorderPane.setMargin(imageView, new Insets(30));
        root.setTop(vb);
        root.setLeft(previousButton);
        root.setCenter(imageView);
        root.setRight(nextButton);

        // Mostrar la primera imagen
        mostrarImagen(imageView, currentIndex);
    }

    private List<String> obtenerRutasImagenes(List<String[]> consultaSimple, File folder) {
        imagePaths = new ArrayList<>() ;
        consultaSimple.forEach((t) -> {
            //nombramiento, cajon,carpeta            
            imagePaths.add(folder.getAbsolutePath()+"\\"+t[3]+"\\"+t[2]+"\\"+t[0]+"\\"+t[1]);
            System.out.println(folder.getAbsolutePath()+"\\"+t[3]+"\\"+t[2]+"\\"+t[0]+"\\"+t[1]);
        });
        return imagePaths;
    }
    
    private void mostrarSiguienteImagen(ImageView imageView) {
        root.setBottom(null);
        currentIndex++;
        if (currentIndex >= imagePaths.size()) {
            currentIndex = 0;
        }
        mostrarImagen(imageView, currentIndex);
    }

    private void mostrarImagenAnterior(ImageView imageView) {
        root.setBottom(null);
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = imagePaths.size() - 1;
        }
        mostrarImagen(imageView, currentIndex);
    }
    
    private void mostrarImagen(ImageView imageView, int index) {
        label.setText(muestraLabel());
        if (index >= 0 && index < imagePaths.size()) {
            String imagePath = imagePaths.get(index);
            File file = new File(imagePath);
            try {
                Image image = loadTiffImage(file);
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        agregaVisto() ;
    }
    
    private HBox createButtonBar() {
        Button b1 = new Button("Agregar a colección");
        b1.setOnAction((t) -> {
            ComboBox<String> combo = agregarColeccion(); 
            Alert al = new Alert(Alert.AlertType.CONFIRMATION) ;
            al.setTitle("Agregar a colección");
            al.getDialogPane().setContent(combo);
            Optional<ButtonType> result = al.showAndWait();
            if (result.isPresent()) {
                enviaAColeccion(combo.getSelectionModel().getSelectedItem()) ;
            }
        });
        Button b2 = new Button("Asignar descriptores");
        b2.setOnAction((t) -> {
            TextInputDialog descriptores = agregaDescriptores() ;
        });
        Button b3 = new Button("Indización individual");
        b3.setOnAction((t) -> {
            addIndizacion() ;
        });
        Button b4 = new Button("Pedir copia");
        b4.setOnAction((event) -> {
            //agregarAlCarritoUsuario(imagePaths.get(currentIndex)) ;
            System.out.println(imagePaths.get(currentIndex));
            Contactos ctc = new Contactos(cron) ;
            ctc.cargar(imagePaths.get(currentIndex).split("\\\\")[5]);
        });
        Button b5 = new Button("Zoom");
        b5.setOnAction((t) -> {
            VisorImagenes vi = new VisorImagenes(new File(imagePaths.get(currentIndex))) ;
        });
        Button b6 = new Button("Mis imágenes");
        b6.setOnAction((t) -> {
            verMisImagenes() ;
        });
        Button b7 = new Button("Ver edición impresa");
        b7.setOnAction((t) -> {
            EdicionImpresa ei = new EdicionImpresa(imagePaths.get(currentIndex),cron) ;
        });
        HBox buttonBar = new HBox(b1,b2,b3,b4,b5,b6,b7);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setSpacing(10);
        if (!cron.getUser().getRol().equals("1")) {
            b1.setVisible(false);
            //b2.setVisible(false);
            b3.setVisible(false);
            //b4.setVisible(false);
        }
        return buttonBar;
    }

    private String muestraLabel() {        
        String[] split = imagePaths.get(currentIndex).split("\\\\");
        nombramiento = split[split.length-1].replace(".jpg","");
        return split[split.length-3]+" -> "+split[split.length-2]+" -> "+
            split[split.length-1].replace(".jpg","")+"/"+(imagePaths.size()-1);
    }

    private ComboBox<String> agregarColeccion() {
        // Creamos un choice box
        ComboBox<String> conjuntoComboBox = new ComboBox<>();
        // Obtenemos los conjuntos de la base de datos y los agregamos al choice box
        List<String> conj = obtenerColecciones();
        conjuntoComboBox.getItems().addAll(conj);
        // Agregamos la opción "Crear nuevo conjunto"
        conjuntoComboBox.getItems().add("Crear nueva colección");

        // Configuramos los controladores de eventos
        conjuntoComboBox.setOnAction((event) -> {
            String seleccion = conjuntoComboBox.getSelectionModel().getSelectedItem();
            if (seleccion.equals("Crear nueva colección")) {
                // Abrimos una ventana modal para ingresar el nombre del nuevo conjunto
                Stage modalStage = new Stage();
                modalStage.setTitle("Nueva colección");
                modalStage.initModality(Modality.APPLICATION_MODAL);
                modalStage.initOwner(stage);

                // Creamos los componentes para la ventana modal
                Label label = new Label("Ingrese el nombre de la nueva colección:");
                TextField textField = new TextField();                
                Button aceptarButton = new Button("Aceptar");

                // Configuramos los contenedores para la ventana modal
                VBox vbox = new VBox(label, textField, aceptarButton);
                vbox.setAlignment(Pos.CENTER);
                vbox.setSpacing(10);
                vbox.setPadding(new Insets(10, 10, 10, 10));

                // Configuramos la escena para la ventana modal
                Scene modalScene = new Scene(vbox, 300, 200);
                modalStage.setScene(modalScene);

                aceptarButton.setOnAction((e) -> {
                    String nuevoConjunto = textField.getText();
                    // Agregamos el nuevo conjunto al choice box y lo seleccionamos
                    conjuntoComboBox.getItems().add(nuevoConjunto);
                    conjuntoComboBox.getSelectionModel().select(nuevoConjunto);
                    modalStage.close();
                });

                // Mostramos la ventana modal
                modalStage.showAndWait();
            } else {
                // Se seleccionó un conjunto existente, hacemos lo que corresponda
            }
        });
        return conjuntoComboBox ;
    }
    
    private List<String> obtenerColecciones() {
        //status 1. conjuntos, 2. registro para actualizar, 3. cargado en aleph
        String consulta = "SELECT DISTINCT coleccion FROM colecciones ORDER BY coleccion" ;
        List<String> conjs = cron.consultaSimple(consulta, 1);
        return conjs ;
    }

    private void enviaAColeccion(String coleccion) {
        String[] split = imagePaths.get(currentIndex).split("\\\\");
        String imagen = split[split.length-1] ;
        String consulta = "INSERT INTO colecciones(nombramiento, coleccion) VALUES ("
                + "'"+imagen+"','"+coleccion+"')";
        cron.consultaSimple(consulta, 1);
    }

    private TextInputDialog agregaDescriptores() {
        TextInputDialog txtin = new TextInputDialog("Separe los descriptores con ';'") ;
        Optional<String> result = txtin.showAndWait();
        if (result.isPresent()) {
            String[] split = imagePaths.get(currentIndex).split("\\\\");
            String imagen = split[split.length-1].replace(".jpg","");            
            enviaDescriptores(imagen,result.get()) ;
        }
        return txtin ;
    }

    private void enviaDescriptores(String imagen, String descriptores) {        
        String consulta ;
        if (descriptores.contains(";")) {
            String[] split1 = descriptores.split(";");
            for (String desc : split1) {
                consulta = "SELECT '"+imagen+"' FROM descriptoresimagenes WHERE"
                    + "descriptor LIKE '"+desc+"'";
                List<String> consultaSimple = cron.consultaSimple(consulta, 1);
                if (consultaSimple.isEmpty()) {
                    consulta = "INSERT INTO descriptoresimagenes(nombramiento, descriptor) "
                        + "VALUES ('"+imagen+"','"+desc+"')";                    
                    cron.consultaSimple(consulta, 1);
                }
            }
        } else {
            consulta = "INSERT INTO descriptoresimagenes(nombramiento, descriptor) "
                    + "VALUES ('"+imagen+"','"+descriptores+"')";
            cron.consultaSimple(consulta, 1);
        }
    }

    private void addIndizacion() {
        HBox hb = new HBox() ;
        
        //persona
        Label p1 = new Label("Persona en la imagen") ;
        ComboBox p2 = new ComboBox() ;
        agregaContenidoCombo(p2, "persona") ;
        VBox persona = new VBox(p1,p2) ;
        persona.setAlignment(Pos.CENTER);
        persona.setSpacing(10);
        
        //lugar        
        Label l1 = new Label("Lugar en la imagen") ;
        ComboBox l2 = new ComboBox() ;
        agregaContenidoCombo(l2, "lugar") ;
        VBox lugar = new VBox(l1,l2) ;
        lugar.setAlignment(Pos.CENTER);
        lugar.setSpacing(10);
        //objeto        
        Label o1 = new Label("Objeto/tema en la imagen") ;
        ComboBox o2 = new ComboBox() ;
        agregaContenidoCombo(o2, "objeto") ;
        VBox objeto = new VBox(o1,o2) ;
        objeto.setAlignment(Pos.CENTER);
        objeto.setSpacing(10);
        //evento        
        Label e1 = new Label("Evento en la imagen") ;
        ComboBox e2 = new ComboBox() ;
        agregaContenidoCombo(e2, "evento") ;
        VBox evento = new VBox(e1,e2) ;
        evento.setAlignment(Pos.CENTER);
        evento.setSpacing(10);
        //institucion
        Label i1 = new Label("Institución en la imagen") ;
        ComboBox i2 = new ComboBox() ;
        agregaContenidoCombo(i2, "institucion") ;
        VBox institucion = new VBox(i1,i2) ;
        institucion.setAlignment(Pos.CENTER);
        institucion.setSpacing(10);
        
        hb.getChildren().addAll(persona,lugar,objeto,evento,institucion) ;
        hb.setAlignment(Pos.CENTER);
        hb.setSpacing(10);
        root.setBottom(hb);
    }

    private void agregaContenidoCombo(ComboBox combo, String tipo) {
        combo.getItems().clear();
        String consulta = "" ;
        switch(tipo){
            case "persona":
                consulta = "SELECT personaEnImagen FROM indizImagenes "
                        + "WHERE nombramiento LIKE '"+nombramiento+"' ORDER BY personaEnImagen" ;
                break;
            case "lugar":
                consulta = "SELECT lugarEnImagen FROM indizImagenes  "
                        + "WHERE nombramiento LIKE '"+nombramiento+"' ORDER BY lugarEnImagen" ;
                break;
            case "objeto":
                consulta = "SELECT objetoEnImagen FROM indizImagenes  "
                        + "WHERE nombramiento LIKE '"+nombramiento+"' ORDER BY objetoEnImagen" ;
                break;
            case "evento":
                consulta = "SELECT eventoEnImagen FROM indizImagenes  "
                        + "WHERE nombramiento LIKE '"+nombramiento+"' ORDER BY eventoEnImagen" ;
                break;
            case "institucion":
                consulta = "SELECT institucionEnImagen FROM indizImagenes  "
                        + "WHERE nombramiento LIKE '"+nombramiento+"' ORDER BY institucionEnImagen" ;
                break;
        }
        List<String> lista = cron.consultaSimple(consulta, 1);
        if (lista.isEmpty()) {
            combo.getItems().add("Agregar "+tipo) ;
        } else {
            combo.getItems().add("") ;
            combo.getItems().add("Agregar "+tipo) ;
            lista.forEach((t) -> {if (!t.equals("")){combo.getItems().add(t);}});
            combo.getSelectionModel().select(0);
        }
        
        combo.setOnAction(event -> {
            try {
                String seleccion = combo.getValue().toString();
                if (seleccion.equals("Agregar "+tipo)) {
                    agregarNuevo(tipo, combo);
                } else if(!seleccion.equals("")) {
                    preguntaParaBorrar(seleccion, combo);
                }
            } catch (Exception e) {
                //combo vacio?
            }
            
        });
    }

    private void agregarNuevo(String tipo, ComboBox combo) {
        String consulta = "" ;
        switch(tipo){
            case "persona":
                consulta = "SELECT materia as pers FROM materias WHERE CAMPO ='600' "
                        + "UNION SELECT personaEnImagen as pers FROM indizImagenes ORDER BY pers" ;
                break;
            case "lugar":
                consulta = "SELECT DISTINCT materia as mat FROM materias WHERE CAMPO ='651'" 
                        + "UNION SELECT lugarEnImagen as mat FROM indizImagenes ORDER BY mat" ;
                break;
            case "objeto":
                consulta = "SELECT DISTINCT materia as mat FROM materias WHERE CAMPO ='650'" 
                        + "UNION SELECT objetoEnImagen as mat FROM indizImagenes ORDER BY mat" ;
                break;
            case "evento":
                consulta = "SELECT DISTINCT materia as mat FROM materias WHERE CAMPO ='611'" 
                        + "UNION SELECT eventoEnImagen as mat FROM indizImagenes ORDER BY mat" ;
                break;
            case "institucion":
                consulta = "SELECT DISTINCT materia as mat FROM materias WHERE CAMPO ='610'" 
                        + "UNION SELECT institucionEnImagen as mat FROM indizImagenes ORDER BY mat" ;
                break;
        }
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);
        regExistentes(consultaSimple,tipo,combo) ;
    }

    private void preguntaParaBorrar(String palabra, ComboBox combo) {
        Alert al = new Alert(Alert.AlertType.CONFIRMATION) ;
        al.setContentText("Borrar "+palabra+"?");
        al.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Lógica para el botón "Ver"
                System.out.println("Botón 'Ver' presionado");
            } else if (response == ButtonType.NO) {
                al.close();
            }
        });
    }
    
    private void regExistentes(List<String> registros, String tipo, ComboBox combo) {
        VentanaModalEjemplo ventanaModal = new VentanaModalEjemplo(registros,tipo,combo);        
        ventanaModal.showAndWait();
        if (ventanaModal.resultado != null) {
            agregaContenidoCombo(combo, tipo);
        }
    }

    private void agregaVisto() {        
        String user = cron.getUser().getNombre() ;
        // Obtener la fecha y hora actuales en la zona horaria de Buenos Aires
        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");        
        ZonedDateTime now = ZonedDateTime.now(buenosAiresZone);        

        // Formatear la fecha y hora según el formato deseado (YYYYMMDDHHMMSS)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");        
        String fechaFormateada = now.format(formatter);
        String consulta = "INSERT INTO archivocronica.vistoimagenes ("
                + "nombramiento,vistoPor,vistoFecha) VALUES ('"+nombramiento+"', "
                + "'"+user+"', '"+fechaFormateada+"');" ;
        cron.consultaSimple(consulta, 1) ;
    }

    private void agregarAlCarritoUsuario(String imagen) {
        // Formatear la fecha como String (por ejemplo, en formato "yyyy-MM-dd HH:mm:ss")
        LocalDateTime fechaActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = fechaActual.format(formatter);
        String consulta = "INSERT INTO carritousuario(usuario, imagen, fecha) "
                + "VALUES ('"+cron.username+"','"+imagen.replace("\\", "\\\\")+"','"+fechaFormateada+"')" ;
        cron.consultaSimple(consulta, 1) ;
    }

    private void verMisImagenes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    class VentanaModalEjemplo extends Stage {
        String resultado = null ;
        public VentanaModalEjemplo(List<String> registros,String tipo,ComboBox combo) {
            // Configuramos la ventana modal
            initModality(Modality.APPLICATION_MODAL);
            setTitle("Agregar "+tipo);

            // Creamos un TextField y un ListView
            TextField textField = new TextField();
            ListView<String> listView = new ListView<>();

            // Configuramos el ListView
            listView.setPrefWidth(400);

            // Configuramos el TextField para que muestre las sugerencias
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Actualizamos el ListView con los registros obtenidos
                listView.getItems().clear();
                List<String> resultados = new ArrayList<>() ;
                registros.forEach((t) -> {
                    if (t.toLowerCase().contains(newValue.toLowerCase())) {
                        resultados.add(t) ;
                    }
                });
                listView.getItems().addAll(resultados);
            });

            // Botones
            Button agregarNuevoButton = new Button("Agregar Nuevo");
            Button asignarButton = new Button("Asignar");
            Button cerrarButton = new Button("Cerrar");

            // Configuramos acciones para los botones
            agregarNuevoButton.setOnAction(event -> {                
                mostrarAlertaAgregarNuevo(combo,tipo) ;                
                close();
            });            

            asignarButton.setOnAction(event -> {
                // Lógica para el botón "Asignar"
                resultado = listView.getSelectionModel().getSelectedItems().get(0) ;
                agregaIndizacion(tipo,resultado) ;
                close();
            });

            cerrarButton.setOnAction(event -> {
                // Cierra la ventana modal cuando se presiona el botón "Cerrar"
                close();
            });

            // Agregamos los componentes al contenedor VBox
            VBox contenedorPrincipal = new VBox(
                    new Label("Reg. exist:"), textField, listView,
                    new HBox(agregarNuevoButton, asignarButton, cerrarButton)
            );
            contenedorPrincipal.setPadding(new Insets(20));
            contenedorPrincipal.setSpacing(10);

            // Configuramos el contenedor principal
            Scene scene = new Scene(contenedorPrincipal, 500, 300);
            setScene(scene);
        }

        private void mostrarAlertaAgregarNuevo(ComboBox combo, String tipo) {
            // Creamos una nueva alerta
            Alert alertaAgregarNuevo = new Alert(Alert.AlertType.CONFIRMATION);
            alertaAgregarNuevo.setTitle("Agregar Nuevo");
            alertaAgregarNuevo.setHeaderText(null);

            // Creamos un TextField en la alerta
            TextField nuevoRegistroTextField = new TextField();            
            nuevoRegistroTextField.setPromptText("Nuevo Registro");

            // Configuramos el contenido de la alerta
            alertaAgregarNuevo.getDialogPane().setContent(nuevoRegistroTextField);

            // Configuramos los botones de la alerta
            alertaAgregarNuevo.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

            // Mostramos la alerta y esperamos a que el usuario haga clic en un botón
            alertaAgregarNuevo.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Lógica para el botón "Aceptar" en la alerta de agregar nuevo
                    String nuevoRegistro = nuevoRegistroTextField.getText();
                    agregaIndizacion(tipo,nuevoRegistro) ;
                    agregaContenidoCombo(combo, tipo);
                }
            });
        }

        private void agregaIndizacion(String tipo, String selectedItem) {
            String consulta = "" ;
            switch(tipo){
                case "persona":
                    consulta = "INSERT INTO indizimagenes (barcode,nombramiento,materia,personaEnImagen,"
                            + "lugarEnImagen,objetoEnImagen,eventoEnImagen,institucionEnImagen) " 
                            + "VALUES ('"+getBarcode(nombramiento)+"','"+nombramiento+"', '', '"+selectedItem+"', '', "
                            + "'', '', '');" ;
                    break;
                case "lugar":
                    consulta = "INSERT INTO indizimagenes (barcode,nombramiento,materia,personaEnImagen,"
                            + "lugarEnImagen,objetoEnImagen,eventoEnImagen,institucionEnImagen) " 
                            + "VALUES ('"+getBarcode(nombramiento)+"','"+nombramiento+"', '', '', '"+selectedItem+"', "
                            + "'', '', '');" ;
                    break;
                case "objeto":
                    consulta = "INSERT INTO indizimagenes (barcode,nombramiento,materia,personaEnImagen,"
                            + "lugarEnImagen,objetoEnImagen,eventoEnImagen,institucionEnImagen) " 
                            + "VALUES ('"+getBarcode(nombramiento)+"','"+nombramiento+"', '', '', '', '"+selectedItem+"', "
                            + "'', '');" ;
                    break;
                case "evento":
                    consulta = "INSERT INTO indizimagenes (barcode,nombramiento,materia,personaEnImagen,"
                            + "lugarEnImagen,objetoEnImagen,eventoEnImagen,institucionEnImagen) " 
                            + "VALUES ('"+getBarcode(nombramiento)+"','"+nombramiento+"', '', '', '','', '"+selectedItem+"', "
                            + " '');" ;
                    break;
                case "institucion":
                    consulta = "INSERT INTO indizimagenes (barcode,nombramiento,materia,personaEnImagen,"
                            + "lugarEnImagen,objetoEnImagen,eventoEnImagen,institucionEnImagen) " 
                            + "VALUES ('"+getBarcode(nombramiento)+"','"+nombramiento+"', '', '', '','','', '"+selectedItem+"');" ;
                    break;
            }
            cron.consultaSimple(consulta, 1) ;
        }

        private String getBarcode(String nombramiento) {
            // Find the index of the first '_' character in the 'nombramiento' string
            int firstUnderscoreIndex = nombramiento.indexOf('_');
            // Find the index of the second '_' character in the 'nombramiento' string
            int secondUnderscoreIndex = nombramiento.indexOf('_', firstUnderscoreIndex + 1);
            // Extract the barcode substring from the 'nombramiento' string
            String barcode = nombramiento.substring(firstUnderscoreIndex + 1, secondUnderscoreIndex);
            // Now 'barcode' variable contains the value 'FO045061'
            return barcode ;
        }
    }
    
    public BorderPane getRoot() {
        return root ;
    }
    
     public static Image loadTiffImage(File tiffFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(tiffFile);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
