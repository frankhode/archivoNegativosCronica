/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


/**
 *
 * @author francisco.ortiz
 */
public class RegistroCronica {
    private final List<Registro> arrayRegistros ;
    
    public RegistroCronica() throws FileNotFoundException, IOException {
        arrayRegistros = new ArrayList<>() ;        
        completaArrayRegistros() ;        
    }

    private void completaArrayRegistros() throws FileNotFoundException, IOException {
        try {
            procesa() ;            
        } catch (IOException e) {
            //mensaje de error
            Alert a = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK) ;
            a.setContentText("Error al elegir el archivo. Intente de nuevo");
            a.showAndWait() ;            
            procesa() ;
        }
        
    }

    /**
     * @return the arrayRegistros
     */
    public List<Registro> getArrayRegistros() {
        return arrayRegistros;
    }

    private void procesa() throws IOException {        
        Path path = new Path() ;
        String archivo = path.getPath() ;
        File file = new File(archivo) ;
        //busca y almacena el primer sys
        BufferedReader br_inicio = new BufferedReader(new FileReader(file));
        String inicio = br_inicio.readLine().substring(0,9) ;

        //lee el archivo por lineas
        //divide la linea en sys-campo-indicadores-dato
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8));

        String st,sysReg="",txt="",campo,subcampo,dato=null ;
        Registro reg = new Registro() ;
        reg.setSys(inicio);

        while ((st = br.readLine()) != null) {
            //campos formulario            
            Pattern p = Pattern.compile("^[0-9]{9} ..... L ");
            Matcher m = p.matcher(st);
            if (m.find() == true) {
                sysReg = st.substring(0,9) ;
                campo = st.substring(10,13) ;            
                dato = st.substring(18) ;
            } else {
                st = txt + st ;
            }

            if (sysReg.equals(inicio)) {
                reg.addLine(st);
                txt = st ;
            } else {
                //Agrega a la lista
                if (reg.getColeccion().contains("Crónica")) {
                    getArrayRegistros().add(reg) ;
                    
                }
                //PEPARA EL NUEVO REGISTRO
                sysReg = st.substring(0,9) ;
                inicio = sysReg;                
                reg = new Registro() ;
                reg.setSys(sysReg);
                reg.addLine(st);                
            }
        }
        if (reg.getColeccion().contains("Crónica")) {                    
            getArrayRegistros().add(reg) ;
        }
    }
    
}
