/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 *
 * @author francisco.ortiz
 */
public class BarraDeProgreso extends StackPane {
    
    public BarraDeProgreso(){}
    
    public void deleteText(){
        ObservableList<Node> childrens = this.getChildren() ;
        for (Node node : childrens) {
            if (node instanceof Text) {
                this.getChildren().remove(node) ;
            }
        }
    }
}
