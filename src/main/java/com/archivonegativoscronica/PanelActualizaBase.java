/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author francisco.ortiz
 */
class PanelActualizaBase {
    Funciones cron ;
    PreparedStatement stmt ;
    int cant_consultas,cant_con ;
    RegistroCronica rc ;

    PanelActualizaBase(Funciones cron) throws IOException {
        this.cron = cron ;
        cant_consultas = 0 ;
        cant_con = 0 ;
        rc = new RegistroCronica() ;
        Stage primaryStage = new Stage() ;
        // Crear el panel principal
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        // Agregar las funciones
        root.getChildren().add(crearFuncion("Registros", () -> {
            try {
                this.cargaRegistros();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        
        root.getChildren().add(crearFuncion("Areas", () -> {
            try {
                this.cargaAreas();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        
        root.getChildren().add(crearFuncion("Materias", () -> {
            try {
                this.cargaMaterias();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        
        root.getChildren().add(crearFuncion("Términos", () -> {
            try {
                this.cargaTerminos();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        
        root.getChildren().add(crearFuncion("Items", () -> {
            try {
                this.cargaItems();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        
        root.getChildren().add(crearFuncion("Títulos", () -> {
            try {
                this.cargaTitulos();
            } catch (SQLException ex) {
                Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));        

        // Configurar la escena y mostrarla
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Actualización de la base bibliográfica");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
    }
    
    private HBox crearFuncion(String nombreFuncion, Runnable funcion) {
        // Crear la fila para la función
        HBox funcionRow = new HBox();
        funcionRow.setSpacing(10);        
        funcionRow.setAlignment(Pos.CENTER_LEFT);

        // Crear el texto con el nombre de la función
        Text nombreTexto = new Text(nombreFuncion);

        // Crear los círculos para indicar el estado
        Circle apagadoCircle = crearCirculo(Color.RED);
        Circle corriendoCircle = crearCirculo(Color.GREY);
        Circle finalizadoCircle = crearCirculo(Color.GREY);

        // Crear el botón para ejecutar la función
        Button ejecutarButton = new Button("Ejecutar");
        Platform.runLater(() -> {
            ejecutarButton.setOnAction(event -> {
                // Cambiar el estado del círculo a "corriendo" al ejecutar la función
                apagadoCircle.setFill(Color.GREY);
                corriendoCircle.setFill(Color.YELLOW);
                ejecutarButton.setDisable(true);

                // Crear un Task para ejecutar la función en segundo plano
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Llamar a la función correspondiente
                        funcion.run();
                        return null;
                    }
                };

                // Configurar acciones para actualizar la interfaz de usuario después de que se complete el Task
                task.setOnSucceeded(e -> {
                    // Cambiar el estado del círculo a "finalizado" después de ejecutar la función
                    corriendoCircle.setFill(Color.GREY);
                    finalizadoCircle.setFill(Color.GREEN);
                    ejecutarButton.setDisable(false);
                });

                task.setOnFailed(e -> {
                    // Manejar cualquier error o excepción que ocurra durante la ejecución del Task
                    Throwable exception = task.getException();
                    if (exception != null) {
                        exception.printStackTrace();
                    }

                    // Restaurar el estado inicial del círculo en caso de fallo
                    apagadoCircle.setFill(Color.RED);
                    corriendoCircle.setFill(Color.GREY);
                    finalizadoCircle.setFill(Color.GREY);
                    ejecutarButton.setDisable(false);
                });

                // Ejecutar el Task en un nuevo hilo utilizando un ExecutorService
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task);

                // Cerrar el ExecutorService después de que se complete el Task
                executor.shutdown();
            });
        });

        // Agregar los elementos a la fila
        HBox hb1 = new HBox(nombreTexto) ;
        hb1.setPrefWidth(100);
        HBox hb2 = new HBox(apagadoCircle, corriendoCircle, finalizadoCircle) ;
        hb2.setPrefWidth(180);
        HBox hb3 = new HBox(ejecutarButton) ;
        hb3.setPrefWidth(100);
        funcionRow.getChildren().addAll(hb1,hb2,hb3);

        return funcionRow;
    }

    private Circle crearCirculo(Color color) {
        Circle circle = new Circle(10);
        circle.setFill(color);
        return circle;
    }
    
    private void cargaRegistros() throws SQLException {
        cron.envia("TRUNCATE registros") ;
        try {        
            List<Registro> arrayRegistros = rc.getArrayRegistros() ;            
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT INTO archivoCronica.registros ("
                    + "sys,registro,titulo245) VALUES (?,?,?);");
            
            try {
                arrayRegistros.forEach((reg) -> {
                    String sys = reg.getSys() ;
                    String registro = reg.getRegistro() ;
                    String tit245 = reg.getTituloFormateado().replace(" [material gráfico].","") ;
                    try {
                        stmt.setString(1,sys);//sys
                        stmt.setString(2,registro);//registro completo
                        stmt.setString(3,tit245);//registro completo
                        stmt.addBatch();
                        
                        if (cant_consultas == 50) {
                            int [] results = stmt.executeBatch();
                            cron.conn.commit();
                            cant_con = cant_con+results.length ;
                            stmt = cron.conn.prepareStatement("INSERT INTO archivoCronica.registros ("
                                    + "sys,registro,titulo245) VALUES (?,?,?);");
                            //reinicia para la proxima consulta
                            cant_consultas = 0 ;
                        }
                        cant_consultas++;
                    } catch (SQLException ex) {
                        cron.mensajeSalida("SQLException: " + ex.getMessage() + "\n" +
                                "SQLState: " + ex.getSQLState()+ "\n" +
                                        "VendorError: " + ex.getErrorCode());
                    }
                });
                int [] results = stmt.executeBatch();
                cron.conn.commit();
            } catch (SQLException e) {
                System.out.println(e);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        cron.conn.close();
        cron.conectarMySQL() ;
    }    
    
    public void cargaAreas() throws SQLException {
        cron.envia("TRUNCATE areas") ;
        try {
            List<Registro> arrayRegistros = rc.getArrayRegistros() ;
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT INTO areas(sys, area) VALUES (?,?);");
            //inicia objeto materia y lista para almacenarlos
            try {
                arrayRegistros.forEach((reg) -> {
                    String sys = reg.getSys() ;
                    List<String> areas = reg.getAreas() ;
                    areas.forEach((area) -> {
                        area = area.substring(0,7) ;
                        try {
                            stmt.setString(1,sys);
                            stmt.setString(2,area);
                            stmt.addBatch();
                            
                            if (cant_consultas == 1000) {
                                int [] results = stmt.executeBatch();
                                cron.conn.commit();
                                cant_con = cant_con+results.length ;
                                stmt = cron.conn.prepareStatement("INSERT INTO areas(sys, area) VALUES (?,?);");
                                cant_consultas = 0 ;
                            }
                            cant_consultas++;
                        } catch (SQLException e) {
                            cron.mensajeSalida("Algo falló al cargar la tabla areas...\n"+e
                                    +"area: '"+area+"'");
                            System.out.println(reg.getRegistro());
                        }
                    });
                });
                int [] results = stmt.executeBatch();
                cron.conn.commit();                
            } catch (SQLException e) {                
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        cron.conn.close();
        cron.conectarMySQL() ;
    }
    
    public void cargaMaterias() throws SQLException {
        cron.envia("TRUNCATE materias") ;
        try {
            List<Registro> arrayRegistros = rc.getArrayRegistros() ;
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT INTO materias (sys,campo,materia,"
                    + "linea) VALUES (?,?,?,?);");
            //inicia objeto materia y lista para almacenarlos
            try {
                arrayRegistros.forEach((reg) -> {
                    String sys = reg.getSys() ;
                    try {
                        List<String> campos = new ArrayList<>() ;
                        campos.add("600") ;
                        campos.add("610") ;
                        campos.add("611") ;
                        campos.add("630") ;
                        campos.add("650") ;
                        campos.add("651") ;
                        campos.add("655") ;
                        
                        String[] split = reg.getRegistro().split("\n");
                        for (String linea : split) {
                            String campo = linea.substring(10,13) ;
                            if (campos.contains(campo)) {
                                /*String[] subcampos = linea.substring(18).split("\\$\\$.") ;
                                String materiaFormateada = subcampos[1] ;*/
                                String materiaFormateada = cron.formateaMateria(linea) ;                                
                                stmt.setString(1,sys);
                                stmt.setString(2,campo);
                                stmt.setString(3,materiaFormateada);
                                stmt.setString(4,linea);
                                stmt.addBatch();
                                
                            }
                        }
                        if (cant_consultas == 10) {
                            int [] results = stmt.executeBatch();
                            cron.conn.commit();
                            cant_con = cant_con+results.length ;
                            
                            stmt = cron.conn.prepareStatement("INSERT INTO materias (sys,campo,materia,"
                                    + "linea) VALUES (?,?,?,?);");
                            cant_consultas = 0 ;
                        }
                        cant_consultas++;
                    } catch (SQLException e) {
                        cron.mensajeSalida("Algo falló al cargar la tabla materias...\n"+e);
                    }
                });
                int [] results = stmt.executeBatch();
                cron.conn.commit();
            } catch (Exception e) {
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }        
        cron.conn.close();
        cron.conectarMySQL() ;
    }
    
    public void cargaTerminos() throws SQLException {        
        try {
            List<String> ter = new ArrayList<>() ;
            rc.getArrayRegistros().forEach((reg) -> {
                reg.getCampo("650").forEach((mat) -> {
                    if (mat.contains(" -- ")) {
                        String[] subcampos = mat.split(" -- ") ;
                        ter.add(subcampos[0]) ;
                    } else {
                        ter.add(mat) ;
                    }                    
                });
            });
            
            LinkedHashSet<String> hashSet = new LinkedHashSet<>(ter);
            ArrayList<String> listWithoutDuplicates = new ArrayList<>(hashSet);
            
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT IGNORE INTO terminos (termino) VALUES (?);");
            
            listWithoutDuplicates.forEach((t) -> {
                try {
                    stmt.setString(1,t);
                    stmt.addBatch();
                    
                    if (cant_consultas == 50) {
                        int [] results = stmt.executeBatch();
                        cron.conn.commit();
                        cant_con = cant_con+results.length ;
                        stmt = cron.conn.prepareStatement("INSERT IGNORE INTO terminos (termino) VALUES (?);");
                        //reinicia para la proxima consulta
                        cant_consultas = 0 ;
                    }
                    cant_consultas++;
                } catch (SQLException ex) {
                    cron.mensajeSalida("SQLException: " + ex.getMessage() + "\n" +
                            "SQLState: " + ex.getSQLState()+ "\n" +
                                    "VendorError: " + ex.getErrorCode());
                }
            });
            int [] results = stmt.executeBatch();
            cron.conn.commit();
        } catch (SQLException ex) {
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        cron.conn.close();
        cron.conectarMySQL() ;
    }
    
    public void cargaItems() throws SQLException{
        cron.envia("TRUNCATE items") ;
        try {
            List<Registro> arrayRegistros = rc.getArrayRegistros() ;
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT INTO items(sys,dato,barcode,ufi,nroA) "
                    + "VALUES (?,?,?,?,?);");
            //inicia objeto materia y lista para almacenarlos
            try {
                arrayRegistros.forEach((reg) -> {
                    String sys = reg.getSys() ;
                    List<Item> items = reg.items;
                    items.forEach((item) -> {
                        try {
                            stmt.setString(1,sys);
                            stmt.setString(2,item.imprimeItem());
                            stmt.setString(3,item.getBarcode());
                            stmt.setString(4,item.getUfi());
                            stmt.setString(5,item.getDescripcion());
                            stmt.addBatch();
                            
                            if (cant_consultas == 1000) {
                                int [] results = stmt.executeBatch();
                                cron.conn.commit();
                                cant_con = cant_con+results.length ;
                                stmt = cron.conn.prepareStatement("INSERT INTO items(sys,dato,barcode,ufi,nroA) "
                                        + "VALUES (?,?,?,?,?);");
                                cant_consultas = 0 ;
                            }
                            cant_consultas++;
                        } catch (SQLException e) {
                            cron.mensajeSalida("Algo falló al cargar la tabla areas...\n"+e
                                    +"area: '"+item.imprimeItem()+"'");
                        }
                    });
                });
                int [] results = stmt.executeBatch();
                cron.conn.commit();
            } catch (SQLException e) {
            }
        } catch (SQLException ex) {
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        cron.conn.close();
        cron.conectarMySQL() ;
    }
    
    public void cargaTitulos() throws SQLException {
        cron.envia("TRUNCATE titulos") ;
        try {
            List<Registro> arrayRegistros = rc.getArrayRegistros() ;
            //conecta y prepara el esquema de las consultas
            cron.conn.setAutoCommit(false);
            stmt = cron.conn.prepareStatement("INSERT INTO titulos(sys, titulo, nroA, barcode, ufi, fecha) "
                    + "VALUES (?,?,?,?,?,?);");
            //inicia objeto materia y lista para almacenarlos
            arrayRegistros.forEach((reg) -> {                
                if (!reg.items.isEmpty()) {
                    String sys = reg.getSys() ;
                    String titulo,nroA="",barcode="",ufi="",fecha ;
                    List<String> c505 = reg.getCampo("505");
                    if (c505.size() > 0) {
                        for (String campo : c505) {
                            String[] titulos = campo.split(" -- ") ;
                            for (String tit : titulos) {
                                try {
                                    nroA = tit.split("\\.")[0] ;                                    
                                } catch (Exception e) {
                                    nroA = "" ;
                                    System.out.print(sys+" -> tit");
                                }
                                
                                String[] f = tit.split(", ") ;
                                if (f.length > 1) {
                                    fecha = cron.formateaFecha(f[f.length-1]) ;
                                } else {
                                    fecha = "" ;
                                }
                                titulo = "" ;
                                try {
                                    titulo = tit.substring(nroA.length()+2) ;
                                } catch (Exception e) {
                                    System.out.println(reg.getRegistro());
                                }
                                
                                for (Item item : reg.items) {
                                    String descripcion = item.getDescripcion() ;
                                    if (nroA.equals(descripcion)) {
                                        barcode = item.getBarcode() ;
                                        ufi = item.getUfi() ;
                                    }
                                }
                                try {
                                    stmt.setString(1,sys);
                                    stmt.setString(2,titulo);
                                    stmt.setString(3,nroA);
                                    stmt.setString(4,barcode);
                                    stmt.setString(5,ufi);
                                    stmt.setString(6,fecha);
                                    stmt.addBatch();
                                    
                                    if (cant_consultas == 1000) {
                                        int [] results = stmt.executeBatch();
                                        cron.conn.commit();
                                        cant_con = cant_con+results.length ;
                                        stmt = cron.conn.prepareStatement("INSERT INTO titulos(sys, titulo, nroA, barcode, ufi, fecha) "
                                                + "VALUES (?,?,?,?,?,?);");
                                        cant_consultas = 0 ;
                                    }
                                    cant_consultas++;
                                } catch (SQLException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    } else {
                        titulo = reg.getTituloFormateado() ;
                        titulo = titulo.replace(" [material gráfico]","") ;
                        String[] f = titulo.split(", ") ;
                        fecha = cron.formateaFecha(f[f.length-1]) ;
                        try {
                            nroA = reg.items.get(0).getDescripcion() ;
                        } catch (Exception e) {
                            System.out.println(reg.getRegistro());
                        }
                        
                        barcode = reg.items.get(0).getBarcode();
                        ufi = reg.items.get(0).getUfi() ;
                        if (barcode.length() == 8) {
                            try {
                            stmt.setString(1,sys);
                            stmt.setString(2,titulo);
                            stmt.setString(3,nroA);
                            stmt.setString(4,barcode);
                            stmt.setString(5,ufi);
                            stmt.setString(6,fecha);
                            stmt.addBatch();
                            
                            if (cant_consultas == 1000) {
                                int [] results = stmt.executeBatch();
                                cron.conn.commit();
                                cant_con = cant_con+results.length ;
                                stmt = cron.conn.prepareStatement("INSERT INTO titulos(sys, titulo, nroA, barcode, ufi, fecha) "
                                        + "VALUES (?,?,?,?,?,?);");
                                cant_consultas = 0 ;
                            }
                            cant_consultas++;
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());                            
                        }
                        } else {
                            System.out.println("SYS:" + sys);   
                            System.out.println("TITULO:" + titulo);
                            System.out.println("NROA:" + nroA);
                            System.out.println("BARCODE:" + barcode);
                            System.out.println("UFI:" + ufi);
                            System.out.println("FECHA:" + fecha);
                        }
                        
                    }
                }
            });
        } catch (SQLException ex) {
            System.out.println(ex);
            Logger.getLogger(PanelActualizaBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            int [] results = stmt.executeBatch();
            //System.out.println(Arrays.toString(results));
            cron.conn.commit();
        } catch (SQLException e) {
            System.out.println(e);
        }        
        cron.conn.close();
        cron.conectarMySQL() ;
    }
   
}
