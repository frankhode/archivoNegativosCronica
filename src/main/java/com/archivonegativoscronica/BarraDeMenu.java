/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.AWTException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.TextInputDialog;

/**
 * Crea la barra de menun con las funciones asignadas
 * @author francisco.ortiz
 */
class BarraDeMenu {
    
    private final MenuBar menuBar ;
    private final Funciones cron ;
    private final Usuario usuario ;

    public BarraDeMenu(Usuario user, Funciones cron) {
        this.cron = cron ;
        usuario = user ;        
        this.cron.setUser(user);
        
        Menu tes = creaMenuTesauro() ;        
        Menu indices = creaMenuIndice() ;
        Menu ftb = creaMenuFutbol() ;
        Menu fichero = creaMenuFichero() ;
        Menu admin = creaMenuAdmin() ;
        Menu inventario = creaMenuInv() ;
        Menu util = creaMenuUtil() ;
        Menu ayuda = creaMenuAyuda() ;
        Menu digi = creaMenuDigitalizacion() ;
        Menu cat = creaMenuCatalogacion() ;
        Menu buscador = creaMenuBuscador() ;
        
        menuBar = new MenuBar();
        menuBar.setStyle("-fx-font-size: 16;");
        
        switch(usuario.getRol()){
            case "1":
                //administrador
                menuBar.getMenus().addAll(tes,indices,ftb, buscador,fichero,admin,inventario,
                        cat,digi,util,ayuda);
                break;
            case "2":
                if (usuario.getNivel().equals("A")) {
                    //usuario simple
                    menuBar.getMenus().addAll(indices,ftb, buscador,fichero,ayuda);
                } else {
                    //usuario avanzado
                    menuBar.getMenus().addAll(tes,indices,ftb, buscador,fichero,ayuda);
                }
                break;
            case "3":
                switch(usuario.getNivel()) {
                    case "A":
                        //catalogador nivel inventario
                        menuBar.getMenus().addAll(tes,indices,ftb, buscador,inventario,ayuda);
                        break;
                    case "B":
                        //catalogador nivel avanzado
                        menuBar.getMenus().addAll(tes,indices,ftb, buscador,fichero,inventario,util,ayuda);
                        break;
                    case "C":
                        //catalogador funciones internas
                        menuBar.getMenus().addAll(tes,indices,ftb, buscador,fichero,util,ayuda);
                        break;
                }                
            case "4":
                if (usuario.getNivel().equals("A")) {
                    //digitalizador simple
                    menuBar.getMenus().addAll(tes,indices,ftb, buscador,digi,ayuda);
                } else {
                    //digitalizador avanzado
                    menuBar.getMenus().addAll(tes,indices,ftb, buscador,digi,ayuda);
                }
                break;
        }        
    }

    public MenuBar getMenu() {
        return menuBar ;
    }

