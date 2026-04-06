/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;

/**
 *
 * @author francisco.ortiz
 */
public class ResultadosArea {
    
    private final List<Registro> arrayRegistros ;
    private Tab tab ;
    private final Funciones cron ;
    private final File folder ;
    private final String area ;
    private String consulta ;

    public ResultadosArea(Funciones cron, String a, TabPane tabpane) {
        this.cron = cron ;
        this.folder = cron.folder ;
        this.area = a ;
        arrayRegistros = new ArrayList<>() ;
        
        consulta = "SELECT registros.registro "
                + "FROM registros JOIN areas ON areas.sys=registros.sys "
                + "WHERE (areas.area LIKE "
                + "(SELECT cod FROM mapaareas WHERE espaniol = '"+area+"'))" ;
        System.out.println(consulta);
        List<String> consultaSimple = cron.consultaSimple(consulta,1);
        consultaSimple.forEach((t) -> {
            arrayRegistros.add(new Registro(t)) ;
        });
        if (!consultaSimple.isEmpty()) {            
            tab = indiceTitulos();
            cron.shortCutTab(tab);
            tabpane.getTabs().add(tab) ;
            tabpane.getSelectionModel().select(tab);
        }
    }
    
    private Tab indiceTitulos() {
        Tab it = new Tab() ;
        cron.shortCutTab(tab);
        it.setText(area) ;
        //prepara los datos        
        List<Pair<String,Registro>> titulos = new ArrayList<>() ;
        arrayRegistros.forEach((reg) -> {
            String titulo = reg.getTituloFormateado() ;
            titulos.add(new Pair(titulo,reg)) ;
        });
        
        //crea una vista con la lista de resultados
        listaResultados(it,titulos) ;
        return it ;
    }
    
    private void listaResultados(Tab ia, List<Pair<String, Registro>> titulos) {
        TableView tablaResultados = creaTablaResultados(titulos) ;
        BorderPane bp = new BorderPane(tablaResultados) ;
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
        ia.setContent(bp);
    }
    
    private TableView creaTablaResultados(List<Pair<String, Registro>> lista) {
        //Crea una vista de tabla
        TableView table = new TableView() ;
        TableColumn nombre = new TableColumn("Titulo");
        TableColumn registro = new TableColumn("");
        TableColumn digitalizado = new TableColumn("Digitalizado");
        TableColumn individualOConjunto = new TableColumn("Individual o conjunto");
        TableColumn cantRegs = new TableColumn("Cantidad de registros");
        table.getColumns().addAll(nombre,digitalizado,individualOConjunto,registro,cantRegs) ;
        table.getSortOrder().add(nombre);

        //contador para repetidos
        List<String> contador = new ArrayList<>() ;
        lista.forEach((t) -> {
            contador.add(t.getKey()) ;
        });
        
        //convierte la lista inicial en una lista con unificaciones
        Map<String,List<Registro>> listaUnificada = new HashMap<>() ;
        Set<String> uniqueSet = new HashSet<>(contador);        
        uniqueSet.forEach((temp) -> {
            lista.forEach((t) -> {
                ArrayList<Registro> list;
                if (t.getKey().equals(temp)) {                    
                    if(listaUnificada.containsKey(temp)){
                        // if the key has already been used,
                        // we'll just grab the array list and add the value to it
                        list = (ArrayList<Registro>) listaUnificada.get(temp);
                        list.add(t.getValue());
                    } else {
                        // if the key hasn't been used yet,
                        // we'll create a new ArrayList<String> object, add the value
                        // and put it in the array list with the new key
                        list = new ArrayList<>();
                        list.add(t.getValue());
                        listaUnificada.put(temp, list);
                    }                    
                }
            });
        });
        
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        for (Map.Entry<String, List<Registro>> entry : listaUnificada.entrySet()) {
            String key = entry.getKey();
            List<Registro> value = entry.getValue();
            String tema,dig,indiConj ;
            Button btn ;
            List<Registro> get = listaUnificada.get(key);
            if (value.size() == 1) {
                btn = new Button("Ver registro") ;
                tema = key ;
                btn.setOnAction((event) -> {
                    OpacWeb ow = new OpacWeb(value.get(0).getSys()) ;                    
                });
                
                dig = verificaDigi(get) ;                
                
                List<Item> items = value.get(0).items;
                if (items.size() > 1) {
                    indiConj = "Conjunto" ;
                } else {
                    indiConj = "Individual" ;
                }
            } else {
                tema = key ;
                btn = new Button("Ver sobres") ;
                btn.setOnAction((event) -> {
                    muestraRegistroMultiples(key,value) ;
                });
                dig = verificaDigi(get) ;
                indiConj = verificaConj(get) ;                
            }
            data.add(new Tabla(tema,btn,dig,indiConj,Collections.frequency(contador, tema))) ;
        }
        
        nombre.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        registro.setCellValueFactory(new PropertyValueFactory<>("btn"));
        digitalizado.setCellValueFactory(new PropertyValueFactory<>("digitalizado"));
        individualOConjunto.setCellValueFactory(new PropertyValueFactory<>("indiConj"));
        cantRegs.setCellValueFactory(new PropertyValueFactory<>("cantRegs"));
        
        table.setItems(data);
        return table ;
    }
    
