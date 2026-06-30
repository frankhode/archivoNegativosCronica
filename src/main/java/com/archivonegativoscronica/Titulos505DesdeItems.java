package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad experimental para la nueva generación de registros/salida Aleph.
 *
 * No modifica el flujo actual del Catalogador-O-Matic. Su objetivo es reconstruir
 * el contenido de las 505 a partir de los ítems reales del registro Aleph
 * existente (Z30) y de los inventarios nuevos que se van a agregar.
 */
public class Titulos505DesdeItems {

    private static final int LIMITE_CAMPO_505 = 1800;

    private final Funciones cron;

    public Titulos505DesdeItems(Funciones cron) {
        this.cron = cron;
    }

    /**
     * Reconstruye la lista de títulos para 505 desde:
     * 1) los Z30/items existentes en el registro Aleph actual;
     * 2) los registros de inventario nuevos a agregar.
     *
     * @param registroAlephActual registro en Aleph Sequential, incluyendo Z30 si los hay
     * @param nuevosInventarios lista de filas de inventario nuevas, con el formato usado por COM
     * @return lista ordenada y sin duplicados de títulos para 505
     */
    public List<String> reconstruirTitulos(String registroAlephActual, List<String[]> nuevosInventarios) {
        List<String> titulos = new ArrayList<>();

        agregarTitulosDeItemsExistentes(titulos, registroAlephActual);
        agregarTitulosDeInventarios(titulos, nuevosInventarios);

        Collections.sort(titulos);
        return titulos;
    }

    /**
     * Genera campos 505 en Aleph Sequential a partir de una lista de títulos.
     * Mantiene el mismo criterio histórico del proyecto: dividir el campo cuando
     * el acumulado supera aproximadamente 1800 caracteres.
     */
    public List<String> generarCampos505(String sys, List<String> titulos) {
        List<String> campos = new ArrayList<>();

        if (titulos == null || titulos.isEmpty()) {
            return campos;
        }

        StringBuilder campoActual = nuevoCampo505(sys);

        for (String titulo : titulos) {
            if (titulo == null || titulo.trim().equals("")) {
                continue;
            }

            String tituloLimpio = limpiar(titulo);

            if (tituloLimpio.equals("")) {
                continue;
            }

            String parte = tituloLimpio + " -- ";

            if (campoActual.length() + parte.length() >= LIMITE_CAMPO_505
                    && campoActual.length() > nuevoCampo505(sys).length()) {
                cerrarYAgregarCampo(campos, campoActual, true);
                campoActual = nuevoCampo505(sys);
            }

            campoActual.append(parte);
        }

        if (campoActual.length() > nuevoCampo505(sys).length()) {
            cerrarYAgregarCampo(campos, campoActual, false);
        }

        return campos;
    }

    private void agregarTitulosDeItemsExistentes(List<String> titulos, String registroAlephActual) {
        if (registroAlephActual == null || registroAlephActual.trim().equals("")) {
            return;
        }

        Registro registro = new Registro(registroAlephActual);

        registro.getItems().forEach((item) -> {
            String titulo = tituloDesdeItem(item);
            agregarSiCorresponde(titulos, titulo);
        });
    }

    private void agregarTitulosDeInventarios(List<String> titulos, List<String[]> inventarios) {
        if (inventarios == null) {
            return;
        }

        inventarios.forEach((inventario) -> {
            String titulo = tituloDesdeInventario(inventario);
            agregarSiCorresponde(titulos, titulo);
        });
    }

    private String tituloDesdeItem(Item item) {
        String barcode = limpiar(item.getBarcode());

        if (!barcode.equals("")) {
            String[] inventario = buscarInventarioPorBarcode(barcode);
            if (inventario != null) {
                return tituloDesdeInventario(inventario);
            }
        }

        String descripcion = limpiar(item.getDescripcion());
        if (!descripcion.equals("")) {
            return descripcion;
        }

        String inventario = limpiar(item.getInventario());
        if (!inventario.equals("")) {
            return "[" + inventario + "]";
        }

        if (!barcode.equals("")) {
            return "[" + barcode + "]";
        }

        return "";
    }

    private String[] buscarInventarioPorBarcode(String barcode) {
        String barcodeSql = barcode.replace("'", "''");
        String consulta = "SELECT * FROM inventario WHERE barcode LIKE '" + barcodeSql + "'";
        List<String[]> resultado = cron.consultaCompleta(consulta);

        if (resultado == null || resultado.isEmpty()) {
            return null;
        }

        return resultado.get(0);
    }

    private String tituloDesdeInventario(String[] inventario) {
        if (inventario == null || inventario.length == 0) {
            return "";
        }

        String barcode = valor(inventario, 0);
        String nroA = valor(inventario, 1);
        String titulo = valor(inventario, 5);
        String fecha = valor(inventario, 6);

        if (titulo.equals("")) {
            return fallbackIdentificador(nroA, barcode);
        }

        StringBuilder salida = new StringBuilder();

        if (nroA.equals("")) {
            salida.append("[").append(barcode).append("]. ");
        } else {
            salida.append(nroA).append(". ");
        }

        salida.append(titulo);

        if (!fecha.equals("")) {
            salida.append(", ").append(fecha);
        }

        return salida.toString();
    }

    private String fallbackIdentificador(String nroA, String barcode) {
        if (!nroA.equals("")) {
            return nroA;
        }
        if (!barcode.equals("")) {
            return "[" + barcode + "]";
        }
        return "";
    }

    private void agregarSiCorresponde(List<String> titulos, String titulo) {
        String limpio = limpiar(titulo);

        if (!limpio.equals("") && !titulos.contains(limpio)) {
            titulos.add(limpio);
        }
    }

    private StringBuilder nuevoCampo505(String sys) {
        return new StringBuilder().append(sys).append(" 5058  L $$a");
    }

    private void cerrarYAgregarCampo(List<String> campos, StringBuilder campo, boolean continua) {
        String texto = campo.toString().trim();

        // Sacamos cualquier separador final para controlar acá el cierre correcto.
        texto = texto.replaceAll("(\\s*--\\s*)+$", "").trim();

        if (continua) {
            // Si hay otro 505 después, Aleph visualiza mejor con separador final.
            texto = texto + " --";
        } else {
            // Solo el último 505 cierra con punto.
            if (!texto.endsWith(".")) {
                texto = texto + ".";
            }
        }

        campos.add(texto);
    }

    private String valor(String[] array, int index) {
        if (array.length <= index || array[index] == null) {
            return "";
        }
        return limpiar(array[index]);
    }

    private String limpiar(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .replaceAll("^(--\\s*)+", "")
                .replaceAll("(\\s*--\\s*\\.?\\s*)+$", "")
                .trim();
    }
}