    private Menu creaMenuTesauro() {
        //////////////////////*  1. TESAURO  *//////////////////////////
        Menu tes = new Menu("Tesauro");
        MenuItem tes1 = new MenuItem("Editor de relaciones - Términos sin relaciones");
        tes1.setOnAction((event) -> {
            try {
                cron.editorInicial() ;
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });        
        MenuItem tes2 = new MenuItem("Editor de relaciones - Términos huérfanos");
        tes2.setOnAction((event) -> {
            try {
                cron.editorDeHuerfanos();
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        /*MenuItem tes3 = new MenuItem("Editor de términos");
        tes3.setOnAction((event) -> {
            try {
                cron.editorRelacionados();
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });*/
        MenuItem tes4 = new MenuItem("Ver Tesauro");
        tes4.setOnAction((event) -> {
            cron.muestraTesauro() ;
        });
        switch(usuario.getRol()){
            case "1":
                //administrador
                tes.getItems().addAll(tes1,tes2,tes4) ;                
                break;
            case "2":
                tes.getItems().addAll(tes4) ;
                break;
            case "3":
                tes.getItems().addAll(tes1,tes2,tes4) ;                
            case "4":
                tes.getItems().addAll(tes4) ;
                break;
        }
        return tes ;
    }

    public Menu creaMenuIndice() {
        Menu menuIndices = new Menu("Índices");

        MenuItem itemMarc = new MenuItem("Índices MARC...");

        itemMarc.setOnAction(e -> {
            // crear la vista unificada de índices
            IndiceMarc indiceMarc = new IndiceMarc(cron, cron.getTabPane());

            Tab tab = new Tab("Índices MARC");
            tab.setContent(indiceMarc);          // 👈 ya es un BorderPane entero

            cron.shortCutTab(tab);

            if (!cron.getTabPane().getTabs().contains(tab)) {
                cron.getTabPane().getTabs().add(tab);
            }
            cron.getTabPane().getSelectionModel().select(tab);
        });

        menuIndices.getItems().clear();
        menuIndices.getItems().add(itemMarc);

        return menuIndices;
    }

    
    private Menu creaMenuFutbol() {
        Menu index = new Menu("Fútbol");
        MenuItem index1 = new MenuItem("Campeonatos");
        index1.setOnAction((event) -> {
            Campeonatos camp = new Campeonatos(cron,cron.getTabPane()) ;
            cron.shortCutTab(camp);
            if (!cron.getTabPane().getTabs().contains(camp)) {
                cron.getTabPane().getTabs().add(camp) ;
            }
            cron.getTabPane().getSelectionModel().select(camp);
        });
        MenuItem index2 = new MenuItem("Equipos");
        index2.setOnAction((event) -> {
            IndiceEquipos eqp = new IndiceEquipos(cron,cron.getTabPane()) ;
            
        });
        index.getItems().addAll(index1,index2) ;
        return index ;
    }

    private Menu creaMenuFichero() {
        //////////////////////*  3. FICHERO  *//////////////////////////        
        Menu ficha = new Menu("Fichero");
        MenuItem ficha1 = new MenuItem("Ver fichero");
        ficha1.setOnAction((event) -> {cron.verFichero() ;});
        ficha.getItems().add(ficha1) ;
        return ficha ;
    }

    private Menu creaMenuAdmin() {
        //////////////////////*  4. ADMINISTRACION  *//////////////////////////        
        Menu admin = new Menu("Administración");        
        MenuItem admin1 = new MenuItem("Actualizar base");
        admin1.setOnAction((event) -> {
            try {
                PanelActualizaBase pab = new PanelActualizaBase(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });        
        Menu admin2 = new Menu("Base de datos");
        MenuItem admin2a = new MenuItem("Crear Base de datos");
        admin2a.setOnAction((event) -> {cron.creaBaseyTablas();});
        MenuItem admin2b = new MenuItem("Borrar Base de datos");
        admin2b.setOnAction((event) -> {cron.borraBaseyTablas();});
        admin2.getItems().addAll(admin2a,admin2b) ;
        
        Menu admin3 = new Menu("Digitales");        
        MenuItem admin3a = new MenuItem("Cargar tabla de digitales");
        admin3a.setOnAction((event) -> {
            cron.ejecutarEnSegundoPlano(
                    () -> {
                        // Llamar al método cargaDigitales, que lanza excepciones
                        cron.cargaDigitales();
                        return null; // Callable necesita retornar algo, aunque sea null
                    },
                    () -> {
                        // Acción cuando la tarea termine exitosamente (UI update)
                        cron.mensajeSalida("Carga digitales completada");
                    },
                    (ex) -> {
                        cron.mensajeSalida("Error en la carga digital: " + ex.getMessage());
                    }
            );
        });
        MenuItem admin3b = new MenuItem("Vaciar tabla de digitales");
        admin3b.setOnAction((event) -> {cron.vaciaTabla("digitales");});
        MenuItem admin3c = new MenuItem("Cargar digitales externos");
        admin3c.setOnAction((event) -> {
            try {
                cron.cargaDigitalesExterno();
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        admin3.getItems().addAll(admin3a,admin3b,admin3c) ;        
        
        Menu admin5 = new Menu("Edición impresa");
        MenuItem admin5a = new MenuItem("Cargar tabla de edición impresa");
        admin5a.setOnAction((event) -> {
            try {
                cron.cargaEdImpresa();
            } catch (IOException | SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        MenuItem admin5b = new MenuItem("Vaciar tabla de edición impresa");
        admin5b.setOnAction((event) -> {cron.vaciaTabla("edicionimpresa");});
        admin5.getItems().addAll(admin5a,admin5b) ;
        
        Menu admin6 = new Menu("Access");
        MenuItem admin6a = new MenuItem("Exportar tablas");
        admin6a.setOnAction((event) -> {
            try {
                ExportarParaAccess epa = new ExportarParaAccess(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        admin6.getItems().addAll(admin6a) ;
        
        Menu admin7 = new Menu("SQLite");
        MenuItem admin7a = new MenuItem("Cerar base SQLite");
        admin7a.setOnAction((event) -> {
            try {
                SQLite sqlite = new SQLite(cron) ;
            } catch (ClassNotFoundException | IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        admin7.getItems().addAll(admin7a) ;
        
        
        admin.getItems().addAll(admin1,admin2,admin3,
                admin5, admin6, admin7) ;
        return admin ;
    }

    private Menu creaMenuInv() {
        /////////////////////  REGISTROS DE INVENTARIO  //////////////////////////        
        Menu menureg = new Menu("Inventario");
        MenuItem menu5a = new MenuItem("Ver registros de inventario");
        menu5a.setOnAction((event) -> {
            
        });
        MenuItem menu5b = new MenuItem("Inventario puntual x nroA") ;
        menu5b.setOnAction((var event) -> {
            InputStream in = getClass().getResourceAsStream("/files/rangos_ufi.txt");
            if (in == null) {
                System.err.println("⚠️ No se encontró /files/rangos_ufi.txt dentro del JAR.");
                return;
            }
            String lista = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Parseamos la lista
            List<InventarioPorCajon.RangeUfi> rangos =
                    InventarioPorCajon.RangeUfiParser.parse(lista);

            try {
                // Lanzamos la función “sin conocer la UFI”
                InventarioPorCajon inventarioPorCajon = new InventarioPorCajon(cron, rangos);                
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        MenuItem menu5c = new MenuItem("Inventario por cajón") ;
        menu5c.setOnAction((event) -> {
            try {            
                InventarioPorCajon inv = new InventarioPorCajon(cron) ;
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        MenuItem menu5d = new MenuItem("Importar inventario desde excel") ;
        menu5d.setOnAction((event) -> {
            ImportadorInventarioUI iiui = new ImportadorInventarioUI(cron.getStage(),cron) ;
        });
        menureg.getItems().addAll(menu5a,menu5b,menu5c,menu5d) ;        
        return menureg ;
    }

    private Menu creaMenuUtil() {
        //////////////////////*  UTILIDADES  *//////////////////////////
        Menu util = new Menu("Utilidades"); 
        //Corregidor
        MenuItem util1 = new MenuItem("Corregidor-o-Matic") ;        
        util1.setOnAction((event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Tipo de programa");

            ButtonType buttonTypeOne = new ButtonType("COM por cat");
            ButtonType buttonTypeTwo = new ButtonType("COM por ufi");

            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

            alert.showAndWait().ifPresent(response -> {
                if (response == buttonTypeOne) {
                    try {
                        CorregidorOMatic com = new CorregidorOMatic(cron) ;
                    } catch (AWTException ex) {
                        Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (response == buttonTypeTwo) {
                    try {
                        CorregidorOMatic com = new CorregidorOMatic(cron,"") ;
                    } catch (AWTException ex) {
                        Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
        });
        //Revisar registros por catalogador y fecha
        MenuItem util2 = new MenuItem("Registros por catalogador y fecha") ;
        util2.setOnAction((event) -> {
            try {
                RevisaRegistros rr = new RevisaRegistros(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Buscar registros para agregar
        MenuItem util3 = new MenuItem("Unificador-o-matic") ;
        util3.setOnAction((event) -> {
            Unificador cpa = new Unificador(cron) ;            
        });
        
        //Des-unificador-O-Matic
        MenuItem util4 = new MenuItem("Des-unificador-O-Matic") ;
        util4.setOnAction((event) -> {
            try {
                DesUnificadorOMatic duom = new DesUnificadorOMatic(cron) ;
            } catch (SQLException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Exceleador-O-Matic
        MenuItem util5 = new MenuItem("Exceleador-O-Matic") ;
        util5.setOnAction((event) -> {            
            ExceleadorOMatic eom = new ExceleadorOMatic(cron) ;
        });
        
        //Coleccionador-O-Matic
        MenuItem util6 = new MenuItem("Coleccionador-O-Matic") ;
        util6.setOnAction((event) -> {
            ColeccionadorOMatic eom = new ColeccionadorOMatic(cron) ;
        });
        
        //Listador O-Matic
        MenuItem util7 = new MenuItem("Listador-O-Matic") ;
        util7.setOnAction((event) -> {
            try {
                ListadorOMatic lom = new ListadorOMatic(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Verificador de titulos <-> 505
        MenuItem util8 = new MenuItem("Verificador de títulos <-> 505/items") ;
        util8.setOnAction((event) -> {
            VerificadorDeTitulos vdt = new VerificadorDeTitulos(cron) ;
        });
        
        //Reubicar
        MenuItem util9 = new MenuItem("Reubicador-O-Matic") ;
        util9.setOnAction((event) -> {
            try {             
                ReubicadorItemsOMatic rom = new ReubicadorItemsOMatic(cron,true) ;
            } catch (AWTException | InterruptedException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Reubicar
        MenuItem util9a = new MenuItem("Reubicador-O-Matic / lista") ;
        util9a.setOnAction((event) -> {
            try {             
                ReubicadorItemsOMatic rom = new ReubicadorItemsOMatic(cron, false) ;
            } catch (AWTException | InterruptedException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Posicion del mouse
        MenuItem util10 = new MenuItem("Capturar posición del mouse") ;
        util10.setOnAction((event) -> {
            try {
                Keyboard key = new Keyboard() ;
                key.capturaPosicionMouse();
            } catch (AWTException | InterruptedException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        //Pedidos para usuarios
        MenuItem util11 = new MenuItem("Pedidos para Usuarios") ;
        util11.setOnAction((event) -> {
            PedidosParaUsuarios ppu = new PedidosParaUsuarios(cron) ;
        });
        
        //Exportar coleccion
        MenuItem util12 = new MenuItem("Exportar colección") ;
        util12.setOnAction((event) -> {
            try {
                ExportarColeccion ec = new ExportarColeccion(cron) ;
            } catch (Exception ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        util.getItems().addAll(util1,util2,util3,util4,
                util5,util6, util7, util8,util9,util9a,
                util10, util11, util12) ;
        return util ;
    }

    private Menu creaMenuAyuda() {
        //////////////////////*  AYUDA  *//////////////////////////
        Menu help = new Menu("Ayuda");
        return help ;
    }

    private Menu creaMenuDigitalizacion() {
        Menu digi = new Menu("Digitalización");
        // Renombrador
        MenuItem digi1 = new MenuItem("Renombrador") ;
        digi1.setOnAction((event) -> {
            Renombrador renombrador = new Renombrador() ;
        });
        digi.getItems().add(digi1) ;
        // verificador
        MenuItem digi2 = new MenuItem("Verificar cajones completos") ;
        digi2.setOnAction((event) -> {
            VerificadorDigitales vd = new VerificadorDigitales(cron) ;
            vd.verificaCajonCompleto();
        });
        digi.getItems().add(digi2) ;        
        // miniaturas
        MenuItem digi3 = new MenuItem("Verificar bajas") ;
        digi3.setOnAction((event) -> {
            VerificadorDigitales vd = new VerificadorDigitales(cron) ;
            vd.verificaBajas();
        });
        digi.getItems().add(digi3) ;
        // miniaturas
        MenuItem digi4 = new MenuItem("Acomodar altas") ;
        digi4.setOnAction((event) -> {
            VerificadorDigitales vd = new VerificadorDigitales(cron) ;
            vd.verificaCarpetas();
        });
        digi.getItems().add(digi4) ;
        MenuItem digi5 = new MenuItem("Acomodar bajas") ;
        digi5.setOnAction((event) -> {
            VerificadorDigitales vd = new VerificadorDigitales(cron) ;
            vd.acomodaBajas();
        });
        digi.getItems().add(digi5) ;
        // formulario
        MenuItem digi6 = new MenuItem("Subir a guarda definitiva") ;
        digi6.setOnAction((event) -> {
            GuardaDefinitiva gd = new GuardaDefinitiva(cron) ;            
        });
        digi.getItems().add(digi6) ;
        // formulario
        MenuItem digi7 = new MenuItem("Formulario digitalización/conservación") ;
        digi7.setOnAction((event) -> {
            FormularioDigitalizacion fd = new FormularioDigitalizacion(cron) ;
        });
        digi.getItems().add(digi7) ;
        // formulario
        MenuItem digi8 = new MenuItem("Extraer imagenes de edición impresa") ;
        digi8.setOnAction((event) -> {
            cron.ejecutarEnSegundoPlano(
                    () -> {
                        // Llamar al método cargaDigitales, que lanza excepciones
                        ExtraeImagenesDiarios fd = new ExtraeImagenesDiarios(cron) ;
                        return null; // Callable necesita retornar algo, aunque sea null
                    },
                    () -> {
                        // Acción cuando la tarea termine exitosamente (UI update)
                        cron.mensajeSalida("Imágenes extraidas con éxito!");
                    },
                    (ex) -> {
                        cron.mensajeSalida("Error al extraer imagenes: " + ex.getMessage());
                    }
            );            
        });
        digi.getItems().add(digi8) ;
        
        //contactos
        MenuItem digi9 = new MenuItem("Hoja de contactos") ;
        digi9.setOnAction((event) -> {
            Contactos cont = new Contactos(cron) ;            
            cont.abrir();
        });
        digi.getItems().add(digi9) ;
        
        //new ContactosPorLotes(cron).abrir();
        MenuItem digi10 = new MenuItem("Contactos por lote") ;
        digi10.setOnAction((event) -> {
            ContactosPorLotes cont = new ContactosPorLotes(cron) ;
            cont.abrir();

        });
        digi.getItems().add(digi10) ;
        
        return digi ;
    }

    private Menu creaMenuCatalogacion() {
        Menu cat = new Menu("Catalogación");
        //Catalogador O Matic
        MenuItem cat1 = new MenuItem("Catalogador-O-Matic") ;
        cat1.setOnAction((event) -> {
            try {
                CatalogadorOMatic com = new CatalogadorOMatic(cron) ;
            } catch (AWTException | InterruptedException | IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });        
        MenuItem cat2 = new MenuItem("PreUnificador-O-Matic");
        cat2.setOnAction((event) -> {
            try {
                PreUnificadorOMatic puom = new PreUnificadorOMatic(cron);
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        MenuItem cat3 = new MenuItem("Precatalogador-O-Matic") ;
        cat3.setOnAction((event) -> {
            try {
                PreCatalogadorOMatic precatom = new PreCatalogadorOMatic(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        MenuItem cat4 = new MenuItem("Crear registros para Aleph desde txt") ;
        cat4.setOnAction((event) -> {
            try {
                RegistrosParaAleph reg = new RegistrosParaAleph();
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        MenuItem cat5 = new MenuItem("Crear registros para Aleph desde inventario") ;
        cat5.setOnAction((event) -> {
            try {
                ConjuntosParaAleph cpa = new ConjuntosParaAleph(cron) ;
            } catch (IOException ex) {
                Logger.getLogger(BarraDeMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        MenuItem cat6 = new MenuItem("Catalogar desde el fichero") ;
        cat6.setOnAction((event) -> {
            FicheroCatalogador fc = new FicheroCatalogador(cron) ;
        });

        MenuItem cat7 = new MenuItem("Indizador-O-Matic") ;
        cat7.setOnAction((event) -> {
            cron.tabPane.getTabs().add(new IndizadorOMatic(cron)) ;
        });

        MenuItem cat8 = new MenuItem("Futboleador-O-Matic") ;
        cat8.setOnAction((event) -> {
            FutboleadorOMatic fulbo = new FutboleadorOMatic(cron) ;
        });

        cat.getItems().addAll(cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8);
        return cat ;
    }

    private Menu creaMenuBuscador() {
        Menu busca = new Menu("Búsqueda");

        MenuItem busca1 = new MenuItem("Búsqueda simple") ;
        busca1.setOnAction((event) -> {
            BuscadorBasico buscaSimple = new BuscadorBasico(cron) ;
            Tab tab = new Tab("Búsqueda simple") ;
            cron.shortCutTab(tab);
            tab.setContent(buscaSimple);
            if (!cron.getTabPane().getTabs().contains(tab)) {
                cron.getTabPane().getTabs().add(tab) ;
            }
            cron.getTabPane().getSelectionModel().select(tab);
        });        

        MenuItem busca2 = new MenuItem("Búsqueda avanzada") ;
        busca2.setOnAction((event) -> {
            BuscadorAvanzado buscaSimple = new BuscadorAvanzado(cron) ;
            Tab tab = new Tab("Búsqueda avanzada") ;
            cron.shortCutTab(tab);
            tab.setContent(buscaSimple);
            if (!cron.getTabPane().getTabs().contains(tab)) {
                cron.getTabPane().getTabs().add(tab) ;
            }
            cron.getTabPane().getSelectionModel().select(tab);
        });
        
        MenuItem busca3 = new MenuItem("Búsqueda SQL") ;        
        busca3.setOnAction((event) -> {
        });
        busca3.setDisable(true);
                
        switch(usuario.getRol()){
            case "1":
                //administrador
                busca.getItems().addAll(busca1,busca2,busca3) ;
                break;
            case "2":
                busca.getItems().addAll(busca1,busca2) ;
                break;
            case "3":
                busca.getItems().addAll(busca1,busca2) ;
            case "4":
                busca.getItems().addAll(busca1,busca2) ;
                break;
        }        
        
        return busca ;
    }
    
    private String pedirDesdeAlUsuario(String titulo, String valorPorDefecto) {
        TextInputDialog dialog = new TextInputDialog(valorPorDefecto);
        dialog.setTitle(titulo);
        dialog.setHeaderText("Mostrar índice desde:");
        dialog.setContentText("Término inicial:");

        Optional<String> res = dialog.showAndWait();
        return res.isPresent() ? res.get().trim() : null;
    }

}
