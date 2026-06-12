package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;

public class ResultadoRegistroCOM2 {

    private String alephTagSinSys = "";
    private String previewConSys = "";
    private final List<String> mensajes = new ArrayList<>();

    public String getAlephTagSinSys() {
        return alephTagSinSys;
    }

    public void setAlephTagSinSys(String alephTagSinSys) {
        this.alephTagSinSys = alephTagSinSys == null ? "" : alephTagSinSys;
    }

    public String getPreviewConSys() {
        return previewConSys;
    }

    public void setPreviewConSys(String previewConSys) {
        this.previewConSys = previewConSys == null ? "" : previewConSys;
    }

    public List<String> getMensajes() {
        return mensajes;
    }

    public void addMensaje(String mensaje) {
        if (mensaje != null && !mensaje.trim().equals("")) {
            mensajes.add(mensaje.trim());
        }
    }

    public void addMensajes(List<String> nuevos) {
        if (nuevos == null) {
            return;
        }

        for (String mensaje : nuevos) {
            addMensaje(mensaje);
        }
    }
}