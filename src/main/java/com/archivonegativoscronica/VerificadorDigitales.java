/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author francisco.ortiz
 */
class VerificadorDigitales {
    Funciones cron ;

    public VerificadorDigitales(Funciones cron) {
        this.cron = cron ;
        SobreDigital sd = new SobreDigital(cron);
        
        //verificar material sin digitalizar desde Aleph
            //verificaDigitalesDesdeAleph() ;
        //verificar si falta sobre
            //verifica() ;
        //verificar ubicaciones aleph vs. ubicaciones srv -> mover carpetas
        //verificar ufi digitalizado completo-> generar baja
            //verificaCajonCompleto() ;        
        //verificar bajas
            //verificaBajas() ;
    }

    private void verificaDigitalesDesdeAleph() {
        String consulta = "SELECT barcode FROM items" ;
        List<String> barcodes = cron.consultaSimple(consulta, 1);
        System.out.println("Items: "+barcodes.size());
        consulta = "SELECT DISTINCT inv FROM digitales" ;
        List<String> barcodesDigi = cron.consultaSimple(consulta, 1);
        System.out.println("Sobres digitalizados: "+barcodesDigi.size());
        barcodes.removeAll(barcodesDigi) ;
        barcodes.forEach((t) -> {
            //System.out.println(t + " no tiene digital");
        });
        System.out.println("Diferencia: "+barcodes.size());
    }

    private void verifica() {
        String consulta = "SELECT DISTINCT carpeta FROM digitales" ;
        List<String> carpetas = cron.consultaSimple(consulta, 1);
        Map<String,List<String>> ufisDigitales = new HashMap<>() ;
        carpetas.forEach((carpeta) -> {
            String[] c = carpeta.split("\\\\") ;
            try {
                List<String> get = ufisDigitales.get(c[0]);
                get.add(c[1]);
            } catch (Exception e) {
                List<String> get = new ArrayList<>() ;
                get.add(c[1]) ;
                ufisDigitales.put(c[0], get) ;
            }
        });
        
        ufisDigitales.forEach((String ufi, List<String> barcodes) -> {
            String cons = "SELECT barcode FROM items WHERE ufi LIKE '"+ufi+"'" ;
            List<String> barcodesItems = cron.consultaSimple(cons, 1);
            // Find elements present in list1 but not in list2
            List<String> elementsOnlyInList1 = new ArrayList<>(barcodes);
            elementsOnlyInList1.removeAll(barcodesItems);

            // Find elements present in list2 but not in list1
            List<String> elementsOnlyInList2 = new ArrayList<>(barcodesItems);
            elementsOnlyInList2.removeAll(barcodes);

            // Display the results
            System.out.println(ufi + "("+barcodes.size()+" sobres digitalizados)");
            System.out.println("Digitalizados sin item: " + elementsOnlyInList1.size());
            StringBuilder sb = new StringBuilder() ;
            elementsOnlyInList1.forEach((t) -> {
                sb.append(t) ;
                sb.append(", ") ;
            });
            System.out.println(sb.toString());
            sb.setLength(0);
            System.out.println("Items sin digitalizar: " + elementsOnlyInList2.size());
            elementsOnlyInList2.forEach((t) -> {
                sb.append(t) ;
                sb.append(", ") ;
            });
            System.out.println(sb.toString());
        });
    }

    public void verificaCajonCompleto() {
        String consulta = "SELECT DISTINCT ufi FROM items" ;
        List<String> ufis = cron.consultaSimple(consulta, 1);
        Collections.sort(ufis);
        ufis.forEach((ufi) -> {
            String consulta2 = "SELECT DISTINCT barcode FROM items WHERE ufi LIKE '"+ufi+"'" ;
            List<String> barcodesUfi = cron.consultaSimple(consulta2, 1);
            int barcodesEnUfi = barcodesUfi.size() ; 
            consulta2 = "SELECT DISTINCT inv FROM digitales WHERE cajon LIKE '"+ufi+"'" ;
            List<String> barcodesDigi = cron.consultaSimple(consulta2, 1);
            barcodesUfi.removeAll(barcodesDigi) ;
            if (barcodesUfi.isEmpty()) {
                System.out.println("Cajón "+ufi+" digitalizado completo!");
            } else {
                if (barcodesEnUfi == barcodesUfi.size()) {
                    System.out.println("Cajón "+ufi+" sin digitales. Faltan "+barcodesUfi.size()+" sobres");
                } else {
                    System.out.println("Cajón "+ufi+" incompleto. Faltan "+barcodesUfi.size()+" sobres");
                    barcodesUfi.forEach((t) -> {
                        String verificaUfiDigital = verificaUfiDigital(t,ufi);
                        if (verificaUfiDigital.equals(ufi)) {
                            System.out.println(t);
                        } else {
                            System.out.println(t+" en "+verificaUfiDigital);
                        }
                        
                    }) ;
                }
            }
        });
    }

