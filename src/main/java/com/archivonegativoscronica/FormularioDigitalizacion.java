/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class FormularioDigitalizacion {
    ComboBox<String> digitalizadores;
    TextField barcode, obs;
    ToggleGroup estSobre;
    String estSobreSt;
    RadioButton det1, det2, det3, det4, det5, det6;
    List<FilmData> filmDataList;
    Funciones cron ;

    // Predefined map of manufacturers and models for each film type and size
    private final Map<String, Map<String, String[]>> filmData = new HashMap<>();

    public FormularioDigitalizacion(Funciones cron) {
        this.cron = cron ;
        filmDataList = new ArrayList<>();

        // Populate film data
        populateFilmData();
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();

        // Creating sections
        VBox sections = new VBox();
        sections.setSpacing(10);

        // Section 1: Location and Barcode
        GridPane section1 = new GridPane();
        section1.setHgap(10);
        section1.setVgap(5);
        section1.setPadding(new Insets(10));

        section1.add(new Label("Digitalizador:"), 0, 0);
        digitalizadores = new ComboBox<>();
        digitalizadores.getItems().addAll("Carla", "Francisco", "Gabriela", "Guillermo", "Sofía", "Vanesa");
        section1.add(digitalizadores, 1, 0);

        section1.add(new Label("Código de barras:"), 0, 1);
        barcode = new TextField();
        section1.add(barcode, 1, 1);
        section1.add(separator1, 0, 2, 3, 1);

        Label est = new Label("Estado del sobre");
        RadioButton b1 = new RadioButton("B");
        RadioButton b2 = new RadioButton("R");
        RadioButton b3 = new RadioButton("M");
        estSobre = new ToggleGroup();
        b1.setToggleGroup(estSobre);
        b2.setToggleGroup(estSobre);
        b3.setToggleGroup(estSobre);
        estSobre.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                estSobreSt = selectedRadioButton.getText();
            } else {
                estSobreSt = "";
            }
        });
        HBox hb = new HBox(10, b1, b2, b3);
        hb.setPadding(new Insets(0));
        section1.add(est, 0, 3);
        section1.add(hb, 1, 3);
        section1.add(separator2, 0, 4, 3, 1);

        section1.add(new Label("Deterioros:"), 0, 5);
        det1 = new RadioButton("Ilegible por el escáner");
        det2 = new RadioButton("Adheridos entre sí");
        det3 = new RadioButton("Manchas (parcial o total)");
        det4 = new RadioButton("Rotura - faltante");
        det5 = new RadioButton("Pliegue - doblez");
        det6 = new RadioButton("Otros   ");
        TextField otros = new TextField();
        GridPane deterioros = new GridPane();
        deterioros.add(det1, 0, 0, 2, 1);
        deterioros.add(det2, 0, 1, 2, 1);
        deterioros.add(det3, 0, 2, 2, 1);
        deterioros.add(det4, 0, 3, 2, 1);
        deterioros.add(det5, 0, 4, 2, 1);
        deterioros.add(det6, 0, 5);
        deterioros.add(otros, 1, 5);
        section1.add(deterioros, 1, 5);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        deterioros.getColumnConstraints().addAll(columnConstraints);

        Label observ = new Label("Observaciones");
        obs = new TextField();
        section1.add(observ, 0, 8);
        section1.add(obs, 1, 8);

        sections.getChildren().add(section1);

        // Section 2: Film Negatives
        VBox section2Container = new VBox();
        section2Container.setSpacing(10);
        section2Container.setPadding(new Insets(10));

        ScrollPane section2ScrollPane = new ScrollPane(section2Container);
        section2ScrollPane.setFitToWidth(true);
        section2ScrollPane.setPrefHeight(800);

        Accordion section2Accordion = new Accordion();
        TitledPane filmsPane = new TitledPane("Films", section2ScrollPane);
        filmsPane.setExpanded(true);
        section2Accordion.getPanes().add(filmsPane);

        sections.getChildren().add(section2Accordion);

        // Add Film Button
        Button addFilmButton = new Button("Agregar");
        addFilmButton.setOnAction(e -> addFilmTypeSection(section2Container, addFilmButton));
        section2Container.getChildren().add(addFilmButton);

        // Botones
        Button guardar = new Button("Guardar");
        Button nuevo = new Button("Nuevo");
        Button cerrar = new Button("Cerrar");

        section1.add(guardar, 4, 1);
        section1.add(nuevo, 4, 2);
        section1.add(cerrar, 4, 3);

        guardar.setOnAction((event) -> {
            try {
                enviaDatos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Main layout
        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().add(sections);

        Scene scene = new Scene(mainLayout, 500, 600);
        Stage stage = new Stage() ;
        stage.setScene(scene);
        stage.setTitle("Formulario de Digitalización");
        stage.show();
    }

    // Method to add film type section
    private void addFilmTypeSection(VBox filmTypeContainer, Button addFilmButton) {
        GridPane section2 = new GridPane();
        section2.setHgap(10);
        section2.setVgap(5);
        section2.setPadding(new Insets(10));

        ToggleGroup formato = new ToggleGroup();
        RadioButton size35mm = new RadioButton("35mm");
        size35mm.setToggleGroup(formato);
        RadioButton size6x6 = new RadioButton("120");
        size6x6.setToggleGroup(formato);
        HBox sizeBox = new HBox(10, size35mm, size6x6);
        section2.add(new Label("Formato:"), 0, 0);
        section2.add(sizeBox, 1, 0);

        ToggleGroup polaridad = new ToggleGroup();
        RadioButton negativo = new RadioButton("Negativo");
        negativo.setToggleGroup(polaridad);
        RadioButton positivo = new RadioButton("Positivo");
        positivo.setToggleGroup(polaridad);
        HBox hbPol = new HBox(10, negativo, positivo);
        section2.add(new Label("Polaridad:"), 0, 1);
        section2.add(hbPol, 1, 1);

        ToggleGroup procToggleGroup = new ToggleGroup();
        RadioButton byn = new RadioButton("byn");
        byn.setToggleGroup(procToggleGroup);
        RadioButton col = new RadioButton("col.");
        col.setToggleGroup(procToggleGroup);
        HBox procBox = new HBox(10, byn, col);
        section2.add(new Label("Proceso:"), 0, 2);
        section2.add(procBox, 1, 2);

        section2.add(new Label("Cantidad de tiras:"), 0, 3);
        Spinner<Integer> cantTiras = new Spinner<>(1, 100, 1);
        section2.add(cantTiras, 1, 3);

        ComboBox<String> marcaComboBox = new ComboBox<>();
        ComboBox<String> modeloComboBox = new ComboBox<>();

        section2.add(new Label("Marca:"), 0, 4);
        section2.add(marcaComboBox, 1, 4);
        marcaComboBox.setItems(FXCollections.observableArrayList(filmData.get("full").keySet()));

        section2.add(new Label("Modelo:"), 0, 5);
        section2.add(modeloComboBox, 1, 5);
        
        section2.add(new Label("Cantidad de fotogramas:"), 0, 6);
        Spinner<Integer> cantFot = new Spinner<>(1, 100, 1);
        section2.add(cantFot, 1, 6);

        marcaComboBox.setOnAction(e -> {
            String selectedFilmType = "full"; // Assuming 'full' key represents the correct data, adjust as necessary
            String selectedBrand = marcaComboBox.getValue();
            modeloComboBox.setItems(FXCollections.observableArrayList(filmData.get(selectedFilmType).get(selectedBrand)));
        });

        filmTypeContainer.getChildren().add(filmTypeContainer.getChildren().size() - 1, section2);

        FilmData filmData = new FilmData(size35mm, size6x6, negativo, positivo, byn, col, 
                cantTiras, cantFot, marcaComboBox, modeloComboBox);
        filmDataList.add(filmData);
    }

    // Method to send data to a text file
    private void enviaDatos() throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = dtf.format(now);
        String consulta = "INSERT INTO conservaciondigi (fecha, agente, barcode, "
                + "estadodelsobre, deterioros, observaciones, formato, polaridad, "
                + "proceso, cantidadtiras, marca, modelo, cantidadfotogramas) VALUES (" ;
        
        for (FilmData film : filmDataList) {
            //fecha
            consulta = consulta + "'"+timestamp+"', " ;
            //agente
            consulta = consulta + "'"+digitalizadores.getValue()+"', " ;
            //barcode
            consulta = consulta + "'"+barcode.getText()+"', " ;
            //estadodelsobre
            consulta = consulta + "'"+estSobreSt+"', " ;
            //deterioros
            consulta = consulta + "'"+(det1.isSelected() ? det1.getText() + ", " : "") +
                        (det2.isSelected() ? det2.getText() + ", " : "") +
                        (det3.isSelected() ? det3.getText() + ", " : "") +
                        (det4.isSelected() ? det4.getText() + ", " : "") +
                        (det5.isSelected() ? det5.getText() + ", " : "") +
                        (det6.isSelected() ? det6.getText() + ": " + obs.getText() + ", " : "")
                    +"', " ;
            //observaciones
            consulta = consulta + "'"+obs.getText()+"', " ;
            //formato
            consulta = consulta + "'"+(film.size35mm.isSelected() ? film.size35mm.getText() : film.size6x6.getText())+"', " ;
            //polaridad 
            consulta = consulta + "'"+(film.negativo.isSelected() ? film.negativo.getText() : film.positivo.getText())+"', " ;
            //proceso
            consulta = consulta + "'"+(film.byn.isSelected() ? film.byn.getText() : film.col.getText())+"', " ;
            //cantidadtiras
            consulta = consulta + "'"+film.cant.getValue()+"', " ;
            //marca
            consulta = consulta + "'"+film.marcaComboBox.getValue()+"', " ;
            //modelo
            consulta = consulta + "'"+film.modeloComboBox.getValue()+"', " ;
            //cantidadfotogramas
            consulta = consulta + "'"+film.cantFot.getValue()+"')" ;
            
            List<String> consultaSimple = cron.consultaSimple(consulta, 0);
        }
    }

    // Method to populate film data
    private void populateFilmData() {
        Map<String, String[]> fullFrameBrandsAndModels = new HashMap<>();
        fullFrameBrandsAndModels.put("Ilford", new String[]
            {"FP3 Fine Grain Panchromatic", "Otros"});
        fullFrameBrandsAndModels.put("Agfa", new String[]
            {"PRO 200", "Vista 400","Otros"});
        fullFrameBrandsAndModels.put("Fuji", new String[]
            {"Sensia 400", "PROVIA 100", "PROVIA 100 Pro","Otros"});
        fullFrameBrandsAndModels.put("Kodak", new String[]
            {"Plus X","TRI-X","TMAX 400","Ektapress","Ektapress 100","Ektapress 400",
            "GOLD 100","GOLD 400","SUPRA 400","SUPRA 800","Kodakchrome","Otros"});
        fullFrameBrandsAndModels.put("Adox", new String[]
            {"R21","Otros"});
        fullFrameBrandsAndModels.put("Sin especificar", new String[]
            {"Otros"});        

        filmData.put("full", fullFrameBrandsAndModels);
    }

    // Data structure to hold film data
    private static class FilmData {
        RadioButton size35mm;
        RadioButton size6x6;
        RadioButton negativo;
        RadioButton positivo;
        RadioButton byn;
        RadioButton col;
        Spinner<Integer> cant, cantFot;
        ComboBox<String> marcaComboBox;
        ComboBox<String> modeloComboBox;

        FilmData(RadioButton size35mm, RadioButton size6x6, RadioButton negativo, RadioButton positivo, RadioButton byn,
                 RadioButton col, Spinner<Integer> cant, Spinner<Integer> cantFot, ComboBox<String> marcaComboBox, ComboBox<String> modeloComboBox) {
            this.size35mm = size35mm;
            this.size6x6 = size6x6;
            this.negativo = negativo;
            this.positivo = positivo;
            this.byn = byn;
            this.col = col;
            this.cant = cant;
            this.cantFot = cantFot;
            this.marcaComboBox = marcaComboBox;
            this.modeloComboBox = modeloComboBox;
        }
    }
}
