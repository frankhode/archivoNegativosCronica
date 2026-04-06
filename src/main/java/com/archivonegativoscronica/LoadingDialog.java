/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.archivonegativoscronica;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author francisco.ortiz
 */
public class LoadingDialog {
    private final Stage dialogStage;
        private final RotateTransition rotateTransition;

        public LoadingDialog(String message) {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            ImageView imageView = new ImageView(new Image("files/icon.png"));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);

            Label label = new Label(message);
            label.setFont(Font.font(14));
            label.setTextFill(Color.WHITE);

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(imageView, label);
            vbox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20px;");

            Scene scene = new Scene(vbox);
            dialogStage.setScene(scene);

            rotateTransition = new RotateTransition(Duration.seconds(1), imageView);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
        }

        public void show() {
            dialogStage.show();
            rotateTransition.play();
        }

        public void close() {
            rotateTransition.stop();
            dialogStage.close();
        }
}
