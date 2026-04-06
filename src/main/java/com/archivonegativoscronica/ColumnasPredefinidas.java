package com.archivonegativoscronica;


import java.util.Arrays;
import java.util.List;

public final class ColumnasPredefinidas {

    private ColumnasPredefinidas() {}

    // Índice MARC 6XX unificado:
    // [ Título de columna según tipo ] [ Cant. sobres ] [ Tiene digital ] [ Ver sobres ]
    // ColumnasPredefinidas.java
    public static List<ColumnaConfig> indiceMarc(String tituloColumna) {
        return Arrays.asList(
            // texto largo (Personas / Entidades / Lugares / etc.)
            new ColumnaConfig(tituloColumna,   "termino",        600),

            // cantidad de sobres
            new ColumnaConfig("Cant. sobres",  "cantidadSobres", 90),

            // columna de ✓ / X
            new ColumnaConfig("Tiene digital", "digital",        90),

            // botón "Ver sobres"
            new ColumnaConfig("",              "verSobres",      110)
        );
    }

}
