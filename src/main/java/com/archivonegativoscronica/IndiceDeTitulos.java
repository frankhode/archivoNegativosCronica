/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

final class IndiceDeTitulos {
    private final TableView table;
    private final Funciones cron;
    private BarraDeNavegacion bn;
    private File folder;
    private String consulta;
    List<String[]> digitales;
    
    // Constructor para paginación: inicio, cantidad, etc.
    public IndiceDeTitulos(Funciones cron, int inicio, int cantidad, String comienzo, int total, Tab tab) {
        table = new TableView();
        this.cron = cron;
        folder = cron.folder;
        cron.shortCutTab(tab);
        
        // Consulta para obtener los registros de titulos
        consulta = "SELECT sys,titulo,fecha,barcode FROM titulos "
                + "WHERE titulo >= '" + comienzo + "' ORDER BY titulo "
                + "LIMIT " + inicio + ", " + cantidad + " ;";
        
        bn = new BarraDeNavegacion(inicio, cantidad, comienzo, total);
        bn.ocultaComboBox();
        
        List<String[]> resultados = cron.consultaCompleta(consulta);
        
        // Se definen las columnas: Título, Fecha, Registro, Digital y Recortes
        TableColumn col1 = new TableColumn("Título");
        TableColumn col2 = new TableColumn("Fecha");
        TableColumn col3 = new TableColumn(""); // Ver registro
        TableColumn col4 = new TableColumn(""); // Ver digital
        TableColumn col5 = new TableColumn("Recortes"); // Nueva columna
        
        table.getColumns().addAll(col1, col2, col3, col4, col5);
        
        col1.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        col2.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        col3.setCellValueFactory(new PropertyValueFactory<>("verReg"));
        col4.setCellValueFactory(new PropertyValueFactory<>("verDigi"));
        col5.setCellValueFactory(new PropertyValueFactory<>("verRecortes"));
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        // Se itera sobre los resultados de la consulta
        resultados.forEach((t) -> {
            // t[0]=sys, t[1]=titulo, t[2]=fecha, t[3]=barcode
            String sys = t[0];
            String titulo = t[1];
            String fecha = t[2];
            String barcode = t[3];
            
            Button verReg = new Button("Ver registro");
            verReg.setOnAction((event) -> {
                new OpacWeb(sys);
            });
            
            Button verRecortes = new Button("Ver recortes");
            Recortes recortes = new Recortes(cron, barcode);
            if (!recortes.hayRecortes()) {
                verRecortes.setDisable(true);
            }
            verRecortes.setOnAction((event) -> {                
                recortes.showRecortesSlideshowZoom();
            });
            
            // Se evalúa si hay digitalización y se asigna el botón correspondiente.
            if (getDigi(barcode)) {
                Button verDigi = new Button("Ver digital");
                verDigi.setOnAction((event) -> {
                    List<String[]> consultaCompleta;
                    consulta = "SELECT carpeta,nombramiento FROM digitales WHERE "
                            + "(inv ='" + barcode + "'); ";
                    consultaCompleta = cron.consultaCompleta(consulta);
                    cron.abreConjunto(consultaCompleta);
                });
                data.add(new Tabla(titulo, fecha, verReg, verDigi, verRecortes));
            } else {
                data.add(new Tabla(titulo, fecha, verReg, verRecortes));
            }
        });
        
        table.setItems(data);
    }
    
