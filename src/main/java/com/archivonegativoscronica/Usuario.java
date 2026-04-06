/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.List;

/**
 *
 * @author francisco.ortiz
 */

/*****************
    *                ROL       NIVEL
    *          1 -> admin      A
    *          2 -> user       A B
    *          3 -> cat        A B C
    *          4 -> digit      A B
    *  
    *  1A: todas las funciones
    *  2A: usuario simple
    *  2B: usuario avanzado
    *  3A: catalogador nivel inventario
    *  3B: catalogador nivel avanzado
    *  3C: catalogador nivel interno (para data fuera de aleph)
    *  4A: digitalizador basico
    *  4B: digitalizador avanzado
    ******************/
public class Usuario {
    private String id;
    private String nombre;
    private String nivel;
    private String rol;
    private String pass;

    public Usuario() {        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

}
