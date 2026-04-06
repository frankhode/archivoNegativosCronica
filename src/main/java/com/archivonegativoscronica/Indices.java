/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author francisco.ortiz
 */
public class Indices extends VBox{
    private final Funciones cron;
    private String index,inicio ;
    private final HBox filaBusqueda;
    private final TablaResultados tablaResultados;
    TextField cuadroBusqueda ;

    public Indices(Funciones cron, String index,String inicio,Tab tab) {
        this.cron = cron ;
        this.index = index ;
        this.inicio = inicio ;
        cron.shortCutTab(tab);
        String nombreIndex = "Indice de " ;
        switch (index) {
            case "245":
                nombreIndex = nombreIndex+"Títulos" ;
                break;
            case "043":
                nombreIndex = nombreIndex+"Area geográfica" ;
                break;
            case "600":
                nombreIndex = nombreIndex+"Personas" ;
                break;
            case "610":
                nombreIndex = nombreIndex+"Instituciones" ;
                break;
            case "611":
                nombreIndex = nombreIndex+"Reuniones, Congresos, etc." ;
                break;
            case "630":
                nombreIndex = nombreIndex+"Títulos uniformes" ;
                break;
            case "650":
                nombreIndex = nombreIndex+"Temas" ;
                break;
            case "651":
                nombreIndex = nombreIndex+"Lugares" ;
                break;            
            case "eqp":
                nombreIndex = nombreIndex+"Equipos" ;
                break;            
        }
        tab.setText(nombreIndex);
        // Configuración del VBox para el buscador
        setSpacing(10);
        setPadding(new Insets(10));

        // HBox para la fila de búsqueda
        filaBusqueda = new HBox(10);
        filaBusqueda.setPadding(new Insets(0, 0, 10, 0));

        // Cuadro de búsqueda
        cuadroBusqueda = new TextField();
        cuadroBusqueda.setPromptText("Punto de Inicio...");

        // Botón de búsqueda
        Button botonBuscar = new Button("Ver");
        botonBuscar.setOnAction((t) -> {
            this.inicio = cuadroBusqueda.getText() ;
            realizarBusqueda();
        });

        // Agregar elementos a la fila de búsqueda
        filaBusqueda.getChildren().addAll(new Label("Punto de Inicio:"), cuadroBusqueda, botonBuscar);

        // Inicializar la tabla de resultados
        tablaResultados = new TablaResultados();

        // Agregar la tabla de resultados al VBox
        getChildren().addAll(filaBusqueda, tablaResultados.getTableView());
        setVgrow(tablaResultados.getTableView(), Priority.ALWAYS);
                
        // Ocultar la tabla inicialmente
        tablaResultados.mostrarTabla(false) ;

        // Configurar acciones
        configurarAcciones(cuadroBusqueda);
    }
    
    private void configurarAcciones(TextField cuadroBusqueda) {
        // Manejar la acción del botón de búsqueda
        cuadroBusqueda.setOnAction(e -> realizarBusqueda());
    }
    
    private void realizarBusqueda() {
        // Lógica de búsqueda y actualización de la tabla
        String consulta ;
        switch (index) {
            case "245":
                consulta = "SELECT DISTINCT titulos.titulo FROM titulos WHERE ("
                        + "titulos.titulo >= '"+inicio+"') "
                        + "ORDER BY titulo LIMIT 50;" ;
                break;
            case "043":
                consulta = "SELECT ma.espaniol AS mapaarea_espaniol FROM areas a "
                        + "JOIN mapaareas ma ON a.area = ma.cod "
                        + "GROUP BY ma.espaniol LIMIT 1000" ;
                break;
            case "eqp":
                consulta = "SELECT equipo FROM ( "
                        + "SELECT equipo1 AS equipo FROM partidos "
                        + "UNION "
                        + "SELECT equipo2 AS equipo FROM partidos ) "
                        + "AS equipos ORDER BY equipo;" ;                
                break;
            default:
                consulta = "SELECT DISTINCT materia FROM materias WHERE ("
                        + "campo LIKE '"+index+"' AND materia >= '"+inicio+"') "
                        + "ORDER BY materia LIMIT 100;" ;                
                break;            
        }
        
        // Limpiar resultados anteriores
        tablaResultados.limpiarResultados();

        // Aquí podrías realizar la búsqueda y agregar resultados a la tabla
        List<String> resultados = cron.consultaSimple(consulta, 1);
        
        for (int i = 0; i < resultados.size(); i++) {
            tablaResultados.agregarResultado(new TablaResultados.Resultado(
                    resultados.get(i).replace("'", "''"), index, cron));
        }
        tablaResultados.mostrarTabla(true) ;
    }
    
}
