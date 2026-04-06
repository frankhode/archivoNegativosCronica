/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author francisco.ortiz
 */
class CatalogadorOMatic {
    Keyboard key ;    
    RegistrosParaAgregar regsParaActualizar ;
    ConjuntosParaAleph registrosInventario ;
    //private final InterfazCatalogadorOMatic icom ;

    CatalogadorOMatic(Funciones cron) throws AWTException, InterruptedException, IOException {
        key = new Keyboard(new Robot()) ;        
        
        //conjunto de registros para trabajar
        registrosInventario = new ConjuntosParaAleph(cron, 0, true, true) ;
        
        //separamos en tres categorias IND - GRUPOS - REGS PARA ACTUALIZAR
        regsParaActualizar = new RegistrosParaAgregar(cron) ;
        //registros individuales para agregar a registros existentes en aleph        
        InventariosPendientes pendientes = new InventariosPendientes(registrosInventario) ;
        COM com = new COM(pendientes,cron,key) ;
        com.setRegistrosParaAgregar(regsParaActualizar) ;
        addConju(cron);
    }
    
    CatalogadorOMatic(Funciones cron, ConjuntosParaAleph registrosInventario,HashMap<String,List<String[]>> regs) throws AWTException, InterruptedException, IOException {
        key = new Keyboard(new Robot()) ;        
        
        this.registrosInventario = registrosInventario ;
        
        //separamos en tres categorias IND - GRUPOS - REGS PARA ACTUALIZAR
        regsParaActualizar = new RegistrosParaAgregar(cron) ;
        regs.forEach((sys, list) -> {
            if (regsParaActualizar.getRegs().containsKey(sys)) {
                list.forEach((t) -> {
                    regsParaActualizar.addReg(sys, t);
                    regsParaActualizar.addTitulo(sys,tituloCompleto(t));
                }) ;                
            } else {
                list.forEach((t) -> {
                    regsParaActualizar.addReg(sys, t);
                    regsParaActualizar.addTitulo(sys,tituloCompleto(t));
                }) ;
            }
        });
        
        //registros individuales para agregar a registros existentes en aleph        
        InventariosPendientes pendientes = new InventariosPendientes(registrosInventario) ;
        COM com = new COM(pendientes,cron,key) ;
        com.setRegistrosParaAgregar(regsParaActualizar) ;
        addConju(cron);
    }
    
    public void regsIndParaAgregar(ConjuntosParaAleph regs) {
        List<String[]> regsParaQuitarDeLaLista = new ArrayList<>() ;
        regs.getRegIndi().forEach((reg) -> { 
            if (regs.verificaUnificacion(reg[5])) {
                String sysUnificador = regs.getUni().getSysUnificador(reg[5]);
                regsParaActualizar.addReg(sysUnificador, reg);                
                regsParaActualizar.addTitulo(sysUnificador, tituloCompleto(reg));
                regsParaQuitarDeLaLista.add(reg) ;
            } else {
                //System.out.println("No hay posible unificacion para "+reg[5]);
            }
        });
        regs.getRegIndi().removeAll(regsParaQuitarDeLaLista) ;
    }
    
    public void regsGrupalesParaAgregar(ConjuntosParaAleph regs) {
        List<RegistroGrupal> regsParaQuitarDeLaLista = new ArrayList<>() ;        
        regs.getRegGrupos().forEach((reg) -> { 
            if (regs.verificaUnificacion(reg.getTitulo())) {
                String sysUnificador = regs.getUni().getSysUnificador(reg.getTitulo());
                reg.getArrayRegistros().forEach((t) -> {
                    regsParaActualizar.addReg(sysUnificador, t);
                    regsParaActualizar.addTitulo(sysUnificador, tituloCompleto(t));
                });
                regsParaQuitarDeLaLista.add(reg) ;
            } else {
                //System.out.println("No hay posible unificacion para "+reg[5]);
            }
        });
        regs.getRegGrupos().removeAll(regsParaQuitarDeLaLista) ;
    }