    private String verificaConj(List<Registro> get) {
        String indiConj = null ;
        for (Registro reg : get) {
            List<Item> items = reg.items;
            if (items.size() > 1) {
                indiConj = "Conjunto" ;
                return indiConj ;
            } else {
                indiConj = "Individual" ;
            }
        }
        return indiConj ;
    }
    
    private void muestraRegistro(Registro reg) throws SQLException {
        Alert a = new Alert(Alert.AlertType.NONE,null,ButtonType.CLOSE);
        a.setResizable(true);
        a.setGraphic(null);
        a.setHeaderText(null);
        a.setTitle(null);
        a.getButtonTypes().clear();                
        a.initStyle(StageStyle.UTILITY);
        Window window = a.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> a.hide());
        BorderPane bp = new BorderPane() ;
        if (reg.tieneDigital()) {
            String dig = reg.getDigital().replaceAll("BNA_", "") ;
            dig = dig.replaceAll(" ", "") ;
            List<String> digital = Arrays.asList(dig.split("\\,")) ;            
            creaBotoneraDigital(digital,bp) ;
        }
        bp.setCenter(new PanelRegistroVistas(reg).getCont()); 
        ScrollPane sp = new ScrollPane(bp) ;
        a.getDialogPane().setContent(sp) ;
        
