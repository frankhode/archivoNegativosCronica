/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
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
public final class Resultados {
        
    private List<Registro> arrayRegistros ;
    private Tab tab ;
    private final Funciones cron ;
    private File folder ;
    private String consulta ;
    private TabPane tabpane ;
    private boolean bool;

    public Resultados(Funciones cron, List<String> arrayTemas, TabPane tabpane) throws IOException, SQLException {        
        this.cron = cron ;
        folder = cron.folder ;
        this.tabpane = tabpane ;
        arrayRegistros = new ArrayList<>() ;        
        
        String termino = "" ;        
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(arrayTemas);         
        ArrayList<String> paraConsulta = new ArrayList<>(hashSet);
        
        switch(paraConsulta.size()){
            case 0 :
                mensajeSalida("Algo falló...") ;
                break;
            case 1 :
                termino = "(materias.materia='" + paraConsulta.get(0) ;
                break;
            default:
                termino = "(materias.materia='" + paraConsulta.get(0) ;
                for (int i = 1; i < paraConsulta.size(); i++) {
                    termino = termino + "') OR (materias.materia='" + paraConsulta.get(i) ;
                }
                break;
        }
        consulta = "SELECT registros.registro FROM registros "
                + "JOIN materias ON materias.sys=registros.sys WHERE "
                + termino+"')" ;
        
        if (envia(consulta)) {            
            tab = indiceTematico(paraConsulta);
            cron.shortCutTab(tab);
            this.tabpane.getTabs().add(tab) ;
            this.tabpane.getSelectionModel().select(tab);
        }
    }
    
    public Resultados(Funciones cron, String tema, TabPane tabpane,String tipo) throws IOException, SQLException {
        this.cron = cron ;
        folder = cron.folder ;
        this.tabpane = tabpane ;        
        IndiceDeTitulos index = new IndiceDeTitulos(cron,tema,tipo) ;
        
        HBox hp = botoneraRelaciones(tema) ;
        hp.setPadding(new Insets(5, 5, 5, 5));
        hp.setAlignment(Pos.BASELINE_RIGHT);
        
        Tab pestagna = new Tab(tema) ;
        cron.shortCutTab(pestagna);
        BorderPane bp = new BorderPane(index.getTable()) ;
        bp.setTop(hp);
        bp.setPadding(new Insets(15, 15, 15, 15));
                
        pestagna.setContent(bp);
        this.tabpane.getTabs().add(pestagna) ;
        this.tabpane.getSelectionModel().select(pestagna);
    }
    
    private boolean envia(String consulta) {
        List<String> consultaSimple = cron.consultaSimple(consulta, 1);
        if (!consultaSimple.isEmpty()) {
            consultaSimple.forEach((t) -> {
                Registro reg = new Registro(t) ;
                arrayRegistros.add(reg) ;
            });                
            return true ;
        } else {
            return false ;        
        }
    }
    
