package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.List;

public class Resultado505 {

    private final List<String> titulos = new ArrayList<>();
    private final List<String> campos505 = new ArrayList<>();
    private final List<String> mensajes = new ArrayList<>();

    public List<String> getTitulos() {
        return titulos;
    }

    public List<String> getCampos505() {
        return campos505;
    }

    public List<String> getMensajes() {
        return mensajes;
    }

    public void addTitulo(String titulo) {
        if (titulo != null && !titulo.trim().equals("")) {
            titulos.add(titulo.trim());
        }
    }

    public void addCampo505(String campo) {
        if (campo != null && !campo.trim().equals("")) {
            campos505.add(campo.trim());
        }
    }

    public void addMensaje(String mensaje) {
        if (mensaje != null && !mensaje.trim().equals("")) {
            mensajes.add(mensaje.trim());
        }
    }

    public String mensajesComoTexto() {
        if (mensajes.isEmpty()) {
            return "Sin observaciones.";
        }

        StringBuilder sb = new StringBuilder();

        for (String mensaje : mensajes) {
            sb.append("• ").append(mensaje).append("\n");
        }

        return sb.toString();
    }
}