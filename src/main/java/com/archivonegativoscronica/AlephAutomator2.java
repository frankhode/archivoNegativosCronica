package com.archivonegativoscronica;

import com.archivonegativoscronica.AlephClipboardWriter;
import com.archivonegativoscronica.Keyboard;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class AlephAutomator2 {

    private final Keyboard key;
    private final Robot robot;
    private final AlephClipboardWriter clipboardWriter;

    public AlephAutomator2() throws AWTException {
        this.robot = new Robot();
        this.key = new Keyboard(robot);
        this.clipboardWriter = new AlephClipboardWriter();
    }

    public void copiarRegistroAlephTag(String registroSinSys) {
        clipboardWriter.copiarAlephTagExperimentalDesdeTexto(normalizarSaltos(registroSinSys), "UTF-8");
    }
    
    /**
    * Flujo completo COM2:
    *
    * 1. Enfoca Aleph.
    * 2. Abre el registro bibliográfico por SYS.
    * 3. Borra los campos editables.
    * 4. Copia el registro definitivo al clipboard como ALEPH_TAG.
    * 5. Vuelve a enfocar Aleph.
    * 6. Pega con Alt+T.
    */
   public void reemplazarRegistroCompletoCOM2(String sys, String registroSinSys) throws InterruptedException {
       enfocarAleph();
       abrirRegistro(sys);
       borrarCampos();

       copiarRegistroAlephTag(registroSinSys);

       enfocarAleph();
       pegarConAltT();
   }

    public void enfocarAleph() throws InterruptedException {
        key.enfocaAleph();
        dormir(500);
    }

    public void abrirRegistro(String sys) {
        key.abreBibliografico(sys);
        dormir(1800);
    }

    public void borrarCampos() {
        key.borraCampos();
        dormir(800);
    }

    public void pegarConAltT() {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_T);
        dormir(80);
        robot.keyRelease(KeyEvent.VK_T);
        robot.keyRelease(KeyEvent.VK_ALT);
        dormir(800);
    }

    /**
     * Prueba segura: no abre ni borra; solo enfoca Aleph y pega el contenido.
     */
    public void pegarRegistroSinBorrar(String registroSinSys) throws InterruptedException {
        copiarRegistroAlephTag(registroSinSys);
        enfocarAleph();
        pegarConAltT();
    }

    /**
     * Flujo completo COM2:
     *
     * 1. Copia registro definitivo al clipboard como ALEPH_TAG.
     * 2. Enfoca Aleph.
     * 3. Abre el registro bibliográfico por SYS.
     * 4. Borra campos.
     * 5. Pega el registro generado con Alt+T.
     */
    public void reemplazarCamposConRegistro(String sys, String registroSinSys) throws InterruptedException {
        copiarRegistroAlephTag(registroSinSys);
        enfocarAleph();
        abrirRegistro(sys);
        borrarCampos();
        pegarConAltT();
    }

    /**
     * Variante anterior: asume que el registro ya está abierto.
     */
    public void reemplazarCamposConRegistro(String registroSinSys) throws InterruptedException {
        copiarRegistroAlephTag(registroSinSys);
        enfocarAleph();
        borrarCampos();
        pegarConAltT();
    }

    private String normalizarSaltos(String texto) {
        if (texto == null) {
            return "";
        }

        String normalizado = texto.replace("\r\n", "\n").replace("\r", "\n");
        normalizado = normalizado.replace("\n", "\r\n");

        if (!normalizado.endsWith("\r\n")) {
            normalizado = normalizado + "\r\n";
        }

        return normalizado;
    }

    private void dormir(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}