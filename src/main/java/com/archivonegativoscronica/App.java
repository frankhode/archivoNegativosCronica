package com.archivonegativoscronica;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {
    LoginModal loginModal ;    
    Funciones cron ;    
    List<Usuario> usuarios ;    

    @Override
    public void start(Stage stage) throws SQLException, IOException, InterruptedException {
        cron = new Funciones() ;                
        cron.setStage(stage) ;        
        usuarios = cron.obtenerUsuarios();
        
        // Verifica si la autenticación fue exitosa
        autenticaUsuario() ;    
    }

    public static void main(String[] args) {
        launch();
    }
    
    private void autenticaUsuario() {
        // Ventana de inicio de sesión
        loginModal = new LoginModal(usuarios);
        loginModal.showAndWait();
        if (loginModal.getLogin()){ 
            // Continuar con el resto de tu aplicación usando funciones y usuarios autenticados
            Usuario usuarioAutenticado = loginModal.getUsuarioAutenticado();            
            // Crea la BarraDeMenu() basada en el nivel del usuario
            BarraDeMenu bar = new BarraDeMenu(usuarioAutenticado,cron);
            loginModal.close();
            //texto agregado
            BorderPane bp = new BorderPane() ;
            bp.setTop(bar.getMenu());
            bp.setCenter(cron.getTabPane());
            Scene scene = new Scene(bp);
            Stage stage = new Stage() ;
            stage.getIcons().add(new Image(getClass().getResource("/files/icon.png").toExternalForm()));
            stage.setMaximized(true);
            stage.setTitle("Archivo Fotográfico del Diario Crónica *-* "
                    +usuarioAutenticado.getNombre()+"!");
            stage.setScene(scene);            
            stage.show();
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR) ;
            a.setContentText("Usuario o contraseña no valido");
            Optional<ButtonType> showAndWait = a.showAndWait();
            if (showAndWait.isPresent()) {
                autenticaUsuario();
            }
        }
    }    

}