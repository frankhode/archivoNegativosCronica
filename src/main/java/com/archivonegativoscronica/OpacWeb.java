package com.archivonegativoscronica;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpacWeb {

    public OpacWeb(String sys) {
        String url = "https://catalogo.bn.gov.ar/F/?func=find-c&ccl_term=SYS%20=%20("+sys+")" ;
        abrirURL(url);
    }

    private void abrirURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                URI uri = new URI(url);
                Desktop.getDesktop().browse(uri);
            } else {
                System.err.println("Navegador no soportado en este sistema.");
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
