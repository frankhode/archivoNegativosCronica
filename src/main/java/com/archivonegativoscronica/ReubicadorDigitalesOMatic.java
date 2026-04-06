/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

/**
 *
 * @author francisco.ortiz
 */
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReubicadorDigitalesOMatic {
    public final Map<String, String> correctUfis ;

    public ReubicadorDigitalesOMatic(Funciones cron) {
        correctUfis = new HashMap<>();
        String consulta = "SELECT barcode, ufi FROM items" ;
        List<String[]> consulta1 = cron.consultaCompleta(consulta);
        consulta1.forEach((t) -> {
            correctUfis.put(t[0], t[1]);
        });        
    }

    // Método para mostrar una alerta y solicitar confirmación al usuario
    public boolean mostrarAlertaMover(String barcode, String ufiActual, String ufiCorrecto) {
        // Crear una alerta de confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmación de reubicación");
        alerta.setHeaderText("El barcode '" + barcode + "' debería estar en '" + ufiCorrecto + "' pero se encuentra en '" + ufiActual + "'");
        alerta.setContentText("¿Desea mover la carpeta a la ubicación correcta?");

        // Mostrar la alerta y esperar la respuesta del usuario
        Optional<ButtonType> resultado = alerta.showAndWait();

        // Retornar verdadero si el usuario acepta mover la carpeta
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    // Método para mover una carpeta de Barcode a su UFI correcto
    public void moverCarpetaBarcode(String barcode, String ufiActual, String ufiCorrecto, 
            String ubicacionBase, boolean muestraMensaje) {
        if (muestraMensaje) {
            // Mostrar alerta al usuario y pedir confirmación
            boolean moverCarpeta = mostrarAlertaMover(barcode, ufiActual, ufiCorrecto);
            if (moverCarpeta) {
                // Definir la ruta actual del barcode (directorio de origen)
                File carpetaBarcode = new File(ubicacionBase + "/" + ufiActual + "/" + barcode);
                // Definir la ruta destino en la carpeta UFI correcta
                File carpetaDestino = new File(ubicacionBase + "/" + ufiCorrecto + "/" + barcode);
                // Verificar si la carpeta Barcode existe
                if (!carpetaBarcode.exists()) {
                    System.out.println("Carpeta del barcode no existe: " + barcode);
                    return;
                }
                // Crear la carpeta UFI destino si no existe
                if (!carpetaDestino.getParentFile().exists()) {
                    carpetaDestino.getParentFile().mkdirs();
                }
                // Mover la carpeta Barcode a la ubicación correcta
                try {
                    Files.move(carpetaBarcode.toPath(), carpetaDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Carpeta movida de " + ufiActual + " a " + ufiCorrecto);
                } catch (IOException e) {
                    System.out.println("Error al mover la carpeta " + barcode + " a " + ufiCorrecto);
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("El usuario ha cancelado la reubicación.");
            }
        } else {
            // Definir la ruta actual del barcode (directorio de origen)
            File carpetaBarcode = new File(ubicacionBase + "/" + ufiActual + "/" + barcode);
            // Definir la ruta destino en la carpeta UFI correcta
            File carpetaDestino = new File(ubicacionBase + "/" + ufiCorrecto + "/" + barcode);
            // Verificar si la carpeta Barcode existe
            if (!carpetaBarcode.exists()) {
                System.out.println("Carpeta del barcode no existe: " + barcode);
                return;
            }
            // Crear la carpeta UFI destino si no existe
            if (!carpetaDestino.getParentFile().exists()) {
                carpetaDestino.getParentFile().mkdirs();
            }
            // Mover la carpeta Barcode a la ubicación correcta
            try {
                Files.move(carpetaBarcode.toPath(), carpetaDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Carpeta movida de " + ufiActual + " a " + ufiCorrecto);
            } catch (IOException e) {
                System.out.println("Error al mover la carpeta " + ufiActual + " a " + ufiCorrecto);
                System.out.println(e.getMessage());
            }
        }
    }
    
    public void moverCarpetaBarcode(String barcode, String ufiActual, String ufiCorrecto) {
        // Mostrar alerta al usuario y pedir confirmación
        boolean moverCarpeta = mostrarAlertaMover(barcode, ufiActual, ufiCorrecto);
        if (moverCarpeta) {
            // Definir la ruta actual del barcode (directorio de origen)
            File carpetaBarcode = new File(ufiActual);
            System.out.println("**** "+ufiActual);
            // Definir la ruta destino en la carpeta UFI correcta
            File carpetaDestino = new File(ufiCorrecto);
            System.out.println("**** "+ufiCorrecto);

            // Verificar si la carpeta Barcode existe
            if (!carpetaBarcode.exists()) {
                System.out.println("Carpeta del barcode no existe: " + barcode);
                return;
            }

            // Crear la carpeta UFI destino si no existe
            if (!carpetaDestino.getParentFile().exists()) {
                carpetaDestino.getParentFile().mkdirs();
            }

            // Mover la carpeta Barcode a la ubicación correcta
            try {
                Files.move(carpetaBarcode.toPath(), carpetaDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Carpeta movida de " + ufiActual + " a " + ufiCorrecto);
            } catch (IOException e) {
                System.out.println("Error al mover la carpeta " + barcode + " a " + ufiCorrecto);
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario ha cancelado la reubicación.");
        }
    }
}