        a.showAndWait() ; 
    }
    
    private void creaBotoneraDigital(List<String> digital, BorderPane bp) throws SQLException {
        List<String[]> consultaCompleta;
        consulta = "SELECT carpeta,nombramiento FROM digitales WHERE (inv ='" + digital.get(0) + "' " ;
        //digital.remove(0) ;
        try {
            digital.forEach((t) -> {
                consulta = consulta + " OR inv='" + t + "'" ;
            });
        } catch (Exception e) {/* solo un item */}
        consulta = consulta + ") ;" ;
        consultaCompleta = cron.consultaCompleta(consulta) ;
        consultaCompleta.forEach((t) -> {
            t[0] = t[0].replace("\\","\\\\") ;
        });
        
        Button btnDigi = new Button("Ver digital");
        bp.setTop(btnDigi);
        switch(consultaCompleta.size()){
            case 0:
                break;
            case 1 :
            default:
                btnDigi.setOnAction((event) -> {
                    cron.abreConjunto(consultaCompleta) ;
                }); 
                break;
        }
    }
    
    private String verificaDigi(List<Registro> get) {
        String dig = "" ;
        for (Registro regi : get) {
            if (regi.tieneDigital()) {
                dig = "Si" ;
                return dig ;
            } else {
                dig = "No" ;
            }
        }
        return dig ;
    }
    
    private void muestraRegistroMultiples(String tema, List<Registro> lista) {
        Alert a = new Alert(Alert.AlertType.NONE,null,ButtonType.CLOSE);
        a.setResizable(true);
        a.setGraphic(null);
        a.setHeaderText(null);
        a.setTitle(null);
        a.getButtonTypes().clear();                
        a.initStyle(StageStyle.UTILITY);
        Window window = a.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> a.hide());
        
        //crea la lista para mostrar la tabla
        List<Pair<String,Registro>> listaParaTabla = new ArrayList<>() ;
        lista.forEach((t) -> {            
            listaParaTabla.add(new Pair(tema,t)) ;
        });
                
        //crea una vista con la lista de resultados
        TableView tablaResultados = creaTablaRegistrosMultiples(listaParaTabla) ;
        tablaResultados.setMinSize(1000, 500);
        BorderPane bp = new BorderPane(tablaResultados) ;
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
        
        a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        a.getDialogPane().setContent(bp) ;
        a.showAndWait() ; 
    }
    
    private TableView creaTablaRegistrosMultiples(List<Pair<String, Registro>> lista) {
        //Crea una vista de tabla
        TableView table = new TableView() ;
        TableColumn nombre = new TableColumn("Titulo");
        TableColumn registro = new TableColumn("");
        TableColumn digitalizado = new TableColumn("Digitalizado");
        TableColumn individualOConjunto = new TableColumn("Individual o conjunto");
        TableColumn cantRegs = new TableColumn("Cantidad de registros");
        table.getColumns().addAll(nombre,digitalizado,individualOConjunto,registro,cantRegs) ;
        table.getSortOrder().add(nombre);
        ObservableList<Tabla> data = FXCollections.observableArrayList();
        
        lista.forEach((t) -> {
            Button btn = new Button("Ver registro") ;
            btn.setOnAction((event) -> {
                OpacWeb ow = new OpacWeb(t.getValue().getSys()) ;
            });
            String dig,indiConj ;
            if (t.getValue().tieneDigital()) {
                dig = "Si" ;                
            } else {
                dig = "No" ;
            }
            List<Item> items = t.getValue().items;
            if (items.size() > 1) {
                indiConj = "Conjunto" ;
            } else {
                indiConj = "Individual" ;
            }
            data.add(new Tabla(t.getValue().getTituloFormateado(),btn,dig,indiConj,1)) ;
        });
        
        nombre.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        registro.setCellValueFactory(new PropertyValueFactory<>("btn"));
        digitalizado.setCellValueFactory(new PropertyValueFactory<>("digitalizado"));
        individualOConjunto.setCellValueFactory(new PropertyValueFactory<>("indiConj"));
        cantRegs.setCellValueFactory(new PropertyValueFactory<>("cantRegs"));
        
        table.setItems(data);
        return table ;
    }
    
    public static class Tabla {
        private final SimpleStringProperty titulo,digitalizado,indiConj;
        private final SimpleIntegerProperty cantRegs;
        private Button btn ;
        private Tabla(String con,Button reg,String di,String ic,int cant){
            titulo = new SimpleStringProperty(con);
            btn = reg ;
            digitalizado = new SimpleStringProperty(di);
            indiConj = new SimpleStringProperty(ic);
            cantRegs = new SimpleIntegerProperty(cant) ;
        }

        public String getTitulo() {
            return titulo.get() ;
        }

        public void setTitulo(String titulo) {
            this.titulo.set(titulo);
        }
        
        public String getDigitalizado() {
            return digitalizado.get() ;
        }

        public void setDigitalizado(String digitalizado) {
            this.digitalizado.set(digitalizado);
        }
        
        public int getCantRegs() {
            return cantRegs.get() ;
        }

        public void setCantRegs(int c) {
            this.cantRegs.set(c);
        }
        
        public String getIndiConj() {
            return indiConj.get() ;
        }

        public void setIndiConj(String i) {
            this.indiConj.set(i);
        }

        public Button getBtn() {
            return btn;
        }

        public void setBtn(Button btn) {
            this.btn = btn;
        }
    }
    
    private ImageView createImageView(File imageFile, Stage stage) {
        // DEFAULT_THUMBNAIL_WIDTH is a constant you need to define
        // The last two arguments are: preserveRatio, and use smooth (slower)
        // resizing

        ImageView imageView = null;
        try {
            final Image image = new Image(new FileInputStream(imageFile), 250, 0, true,
                    true);
            imageView = new ImageView(image);
            imageView.setFitWidth(250);
            imageView.setOnMouseClicked((MouseEvent mouseEvent) -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        VisorImagenes zoom = new VisorImagenes(imageFile) ;
                    }
                }
            });
        } catch (FileNotFoundException ex) {
        }
        return imageView;
    }
}
