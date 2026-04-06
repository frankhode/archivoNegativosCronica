/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author francisco.ortiz
 */
class ExtraeImagenesDiarios {
    private final Funciones cron ;

    public ExtraeImagenesDiarios(Funciones cron) throws IOException, InterruptedException {
        this.cron = cron ;
        String pythonExecutable = "src/python/python.exe"; // Cambia esto si usas "python3" u otro intérprete
        String scriptPath = "src/python/extrae_imagenes_pdf_java.py";
        String inputFolder = "G:\\1980";
        String outputFolder = "G:\\1980\\output_images";

        ProcessBuilder processBuilder = new ProcessBuilder(
            pythonExecutable, scriptPath, inputFolder, outputFolder
        );

        try {
            Process process = processBuilder.start();

            // Leer la salida del script Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Verificar errores
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
