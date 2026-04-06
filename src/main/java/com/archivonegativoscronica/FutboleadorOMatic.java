/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.layout.ColumnConstraints;

public class FutboleadorOMatic {
    Funciones cron;
    CorregidorOMatic corr;
    String sysUnificador,tituloReg,barcode;
    int cont ;
    Label barcodeLabel,tituloSobreLabel,recortesLabel ;
    TextField fechaField,equipo1Field,equipo2Field,canchaField ;
    Button prevButton,guardarButton,nextButton,pegarRecorte,verRecortes ;
    List<String[]> sobres ;
    GridPane gridPane ;
    private final List<String> listaEquipos ;

    public FutboleadorOMatic(Funciones cron) {
        this.cron = cron;
        cont = 0 ;
        corr = new CorregidorOMatic(cron, true);
        corr.regExistentes();
        corr.verificador();
        sysUnificador = corr.sysUnificador;
        String consulta = "SELECT titulo,barcode FROM titulos WHERE sys LIKE '" + 
                sysUnificador + "' AND barcode NOT IN (SELECT barcode FROM partidos)";
        tituloReg = cron.consultaSimple("SELECT titulo245 FROM registros WHERE SYS LIKE '"+sysUnificador+
                "'", 1).get(0) ;
        sobres = cron.consultaCompleta(consulta);
        listaEquipos = cron.consultaSimple("SELECT DISTINCT materia AS valor " +
                "FROM materias WHERE campo = '610' "
                + "UNION "
                + "SELECT equipo1 AS valor FROM partidos "
                + "UNION "
                + "SELECT equipo2 AS valor FROM partidos;", 1) ;

        // Open the modal dialog to capture data for the partidos table
        openPartidosInputDialog();
    }

    private void openPartidosInputDialog() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Futboleador-O-Matic");

        gridPane = new GridPane();
        gridPane.setPrefSize(800, 200);
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        
        barcodeLabel = new Label() ;
        tituloSobreLabel = new Label() ;
        recortesLabel = new Label() ;
        verRecortes = new Button("Ver diarios") ;
        labelGeneral() ;
        gridPane.add(barcodeLabel, 0, 0, 2, 1);
        gridPane.add(tituloSobreLabel, 1, 0, 2, 1);
        gridPane.add(recortesLabel, 1, 1, 2, 1);
        gridPane.add(verRecortes, 2, 1);
        verRecortes.setOnAction((ActionEvent event) -> {
            EdicionImpresa edicionImpresa = new EdicionImpresa(sobres.get(cont)[1], cron);
        });

        // Add labels and text fields for other fields
        gridPane.add(new Label("Fecha:"), 0, 2);
        fechaField = new TextField(); // Add preloaded value if needed
        gridPane.add(fechaField, 1, 2);

        gridPane.add(new Label("Equipo 1:"), 0, 3);
        equipo1Field = new TextField();
        gridPane.add(equipo1Field, 1, 3, 2, 1);

        gridPane.add(new Label("Equipo 2:"), 0, 4);
        equipo2Field = new TextField();
        gridPane.add(equipo2Field, 1, 4, 2, 1);

        gridPane.add(new Label("Cancha:"), 0, 5);
        canchaField = new TextField();
        gridPane.add(canchaField, 1, 5, 2, 1); // Spanning from column 1 to 2
        
        /*TextFields.bindAutoCompletion(equipo1Field,listaEquipos);
        TextFields.bindAutoCompletion(equipo2Field,listaEquipos);
        TextFields.bindAutoCompletion(canchaField,listaEquipos);*/
        AutoCompleteHelper.bindAutoCompletion(equipo1Field, listaEquipos);
        AutoCompleteHelper.bindAutoCompletion(equipo2Field, listaEquipos);
        AutoCompleteHelper.bindAutoCompletion(canchaField, listaEquipos);

        // Add buttons for Prev, Guardar, and Next
        prevButton = new Button("Prev");
        prevButton.setOnAction(event -> {
            // Logic for going to the previous match data if applicable
            if (cont != 0) {
                cont-- ;
            }
            navegaSobres();
        });
        gridPane.add(prevButton, 0, 6);

