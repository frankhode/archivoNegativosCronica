package com.archivonegativoscronica;

import java.util.List;
import javafx.scene.control.Button;

public class RegistroMSInventario {

    private final Registro registro;
    private final Item item;

    private final String sys;
    private final String barcode;
    private final String nroA;
    private final String titulo;
    private final String autor;
    private final String fechaISO;
    private final String ufi;
    private String estado;

    private Button verRegistro;

    public RegistroMSInventario(Registro registro, Item item) {
        this.registro = registro;
        this.item = item;

        this.sys = limpiar(registro.getSys());
        this.barcode = limpiar(item.getBarcode());
        this.nroA = limpiar(item.getDescripcion());
        this.titulo = limpiar(registro.getTituloFormateadoSinFecha());
        this.autor = limitar(limpiar(extraerFotografo(registro)), 100);
        this.fechaISO = limpiar(registro.getFechaISO());
        this.ufi = limpiar(item.getUfi());
        this.estado = "pendiente";
    }

    public Registro getRegistro() {
        return registro;
    }

    public Item getItem() {
        return item;
    }

    public String getSys() {
        return sys;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getNroA() {
        return nroA;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getFechaISO() {
        return fechaISO;
    }

    public String getUfi() {
        return ufi;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Button getVerRegistro() {
        return verRegistro;
    }

    public void setVerRegistro(Button verRegistro) {
        this.verRegistro = verRegistro;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String extraerFotografo(Registro registro) {
        String desde500 = extraerFotografoDesdeCampo(registro, "500");
        if (!desde500.isEmpty()) {
            return desde500;
        }

        String desde520 = extraerFotografoDesdeCampo(registro, "520");
        if (!desde520.isEmpty()) {
            return desde520;
        }

        return "";
    }

    private String extraerFotografoDesdeCampo(Registro registro, String campo) {
        List<String> notas = registro.getCampo(campo);

        for (String nota : notas) {
            String n = nota == null ? "" : nota.trim();
            String lower = n.toLowerCase();

            if (lower.contains("fotógrafo") || lower.contains("fotografo")) {
                int idx = n.indexOf(":");

                if (idx >= 0 && idx + 1 < n.length()) {
                    String autor = n.substring(idx + 1).trim();
                    return quitarPuntoFinal(autor);
                }

                return quitarPuntoFinal(n);
            }
        }

        return "";
    }

    private String quitarPuntoFinal(String valor) {
        if (valor == null) {
            return "";
        }

        valor = valor.trim();

        while (valor.endsWith(".")) {
            valor = valor.substring(0, valor.length() - 1).trim();
        }

        return valor;
    }
    
    private String limitar(String valor, int max) {
        if (valor == null) {
            return "";
        }

        valor = valor.trim();

        if (valor.length() <= max) {
            return valor;
        }

        return valor.substring(0, max).trim();
    }
}