/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author francisco.ortiz
 */
public class WikiPane extends BorderPane {
    TabPane tabPane ;
    Tab esp, eng ;
    
    public WikiPane(String tema) {
        //top cuadro de busqueda + boton
        Button btn = new Button("Buscar") ;
        TextField search = new TextField();
        search.setPromptText(tema);
        btn.setOnAction((t) -> {
            busca(search.getText()) ;
        });
        HBox hb = new HBox(search,btn) ;
        this.setTop(hb);
        
        //center resultados
        tabPane = new TabPane() ;
        this.setCenter(tabPane);
    }

    private void busca(String txt) {
        ResultadoWiki es = searchInWikipedia(txt, "es");
        ResultadoWiki en = searchInWikipedia(txt, "en");
        if (!es.titulos.isEmpty()) {
            esp = new Tab("Español") ;
            agregaResultados(esp,es) ;
            tabPane.getTabs().add(esp) ;
        } else {
            tabPane.getTabs().remove(esp) ;
        }
        if (!en.titulos.isEmpty()) {
            eng = new Tab("English") ;            
            agregaResultados(eng,en) ;
            tabPane.getTabs().add(eng) ;
        } else {
            tabPane.getTabs().remove(eng) ;
        }
    }
    
    private ResultadoWiki searchInWikipedia(String searchTerm, String language) {
        ResultadoWiki resultado = new ResultadoWiki() ;
        try {
            String encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8");
            String urlStr = "https://" + language + 
                    ".wikipedia.org/w/api.php?action=query&format=json&prop="
                    + "extracts&exintro&explaintext&redirects&list=search&srsearch=" 
                    + encodedSearchTerm;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()))) {
                    response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Analizar la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray searchResults = jsonResponse.getJSONObject("query").getJSONArray("search");

                // Imprimir los resultados
                System.out.println("Resultados de la búsqueda para '" + searchTerm + "' en " + language + ":");
                for (int i = 0; i < searchResults.length(); i++) {
                    JSONObject result = searchResults.getJSONObject(i);
                    String title = result.getString("title");
                    String snippet = result.getString("snippet");
                    int pageId = result.getInt("pageid");
                    resultado.addTitle(title) ;
                    resultado.addSnippet(snippet) ;
                    resultado.addEnlace("https://" + language + ".wikipedia.org/?curid=" + pageId) ;
                }
            } else {
                System.out.println("HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
        }
        return resultado ;
    }

    private void agregaResultados(Tab tab, ResultadoWiki es) {        
        VBox vb = new VBox();
        ScrollPane sp = new ScrollPane(vb);
        vb.setStyle("-fx-background-color: black;");
        tab.setContent(sp);
        int cont = 0;

        for (String titulo : es.getTitulos()) {            
            Label tit = new Label(titulo);
            tit.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
            WebView snip = new WebView();            
            snip.setPrefWidth(300);
            snip.setPrefHeight(110);
            String snippetContent = "<html><body style='background-color: white; color: black;'>"
                + es.getSnippets().get(cont)
                + "</body></html>";
            snip.getEngine().loadContent(snippetContent);
            String enlace = es.getEnlaces().get(cont);
            Hyperlink link = new Hyperlink("Abrir URL");
            link.setPrefHeight(10);
            link.setOnAction(event -> {
                try {
                    openURL(enlace);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(WikiPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            VBox entryBox = new VBox(tit, snip, link);
            entryBox.setSpacing(10);
            entryBox.setStyle("-fx-background-color: #eaeaea; "
                    + "-fx-padding: 10px; -fx-border-color: white; "
                    + "-fx-border-width: 1px; -fx-border-radius: 5px;");

            cont++;
            vb.getChildren().add(entryBox);
        }
    }


    private void openURL(String url) throws URISyntaxException {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
        }
    }

    private static class ResultadoWiki {

        private final List<String> titulos,snippets,enlaces ;
        
        public ResultadoWiki() {
            titulos = new ArrayList<>() ;
            snippets = new ArrayList<>() ;
            enlaces = new ArrayList<>() ;
        }

        private void addTitle(String titulo) {
            titulos.add(titulo) ;
        }
        
        private void addSnippet(String snip) {
            snippets.add(snip) ;
        }
        
        private void addEnlace(String link) {
            enlaces.add(link) ;
        }
        
        public List<String> getTitulos() {
            return titulos;
        }
        
        public List<String> getSnippets() {
            return snippets;
        }

        public List<String> getEnlaces() {
            return enlaces;
        }
        
        
    }
    
}
