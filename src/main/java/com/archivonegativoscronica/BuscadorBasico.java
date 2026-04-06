/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BuscadorBasico extends VBox {

    private final HBox filaBusqueda;
    private final TablaResultados tablaResultados;
    private final Funciones cron;
    TextField cuadroBusqueda ;

    public BuscadorBasico(Funciones cron) {
        this.cron = cron;

        // Configuración del VBox para el buscador
        setSpacing(10);
        setPadding(new Insets(10));

        // HBox para la fila de búsqueda
        filaBusqueda = new HBox(10);
        filaBusqueda.setPadding(new Insets(0, 0, 10, 0));

        // Cuadro de búsqueda
        cuadroBusqueda = new TextField();
        cuadroBusqueda.setPromptText("Buscar...");

        // Botón de búsqueda
        Button botonBuscar = new Button("Buscar");
        botonBuscar.setOnAction((t) -> {
            realizarBusqueda();
        });

        // Agregar elementos a la fila de búsqueda
        filaBusqueda.getChildren().addAll(new Label("Buscar:"), cuadroBusqueda, botonBuscar);

        // Inicializar la tabla de resultados
        tablaResultados = new TablaResultados();

        // Agregar la tabla de resultados al VBox
        getChildren().addAll(filaBusqueda, tablaResultados.getTableView());
        
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
        String buscar = cuadroBusqueda.getText();
        String consulta = "SELECT DISTINCT titulos.titulo, titulos.barcode FROM titulos "
                + "LEFT JOIN materias ON titulos.sys = materias.sys "
                + "WHERE titulos.titulo LIKE '%" + buscar + "%' "
                + "OR materias.materia LIKE '%" + buscar + "%';";

        // Limpiar resultados anteriores
        tablaResultados.limpiarResultados();

        // Aquí podrías realizar la búsqueda y agregar resultados a la tabla
        List<String> resultados = cron.consultaSimple(consulta, 1);        
        List<String> resultados2 = cron.consultaSimple(consulta, 2);
        for (int i = 0; i < resultados.size(); i++) {
            tablaResultados.agregarResultado(new TablaResultados.Resultado(
                    resultados.get(i).replace("'", "''"), "245", cron));
        }
        tablaResultados.mostrarTabla(true) ;
    }
}
