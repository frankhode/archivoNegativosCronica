/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class Recortes {

    private Funciones cron;
    private boolean hayRecortes ;
    List<String[]> resultados ;

    // Dimensiones fijas para el recorte; ajusta según sea necesario.
    private static final double CROP_WIDTH = 200;
    private static final double CROP_HEIGHT = 200;

    /**
     * Constructor que recibe una lista de barcodes.
     *
     * @param cron instancia de Funciones para acceder a la base de datos y otras utilidades.
     * @param barcodes lista de códigos de barras (String) para los cuales se buscarán recortes.
     */
    public Recortes(Funciones cron, List<String> barcodes) {
        this.cron = cron;
        hayRecortes = false ;
        for (String barcode : barcodes) {
            String query = "SELECT recortadoDe, xval, yval, ancho, alto FROM recortes "
                    + "WHERE barcode = '" + barcode + "'";
            resultados = cron.consultaCompleta(query);
            if (!resultados.isEmpty()) {
                hayRecortes = true ;
            }            
        }
    }

    public Recortes(Funciones cron, String barcode) {
        this(cron, Arrays.asList(barcode));
    }

    public void showRecortesWindow() {
        List<ImageView> recorteViews = new ArrayList<>();
        if (hayRecortes) {
            for (String[] row : resultados) {
                System.out.println(Arrays.toString(row));
                // row[0] = recortadoDe, row[1] = xval, row[2] = yval
                String recortadoDe = row[0];
                double xVal, yVal, cropWidth, cropHeight;
                try {
                    xVal = Double.parseDouble(row[1]);
                    yVal = Double.parseDouble(row[2]);
                    cropWidth = Double.parseDouble(row[3]);  // ancho real del recorte
                    cropHeight = Double.parseDouble(row[4]); // alto real del recorte

                } catch (NumberFormatException e) {
                    // Si no se pueden parsear los valores, se omite este recorte.
                    continue;
                }

                File imageFile = new File(recortadoDe);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);
                    // Define el viewport para mostrar sólo la parte recortada de la imagen.
                    imageView.setViewport(new Rectangle2D(xVal, yVal, cropWidth, cropHeight));
                    imageView.setFitWidth(cropWidth);
                    recorteViews.add(imageView);
                }
            }
        }
        
        // Se organiza la visualización de las imágenes en un FlowPane y se incorpora en un ScrollPane.
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.getChildren().addAll(recorteViews);

        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Visualizador de Recortes");

        Scene scene = new Scene(scrollPane, 800, 600);
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public boolean hayRecortes() {
        return hayRecortes ;
    }

    public void showRecortesSlideshowZoom() {
        List<Image> images = new ArrayList<>();
        if (hayRecortes) {
            for (String[] row : resultados) {
                String recortadoDe = row[0];
                File imageFile = new File(recortadoDe);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    images.add(image);
                }
            }
        }

        if (images.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No se encontraron recortes para mostrar.");
            alert.showAndWait();
            return;
        }

        // ImageView para la imagen actual.
        ImageView imageView = new ImageView(images.get(0));
        imageView.setPreserveRatio(true);

        // Grupo que contendrá la imagen, al que aplicamos transformaciones para zoom y pan.
        Group imageGroup = new Group(imageView);

        // Transformaciones para zoom (Scale) y movimiento (Translate)
        Scale scaleTransform = new Scale(1, 1, 0, 0);
        Translate translate = new Translate(0, 0);
        imageGroup.getTransforms().addAll(translate, scaleTransform);

        // Pane contenedor en lugar de ScrollPane.
        Pane pane = new Pane(imageGroup);
        pane.setPrefSize(800, 550); // 550px de alto para dejar espacio a la barra de navegación.

        // Ajusta la imagen al tamaño del Pane centrando y escalando inicialmente.
        Runnable adjustScaleAndCenter = () -> {
            if (imageView.getImage() != null) {
                double paneWidth = pane.getWidth();
                double paneHeight = pane.getHeight();
                double imgWidth = imageView.getImage().getWidth();
                double imgHeight = imageView.getImage().getHeight();
                double scaleX = paneWidth / imgWidth;
                double scaleY = paneHeight / imgHeight;
                double initialScale = Math.min(scaleX, scaleY);
                scaleTransform.setX(initialScale);
                scaleTransform.setY(initialScale);
                // Centrar la imagen en el Pane
                double offsetX = (paneWidth - imgWidth * initialScale) / 2;
                double offsetY = (paneHeight - imgHeight * initialScale) / 2;
                translate.setX(offsetX);
                translate.setY(offsetY);
            }
        };
        // Se ejecuta una vez que ya se han definido las dimensiones del Pane.
        Platform.runLater(adjustScaleAndCenter);

        // Variables para el panning (movimiento) con el mouse.
        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];
        final double[] initialTranslateX = new double[1];
        final double[] initialTranslateY = new double[1];

        pane.setOnMousePressed((MouseEvent event) -> {
            mouseAnchorX[0] = event.getSceneX();
            mouseAnchorY[0] = event.getSceneY();
            initialTranslateX[0] = translate.getX();
            initialTranslateY[0] = translate.getY();
        });

        pane.setOnMouseDragged((MouseEvent event) -> {
            double deltaX = event.getSceneX() - mouseAnchorX[0];
            double deltaY = event.getSceneY() - mouseAnchorY[0];
            translate.setX(initialTranslateX[0] + deltaX);
            translate.setY(initialTranslateY[0] + deltaY);
        });

        // Implementación del zoom con la rueda del mouse.
        pane.setOnScroll((ScrollEvent event) -> {
            double delta = event.getDeltaY();
            double zoomFactor = (delta > 0) ? 1.1 : 0.9;
            scaleTransform.setX(scaleTransform.getX() * zoomFactor);
            scaleTransform.setY(scaleTransform.getY() * zoomFactor);
            event.consume();
        });

        // Controles de navegación.
        Button btnPrev = new Button("Anterior");
        Button btnNext = new Button("Siguiente");
        Label lblCounter = new Label("1/" + images.size());
        final int[] currentIndex = {0};

        btnPrev.setOnAction(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                imageView.setImage(images.get(currentIndex[0]));
                lblCounter.setText((currentIndex[0] + 1) + "/" + images.size());
                // Al cambiar la imagen se reajusta la escala y se centra
                adjustScaleAndCenter.run();
            }
        });

        btnNext.setOnAction(e -> {
            if (currentIndex[0] < images.size() - 1) {
                currentIndex[0]++;
                imageView.setImage(images.get(currentIndex[0]));
                lblCounter.setText((currentIndex[0] + 1) + "/" + images.size());
                adjustScaleAndCenter.run();
            }
        });

        // Organización de la ventana.
        HBox navigation = new HBox(10);
        navigation.setAlignment(Pos.CENTER);
        navigation.getChildren().addAll(btnPrev, lblCounter, btnNext);

        BorderPane root = new BorderPane();
        root.setCenter(pane);
        root.setBottom(navigation);

        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Slideshow con Zoom y Pan");
        stage.setScene(scene);
        stage.showAndWait();
    }

}
