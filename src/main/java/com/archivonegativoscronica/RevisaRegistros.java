/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/**
 *
 * @author francisco.ortiz
 */
class RevisaRegistros {
    
    private final List<Registro> arrayRegistros ;
    private final TabPane pestagnas,resumenPane ;
    private final Tab tab ;

    public RevisaRegistros(Funciones cron) throws IOException {
        
        resumenPane = cron.getTabPane() ;
        tab = new Tab();
        cron.shortCutTab(tab);
        tab.setText("Aca") ;
        pestagnas = new TabPane() ;
        
        RegistroCronica rc = new RegistroCronica() ;
        arrayRegistros = rc.getArrayRegistros() ;
        
        Tab e = estadisticasTab() ;
        resumenPane.getTabs().add(e);        
        
        pestagnas.getTabs().add(tab);
        SelectionModel selectionModel = pestagnas.getSelectionModel();
        selectionModel.select(tab); //select by object
        
    }
    
    private Tab estadisticasTab() {
        Tab e = new Tab() ;
        e.setText("Estadísticas") ;
        e.setClosable(false);
        
        TableView table = new TableView() ;
        table.setEditable(true);
                
        TableColumn titulo = new TableColumn("Título");
        TableColumn fecha = new TableColumn("Fecha");
        TableColumn nroA = new TableColumn("Nro. Crónica");
        TableColumn inv = new TableColumn("Inventario");
        TableColumn bar = new TableColumn("Código de barras");
        TableColumn ufi = new TableColumn("Ubicación Física");
        TableColumn digi = new TableColumn("Digital");
        TableColumn cata = new TableColumn("Catalogador");
        TableColumn cataFecha = new TableColumn("Fecha de catalogación");
        table.getColumns().addAll(titulo,fecha,nroA,inv,bar,ufi,digi,cata,cataFecha) ;
        table.getSortOrder().add(inv);
               

        ObservableList<TablaEstadisticas> data = FXCollections.observableArrayList();
        
        //prepara los datos
        arrayRegistros.forEach((t) -> {
            
            String tit,fech,nA,in,ba,uf,dig,cat,catF ;
            tit = t.getTituloFormateado() ;
            try {
                fech = t.getFecha() ;
            } catch (Exception s) {
                fech = "" ;
            }
            
            List<Item> items = t.items ;
            for (Item item : items) {
                nA = item.getDescripcion() ;
                in = item.getInventario() ;
                ba = item.getBarcode() ;
                uf = item.getUfi() ;
                try {
                    dig = t.getDigital() ;
                } catch (Exception n) {
                    dig = "" ;
                }
                cat = t.getUltimoCatalogador() ;
                catF = t.getUltimaIntervencion() ;
                data.add(new TablaEstadisticas(tit, fech, nA, in, ba, uf, dig, cat, catF)) ;
            }
        });
        
        titulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        nroA.setCellValueFactory(new PropertyValueFactory<>("nroA"));
        inv.setCellValueFactory(new PropertyValueFactory<>("inv"));
        bar.setCellValueFactory(new PropertyValueFactory<>("bar"));
        ufi.setCellValueFactory(new PropertyValueFactory<>("ufi"));
        digi.setCellValueFactory(new PropertyValueFactory<>("digi"));
        cata.setCellValueFactory(new PropertyValueFactory<>("cata"));
        cataFecha.setCellValueFactory(new PropertyValueFactory<>("cataFecha"));
                
        table.setItems(data);
        
        //crea una vista con la lista de resultados        
        BorderPane bp = new BorderPane(table) ;
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);        
        spacer.setMinSize(1, 10);
        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);        
        spacer2.setMinSize(1, 10);
        Pane spacer3 = new Pane();
        HBox.setHgrow(spacer3, Priority.ALWAYS);        
        spacer3.setMinSize(10, 1);
        Pane spacer4 = new Pane();
        HBox.setHgrow(spacer4, Priority.ALWAYS);        
        spacer4.setMinSize(10, 1);
        bp.setTop(spacer);
        bp.setBottom(spacer2);
        bp.setLeft(spacer3);
        bp.setRight(spacer4);
        e.setContent(bp);
        return e ;
    }

    private Button botonVerRegistro(Registro t) {
        Button reg = new Button("Ver") ;
        reg.setOnAction((event) -> {
            PanelRegistroVistas ver = new PanelRegistroVistas(t) ;
            Alert a = new Alert(Alert.AlertType.INFORMATION) ;
            a.setGraphic(null);
            a.getDialogPane().setContent(reg);
        });
        return reg ;
    }

    public static class TablaEstadisticas {
        
        private SimpleStringProperty titulo,fecha,nroA,inv,bar,ufi,digi,cata,cataFecha;
                
        private TablaEstadisticas(String titulo,String fecha,
                String nroA,String inv,String bar,String ufi,String digi,
                String cata,String cataFecha){
            this.titulo = new SimpleStringProperty(titulo);
            this.fecha = new SimpleStringProperty(fecha);
            this.nroA = new SimpleStringProperty(nroA);
            this.inv = new SimpleStringProperty(inv);
            this.bar = new SimpleStringProperty(bar);
            this.ufi = new SimpleStringProperty(ufi);
            this.digi = new SimpleStringProperty(digi);
            this.cata = new SimpleStringProperty(cata);
            this.cataFecha = new SimpleStringProperty(cataFecha);
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

        public String getNroA() {
            return nroA.get();
        }

        public void setNroA(SimpleStringProperty nroA) {
            this.nroA = nroA;
        }

        public String getInv() {
            return inv.get();
        }

        public void setInv(SimpleStringProperty inv) {
            this.inv = inv;
        }
        
        public String getBar() {
            return bar.get();
        }

        public void setBar(SimpleStringProperty bar) {
            this.bar = bar;
        }

        public String getUfi() {
            return ufi.get();
        }

        public void setUfi(SimpleStringProperty ufi) {
            this.ufi = ufi;
        }

        public String getDigi() {
            return digi.get();
        }

        public void setDigi(SimpleStringProperty digi) {
            this.digi = digi;
        }

        public String getCata() {
            return cata.get();
        }

        public void setCata(SimpleStringProperty cata) {
            this.cata = cata;
        }

        public String getCataFecha() {
            return cataFecha.get();
        }

        public void setCataFecha(SimpleStringProperty cataFecha) {
            this.cataFecha = cataFecha;
        }

    }
    
    
    
}
