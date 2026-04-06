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
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class BuscadorAvanzado extends VBox {

    private final VBox contenedorCondiciones;
    private final TablaResultados tablaResultados;
    private final Funciones cron;
    private boolean primeraFila = true;


    public BuscadorAvanzado(Funciones cron) {
        this.cron = cron;

        // Configuración del VBox para el buscador avanzado
        setSpacing(10);
        setPadding(new Insets(10));

        // VBox para contener las condiciones de búsqueda
        contenedorCondiciones = new VBox(10);

        // Inicializar la tabla de resultados
        tablaResultados = new TablaResultados();
        
        Button buscar = new Button("Buscar") ;
        
        buscar.setOnAction((t) -> {
            realizarBusqueda();
        });

        // Agregar la primera fila de búsqueda al inicio
        agregarFilaBusqueda();

        // Agregar elementos al VBox
        getChildren().addAll(contenedorCondiciones,buscar, tablaResultados.getTableView());
    }

    private void agregarFilaBusqueda() {
        primeraFila = contenedorCondiciones.getChildren().isEmpty() ;
        
        FilaBusqueda filaBusqueda = new FilaBusqueda(primeraFila,this);

        // Agregar la fila de búsqueda al contenedor
        contenedorCondiciones.getChildren().add(filaBusqueda);    
    }
    
    public void realizarBusqueda() {
        // Limpiar la tabla de resultados
        tablaResultados.limpiarResultados();

        // Obtener las filas de búsqueda
        ObservableList<Node> filasBusqueda = contenedorCondiciones.getChildren();

        // Verificar si hay alguna fila de búsqueda
        if (!filasBusqueda.isEmpty()) {
            // Construir la consulta
            StringBuilder consulta = new StringBuilder("SELECT DISTINCT titulos.titulo, titulos.barcode FROM titulos ");

            boolean primerCampo = true;

            for (Node fila : filasBusqueda) {
                if (fila instanceof FilaBusqueda) {
                    FilaBusqueda filaBusqueda = (FilaBusqueda) fila;

                    String campo = filaBusqueda.getCampoComboBox().getValue();
                    String texto = filaBusqueda.getTextoBusquedaTextField().getText();
                    String operador = filaBusqueda.getBooleanoComboBox().getValue();

                    // Agregar la condición a la consulta
                    if (!primerCampo) {
                        consulta.append(" ").append(operador).append(" ");
                    } else {
                        consulta.append(" LEFT JOIN materias ON titulos.sys = materias.sys WHERE ");
                        primerCampo = false;
                    }

                    // Ajustar la condición según el campo seleccionado
                    //"Titulo", "Tema", "Lugar", "Persona", "Entidad", "Evento","Año"
                    switch (campo) {
                        case "Titulo":
                            consulta.append("titulos.titulo LIKE '%").append(texto).append("%'");
                            break;
                        case "Tema":
                            consulta.append("materias.materia LIKE '%").append(texto).append("%'");
                            break;
                        case "Lugar":
                            consulta.append("materias.campo=651 AND materias.materia LIKE '%").append(texto).append("%'");
                            break;
                        case "Persona":
                            consulta.append("materias.campo=600 AND materias.materia LIKE '%").append(texto).append("%'");
                            break;
                        case "Entidad":
                            consulta.append("materias.campo=610 AND materias.materia LIKE '%").append(texto).append("%'");
                            break;
                        case "Evento":
                            consulta.append("materias.campo=611 AND materias.materia LIKE '%").append(texto).append("%'");
                            break;
                        case "Año":
                            consulta.append("SUBSTRING(titulos.fecha, 1, 4) LIKE '%").append(texto).append("%'");
                            break;
                        default:
                            // Manejar un caso no previsto, si es necesario
                            break;
                    }
                }
            }

            // Imprimir la consulta (solo con fines de depuración)
            System.out.println(consulta.toString());
             // Aquí podrías realizar la búsqueda y agregar resultados a la tabla
            List<String> resultados = cron.consultaSimple(consulta.toString(), 1);        
            List<String> resultados2 = cron.consultaSimple(consulta.toString(), 2);
            for (int i = 0; i < resultados.size(); i++) {
                tablaResultados.agregarResultado(new TablaResultados.Resultado(resultados.get(i), "245", cron));
            }
            tablaResultados.mostrarTabla(true) ;
        } else {
            // No hay condiciones de búsqueda, mostrar un mensaje o realizar alguna acción apropiada
            System.out.println("No hay condiciones de búsqueda.");
        }
    }


    class FilaBusqueda extends HBox {

        private final ComboBox<String> campoComboBox;
        private final TextField textoBusquedaTextField;
        private final ComboBox<String> booleanoComboBox;
        private final Button botonEliminarFila;
        private final Button botonAgregarFila;  // Nuevo botón para agregar filas

        public FilaBusqueda(boolean esPrimeraFila, BuscadorAvanzado buscadorAvanzado) {
            // Configurar la fila de búsqueda
            setSpacing(10);

            // ComboBox para seleccionar el campo de búsqueda
            campoComboBox = new ComboBox<>();
            ObservableList<String> opcionesCampos = FXCollections.observableArrayList(
                    "Titulo", "Tema", "Lugar", "Persona", "Entidad", "Evento","Año");
            campoComboBox.setItems(opcionesCampos);
            campoComboBox.setValue(opcionesCampos.get(0));

            // TextField para ingresar el texto a buscar
            textoBusquedaTextField = new TextField();
            textoBusquedaTextField.setPromptText("Texto a buscar...");

            // ComboBox para operadores booleanos
            booleanoComboBox = new ComboBox<>();
            ObservableList<String> opcionesBooleano = FXCollections.observableArrayList("AND", "OR");
            booleanoComboBox.setItems(opcionesBooleano);
            booleanoComboBox.setValue(opcionesBooleano.get(0));

            // Botón para eliminar la fila de búsqueda
            botonEliminarFila = new Button("Eliminar");
            botonEliminarFila.setOnAction(e -> {
                //quiero que me resuelvas esta funcionalidad!!!
                if (contenedorCondiciones.getChildren().size() > 1) {
                    contenedorCondiciones.getChildren().remove(this);
                }
                if (contenedorCondiciones.getChildren().size() == 1) {
                    ((FilaBusqueda) contenedorCondiciones.getChildren().get(0)).mostrarElementos(false);
                }
            });

            // Botón para agregar una nueva fila
            botonAgregarFila = new Button("Agregar Fila");
            botonAgregarFila.setOnAction(e -> {
                mostrarElementos(true);
                buscadorAvanzado.agregarFilaBusqueda() ;
            });

            getChildren().addAll(campoComboBox, textoBusquedaTextField, booleanoComboBox, botonEliminarFila, botonAgregarFila);

            // Mostrar u ocultar elementos según sea necesario
            mostrarElementos(!esPrimeraFila);
        }

        private void mostrarElementos(boolean mostrar) {
            booleanoComboBox.setVisible(mostrar);
            booleanoComboBox.setManaged(mostrar);
            botonEliminarFila.setVisible(mostrar);
            botonEliminarFila.setManaged(mostrar);
        }

        public ComboBox<String> getCampoComboBox() {
            return campoComboBox;
        }

        public TextField getTextoBusquedaTextField() {
            return textoBusquedaTextField;
        }

        public ComboBox<String> getBooleanoComboBox() {
            return booleanoComboBox;
        }

        public Button getBotonEliminarFila() {
            return botonEliminarFila;
        }

        public Button getBotonAgregarFila() {
            return botonAgregarFila;
        }        
    }

}
