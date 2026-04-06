/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class EdicionImpresa {
    private List<String> paginas;
    private String fecha, barcode, ed;
    private ImageView izquierda;
    private ImageView derecha;
    private ComboBox<String> edicionesBox;
    private double recorteInicioX, recorteInicioY;
    private final Funciones cron;
    private boolean hayArchivos;
    private List<String> edicionesDisponibles;
    private Rectangle recorteSeleccion;
    private boolean modoRecorteActivo = false;
    private int paginaActual = 0;
    private Button botonSiguiente, botonAnterior;
    private Label fechaLabel;
    
    // Variables para pan
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double initialTranslateX;
    private double initialTranslateY;
    
    DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Nuevo campo: Group que contendrá el contenedor de imágenes
    private Group imageGroup;

    public EdicionImpresa(String file, Funciones cron) {
        this.cron = cron;
        if (file.length() == 8) {
            barcode = file ;
        } else {
            getBarcode(file);
        }
        if (getFecha()) {
            edicionesDisponibles = obtenerEdiciones();
            if (hayArchivos) {
                this.paginas = obtenerImagenes();
                mostrarVentana();
            }
        }
    }
    
    private void mostrarVentana() {
        Stage stage = new Stage();

        // Contenedor principal: VBox para tener zonas fijas en top y bottom
        VBox root = new VBox();

        // --- Zona Superior ---
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setStyle("-fx-padding: 10px;");
        top.setMinHeight(50);

        Label labelEdicion = new Label("Edición: ");
        edicionesBox = new ComboBox<>(FXCollections.observableArrayList(edicionesDisponibles));
        edicionesBox.setOnAction(e -> actualizarEdicion());

        // Separador vertical
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);

        // Botón "Ver anterior"
        Button btnVerAnterior = new Button("Ver anterior");
        btnVerAnterior.setOnAction(e -> cambiarFecha(-1));

        // Etiqueta para mostrar la fecha actual (suponemos que 'fecha' ya tiene un valor inicial)
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate currentDate = LocalDate.parse(fecha, inputFormatter);
        fechaLabel = new Label(currentDate.format(displayFormatter));

        // Botón "Ver posterior"
        Button btnVerPosterior = new Button("Ver posterior");
        btnVerPosterior.setOnAction(e -> cambiarFecha(1));
        
        // Botón para activar/desactivar el modo recorte
        Button btnActivarRecorte = new Button("Activar Recorte");
        btnActivarRecorte.setOnAction(e -> activarModoRecorte());
        
        // Botón para guardar el recorte
        Button btnGuardarRecorte = new Button("Guardar Recorte");
        btnGuardarRecorte.setOnAction(e -> guardarRecorte());

        // Agregamos los controles al HBox top
        top.getChildren().addAll(labelEdicion, edicionesBox, separator, btnVerAnterior, fechaLabel, btnVerPosterior, btnActivarRecorte, btnGuardarRecorte);

        // --- Zona Central ---
        // Aquí va la parte de las imágenes (por ejemplo, usando StackPane y Group como antes)
        HBox contenedor = new HBox(10);
        contenedor.setAlignment(Pos.CENTER);

        izquierda = new ImageView();
        derecha = new ImageView();
        izquierda.setPreserveRatio(true);
        derecha.setPreserveRatio(true);
        izquierda.setFitWidth(400);
        izquierda.setFitHeight(600);
        derecha.setFitWidth(400);
        derecha.setFitHeight(600);
        contenedor.getChildren().addAll(izquierda, derecha);

        imageGroup = new Group(contenedor);

        StackPane centerPane = new StackPane(imageGroup);
        centerPane.setStyle("-fx-background-color: transparent;");
        // Aplicamos un clip para que el contenido central no se salga de su área
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(centerPane.widthProperty());
        clip.heightProperty().bind(centerPane.heightProperty());
        centerPane.setClip(clip);

        agregarEventos(centerPane);

        // --- Zona Inferior ---
        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER);
        bottom.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px;");
        bottom.setMinHeight(50);

        botonAnterior = new Button("<<");
        botonAnterior.setOnAction(e -> cambiarPagina(-2));
        botonSiguiente = new Button(">>");
        botonSiguiente.setOnAction(e -> cambiarPagina(2));
        bottom.getChildren().addAll(botonAnterior, botonSiguiente);

        // Agregamos las tres zonas al VBox principal
        root.getChildren().addAll(top, centerPane, bottom);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setTitle("Edición Impresa");
        stage.show();

        cargarImagenes(paginaActual);
    }

    // Método para cambiar la fecha sumándole o restándole días
    private void cambiarFecha(int dias) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate currentDate = LocalDate.parse(fecha, inputFormatter);
            LocalDate newDate = currentDate.plusDays(dias);
            // Mantener el formato interno
            fecha = newDate.format(inputFormatter);
            // Actualizar la etiqueta en formato amigable
            fechaLabel.setText(newDate.format(displayFormatter));

            // Refrescar ediciones e imágenes según la nueva fecha
            edicionesDisponibles = obtenerEdiciones();
            edicionesBox.setItems(FXCollections.observableArrayList(edicionesDisponibles));
            paginas = obtenerImagenes();
            paginaActual = 0;
            cargarImagenes(paginaActual);
        } catch (Exception e) {
            System.out.println("Error al cambiar la fecha: " + e.getMessage());
        }
    }

    // Ajustamos los eventos para que actúen sobre el imageGroup dentro del StackPane
    private void agregarEventos(StackPane pane) {
        // Zoom: se aplica al imageGroup
        pane.setOnScroll(event -> {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            imageGroup.setScaleX(imageGroup.getScaleX() * zoomFactor);
            imageGroup.setScaleY(imageGroup.getScaleY() * zoomFactor);
            event.consume();
        });

        // Pan o movimiento: se mueve el imageGroup sin afectar los controles fijos
        pane.setOnMousePressed(event -> {
            if (modoRecorteActivo && event.isPrimaryButtonDown()) {
                iniciarRecorteGeneric(event, imageGroup.getChildren());
            } else if (event.isPrimaryButtonDown()) {
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
                initialTranslateX = imageGroup.getTranslateX();
                initialTranslateY = imageGroup.getTranslateY();
            }
        });

        pane.setOnMouseDragged(event -> {
            if (modoRecorteActivo && event.isPrimaryButtonDown()) {
                ajustarRecorte(event);
            } else if (event.isPrimaryButtonDown()) {
                double deltaX = event.getSceneX() - mouseAnchorX;
                double deltaY = event.getSceneY() - mouseAnchorY;
                imageGroup.setTranslateX(initialTranslateX + deltaX);
                imageGroup.setTranslateY(initialTranslateY + deltaY);
            }
        });
    }
    
    // Método genérico para iniciar el recorte en el contenedor (usando la lista de nodos)
    private void iniciarRecorteGeneric(MouseEvent event, ObservableList<Node> children) {
        if (!modoRecorteActivo) return; // Solo si el modo recorte está activado

        // Convertir las coordenadas de la escena a las coordenadas locales de imageGroup
        Point2D localPoint = imageGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
        recorteInicioX = localPoint.getX();
        recorteInicioY = localPoint.getY();

        recorteSeleccion.setX(recorteInicioX);
        recorteSeleccion.setY(recorteInicioY);
        recorteSeleccion.setWidth(0);
        recorteSeleccion.setHeight(0);

        if (!children.contains(recorteSeleccion)) {
            children.add(recorteSeleccion);
        }
    }

    private void ajustarRecorte(MouseEvent event) {
        if (!modoRecorteActivo || recorteSeleccion == null) return;

        // Convertir las coordenadas de la escena a las coordenadas locales del imageGroup
        Point2D localPoint = imageGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
        double currentX = localPoint.getX();
        double currentY = localPoint.getY();

        double ancho = currentX - recorteInicioX;
        double alto = currentY - recorteInicioY;

        recorteSeleccion.setWidth(Math.abs(ancho));
        recorteSeleccion.setHeight(Math.abs(alto));

        if (ancho < 0) {
            recorteSeleccion.setX(currentX);
        }
        if (alto < 0) {
            recorteSeleccion.setY(currentY);
        }
    }

    
    private void guardarRecorte() {
        if (!modoRecorteActivo || recorteSeleccion == null) return;

        // Determinar sobre qué imagen se aplicó el recorte.
        // En este ejemplo, si estamos en portada (paginaActual == 0), usamos la imagen 'derecha',
        // de lo contrario, usamos 'izquierda' (ajusta según tu lógica).
        ImageView targetImageView;
        if (paginaActual == 0 && !paginas.isEmpty()) {
            targetImageView = derecha;
        } else {
            targetImageView = izquierda;
        }

        // Obtener las coordenadas en la escala original de la imagen
        double[] originalCoords = getRecorteEnOriginalCoordinates(targetImageView, recorteSeleccion);
        double originalX = originalCoords[0];
        double originalY = originalCoords[1];
        double originalWidth = originalCoords[2];
        double originalHeight = originalCoords[3];

        // Determinar la referencia de la imagen actual ("recortadoDe")
        String recortadoDe = "";
        if (paginaActual == 0 && !paginas.isEmpty()) {
            recortadoDe = paginas.get(0);
        } else if (paginaActual < paginas.size()){
            recortadoDe = paginas.get(paginaActual);
        }
        
        recortadoDe = recortadoDe.replace("\\", "\\\\") ;

        System.out.println("Guardando recorte: barcode=" + barcode + ", recortadoDe=" + recortadoDe +
                           ", xval=" + originalX + ", yval=" + originalY);

        // Crear la consulta SQL para insertar en la tabla recortes
        String consulta = "INSERT INTO recortes (barcode, recortadoDe, xval, yval, ancho, alto) VALUES ('" 
                  + barcode + "', '" + recortadoDe + "', " + originalX + ", " + originalY 
                  + ", " + originalWidth + ", " + originalHeight + ")";
        cron.consultaSimple(consulta,1);

        modoRecorteActivo = false;
        ((Group) recorteSeleccion.getParent()).getChildren().remove(recorteSeleccion);
        recorteSeleccion = null;
    }

    
    private void activarModoRecorte() {
        modoRecorteActivo = !modoRecorteActivo;
        System.out.println("Modo Recorte: " + (modoRecorteActivo ? "Activado" : "Desactivado"));

        if (modoRecorteActivo) {
            recorteSeleccion = new Rectangle();
            recorteSeleccion.setStroke(Color.RED);
            recorteSeleccion.setFill(Color.TRANSPARENT);
            recorteSeleccion.setStrokeWidth(2);
        } else {
            recorteSeleccion = null;
        }
    }

    // Cargar imágenes según el índice actual (paginaActual)
    private void cargarImagenes(int indice) {
        if (indice == 0 && !paginas.isEmpty()) {
            izquierda.setImage(null);
            derecha.setImage(new Image(new File(paginas.get(0)).toURI().toString()));
        } else {
            if (indice < paginas.size()) {
                izquierda.setImage(new Image(new File(paginas.get(indice)).toURI().toString()));
            } else {
                izquierda.setImage(null);
            }
            if (indice + 1 < paginas.size()) {
                derecha.setImage(new Image(new File(paginas.get(indice + 1)).toURI().toString()));
            } else {
                derecha.setImage(null);
            }
        }
    }

    // Método para cambiar de página (vista de diario)
    private void cambiarPagina(int direccion) {
        System.out.println(paginaActual);
        if (direccion > 0) { // Avanzar
            if (paginaActual == 0) { // Desde la tapa, ir al primer par
                if (paginas.size() > 2) {
                    paginaActual = 1;
                }
            } else {
                if (paginaActual + 2 < paginas.size()) {
                    paginaActual += 2;
                }
            }
        } else { // Retroceder
            if (paginaActual == 2) {
                paginaActual = 0;
            } else if (paginaActual > 2) {
                paginaActual -= 2;
            }
        }
        cargarImagenes(paginaActual);
    }


    
    private boolean getFecha() {
        String consultaFecha = "SELECT fecha FROM titulos WHERE barcode ='" + barcode + "'";
        List<String> resultado = cron.consultaSimple(consultaFecha, 1);
        
        if (resultado.isEmpty() || resultado.get(0).isEmpty()) {
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setContentText("No hay fecha disponible para el archivo");
            al.show();
            return false;
        }
        
        fecha = resultado.get(0);
        return true;
    }

    private void getBarcode(String file) {
        String[] split = file.split("\\\\");        
        barcode = split[split.length - 2];
    }
    
    private List<String> obtenerImagenes() {
        String consulta = "SELECT folder,barcode FROM edicionimpresa WHERE " +
                "fechaiso ='" + fecha + "' AND ed ='" + ed + "'";        
        List<String[]> consultaCompleta = cron.consultaCompleta(consulta);
        List<String> files = new ArrayList<>();        
        consultaCompleta.forEach((t) -> {
            String archivo = "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Bajas\\Edicion impresa" 
                    + t[0] + File.separator + t[1] + ".jpg";
            files.add(archivo);
        });
        return files;
    }
    
    private List<String> obtenerEdiciones() {
        String consulta = "SELECT DISTINCT ed FROM edicionimpresa " +
                "WHERE fechaiso ='" + fecha + "'";
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);        
        hayArchivos = !consultaSimple.isEmpty();
        if (hayArchivos) {
            if (consultaSimple.contains("M")) {
                ed = "M";
                System.out.println("Contiene M");
            } else {
                ed = consultaSimple.get(0);
            }
        }        
        if (!hayArchivos) {
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setContentText("No hay diarios disponibles para la fecha");
            al.show();
        }        
        return consultaSimple;
    }

    private void setEd(String numero) {
        ed = numero;
    }

    private void actualizarEdicion() {
        setEd(edicionesBox.getValue());
        this.paginas = obtenerImagenes();
        paginaActual = 0;
        cargarImagenes(paginaActual);
    }
    
    private double[] getRecorteEnOriginalCoordinates(ImageView imageView, Rectangle recorte) {
        // Convertir las coordenadas del recorte (definidas en el imageGroup) a coordenadas de la escena
        Point2D sceneTopLeft = imageGroup.localToScene(recorte.getX(), recorte.getY());
        Point2D sceneBottomRight = imageGroup.localToScene(recorte.getX() + recorte.getWidth(),
                                                             recorte.getY() + recorte.getHeight());
        // Convertir las coordenadas de la escena al sistema local del imageView
        Point2D localTopLeft = imageView.sceneToLocal(sceneTopLeft);
        Point2D localBottomRight = imageView.sceneToLocal(sceneBottomRight);

        double displayedX = localTopLeft.getX();
        double displayedY = localTopLeft.getY();
        double displayedWidth = localBottomRight.getX() - displayedX;
        double displayedHeight = localBottomRight.getY() - displayedY;

        // Usar getBoundsInLocal para obtener el tamaño real mostrado de la imagen
        Bounds displayedBounds = imageView.getBoundsInLocal();
        double displayedTotalWidth = displayedBounds.getWidth();
        double displayedTotalHeight = displayedBounds.getHeight();

        Image img = imageView.getImage();
        double origWidth = img.getWidth();
        double origHeight = img.getHeight();

        double ratioX = origWidth / displayedTotalWidth;
        double ratioY = origHeight / displayedTotalHeight;

        double originalX = displayedX * ratioX;
        double originalY = displayedY * ratioY;
        double originalWidth = displayedWidth * ratioX;
        double originalHeight = displayedHeight * ratioY;

        return new double[]{originalX, originalY, originalWidth, originalHeight};
    }

}
