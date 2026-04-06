package com.archivonegativoscronica;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;

public class FilaIndiceMarc {

    private final SimpleStringProperty termino;
    private final SimpleIntegerProperty cantidadSobres;
    private final SimpleStringProperty digital;
    private final Button verSobres;

    public FilaIndiceMarc(String termino, int cantidadSobres, String digital, Button verSobres) {
        this.termino = new SimpleStringProperty(termino);
        this.cantidadSobres = new SimpleIntegerProperty(cantidadSobres);
        this.digital = new SimpleStringProperty(digital);
        this.verSobres = verSobres;
    }

    public String getTermino() {
        return termino.get();
    }

    public int getCantidadSobres() {
        return cantidadSobres.get();
    }

    public String getDigital() {
        return digital.get();
    }

    public Button getVerSobres() {
        return verSobres;
    }
}