    private void mensajeSalida(String txt) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, txt, ButtonType.OK) ;
        a.setHeaderText(null);
        a.setGraphic(null);
        a.setTitle("Archivo Fotográfico del Diario Crónica");
        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("files/icon.png"));
        a.show();
    }

    private Tab indiceTematico(List<String> arrayTemas) {
        Tab it = new Tab() ;
        cron.shortCutTab(it);
        it.setText(arrayTemas.get(0)) ;
        //prepara los datos        
        List<Pair<String,Registro>> temas = new ArrayList<>() ;
        arrayRegistros.forEach((t) -> {
            String[] campos = {"600","610","611","630","650","651"};
            List<String> materias = t.getCampos(campos);
            if (materias.size() > 0) {
                //selecciona solo las materias dentro del array, 
                //no todas las del registro
                materias.forEach((m) -> {
                    try {
                        String[] mat = m.split(" -- ") ;
                        String materia = mat[0] ;
                        arrayTemas.forEach((w) -> {
                            //if (materia.equals(w)) {
                            if(materia.contains(w)){
                                temas.add(new Pair(m,t)) ;
                            }
                        }) ;
                    } catch (Exception e) {}                    
                }) ;
            }
        });
        
        //crea una vista con la lista de resultados
        listaResultados(it,temas) ;
        return it ;
    }
    
    private TableView creaTablaResultados(List<Pair<String, Registro>> lista) {
        TableView table = new TableView();
        TableColumn nombre = new TableColumn("Tema");
        TableColumn digitalizado = new TableColumn("Digitalizado");
        TableColumn individualOConjunto = new TableColumn("Individual o conjunto");
        TableColumn registro = new TableColumn(""); // Botón Ver registro / Ver sobres
        TableColumn recortesCol = new TableColumn("Recortes"); // Nueva columna para Ver recortes
        TableColumn cantRegs = new TableColumn("Cantidad de registros");

        table.getColumns().addAll(nombre, digitalizado, individualOConjunto, registro, recortesCol, cantRegs);
        table.getSortOrder().add(nombre);

        // Contador para repetidos
        List<String> contador = new ArrayList<>();
        lista.forEach((t) -> {
            contador.add(t.getKey());
        });

        // Unificación de registros por tema (como ya se hace)
        Map<String,List<Registro>> listaUnificada = new HashMap<>();
        Set<String> uniqueSet = new HashSet<>(contador);
        uniqueSet.forEach((temp) -> {
            lista.forEach((t) -> {
                if (t.getKey().equals(temp)) {
                    List<Registro> list;
                    if(listaUnificada.containsKey(temp)){
                        list = listaUnificada.get(temp);
                        list.add(t.getValue());
                    } else {
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
            String tema, dig, indiConj;
            Button btn;
            // Crea el botón para ver registro/sobres según la cantidad
            if (value.size() == 1) {
                btn = new Button("Ver registro");
                btn.setOnAction((event) -> {
                    new OpacWeb(value.get(0).getSys());
                });
                tema = key;
                dig = verificaDigi(value);
                indiConj = value.get(0).items.size() > 1 ? "Conjunto" : "Individual";
            } else {
                tema = key;
                btn = new Button("Ver sobres");
                btn.setOnAction((event) -> {
                    muestraRegistroMultiples(key, value);
                });
                dig = verificaDigi(value);
                indiConj = verificaConj(value);
            }
            // Crea el botón "Ver recortes"
            Button btnRecortes = new Button("Ver recortes");
            List<String> barcodes = new ArrayList<>() ;
            for (Item item : value.get(0).getItems()) {
                barcodes.add(item.getBarcode()) ;
            }
            Recortes recortes = new Recortes(cron, barcodes);
            if (!recortes.hayRecortes()) {
                btnRecortes.setDisable(true);
            }
            btnRecortes.setOnAction((event) -> {
                recortes.showRecortesSlideshowZoom();
            });

            data.add(new Tabla(tema, btn, dig, indiConj, Collections.frequency(contador, tema), btnRecortes));
        }

        // Asignar cell value factory a las columnas
        nombre.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        digitalizado.setCellValueFactory(new PropertyValueFactory<>("digitalizado"));
        individualOConjunto.setCellValueFactory(new PropertyValueFactory<>("indiConj"));
        registro.setCellValueFactory(new PropertyValueFactory<>("btn"));
        recortesCol.setCellValueFactory(new PropertyValueFactory<>("btnRecortes"));
        cantRegs.setCellValueFactory(new PropertyValueFactory<>("cantRegs"));

        table.setItems(data);
        return table;
    }

    
    private TableView creaTablaRegistrosMultiples(List<Pair<String, Registro>> lista) {
        // Crea una vista de tabla
        TableView table = new TableView();
        TableColumn nombre = new TableColumn("Titulo");
        TableColumn digitalizado = new TableColumn("Digitalizado");
        TableColumn individualOConjunto = new TableColumn("Individual o conjunto");
        TableColumn registro = new TableColumn(""); // Botón "Ver registro"
        TableColumn recortesCol = new TableColumn("Recortes"); // Nueva columna para "Ver recortes"
        TableColumn cantRegs = new TableColumn("Cantidad de registros");

        table.getColumns().addAll(nombre, digitalizado, individualOConjunto, registro, recortesCol, cantRegs);
        table.getSortOrder().add(nombre);

        ObservableList<Tabla> data = FXCollections.observableArrayList();

        lista.forEach((t) -> {
            // Botón para ver registro individual
            Button btnRegistro = new Button("Ver registro");
            btnRegistro.setOnAction((event) -> {
                new OpacWeb(t.getValue().getSys());
            });

            // Botón para ver recortes asociados
            Button btnRecortes = new Button("Ver recortes");
            List<String> barcodes = new ArrayList<>() ;
            for (Item item : t.getValue().getItems()) {
                barcodes.add(item.getBarcode()) ;
            }
            Recortes recortes = new Recortes(cron, barcodes);
            if (!recortes.hayRecortes()) {
                btnRecortes.setDisable(true);
            }
            btnRecortes.setOnAction((event) -> {
                recortes.showRecortesSlideshowZoom();
            });

            String dig = t.getValue().tieneDigital() ? "Si" : "No";
            String indiConj = t.getValue().items.size() > 1 ? "Conjunto" : "Individual";

            // Se asume que cada registro individual se cuenta como 1
            data.add(new Tabla(t.getValue().getTituloFormateado(), btnRegistro, dig, indiConj, 1, btnRecortes));
        });

        nombre.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        digitalizado.setCellValueFactory(new PropertyValueFactory<>("digitalizado"));
        individualOConjunto.setCellValueFactory(new PropertyValueFactory<>("indiConj"));
        registro.setCellValueFactory(new PropertyValueFactory<>("btn"));
        recortesCol.setCellValueFactory(new PropertyValueFactory<>("btnRecortes"));
        cantRegs.setCellValueFactory(new PropertyValueFactory<>("cantRegs"));

        table.setItems(data);
        return table;
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

    private void listaResultados(Tab ia, List<Pair<String, Registro>> tema) {
        TableView tablaResultados = creaTablaResultados(tema) ;
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

    private List<String> verificaRelacionados(String tema, String relacion) {
        bool = false ;
        List<String> salida = new ArrayList<>() ;
        
        consulta = "SELECT DISTINCT terminos.termino FROM terminos " +
                    "JOIN relaciones ON relaciones.id2 = terminos.id " +
                    "WHERE relacion LIKE '"+relacion+"' " +
                    "AND id1 LIKE (SELECT id FROM terminos WHERE termino LIKE '"
               + tema +"')" ;
        
        //System.out.println(consulta);
        
        List<String[]> resu = cron.consultaCompleta(consulta);
        System.out.println(resu.size());
        resu.forEach((t) -> {
            consulta = "SELECT materia FROM materias WHERE materia = '"+t[0]+"'" ;
            List<String[]> r = cron.consultaCompleta(consulta);
            if (r.size() > 0) {
                bool = true ;
                salida.add(t[0]) ;
            }
        });
        if (!bool) {
            if (!resu.isEmpty()) {
                resu.forEach((t) -> {
                    salida.add(t[0]) ;            
                });
            }
        }
        
        return salida ;
    }

    public HBox botoneraRelaciones(String tema) {
        HBox hb = new HBox() ;
        Pane space = new Pane() ;
        space.setPadding(new Insets(5,5,5,5));
        Label label ;
        
        //editor
        Button editarTermino =  new Button("Editar término");
        editarTermino.setOnAction((t) -> {
            try {
                TerminoTesauro termino = new TerminoTesauro(tema,cron) ;
            } catch (SQLException ex) {
                Logger.getLogger(Resultados.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        if (cron.getUser().getRol().equals("1")) {
            hb.getChildren().add(editarTermino) ;            
            hb.getChildren().add(space) ;
        }
        
        //verifica padres
        List<String> padres = verificaRelacionados(tema,"esHijoDe");
        if (!padres.isEmpty()) {            
            space = new Pane() ;
            label = new Label("Términos generales") ;
            hb.getChildren().add(label) ;
            hb.getChildren().add(comboRelacion(padres)) ;            
            hb.getChildren().add(space) ;
        }
        
        //verifica hijos
        List<String> hijos = verificaRelacionados(tema,"esPadreDe");
        if (!hijos.isEmpty()) {            
            label = new Label("Términos específicos ") ;
            hb.getChildren().add(label) ;
            hb.getChildren().add(comboRelacion(hijos)) ;            
            space = new Pane() ;
            space.setPadding(new Insets(5,5,5,5));
            hb.getChildren().add(space) ;
        }
        
        // verifica relacionados
        List<String> relacionados = verificaRelacionados(tema,"estaRelacionadoCon");
        if (!relacionados.isEmpty()) {            
            label = new Label("Términos relacionados ") ;
            hb.getChildren().add(label) ;
            hb.getChildren().add(comboRelacion(relacionados)) ;            
            space = new Pane() ;
            space.setPadding(new Insets(5,5,5,5));
            hb.getChildren().add(space) ;
        }
        
        //verifica gentilicios
        List<String> gentilicios = verificaRelacionados(tema,"tieneComoGentilicioA");
        if (!gentilicios.isEmpty()) {
            CheckBox check = new CheckBox("[todas las]") ;
            check.setOnAction((t) -> {
                try {
                    Resultados resultado = new Resultados(cron, tema+"%", tabpane,"tema") ;
                } catch (IOException | SQLException ex) {
                    Logger.getLogger(Resultados.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            label = new Label("Nacionalidades ") ;
            hb.getChildren().add(check) ;
            hb.getChildren().add(label) ;
            hb.getChildren().add(comboRelacion(gentilicios)) ;            
        }
        
        hb.getChildren().forEach((t) -> {
            
        });
        
        return hb ;
    }

    private ComboBox comboRelacion(List<String> lista) {        
        ObservableList<String> list = FXCollections.observableArrayList();
        lista.forEach((t) -> {
            list.add(t) ;
        });        
        ComboBox combo = new ComboBox(list) ;
        combo.valueProperty().addListener((obs, oldItem, tema) -> {
            try {
                Resultados resultado = new Resultados(cron, tema.toString(), tabpane,"tema") ;
            } catch (IOException | SQLException ex) {
                Logger.getLogger(Resultados.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return combo ;
    }

    public static class Tabla {
        private final SimpleStringProperty concepto, digitalizado, indiConj;
        private final SimpleIntegerProperty cantRegs;
        private Button btn;
        private Button btnRecortes; // Nuevo botón

        private Tabla(String con, Button reg, String di, String ic, int cant, Button recortes){
            concepto = new SimpleStringProperty(con);
            btn = reg;
            digitalizado = new SimpleStringProperty(di);
            indiConj = new SimpleStringProperty(ic);
            cantRegs = new SimpleIntegerProperty(cant);
            btnRecortes = recortes;
        }

        // Getters y setters para btnRecortes
        public Button getBtnRecortes() {
            return btnRecortes;
        }

        public void setBtnRecortes(Button btnRecortes) {
            this.btnRecortes = btnRecortes;
        }

        // (Mantener los otros getters y setters existentes)
        public String getConcepto() {
            return concepto.get();
        }

        public void setConcepto(String concepto) {
            this.concepto.set(concepto);
        }

        public String getDigitalizado() {
            return digitalizado.get();
        }

        public void setDigitalizado(String digitalizado) {
            this.digitalizado.set(digitalizado);
        }

        public int getCantRegs() {
            return cantRegs.get();
        }

        public void setCantRegs(int c) {
            this.cantRegs.set(c);
        }

        public String getIndiConj() {
            return indiConj.get();
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

    public static class TablaResumen {

        private SimpleStringProperty cataFecha;
        private SimpleIntegerProperty cantItems,cantRegs;
        
        private TablaResumen(String cataFecha,int cantItems, int cantRegs){
            this.cataFecha = new SimpleStringProperty(cataFecha);
            this.cantItems = new SimpleIntegerProperty(cantItems) ;
            this.cantRegs = new SimpleIntegerProperty(cantRegs) ;
        }
        
        public String getCataFecha() {
            return cataFecha.get();
        }

        public void setCataFecha(SimpleStringProperty cataFecha) {
            this.cataFecha = cataFecha;
        }
        
        public int getCantItems() {
            return cantItems.get() ;
        }

        public void setCantItems(SimpleIntegerProperty cantItems) {
            this.cantItems = cantItems;
        }
        
        public int getCantRegs() {
            return cantRegs.get() ;
        }

        public void setCantRegs(SimpleIntegerProperty cantRegs) {
            this.cantRegs = cantRegs;
        }
    }
    
}
