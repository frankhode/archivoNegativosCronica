/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.TextInputDialog;


/**
 *
 * @author francisco.ortiz
 */
class DesUnificadorOMatic {
    private final Funciones cron ;

    DesUnificadorOMatic(Funciones cron) throws SQLException {
        this.cron = cron ;
        TextInputDialog mensaje = new TextInputDialog("Ingrese el número de registro "
                + "\npara des-unificar") ;
        Optional<String> sysReg = mensaje.showAndWait();
        if (sysReg.isPresent()) {
            porcesa(mensaje.getResult()) ;
        }
    }

    private void porcesa(String sys) throws SQLException {
        String consulta = "SELECT registro FROM registros WHERE sys LIKE '"+sys+"'" ;
        System.out.println(consulta);
        List<String> getReg = cron.consultaSimple(consulta, 1);
        if (!getReg.isEmpty()) {
            RegistroParaDesUnificar rpdu = new RegistroParaDesUnificar(getReg.get(0)) ;
        }
    }

    private class RegistroParaDesUnificar {
        int contador ;
        List<String[]> sobres ;

        private RegistroParaDesUnificar(String data) throws SQLException {
            sobres = new ArrayList<>() ;
            Registro reg = new Registro(data) ;
            List<Item> items = reg.getItems();
            List<String> campo = reg.getCampo("505");
            campo.forEach((t) -> {
                String[] titulos = t.split(" -- ");
                for (String titulo : titulos) {
                    String[] split = titulo.split(". ");
                    String nro = split[0] ;
                    String tit = titulo.replace(nro+". ","") ;
                    Pattern pattern = Pattern.compile(", (\\d{1,2}/\\d{1,2}/\\d{4}|\\d{4}|\\d{1,2}/\\d{4})");
                    Matcher matcher = pattern.matcher(tit);
                    String tituloSinFecha = matcher.replaceAll("");
                    for (Item item : items) {
                        if (item.getDescripcion().equals(nro)) {
                            String[] array = new String[]{
                                item.getBarcode(),//barcode
                                item.getDescripcion(),//nroA
                                buscaEnInventario("nroNid",item.getBarcode()),//nroNid
                                buscaEnInventario("nroAnm",item.getBarcode()),//nroAnm
                                buscaEnInventario("autor",item.getBarcode()),//Autor                                    
                                tituloSinFecha,//titulo
                                cron.formateaFecha(tit),//fechaISO
                                buscaEnInventario("observaciones",item.getBarcode()),//observaciones
                                item.getUfi(),//ufi
                                } ;
                            sobres.add(array) ;
                        }
                    }
                }
            });
            agregaAlInventario(sobres) ;
        }

        private String buscaEnInventario(String col, String barcode) {            
            String consulta = "SELECT "+col+" FROM inventario WHERE barcode LIKE '"+barcode+"'" ;
            System.out.println(consulta);
            if (!cron.consultaSimple(consulta, 1).isEmpty()) {
                return cron.consultaSimple(consulta, 1).get(0) ;
            } else {
                return "" ;
            }
            
        }

        private void agregaAlInventario(List<String[]> sobres) throws SQLException {
            String consulta = "DELETE FROM `inventario` WHERE barcode LIKE '" ;
            contador = 1 ;
            for (String[] sobre : sobres) {
                System.out.println(Arrays.toString(sobre));
                if (contador < sobres.size()) {
                    consulta = consulta + sobre[0] + "' OR barcode LIKE '" ;
                    contador++ ;
                } else {
                    consulta = consulta + sobre[0] + "'" ;
                }
            }
            cron.consultaSimple(consulta, 1) ;
            System.out.println(consulta);
            cargaEnInventario(sobres) ;
        }

        private void cargaEnInventario(List<String[]> sobres) throws SQLException {
            PreparedStatement stmt = cron.conn.prepareStatement("INSERT IGNORE INTO inventario("
                    + "barcode,nroA,nroNid,nroAnm,autor,titulo,fechaISO,observaciones,ufi) "
                    + "VALUES (?,?,?,?,?,?,?,?,?);");
            sobres.forEach((sobre) -> {
                try {
                    stmt.setString(1,sobre[0]);
                    stmt.setString(2,sobre[1]);
                    stmt.setString(3,sobre[2]);
                    stmt.setString(4,sobre[3]);
                    stmt.setString(5,sobre[4]);
                    stmt.setString(6,sobre[5]);
                    stmt.setString(7,sobre[6]);
                    stmt.setString(8,sobre[7]);
                    stmt.setString(9,sobre[8]);
                    stmt.addBatch();
                    stmt.clearParameters();                    
                } catch (SQLException ex) {
                    Logger.getLogger(DesUnificadorOMatic.class.getName()).log(Level.SEVERE, null, ex);
                }                
            });
            int [] results = stmt.executeBatch();
            borraItems(sobres) ;
            borraConjuntos(sobres) ;
        }

        private void borraItems(List<String[]> sobres) {
            String consulta = "DELETE FROM items WHERE barcode LIKE '" ;
            contador = 1 ;
            for (String[] sobre : sobres) {
                if (contador < sobres.size()) {
                    consulta = consulta + sobre[0] + "' OR barcode LIKE '" ;
                    contador++ ;
                } else {
                    consulta = consulta + sobre[0] + "'" ;
                }
            }
            cron.consultaSimple(consulta, 1) ;
        }

        private void borraConjuntos(List<String[]> sobres) {
             String consulta = "DELETE FROM conjuntos WHERE barcode LIKE '" ;
            contador = 1 ;
            for (String[] sobre : sobres) {
                if (contador < sobres.size()) {
                    consulta = consulta + sobre[0] + "' OR barcode LIKE '" ;
                    contador++ ;
                } else {
                    consulta = consulta + sobre[0] + "'" ;
                }
            }
            cron.consultaSimple(consulta, 1) ;
        }
    }
    
}
