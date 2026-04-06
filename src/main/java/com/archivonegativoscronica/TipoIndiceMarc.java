package com.archivonegativoscronica;


public enum TipoIndiceMarc {

    PERSONAS("600", "Personas", "tema"),
    ENTIDADES("610", "Entidades", "tema"),
    CONGRESOS("611", "Congresos, reuniones, etc.", "tema"),
    TITULOS_UNIFORMES("630", "Títulos uniformes", "tema"),
    TEMAS("650", "Temas", "tema"),
    LUGARES("651", "Lugares", "tema"),
    AREAS_GEOGRAFICAS("043", "Áreas geográficas", "043"); // 👈 CLAVE

    private final String campoMarc;
    private final String tituloColumna;
    private final String claveResultados;

    TipoIndiceMarc(String campoMarc, String tituloColumna, String claveResultados) {
        this.campoMarc = campoMarc;
        this.tituloColumna = tituloColumna;
        this.claveResultados = claveResultados;
    }

    public String getCampoMarc()       { return campoMarc; }
    public String getTituloColumna()   { return tituloColumna; }
    public String getClaveResultados() { return claveResultados; }
}
