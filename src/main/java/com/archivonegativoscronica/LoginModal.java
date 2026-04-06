/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginModal extends Stage {

    private final List<Usuario> usuarios;
    private String username,password ;
    private Usuario usuarioAutenticado ;
    boolean login ;

    public LoginModal(List<Usuario> usuarios) {
        this.usuarios = usuarios ;
        initModality(Modality.APPLICATION_MODAL);
        
        setTitle("Inicio de Sesión");

        Button loginButton = new Button("Iniciar Sesión");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        Scene scene = new Scene(grid, 300, 150);                
        
        loginButton.setOnAction(e -> {
            Platform.runLater(() -> {
                username = usernameField.getText();
                password = passwordField.getText();
                autenticarUsuario() ;
                this.close() ;
            });
        });

        setScene(scene);
    }

    private void autenticarUsuario() {
        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().equals(username) && usuario.getPass().equals(password)) {
                // Autenticación exitosa
                System.out.println("Autenticación exitosa para: " + username);
                setUsuario(usuario) ;
                login = true ;
                return ;
            }
        }
        // Autenticación fallida
        login = false;
    }

    private void setUsuario(Usuario usuario) {
        usuarioAutenticado = usuario ;
    }
    
    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado ;
    }
    
    public boolean getLogin(){
        return login ;
    }
}