    private String tituloCompleto(String[] reg) {
        String titulo ;
        if (reg[1].equals("")) {
            titulo = "["+reg[0]+"]. "+reg[5] ;            
        } else {
            titulo = reg[1]+". "+reg[5] ;
        }
        if (!reg[6].equals("")) {
            titulo = titulo + ", " + RegistrosParaAleph.fechaFormateada(reg[6]) ;
        }
        return titulo ;
    }


    public class InventariosPendientes {
        List<String[]> regIndi ;
        List<RegistroGrupal> regGrupos ;
        HashMap<String, List<String[]>> regs;

        public InventariosPendientes(ConjuntosParaAleph registrosInventario) {
            regsIndParaAgregar(registrosInventario);
            regsGrupalesParaAgregar(registrosInventario);        
            
            //funcion original
            //regIndi = registrosInventario.getRegIndi() ;
            //funcion nueva, solo los asignados como IND
            regIndi = registrosInventario.getRegIND() ;
            
            regGrupos = registrosInventario.getRegGrupos() ;
            regs = regsParaActualizar.getRegs() ;
        }

        public List<String[]> getRegsIndiv() {
            return regIndi ;
        }

        public List<RegistroGrupal> getRegsGrupos() {
            return regGrupos ;
        }

        public HashMap<String, List<String[]>>  getRegsParaActualizar() {
            return regs ;
        }
        
    }
    
    private void addConju(Funciones cron) {
        String consultaConjuntos = "SELECT DISTINCT titulo FROM conjuntos "
                + "WHERE barcode NOT IN (SELECT barcode FROM items) AND "
                + "status LIKE '1'" ;
        List<String> titulos = cron.consultaSimple(consultaConjuntos,1);        
        titulos.forEach((tit) -> {
            RegistroGrupal rg = new RegistroGrupal(registrosInventario.getUni()) ;            
            rg.setTitulo(tit);            
            String consultaBar = "SELECT barcode FROM conjuntos WHERE titulo LIKE '"+tit+"' "
                    + "AND barcode NOT IN (SELECT barcode FROM items)" ;
            List<String> barcodes = cron.consultaSimple(consultaBar,1);
            for (String barcode : barcodes) {
                String inv = "SELECT * FROM inventario WHERE barcode LIKE '"+barcode+"'"
                        + "AND barcode NOT IN (SELECT barcode FROM items)" ;
                List<String[]> invs = cron.consultaCompleta(inv);
                for (String[] inv1 : invs) {
                    cargaGrupo(rg, inv1);
                    rg.addArrayRegistros(inv1);
                }
            }
            registrosInventario.getRegGrupos().add(rg) ;
        });
    }
    
    public void cargaGrupo(RegistroGrupal reg,String[] lineaReg) {
        //autor
        reg.agregaAutores(lineaReg[4]);
        //fecha
        reg.agregaFecha(lineaReg[6]);
        //para inventario
        String inv,nro_a,ufi ;
        try {inv = lineaReg[0] ;} catch (Exception e) {
            inv = "ERROR DE CODIGO DE BARRAS" ;}
        //numero original
        try {
            if (!lineaReg[1].equals("")) {
                nro_a = lineaReg[1] ;
            } else {
                nro_a = "["+lineaReg[0]+"]" ;
            }
            
        } catch (Exception e) {
            nro_a = "ERROR DE NUMERO ORIGINAL" ;}
        //ubicacion fisica
        try {ufi = lineaReg[8] ;} catch (Exception e) {
            ufi = "ERROR DE CODIGO DE UBICACION FISICA" ;}
        reg.agregaItems(inv, nro_a, ufi);
        //titulo
        reg.agregaTitulos(nro_a+". "+lineaReg[5],RegistrosParaAleph.fechaFormateada(lineaReg[6]));
        reg.sumaTiras(1);
    }
}
