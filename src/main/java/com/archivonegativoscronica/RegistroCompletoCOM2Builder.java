package com.archivonegativoscronica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistroCompletoCOM2Builder {

    private final Funciones cron;

    public RegistroCompletoCOM2Builder(Funciones cron) {
        this.cron = cron;
    }

    public ResultadoRegistroCOM2 construir(String sys, RegistrosParaAgregar regsParaAgregar, boolean color) {
        ResultadoRegistroCOM2 resultado = new ResultadoRegistroCOM2();

        RegistroParaActualizar rpa = new RegistroParaActualizar(cron, sys, regsParaAgregar);
        String registroOriginal = buscarRegistroOriginal(sys);
        List<String[]> inventariosNuevos = regsParaAgregar.returnRegInv(sys);
        int cantSobresTotal = contarSobresTotales(registroOriginal, inventariosNuevos);
        Generador505Seguro gen505 = new Generador505Seguro(cron);
        Resultado505 resultado505 = gen505.construir(
                registroOriginal,
                inventariosNuevos
        );

        resultado.addMensajes(resultado505.getMensajes());

        List<String> lineasSinSys = new ArrayList<>();

        agregarCamposControlOriginales(lineasSinSys, registroOriginal, rpa);

        agregar(lineasSinSys, "040   L $$aAR-BaBN$$bspa$$cAR-BaBN$$eaacr");

        String campo043 = normalizarSubcampoA(rpa.getCampo043());

        if (!campo043.equals("")) {
            agregar(lineasSinSys, "043   L " + campo043);
        }

        agregar(lineasSinSys, "24500 L $$a" + valor(rpa.getTitulo245()));

        agregar(lineasSinSys, campo260Grupal(rpa.getFechas()));

        agregar(lineasSinSys, campo300Grupal(cantSobresTotal, color));

        agregar(lineasSinSys, "500   L $$aTítulo asignado por el personal de la Biblioteca.");

        String campoFotografos = campo500Fotografos(rpa.getFotografos());
        if (!campoFotografos.equals("")) {
            agregar(lineasSinSys, campoFotografos);
        }

        for (String c505 : resultado505.getCampos505()) {
            agregar(lineasSinSys, c505);
        }

        agregar(lineasSinSys, "540   L $$aPuede presentar restricciones. Consultar en el Departamento de Materiales Cartográficos y Fotográficos.$$5AR-BaBN");
        agregar(lineasSinSys, "5611  L $$aForma parte del archivo fotográfico del diario Crónica.$$5AR-BaBN");

        lineasSinSys.addAll(campos6XX("600", rpa.getCampo600()));
        lineasSinSys.addAll(campos6XX("610", rpa.getCampo610()));
        lineasSinSys.addAll(campos6XX("611", rpa.getCampo611()));
        lineasSinSys.addAll(campos6XX("630", rpa.getCampo630()));
        lineasSinSys.addAll(campos6XX("650", rpa.getCampo650()));
        lineasSinSys.addAll(campos6XX("651", rpa.getCampo651()));

        /*
         * 655: lo mantenemos con la lógica original de materiales.
         * No copiamos todos los 655 originales a ciegas; agregamos los
         * que corresponden por presencia de negativo/diapo.
         */
        if (rpa.tieneNega()) {
            agregar(lineasSinSys, "655 4 L $$aNegativos flexibles");
        }

        if (rpa.tieneDiapo() || color) {
            agregar(lineasSinSys, "655 4 L $$aFotografía en color");
        }

        agregar(lineasSinSys, "77318 L $$tSección Archivo fotográfico$$w(AR-BaBN)001412736");
        agregar(lineasSinSys, "OWN   L $$aCAT_FOTO");

        String alephTag = unirCRLF(lineasSinSys);
        resultado.setAlephTagSinSys(alephTag);
        resultado.setPreviewConSys(agregarSysPreview(sys, lineasSinSys));

        return resultado;
    }
    
    private String normalizarSubcampoA(String contenido) {
        String limpio = valor(contenido);

        if (limpio.equals("")) {
            return "";
        }

        if (limpio.startsWith("$$")) {
            return limpio;
        }

        return "$$a" + limpio;
    }

    private void agregarCamposControlOriginales(List<String> salida, String registroOriginal, RegistroParaActualizar rpa) {
        String campo001 = "";
        String campo003 = "";
        String campo005 = "";
        String campo007 = "";

        if (registroOriginal != null) {
            String[] lineas = registroOriginal.split("\\r?\\n");

            for (String linea : lineas) {
                String sinSys = quitarSys(linea);

                if (sinSys.startsWith("001 ")) {
                    campo001 = sinSys;
                } else if (sinSys.startsWith("003 ")) {
                    campo003 = sinSys;
                } else if (sinSys.startsWith("005 ")) {
                    campo005 = sinSys;
                } else if (sinSys.startsWith("007 ")) {
                    campo007 = sinSys;
                }
            }
        }

        if (!campo001.equals("")) {
            agregar(salida, campo001);
        }

        if (!campo003.equals("")) {
            agregar(salida, campo003);
        }

        if (!campo005.equals("")) {
            agregar(salida, campo005);
        }

        if (!campo007.equals("")) {
            agregar(salida, campo007);
        }

        agregar(salida, campo008Grupal(rpa.getFechas()));
    }

    private String campo008Grupal(List<String> fechas) {
        /*
         * Replica la intención de Keyboard.carga008Grupal:
         * - sin fechas: plantilla aproximada
         * - una fecha o todas iguales: s + fecha1
         * - varias fechas: k + menor/mayor
         */
        if (fechas == null || fechas.isEmpty()) {
            return "008   L 191212s197^^^^^^^^^^^^^^^^^^^^knspa^^";
        }

        List<String> limpias = limpiarAnios(fechas);

        if (limpias.isEmpty()) {
            return "008   L 191212s197^^^^^^^^^^^^^^^^^^^^knspa^^";
        }

        Collections.sort(limpias);

        String menor = limpias.get(0);
        String mayor = limpias.get(limpias.size() - 1);

        if (menor.equals(mayor)) {
            return "008   L 191212s" + menor + "^^^^ag^nnn^^^^^^^^^^^^knspa^^";
        }

        return "008   L 191212k" + menor + mayor + "ag^nnn^^^^^^^^^^^^knspa^^";
    }

    private String campo260Grupal(List<String> fechas) {
        if (fechas == null || fechas.isEmpty()) {
            return "260   L $$c[197-?]";
        }

        List<String> limpias = limpiarAnios(fechas);

        if (limpias.isEmpty()) {
            return "260   L $$c[197-?]";
        }

        Collections.sort(limpias);

        String menor = limpias.get(0);
        String mayor = limpias.get(limpias.size() - 1);

        if (menor.equals(mayor)) {
            return "260   L $$c" + menor + ".";
        }

        return "260   L $$c" + menor + "-" + mayor + ".";
    }

    private String campo300Grupal(int cantItems, boolean color) {
        if (color) {
            return "300   L $$a" + cantItems + " sobres (diapositivas) :$$bcol.";
        }

        return "300   L $$a" + cantItems + " sobres (negativos flexibles) :$$bbyn.";
    }

    private String campo500Fotografos(List<String> fotografos) {
        if (fotografos == null || fotografos.isEmpty()) {
            return "";
        }

        List<String> lista = new ArrayList<>();

        for (String f : fotografos) {
            String limpio = valor(f);

            if (!limpio.equals("") && !lista.contains(limpio)) {
                lista.add(limpio);
            }
        }

        if (lista.isEmpty()) {
            return "";
        }

        Collections.sort(lista);

        StringBuilder sb = new StringBuilder();
        sb.append("500   L $$aFotógrafo: ");

        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(lista.get(i));
        }

        sb.append(".");

        return sb.toString();
    }

    private List<String> campos6XX(String campo, List<String[]> campos) {
        List<String> salida = new ArrayList<>();

        if (campos == null || campos.isEmpty()) {
            return salida;
        }

        for (String[] t : campos) {
            if (t == null || t.length < 2) {
                continue;
            }

            String indicadores = normalizarIndicadores(t[0]);
            String contenido = normalizarContenidoConSubcampoA(valor(t[1]));

            if (!contenido.equals("")) {
                salida.add(campo + indicadores + " L " + contenido);
            }
        }

        return salida;
    }

    private String normalizarIndicadores(String indicadores) {
        String ind = indicadores == null ? "" : indicadores;

        /*
         * Ojo: NO hacer trim().
         * En MARC/ALEPH el espacio puede ser indicador real.
         * Ejemplo correcto: " 4" para 650 segundo indicador 4.
         */
        if (ind.length() > 2) {
            ind = ind.substring(0, 2);
        }

        while (ind.length() < 2) {
            ind = ind + " ";
        }

        return ind;
    }

    private String normalizarContenidoConSubcampoA(String contenido) {
        String limpio = valor(contenido);

        if (limpio.equals("")) {
            return "";
        }

        if (limpio.startsWith("$$")) {
            return limpio;
        }

        return "$$a" + limpio;
    }

    private List<String> limpiarAnios(List<String> fechas) {
        List<String> salida = new ArrayList<>();

        for (String f : fechas) {
            String limpio = valor(f);

            if (limpio.matches("\\d{4}") && !salida.contains(limpio)) {
                salida.add(limpio);
            }
        }

        return salida;
    }

    private String buscarRegistroOriginal(String sys) {
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '" + escaparSql(sys) + "'";
        List<String[]> res = cron.consultaCompleta(consulta);

        if (res == null || res.isEmpty() || res.get(0).length == 0) {
            return "";
        }

        return res.get(0)[0];
    }

    private String quitarSys(String linea) {
        if (linea == null) {
            return "";
        }

        /*
         * Las líneas en tabla registros suelen venir:
         * 001526189 24500 L ...
         * El ALEPH_TAG que pegamos no lleva SYS.
         */
        if (linea.length() > 10 && linea.substring(0, 9).matches("\\d{9}") && linea.charAt(9) == ' ') {
            return linea.substring(10);
        }

        return linea;
    }

    private String agregarSysPreview(String sys, List<String> lineasSinSys) {
        StringBuilder sb = new StringBuilder();

        for (String linea : lineasSinSys) {
            sb.append(sys).append(" ").append(linea).append("\r\n");
        }

        return sb.toString();
    }

    private String unirCRLF(List<String> lineas) {
        StringBuilder sb = new StringBuilder();

        for (String linea : lineas) {
            sb.append(linea).append("\r\n");
        }

        return sb.toString();
    }

    private void agregar(List<String> salida, String linea) {
        if (linea != null && !linea.trim().equals("")) {
            salida.add(linea.trim());
        }
    }

    private String valor(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String escaparSql(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.replace("'", "''");
    }
    
    private int contarSobresTotales(String registroOriginal, List<String[]> inventariosNuevos) {
        int total = 0;

        if (registroOriginal != null) {
            String[] lineas = registroOriginal.split("\\r?\\n");

            for (String linea : lineas) {
                String sinSys = quitarSys(linea);

                if (sinSys.startsWith("Z30")) {
                    total++;
                }
            }
        }

        if (inventariosNuevos != null) {
            total += inventariosNuevos.size();
        }

        return total;
    }
}