    // Constructor para búsqueda por tema y tipo
    public IndiceDeTitulos(Funciones cron, String tema, String tipo) {
        table = new TableView();
        this.cron = cron;
        this.folder = cron.folder;
        
        switch (tipo) {
            case "043":
                consulta = "SELECT t.sys,t.nroA,t.titulo,t.fecha,t.barcode FROM titulos t "
                        + "JOIN areas a ON t.sys = a.sys WHERE a.area LIKE "
                        + "(SELECT cod FROM mapaareas WHERE espaniol LIKE'" + tema + "') "
                        + "ORDER BY t.titulo ;";
                break;
            case "245":
                consulta = "SELECT sys,nroA,titulo,fecha,barcode FROM titulos "
                        + "WHERE titulo LIKE '" + tema + "' "
                        + "ORDER BY titulos.titulo ;";
                break;
            case "campeonato":
                consulta = "SELECT titulos.sys,titulos.nroA,titulos.titulo,titulos.fecha,"
                        + "titulos.barcode, partidos.equipo1, partidos.equipo2, partidos.cancha "
                        + "FROM titulos "
                        + "JOIN partidos ON partidos.barcode = titulos.barcode "
                        + "WHERE partidos.tituloReg LIKE '" + tema + "' "
                        + "ORDER BY titulos.titulo ;";
                break;
            default:
                consulta = "SELECT materias.sys,nroA,titulo,fecha,barcode FROM titulos "
                        + "JOIN materias ON materias.sys = titulos.sys "
                        + "WHERE materias.materia LIKE '" + tema + "' "
                        + "ORDER BY titulos.titulo ;";
                break;
        }
        
        bn = new BarraDeNavegacion(0, 100, "", 100);
        
        List<String[]> resultados = cron.consultaCompleta(consulta);
        
        // Definición de columnas: Nro. original, Título, Fecha, Registro, Digital y Recortes.
        TableColumn col1 = new TableColumn("Nro. original");
        TableColumn col2 = new TableColumn("Título");
        TableColumn col3 = new TableColumn("Fecha");
        TableColumn col4 = new TableColumn("Registro");
        TableColumn col5 = new TableColumn("Digital");
        TableColumn col6 = new TableColumn("Recortes"); // Nueva columna
        
        table.getColumns().addAll(col1, col2, col3, col4, col5, col6);
        
        if (tipo.equals("campeonato")) {
            TableColumn col7 = new TableColumn("Equipo 1");
            TableColumn col8 = new TableColumn("Equipo 2");
            TableColumn col9 = new TableColumn("Cancha");
            table.getColumns().addAll(col7, col8, col9);
            col7.setCellValueFactory(new PropertyValueFactory<>("equipo1"));
            col8.setCellValueFactory(new PropertyValueFactory<>("equipo2"));
            col9.setCellValueFactory(new PropertyValueFactory<>("cancha"));
        }
        
        col1.setCellValueFactory(new PropertyValueFactory<>("nroA"));
        col2.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        col3.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        col4.setCellValueFactory(new PropertyValueFactory<>("verReg"));
        col5.setCellValueFactory(new PropertyValueFactory<>("verDigi"));
        col6.setCellValueFactory(new PropertyValueFactory<>("verRecortes"));
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        resultados.forEach((t) -> {
            String sys = t[0];
            String nroA = t[1];
            String titulo = t[2];
            String fecha = t[3];
            String barcode = t[4];
            
            Button verReg = new Button("Ver registro");
            verReg.setOnAction((event) -> {
                new OpacWeb(sys);
            });
            
            Button verRecortes = new Button();
            //Button verRecortes = new Button("Ver ed. impresa");
            Recortes recortes = new Recortes(cron, barcode);
            if (!recortes.hayRecortes()) {
                verRecortes.setText("Ver edición impresa");
                    verRecortes.setOnAction((event) -> {
                    //recortes.showRecortesSlideshowZoom();
                    //recortes.showRecortesWindow();
                    EdicionImpresa ed = new EdicionImpresa(barcode, cron) ;
                });
            } else {
                //verRecortes.setDisable(true);
                verRecortes.setText("Ver recortes");
                    verRecortes.setOnAction((event) -> {
                    //recortes.showRecortesSlideshowZoom();
                    recortes.showRecortesWindow();
                    //EdicionImpresa ed = new EdicionImpresa(barcode, cron) ;
                });                
            }
            
            boolean tieneDigi = false;
            Button verDigi = new Button("Ver digital");
            if (getDigi(barcode)) {
                tieneDigi = true;
                verDigi.setOnAction((event) -> {
                    cron.ejecutarEnSegundoPlano(
                        () -> {
                            digitales = cron.getDigitales(barcode);
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
            
            if (tieneDigi) {
                if (tipo.equals("campeonato")) {
                    String equipo1 = t[5];
                    String equipo2 = t[6];
                    String cancha = t[7];
                    data.add(new Tabla(nroA, titulo, fecha, verReg, verDigi, verRecortes, equipo1, equipo2, cancha));
                } else {
                    data.add(new Tabla(nroA, titulo, fecha, verReg, verDigi, verRecortes));
                }
            } else {
                if (tipo.equals("campeonato")) {
                    String equipo1 = t[5];
                    String equipo2 = t[6];
                    String cancha = t[7];
                    data.add(new Tabla(nroA, titulo, fecha, verReg, verRecortes, equipo1, equipo2, cancha));
                } else {
                    data.add(new Tabla(nroA, titulo, fecha, verReg, verRecortes));
                }
            }
        });
        
        table.setItems(data);
    }
    
    public TableView getTable() {        
        return table;
    }
    
    private boolean getDigi(String barcode) {        
        consulta = "SELECT inv FROM digitales WHERE inv ='" + barcode + "'";
        return !cron.consultaSimple(consulta, 1).isEmpty();
    }    
    
    public BarraDeNavegacion getNavegacion() {
        return bn;
    }
    
    // Clase interna Tabla para representar cada fila de la tabla
    public static class Tabla {
        private SimpleStringProperty titulo, fecha, nroA;
        private Button verReg, verDigi, verRecortes;
        private SimpleStringProperty equipo1, equipo2, cancha;
        
        // Constructor para registros sin nroA (por el primer constructor) sin digital
        private Tabla(String titulo, String fecha, Button verReg, Button verRecortes) {
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verRecortes = verRecortes;
        }
        
        // Constructor para registros sin nroA con digital
        private Tabla(String titulo, String fecha, Button verReg, Button verDigi, Button verRecortes) {
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verDigi = verDigi;
            this.verRecortes = verRecortes;
        }
        
        // Constructor para registros con nroA sin digital (segundo constructor)
        private Tabla(String nroA, String titulo, String fecha, Button verReg, Button verRecortes) {
            this.nroA = new SimpleStringProperty(nroA);
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verRecortes = verRecortes;
        }
        
        // Constructor para registros con nroA con digital
        private Tabla(String nroA, String titulo, String fecha, Button verReg, Button verDigi, Button verRecortes) {
            this.nroA = new SimpleStringProperty(nroA);
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verDigi = verDigi;
            this.verRecortes = verRecortes;
        }
        
        // Constructor para campeonato con digital
        private Tabla(String nroA, String titulo, String fecha, Button verReg, Button verDigi, Button verRecortes,
                      String equipo1, String equipo2, String cancha) {
            this.nroA = new SimpleStringProperty(nroA);
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verDigi = verDigi;
            this.verRecortes = verRecortes;
            this.equipo1 = new SimpleStringProperty(equipo1);
            this.equipo2 = new SimpleStringProperty(equipo2);
            this.cancha = new SimpleStringProperty(cancha);
        }
        
        // Constructor para campeonato sin digital
        private Tabla(String nroA, String titulo, String fecha, Button verReg, Button verRecortes,
                      String equipo1, String equipo2, String cancha) {
            this.nroA = new SimpleStringProperty(nroA);
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.verReg = verReg;
            this.verRecortes = verRecortes;
            this.equipo1 = new SimpleStringProperty(equipo1);
            this.equipo2 = new SimpleStringProperty(equipo2);
            this.cancha = new SimpleStringProperty(cancha);
        }
        
        // Getters y setters
        public String getEquipo1() {
            return equipo1.get();
        }
        
        public void setEquipo1(SimpleStringProperty equipo1) {
            this.equipo1 = equipo1;
        }
        
        public String getEquipo2() {
            return equipo2.get();
        }
        
        public void setEquipo2(SimpleStringProperty equipo2) {
            this.equipo2 = equipo2;
        }
        
        public String getCancha() {
            return cancha.get();
        }
        
        public void setCancha(SimpleStringProperty cancha) {
            this.cancha = cancha;
        }
        
        public String getNroA() {
            return nroA.get();
        }
        
        public void setNroA(SimpleStringProperty nroA) {
            this.nroA = nroA;
        }
        
        public String getTitulo() {
            return titulo.get();
        }
        
        public void setTitulo(SimpleStringProperty titulo) {
            this.titulo = titulo;
        }
        
        public String getFecha() {
            return fecha.get();
        }
        
        public void setFecha(SimpleStringProperty fecha) {
            this.fecha = fecha;
        }
        
        public Button getVerReg() {
            return verReg;
        }
        
        public void setVerReg(Button verReg) {
            this.verReg = verReg;
        }
        
        public Button getVerDigi() {
            return verDigi;
        }
        
        public void setVerDigi(Button verDigi) {
            this.verDigi = verDigi;
        }
        
        public Button getVerRecortes() {
            return verRecortes;
        }
        
        public void setVerRecortes(Button verRecortes) {
            this.verRecortes = verRecortes;
        }
    }
}