        guardarButton = new Button("Guardar");
        guardarButton.setOnAction(event -> {
            // Logic for saving the current match data
            enviaData() ;
            guardarButton.setDisable(true);
        });
        gridPane.add(guardarButton, 1, 6);

        nextButton = new Button("Next");
        nextButton.setOnAction(event -> {
            // Logic for going to the next match data if applicable
            if (cont < sobres.size()-1) {
                cont++ ;
            }
            navegaSobres();
            fechaField.requestFocus();
        });
        gridPane.add(nextButton, 2, 6);
        pegarRecorte = new Button("Pegar recorte");
        pegarRecorte.setOnAction(event -> {
            //Recortes recorte = new Recortes(cron,sobres.get(cont)[1]) ;
        });
        gridPane.add(pegarRecorte, 3, 6);
        // Adjust column constraints to give more weight to text fields
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(40);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30);        
        gridPane.getColumnConstraints().addAll(col1, col2);
        
        verificaCargado() ;

        Scene scene = new Scene(gridPane);        
        stage.setScene(scene);        
        stage.showAndWait();
        
    }

    private void verificaCargado() {
        String consulta = "SELECT barcode FROM partidos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
        System.out.println(consulta);        
        if (!cron.consultaSimple(consulta, 1).isEmpty()) {
            consulta = "SELECT fecha FROM partidos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
            fechaField.setText(cron.consultaSimple(consulta, 1).get(0));
            consulta = "SELECT equipo1 FROM partidos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
            equipo1Field.setText(cron.consultaSimple(consulta, 1).get(0));
            consulta = "SELECT equipo2 FROM partidos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
            equipo2Field.setText(cron.consultaSimple(consulta, 1).get(0));
            consulta = "SELECT cancha FROM partidos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
            canchaField.setText(cron.consultaSimple(consulta, 1).get(0));
        } else {
            consulta = "SELECT fecha FROM titulos WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;
            fechaField.setText(cron.consultaSimple(consulta, 1).get(0));
            equipo1Field.setText("");
            equipo2Field.setText("");
            canchaField.setText("");
        }        
    }

    private void enviaData() {
        String consulta = "INSERT INTO partidos(barcode,tituloSobre,tituloReg,fecha,equipo1,"
                + "equipo2,cancha) VALUES ("
                + "'"+sobres.get(cont)[1]+"',"
                + "'"+escapeQuote(sobres.get(cont)[0])+"',"
                + "'"+escapeQuote(tituloReg)+"',"
                + "'"+fechaField.getText()+"',"
                + "'"+escapeQuote(equipo1Field.getText())+"',"
                + "'"+escapeQuote(equipo2Field.getText())+"',"
                + "'"+escapeQuote(canchaField.getText())+"')" ;
        System.out.println(consulta);
        cron.consultaSimple(consulta, 1) ;
    }

    private void navegaSobres() {
        if (cont == 0) {
            prevButton.setDisable(true);
        } else {
            prevButton.setDisable(false);
        }
        if (cont == sobres.size()-1) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
        guardarButton.setDisable(false);
        labelGeneral();
        verificaCargado();
    }

    private void labelGeneral() {
        // Add labels for Barcode and TituloSobre
        barcodeLabel.setText("Barcode: " + sobres.get(cont)[1]);
        tituloSobreLabel.setText("Sobre "+(cont+1)+" / "+sobres.size()+
                ": " + sobres.get(cont)[0]);
        recortesLabel() ;
    }

    private Label recortesLabel() {        
        String consulta = "SELECT count(barcode) FROM recortes WHERE barcode LIKE '"+
                sobres.get(cont)[1]+"'" ;        
        recortesLabel.setText("Recortes para "+
                sobres.get(cont)[1]+": "+cron.consultaSimple(consulta, 1).get(0)) ;
        return recortesLabel ;
    }
    
    private String escapeQuote(String text) {
        return text.replace("'", "''") ;
    }
}