    private String verificaUfiDigital(String inv, String ufi) {
        String consulta = "SELECT cajon FROM digitales WHERE inv LIKE '"+inv+"'" ;
        List<String> cons = cron.consultaSimple(consulta, 1);
        if (cons.isEmpty()) {
            return ufi ;
        } else if(cons.get(0).equals(ufi)) {
            return ufi ;
        } else {
            return cons.get(0) ;
        }
    }

    public void verificaBajas() {
        String consultaAltas = "SELECT DISTINCT cajon FROM digitales WHERE carpeta LIKE 'Altas'" ;
        List<String> cajonesAltas = cron.consultaSimple(consultaAltas, 1);
        cajonesAltas.forEach((cajon) -> {
            List<String> bajas = new ArrayList<>() ;
            List<String> altas = new ArrayList<>() ;
            
            String consultaBajas = "SELECT nombramiento FROM digitales WHERE carpeta LIKE 'Bajas' "
                    + "AND cajon LIKE '"+cajon+"'" ;
            List<String> archivosBajas = cron.consultaSimple(consultaBajas, 1);
            archivosBajas.forEach((t) -> {                
                t = t.replace(".jpg", "") ;
                bajas.add(t) ;
            }) ;
            String consultaArchivosAltas = "SELECT nombramiento FROM digitales WHERE carpeta LIKE 'Altas' "
                    + "AND cajon LIKE '"+cajon+"'" ;
            List<String> archivosAltas = cron.consultaSimple(consultaArchivosAltas, 1);
            archivosAltas.forEach((r) -> {
                r = r.replace(".tif", "") ;
                altas.add(r) ;
            });
            altas.removeAll(bajas) ;
            if (altas.isEmpty()) {
                System.out.println("Bajas para el cajón "+cajon+" ok!");
            } else {
                if (altas.size() == archivosAltas.size()) {
                    System.out.println("Cajón "+cajon+": no hay bajas realizadas\n");                    
                } else {
                    System.out.println("Cajón "+cajon+", faltan "+altas.size()+" bajas sobre "+archivosAltas.size());
                    altas.forEach((t) -> {
                        System.out.println(t);
                    }) ;
                    System.out.println("\n");
                }
                String barcode = altas.get(0).split("_")[1] ;
                System.out.println("GENERANDO BAJAS PARA: "+barcode);
                try {
                    cron.generaYEnviaBajas(barcode) ;
                } catch (SQLException ex) {
                    Logger.getLogger(VerificadorDigitales.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }    

    public void verificaCarpetas() {
        ReubicadorDigitalesOMatic reubicador = new ReubicadorDigitalesOMatic(cron);
        String consulta = "SELECT DISTINCT inv, cajon FROM digitales WHERE carpeta LIKE 'Altas'" ;
        List<String[]> consulta1 = cron.consultaCompleta(consulta);
        consulta1.forEach((t) -> {
            String barcode = t[0];
            String ufiDigi = t[1];
            // Si el barcode existe en Aleph, comparar los valores UFI
            // Crear una instancia de ReubicadorOMatic            

            if (reubicador.correctUfis.containsKey(barcode)) {
                String ufiAleph = reubicador.correctUfis.get(barcode);
                if (!ufiAleph.equals(ufiDigi)) {
                    // Si el UFI no es correcto, mostrar alerta y posiblemente mover la carpeta
                    /*if (ufiAleph.startsWith("S3AFO03")) {
                        reubicador.moverCarpetaBarcode(barcode, ufiDigi, ufiAleph, "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Altas\\",false);
                    }*/
                    reubicador.moverCarpetaBarcode(barcode, ufiDigi, ufiAleph, 
                            "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Altas\\",
                            false);
                }
            }
        });
        
    }
    
    public void acomodaBajas() {
        ReubicadorDigitalesOMatic reubicador = new ReubicadorDigitalesOMatic(cron);
        String consulta = "SELECT DISTINCT inv, cajon FROM digitales WHERE carpeta LIKE 'Bajas'" ;
        List<String[]> consulta1 = cron.consultaCompleta(consulta);
        consulta1.forEach((t) -> {
            String barcode = t[0];
            String ufiDigi = t[1];
            // Si el barcode existe en Aleph, comparar los valores UFI
            // Crear una instancia de ReubicadorOMatic            

            if (reubicador.correctUfis.containsKey(barcode)) {
                String ufiAleph = reubicador.correctUfis.get(barcode);
                if (!ufiAleph.equals(ufiDigi)) {
                    // Si el UFI no es correcto, mostrar alerta y posiblemente mover la carpeta
                    /*if (ufiAleph.startsWith("S3AFO013")) {
                        reubicador.moverCarpetaBarcode(barcode, ufiDigi, ufiAleph, "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Altas\\");
                    }*/
                    reubicador.moverCarpetaBarcode(barcode, ufiDigi, ufiAleph, 
                            "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Bajas\\",
                            false);
                }
            }
        });
    }
}
