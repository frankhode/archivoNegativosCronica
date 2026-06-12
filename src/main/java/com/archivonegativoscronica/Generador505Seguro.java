package com.archivonegativoscronica;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Generador505Seguro {

    private final Funciones cron;

    public Generador505Seguro(Funciones cron) {
        this.cron = cron;
    }

    public Resultado505 construir(String registroOriginal, List<String[]> inventariosNuevos) {
        Resultado505 resultado = new Resultado505();

        Map<String, ItemActual505> itemsActuales = extraerItemsActuales(registroOriginal);
        Map<String, String> titulos505Conservados = extraerTitulos505Conservados(
                registroOriginal,
                itemsActuales,
                resultado
        );

        HashSet<String> nroAsUsados = new HashSet<>();

        /*
         * 1. Para cada item actual:
         *    - si hay título 505 existente con mismo nroA, conservarlo.
         *    - si no hay, resolver desde inventario por barcode.
         */
        for (String nroA : itemsActuales.keySet()) {
            ItemActual505 item = itemsActuales.get(nroA);

            if (titulos505Conservados.containsKey(nroA)) {
                resultado.addTitulo(titulos505Conservados.get(nroA));
                nroAsUsados.add(nroA);
            } else {
                String tituloInventario = tituloDesdeInventarioPorBarcode(item.getBarcode());

                if (!tituloInventario.equals("")) {
                    resultado.addTitulo(tituloInventario);
                    nroAsUsados.add(nroA);
                    resultado.addMensaje("505: item actual sin título previo en 505; se tomó desde inventario. nroA "
                            + nroA + " / barcode " + item.getBarcode() + ".");
                } else {
                    resultado.addMensaje("505: item actual sin título en 505 y sin inventario encontrado. nroA "
                            + nroA + " / barcode " + item.getBarcode() + ".");
                }
            }
        }

        /*
         * 2. Agregar inventarios nuevos pendientes.
         */
        if (inventariosNuevos != null) {
            for (String[] reg : inventariosNuevos) {
                String nroA = nroADesdeInventario(reg);

                if (nroA.equals("")) {
                    continue;
                }

                if (nroAsUsados.contains(nroA)) {
                    resultado.addMensaje("505: inventario nuevo omitido porque el nroA ya existe en items/505: " + nroA + ".");
                    continue;
                }

                String titulo = tituloCompletoInventario(reg);

                if (!titulo.equals("")) {
                    resultado.addTitulo(titulo);
                    nroAsUsados.add(nroA);
                    resultado.addMensaje("505: título nuevo agregado desde inventario: " + nroA + ".");
                }
            }
        }

        ordenarPorFecha(resultado.getTitulos());
        generarCampos505(resultado);

        return resultado;
    }

    private Map<String, ItemActual505> extraerItemsActuales(String registroOriginal) {
        Map<String, ItemActual505> salida = new HashMap<>();

        if (registroOriginal == null || registroOriginal.trim().equals("")) {
            return salida;
        }

        String[] lineas = registroOriginal.split("\\r?\\n");

        for (String linea : lineas) {
            if (!linea.contains(" Z30-")) {
                continue;
            }

            String nroA = normalizarNroA(extraerSubcampo(linea, "h"));
            String barcode = extraerSubcampo(linea, "5");

            if (nroA.equals("")) {
                continue;
            }

            salida.put(nroA, new ItemActual505(nroA, barcode));
        }

        return salida;
    }

    private Map<String, String> extraerTitulos505Conservados(
            String registroOriginal,
            Map<String, ItemActual505> itemsActuales,
            Resultado505 resultado
    ) {
        Map<String, String> salida = new HashMap<>();
        List<String> titulos505 = extraerTitulosDe505(registroOriginal);

        for (String titulo : titulos505) {
            String nroA = normalizarNroA(extraerNroADesdeTitulo505(titulo));

            if (nroA.equals("")) {
                resultado.addMensaje("505: título descartado porque no se pudo reconocer nroA: " + titulo);
                continue;
            }

            if (!itemsActuales.containsKey(nroA)) {
                resultado.addMensaje("505: título descartado porque no hay item actual con nroA " + nroA + ": " + titulo);
                continue;
            }

            if (salida.containsKey(nroA)) {
                resultado.addMensaje("505: título duplicado descartado para nroA " + nroA + ": " + titulo);
                continue;
            }

            salida.put(nroA, limpiarPuntoFinal(titulo));
        }

        return salida;
    }

    private List<String> extraerTitulosDe505(String registroOriginal) {
        List<String> salida = new ArrayList<>();

        if (registroOriginal == null || registroOriginal.trim().equals("")) {
            return salida;
        }

        String[] lineas = registroOriginal.split("\\r?\\n");

        for (String linea : lineas) {
            if (!linea.contains(" 505")) {
                continue;
            }

            int pos = linea.indexOf("$$a");

            if (pos < 0) {
                continue;
            }

            String contenido = linea.substring(pos + 3).trim();

            String[] partes = contenido.split("\\s+--\\s+");

            for (String parte : partes) {
                String titulo = limpiarPuntoFinal(parte);

                if (!titulo.equals("")) {
                    salida.add(titulo);
                }
            }
        }

        return salida;
    }

    private String extraerNroADesdeTitulo505(String titulo) {
        if (titulo == null) {
            return "";
        }

        String limpio = titulo.trim();
        int posPunto = limpio.indexOf(".");

        if (posPunto < 0) {
            return "";
        }

        return limpio.substring(0, posPunto).trim();
    }

    private String tituloDesdeInventarioPorBarcode(String barcode) {
        if (barcode == null || barcode.trim().equals("")) {
            return "";
        }

        String consulta = "SELECT * FROM inventario WHERE barcode LIKE '" + escaparSql(barcode.trim()) + "'";
        List<String[]> filas = cron.consultaCompleta(consulta);

        if (filas == null || filas.isEmpty()) {
            return "";
        }

        return tituloCompletoInventario(filas.get(0));
    }

    private String tituloCompletoInventario(String[] reg) {
        if (reg == null) {
            return "";
        }

        String barcode = valorSeguro(reg, 0);
        String nroA = valorSeguro(reg, 1);
        String tituloBase = valorSeguro(reg, 5);
        String fecha = valorSeguro(reg, 6);

        String titulo;

        if (nroA.equals("")) {
            titulo = "[" + barcode + "]. " + tituloBase;
        } else {
            titulo = nroA + ". " + tituloBase;
        }

        if (!fecha.equals("")) {
            titulo = titulo + ", " + RegistrosParaAleph.fechaFormateada(fecha);
        }

        return titulo.trim();
    }

    private String nroADesdeInventario(String[] reg) {
        if (reg == null) {
            return "";
        }

        String nroA = valorSeguro(reg, 1);

        if (!nroA.equals("")) {
            return normalizarNroA(nroA);
        }

        return normalizarNroA(valorSeguro(reg, 0));
    }

    private void generarCampos505(Resultado505 resultado) {
        List<String> titulos = resultado.getTitulos();

        if (titulos == null || titulos.isEmpty()) {
            return;
        }

        StringBuilder actual = new StringBuilder();

        for (String tituloRaw : titulos) {
            String titulo = limpiarPuntoFinal(tituloRaw);

            if (titulo.equals("")) {
                continue;
            }

            String agregado = actual.length() == 0 ? titulo : " -- " + titulo;

            if (actual.length() > 0 && actual.length() + agregado.length() >= 1300) {
                resultado.addCampo505("5050  L $$a" + actual.toString() + " --");
                actual = new StringBuilder(titulo);
            } else {
                actual.append(agregado);
            }
        }

        if (actual.length() > 0) {
            resultado.addCampo505("5050  L $$a" + actual.toString() + ".");
        }
    }

    private void ordenarPorFecha(List<String> titulos) {
        if (titulos == null || titulos.size() <= 1) {
            return;
        }

        Collections.sort(titulos, (a, b) -> {
            String fechaA = claveFechaOrden(a);
            String fechaB = claveFechaOrden(b);

            int cmpFecha = fechaA.compareTo(fechaB);

            if (cmpFecha != 0) {
                return cmpFecha;
            }

            return normalizarTexto(a).compareTo(normalizarTexto(b));
        });
    }

    private String claveFechaOrden(String titulo) {
        String fecha = extraerFechaDesdeUltimaComa(titulo);

        if (fecha.equals("")) {
            return "99999999";
        }

        String normalizada = normalizarFecha(fecha);

        if (normalizada.equals("")) {
            return "99999999";
        }

        return normalizada;
    }

    private String extraerFechaDesdeUltimaComa(String titulo) {
        if (titulo == null) {
            return "";
        }

        int pos = titulo.lastIndexOf(",");

        if (pos < 0 || pos + 1 >= titulo.length()) {
            return "";
        }

        return titulo.substring(pos + 1).trim();
    }

    private String normalizarFecha(String fecha) {
        if (fecha == null) {
            return "";
        }

        String f = fecha.trim();

        java.util.regex.Matcher completa = java.util.regex.Pattern
                .compile("^(\\d{1,2})/(\\d{1,2})/(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (completa.find()) {
            return completa.group(3) + pad2(completa.group(2)) + pad2(completa.group(1));
        }

        java.util.regex.Matcher mesAnio = java.util.regex.Pattern
                .compile("^(\\d{1,2})/(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (mesAnio.find()) {
            return mesAnio.group(2) + pad2(mesAnio.group(1)) + "00";
        }

        java.util.regex.Matcher anio = java.util.regex.Pattern
                .compile("^(18\\d{2}|19\\d{2}|20\\d{2})$")
                .matcher(f);

        if (anio.find()) {
            return anio.group(1) + "0000";
        }

        return "";
    }

    private String extraerSubcampo(String linea, String codigo) {
        if (linea == null) {
            return "";
        }

        String marca = "$$" + codigo;
        int pos = linea.indexOf(marca);

        if (pos < 0) {
            return "";
        }

        int inicio = pos + marca.length();
        int siguiente = linea.indexOf("$$", inicio);

        if (siguiente < 0) {
            return linea.substring(inicio).trim();
        }

        return linea.substring(inicio, siguiente).trim();
    }

    private String normalizarNroA(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim();

        while (limpio.startsWith("[")) {
            limpio = limpio.substring(1).trim();
        }

        while (limpio.endsWith("]")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }

        return limpio.trim();
    }

    private String limpiarPuntoFinal(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim();

        while (limpio.endsWith(".")) {
            limpio = limpio.substring(0, limpio.length() - 1).trim();
        }

        return limpio.trim();
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim().replaceAll("\\s+", " ");
        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD);
        limpio = limpio.replaceAll("\\p{M}", "");

        return limpio.toLowerCase();
    }

    private String valorSeguro(String[] arr, int pos) {
        try {
            if (arr[pos] == null) {
                return "";
            }

            return arr[pos].trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String escaparSql(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replace("'", "''");
    }

    private String pad2(String valor) {
        if (valor == null) {
            return "00";
        }

        String limpio = valor.trim();

        if (limpio.length() == 1) {
            return "0" + limpio;
        }

        if (limpio.length() >= 2) {
            return limpio.substring(0, 2);
        }

        return "00";
    }

    private static class ItemActual505 {

        private final String nroA;
        private final String barcode;

        ItemActual505(String nroA, String barcode) {
            this.nroA = nroA;
            this.barcode = barcode == null ? "" : barcode.trim();
        }

        public String getNroA() {
            return nroA;
        }

        public String getBarcode() {
            return barcode;
        }
    }
}