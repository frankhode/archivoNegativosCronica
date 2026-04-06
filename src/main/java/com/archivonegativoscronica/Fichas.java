/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*        Map<String, List<String>> config = new HashMap<>();
        config.put("A", Arrays.asList("AAAT-AHOGADOS", "AHOGADOS-ARECO", "ARGENTINA-ASAMBLEA", "ASESINATO-ATENTADOS"));
        config.put("B", Arrays.asList("BACHES-BAQUEIRO", "BARACA-BARRYMORE", "BAS-BAZZO JOSE", "BEACH-BENERJEE",
                "BENETTI-BEZERRA", "BIAFRA-BLUNDO", "BO-BOMBA", "BOMBA-BOTEJARA", "BOTELLA-BRZIC", "BU BU-BUZZO"));
        config.put("C", Arrays.asList("CAAMAÑO-CALLES", "CAMALOTES-CAPUTO", "CAR LE-CARVALLO", "CASA-CAZON",
                "C.E.A-CLYN", "COBAS-CONSEJALES", "CONCEPCION-CORZO", "COSIA-CZYTERSPILLER"));
        config.put("CH", Arrays.asList("CHIQUILIN-CHURSINA", "CHABAY-CHIOZZA"));
        config.put("D", Arrays.asList("DA CARLI-DATARMINE", "DATI-DE ZILILLO", "DEADAMICH-DENUNCIA", "DEPARDIEU-DHUT",
                "DI BARI-DIARIO", "DIARIO CRONICA-DIARIO CRONICA", "DIARIO EL ATLANTICO-DMITRUK",
                "DO BARRO-DRAGO", "DRAGON-DINAMO"));
        config.put("E", Arrays.asList("EASTWIND-ELECTRICIDAD", "ELECTRICIDAD-ENFERMEDAD",
                "ENFERMEDAD-ESQUIARELA", "ESQUIEVEL-EZEIZABARRENA"));
        config.put("F", Arrays.asList("FABBIANI-FARALL", "FARAONE-FERRARI", "FERRARO-FEYNOORD",
                "FIFA-FOZIATI", "FRA-FUX"));
        config.put("G", Arrays.asList("GABANI-GARCILASO", "GARDEL-GINESIN", "GIOANNINI-GOMOLLAN",
                "GONCALVEZ-GRELA", "GREMIALISTAS-GRUPO VOCAL 4", "GUACHALIA-GUZZETTI"));
        config.put("H", Arrays.asList("HAAR-HERCHEVEZ", "HIDALGO-HORWITZ", "HOSPITALES-HYDER"));
        config.put("I", Arrays.asList("I MEDICI-IGLESIAS VAZQUEZ", "INCHAUSTI-IZIDORE", "IGNOMIRIELO-IZNIAQUI"));
        config.put("J", Arrays.asList("JABARVICH-JUANCHI", "JUANCHO-JUZGADO NAC"));
        config.put("K", Arrays.asList("KAARR MARIA-KUZENCA"));
        config.put("L", Arrays.asList("LA AGRICOLA-LAPISTOY", "LAPLACE-LAZZER", "LA BRETON-LHUILLER",
                "LIBANES-LONARDI", "LONDA-LORENZO", "LORENZO-LOZHNOVA", "LUACES-LYON"));
        config.put("LL", Arrays.asList("LLABRES JUAN B-LLUVIA"));
        config.put("M", Arrays.asList("MABELLINI-MAMONTOV", "MAN-MAQKENZIE", "MAR-MARRALE",
                "MARI-MARROQUINERIA", "MARS-MARZZOCCA", "MAS-MCKEE", "MECA-MENENDEZ BEHETY",
                "MENENDEZ BENJAMIN-MEZZARRO", "MI CIUDAD-MITTERRAND", "MOBILI-MONZON NIEVES",
                "MONZON NICEFORO-MORON", "MOR-MUZZUPPAPA"));
        config.put("N", Arrays.asList("NACCARATTI-NIÑOS", "NIPOLI-ÑOQUIS"));
        config.put("Ñ", Arrays.asList("NACCARATTI-NIÑOS", "NIPOLI-ÑOQUIS"));
        config.put("O", Arrays.asList("OBARRIOS-OCUPACION", "OCHANDIO-ORDENES", "ORDOÑE-OZZAN"));
        config.put("P", Arrays.asList("PABLO-PANAMA", "PANARO-PASCUAL", "PASCUALINI-PAZOS",
                "PEATONES-PERETZ", "PEREYRA-PERONISTAS", "PEROTI-PICO", "PICOLI-PLOMO",
                "POBLETE-POLICIA DE PCIA.BS.AS", "POLICIA DE CORDOBA-PONTI SILVIA",
                "PONTIER-PRAVALOVSKY", "PREBISCH-PROSTITUCION", "PROTESTA-PUSINERI"));
        config.put("Q", Arrays.asList("QUADROS-QUITEGUI"));
        config.put("R", Arrays.asList("RABANAL-RANDAZO", "RANDO-RECUPERO", "REDACCION-REPRODUCCIONES",
                "REQUE-REY", "REYBAUD-RIZZUTO", "ROA-RODRIGUEZ", "ROFERS-ROLLINGS",
                "ROMA-ROZOS", "RUA-RSOJIEVICH"));
        config.put("S", Arrays.asList("S. Y D-SALGUEIRO, ADRIANA", "SALGUEIRO MARIO-SANFRINI",
                "SANGRE-SVHIAVONE", "SCHIFFER-SERRANO", "SERRAO-SLAYTON",
                "SLIPAR-SOTANG", "SOTELO-STEWART ANNE", "STEWART JACKE-SZMETAN"));
        config.put("T", Arrays.asList("T.N.T-TEATRO ALFIL", "TEATRO ALVEAR-TELEVISION CRONICA",
                "TELEVISION CRONICA TV-TINTORELLI", "TIOS QUERIDOS- TORRES VILA",
                "TORRESI-TRIBUNALES", "TRIENTINI-TZACHI"));
        config.put("U", Arrays.asList("U.I.A-UZURIAGA"));
        config.put("V", Arrays.asList("VACA-VARGAS", "VARIEDADES-VELI", "VELIZ-VIDEO SHOW",
                "VIDRIERAS-VILLORESSI", "VIMO-VULTNINK"));
        config.put("W", Arrays.asList("WAAJNEWAJG-WULLICIA"));
        config.put("X", Arrays.asList("XACUR-XUXA"));
        config.put("Y", Arrays.asList("YABER-YUSTINE"));
        config.put("Z", Arrays.asList("ZABALA- ZARASPE", "ZARATE-ZYWICA"));
        CONFIG_FICHAS = Collections.unmodifiableMap(config);*/
    

package com.archivonegativoscronica;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Fichas {

    // Ruta base (ajusta según corresponda o usa digitalFolder() para elegirla)
    private static final String DEFAULT_FOLDER_PATH = "U:\\Mapo-Cronica\\004-ordenados_DMFC\\Bajas\\FICHERO CROAF";

    // Map con las subcarpetas de cada letra.
    // Por ejemplo, para la letra "A" se tienen varias carpetas (cajones)
    private static final Map<String, List<String>> CONFIG_FICHAS;
    static {
        Map<String, List<String>> config = new HashMap<>();
        config.put("A", Arrays.asList("AAAT-AHOGADOS", "AHOGADOS-ARECO", "ARGENTINA-ASAMBLEA", "ASESINATO-ATENTADOS"));
        config.put("B", Arrays.asList("BACHES-BAQUEIRO", "BARACA-BARRYMORE", "BAS-BAZZO JOSE", "BEACH-BENERJEE",
                "BENETTI-BEZERRA", "BIAFRA-BLUNDO", "BO-BOMBA", "BOMBA-BOTEJARA", "BOTELLA-BRZIC", "BU BU-BUZZO"));
        config.put("C", Arrays.asList("CAAMAÑO-CALLES", "CAMALOTES-CAPUTO", "CAR LE-CARVALLO", "CASA-CAZON",
                "C.E.A-CLYN", "COBAS-CONSEJALES", "CONCEPCION-CORZO", "COSIA-CZYTERSPILLER"));
        config.put("CH", Arrays.asList("CHIQUILIN-CHURSINA", "CHABAY-CHIOZZA"));
        config.put("D", Arrays.asList("DA CARLI-DATARMINE", "DATI-DE ZILILLO", "DEADAMICH-DENUNCIA", "DEPARDIEU-DHUT",
                "DI BARI-DIARIO", "DIARIO CRONICA-DIARIO CRONICA", "DIARIO EL ATLANTICO-DMITRUK",
                "DO BARRO-DRAGO", "DRAGON-DINAMO"));
        config.put("E", Arrays.asList("EASTWIND-ELECTRICIDAD", "ELECTRICIDAD-ENFERMEDAD",
                "ENFERMEDAD-ESQUIARELA", "ESQUIEVEL-EZEIZABARRENA"));
        config.put("F", Arrays.asList("FABBIANI-FARALL", "FARAONE-FERRARI", "FERRARO-FEYNOORD",
                "FIFA-FOZIATI", "FRA-FUX"));
        config.put("G", Arrays.asList("GABANI-GARCILASO", "GARDEL-GINESIN", "GIOANNINI-GOMOLLAN",
                "GONCALVEZ-GRELA", "GREMIALISTAS-GRUPO VOCAL 4", "GUACHALIA-GUZZETTI"));
        config.put("H", Arrays.asList("HAAR-HERCHEVEZ", "HIDALGO-HORWITZ", "HOSPITALES-HYDER"));
        config.put("I", Arrays.asList("I MEDICI-IGLESIAS VAZQUEZ", "INCHAUSTI-IZIDORE", "IGNOMIRIELO-IZNIAQUI"));
        config.put("J", Arrays.asList("JABARVICH-JUANCHI", "JUANCHO-JUZGADO NAC"));
        config.put("K", Arrays.asList("KAARR MARIA-KUZENCA"));
        config.put("L", Arrays.asList("LA AGRICOLA-LAPISTOY", "LAPLACE-LAZZER", "LA BRETON-LHUILLER",
                "LIBANES-LONARDI", "LONDA-LORENZO", "LORENZO-LOZHNOVA", "LUACES-LYON"));
        config.put("LL", Arrays.asList("LLABRES JUAN B-LLUVIA"));
        config.put("M", Arrays.asList("MABELLINI-MAMONTOV", "MAN-MAQKENZIE", "MAR-MARRALE",
                "MARI-MARROQUINERIA", "MARS-MARZZOCCA", "MAS-MCKEE", "MECA-MENENDEZ BEHETY",
                "MENENDEZ BENJAMIN-MEZZARRO", "MI CIUDAD-MITTERRAND", "MOBILI-MONZON NIEVES",
                "MONZON NICEFORO-MORON", "MOR-MUZZUPPAPA"));
        config.put("N", Arrays.asList("NACCARATTI-NIÑOS", "NIPOLI-ÑOQUIS"));
        config.put("Ñ", Arrays.asList("NACCARATTI-NIÑOS", "NIPOLI-ÑOQUIS"));
        config.put("O", Arrays.asList("OBARRIOS-OCUPACION", "OCHANDIO-ORDENES", "ORDOÑE-OZZAN"));
        config.put("P", Arrays.asList("PABLO-PANAMA", "PANARO-PASCUAL", "PASCUALINI-PAZOS",
                "PEATONES-PERETZ", "PEREYRA-PERONISTAS", "PEROTI-PICO", "PICOLI-PLOMO",
                "POBLETE-POLICIA DE PCIA.BS.AS", "POLICIA DE CORDOBA-PONTI SILVIA",
                "PONTIER-PRAVALOVSKY", "PREBISCH-PROSTITUCION", "PROTESTA-PUSINERI"));
        config.put("Q", Arrays.asList("QUADROS-QUITEGUI"));
        config.put("R", Arrays.asList("RABANAL-RANDAZO", "RANDO-RECUPERO", "REDACCION-REPRODUCCIONES",
                "REQUE-REY", "REYBAUD-RIZZUTO", "ROA-RODRIGUEZ", "ROFERS-ROLLINGS",
                "ROMA-ROZOS", "RUA-RSOJIEVICH"));
        config.put("S", Arrays.asList("S. Y D-SALGUEIRO, ADRIANA", "SALGUEIRO MARIO-SANFRINI",
                "SANGRE-SVHIAVONE", "SCHIFFER-SERRANO", "SERRAO-SLAYTON",
                "SLIPAR-SOTANG", "SOTELO-STEWART ANNE", "STEWART JACKE-SZMETAN"));
        config.put("T", Arrays.asList("T.N.T-TEATRO ALFIL", "TEATRO ALVEAR-TELEVISION CRONICA",
                "TELEVISION CRONICA TV-TINTORELLI", "TIOS QUERIDOS- TORRES VILA",
                "TORRESI-TRIBUNALES", "TRIENTINI-TZACHI"));
        config.put("U", Arrays.asList("U.I.A-UZURIAGA"));
        config.put("V", Arrays.asList("VACA-VARGAS", "VARIEDADES-VELI", "VELIZ-VIDEO SHOW",
                "VIDRIERAS-VILLORESSI", "VIMO-VULTNINK"));
        config.put("W", Arrays.asList("WAAJNEWAJG-WULLICIA"));
        config.put("X", Arrays.asList("XACUR-XUXA"));
        config.put("Y", Arrays.asList("YABER-YUSTINE"));
        config.put("Z", Arrays.asList("ZABALA- ZARASPE", "ZARATE-ZYWICA"));
        // Agrega las demás letras y sus carpetas según corresponda...
        CONFIG_FICHAS = Collections.unmodifiableMap(config);
    }

    // Componentes principales de la interfaz
    private final Tab mainTab;
    private final TabPane mainTabPane;
    private File folder;

    // Patrón para extraer número y lado (por ejemplo: CROAF_F00001F.jpg)
    // Se usa 0* para ignorar los ceros a la izquierda
    private static final Pattern FICHA_PATTERN = Pattern.compile("CROAF_F0*(\\d+)([FR])\\.jpg", Pattern.CASE_INSENSITIVE);

    // Constructor de Fichas: crea el TabPane principal con pestañas por letra
    public Fichas() {
        // Inicializa la carpeta (o se puede elegir dinámicamente)
        folder = new File(DEFAULT_FOLDER_PATH);

        mainTab = new Tab("Fichero Crónica");
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Ejemplo: letras "A", "B", "C" (ajusta según tus necesidades)
        List<String> letras = Arrays.asList("A","B","C","CH","D","E","F","G","H","I","J",
                "K","L","M","N","Ñ","O","P","Q","R","S","T","U","V","W","X","Y","Z");
        for (String letra : letras) {
            Tab letterTab = new Tab(letra);
            letterTab.setContent(createInternalFichaTabs(letra));
            mainTabPane.getTabs().add(letterTab);
        }
        mainTab.setContent(mainTabPane);
    }

    // Crea el TabPane interno para una letra, cada pestaña es una carpeta (cajón)
    private TabPane createInternalFichaTabs(String letter) {
        TabPane internalTabPane = new TabPane();
        internalTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        List<String> folders = CONFIG_FICHAS.getOrDefault(letter, new ArrayList<>());
        for (String folderName : folders) {
            Tab tab = new Tab(folderName);
            // Se asigna inicialmente null para evitar cargar todo de una vez.
            tab.setContent(null);
            tab.setOnSelectionChanged(e -> {
                if (tab.isSelected()) {
                    // Al seleccionarse, si no tiene contenido, se crea el FichaPane.
                    if (tab.getContent() == null) {
                        tab.setContent(new FichaPane(folder, folderName));
                    }
                } else {
                    // Al deseleccionarse, liberamos la memoria limpiando el contenido.
                    tab.setContent(null);
                }
            });
            internalTabPane.getTabs().add(tab);
        }
        return internalTabPane;
    }



    // Método auxiliar para crear un botón con imagen
    private static Button createImageButton(String resourcePath, double width, double height) {
        Button btn = new Button();
        Image img = new Image(Fichas.class.getResourceAsStream(resourcePath), width, height, false, false);
        btn.setGraphic(new ImageView(img));
        return btn;
    }


    // Clase interna que representa el panel de visualización de un cajón (carpeta)
    private static class FichaPane extends BorderPane {

        // Variables de estado para esta carpeta
        private int currentCard;   // número de ficha actual
        private int totalCards;    // total de fichas en la carpeta
        private int minCard;       // número mínimo de ficha en la carpeta
        private File[] fileList;
        private Image frontImage, backImage;
        private boolean showingBack;
        private String folderName;
        

        // Botones de navegación y giro (propios de este panel)
        private Button btnAnterior, btnPosterior, btnMasDiez, btnMenosDiez, btnGiraFicha;
        private Text txtFichaInfo;
        private BackgroundSize backgroundSize;
        private Background currentBackground;
        private final File baseFolder;

        public FichaPane(File folder, String folderName) {
            this.baseFolder = folder;
            this.folderName = folderName;
            // Inicializar estado
            currentCard = 0;
            totalCards = 0;
            minCard = Integer.MAX_VALUE;
            showingBack = false;
            // Crear y configurar este panel
            setPadding(new Insets(10));

            // Crear botones locales
            btnGiraFicha = new Button("Girar ficha");
            btnGiraFicha.setOnAction(e -> toggleFichaSide());
            btnAnterior = createImageButton("/files/anterior.png", 50, 50);
            btnAnterior.setOnAction(e -> navigateFicha(NavigationDirection.PREVIOUS));
            btnPosterior = createImageButton("/files/posterior.png", 50, 50);
            btnPosterior.setOnAction(e -> navigateFicha(NavigationDirection.NEXT));
            btnMenosDiez = createImageButton("/files/menosDiez.png", 50, 50);
            btnMenosDiez.setOnAction(e -> navigateFicha(NavigationDirection.BACK_TEN));
            btnMasDiez = createImageButton("/files/masDiez.png", 50, 50);
            btnMasDiez.setOnAction(e -> navigateFicha(NavigationDirection.FORWARD_TEN));

            // Cargar las imágenes y determinar el rango de fichas
            loadFicha();
            updateFichaInfo();
            updateNavigationButtons();
        }

        // Carga los archivos y determina el rango de fichas; luego carga la ficha actual
        private void loadFicha() {
            frontImage = null;
            backImage = null;
            File fichaFolder = new File(baseFolder, folderName);
            fileList = fichaFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") && !name.equalsIgnoreCase("thumbs.db")
            );
            // Primer pase: determinar el rango de fichas en esta carpeta
            for (File file : fileList) {
                Matcher matcher = FICHA_PATTERN.matcher(file.getName());
                if (matcher.matches()) {
                    int fileNum = Integer.parseInt(matcher.group(1));
                    int card = (fileNum % 2 == 1) ? (fileNum + 1) / 2 : fileNum / 2;
                    minCard = Math.min(minCard, card);
                    totalCards = Math.max(totalCards, card);
                }
            }
            if (minCard == Integer.MAX_VALUE) {
                minCard = 1;
                totalCards = 1;
            }
            // Si currentCard aún es 0 (primer carga), ajustarlo al mínimo
            if (currentCard < minCard) {
                currentCard = minCard;
            }
            if (currentCard > totalCards) {
                currentCard = totalCards;
            }
            // Segundo pase: cargar las imágenes correspondientes a currentCard
            for (File file : fileList) {
                Matcher matcher = FICHA_PATTERN.matcher(file.getName());
                if (matcher.matches()) {
                    int fileNum = Integer.parseInt(matcher.group(1));
                    int card = (fileNum % 2 == 1) ? (fileNum + 1) / 2 : fileNum / 2;
                    if (card == currentCard) {
                        String side = matcher.group(2).toUpperCase();
                        if ("F".equals(side)) {
                            frontImage = new Image("file:" + file.getAbsolutePath());
                        } else if ("R".equals(side)) {
                            backImage = new Image("file:" + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (frontImage == null && backImage != null) {
                frontImage = backImage;
            }
            showingBack = false;
            if (frontImage != null) {
                updateFichaDisplay(frontImage);
            } else {
                showAlert("No se encontró imagen para la ficha " + currentCard);
            }
        }

        // Actualiza el fondo del panel con la imagen dada
        private void updateFichaDisplay(Image image) {
            backgroundSize = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false
            );
            currentBackground = new Background(new BackgroundImage(
                    image,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    backgroundSize
            ));
            setBackground(currentBackground);
        }

        // Actualiza el texto informativo (número de ficha y lado) y lo coloca en la parte superior
        private void updateFichaInfo() {
            String sideText = showingBack ? " (reverso)" : " (frente)";
            txtFichaInfo = new Text("Ficha " + currentCard + " de " + totalCards + sideText);
            VBox infoBox = new VBox(10, txtFichaInfo, btnGiraFicha);
            infoBox.setPadding(new Insets(20));
            setTop(infoBox);
        }

        // Configura los botones de navegación en función de la ficha actual
        private void updateNavigationButtons() {
            VBox leftBox = null;
            VBox rightBox = null;
            // En el lateral izquierdo: si currentCard > minCard, mostrar "anterior" y, si corresponde, "-10"
            if (currentCard > minCard) {
                List<Button> leftButtons = new ArrayList<>();
                leftButtons.add(btnAnterior);
                if (currentCard >= minCard + 10) {
                    leftButtons.add(btnMenosDiez);
                }
                leftBox = new VBox(10);
                leftBox.getChildren().addAll(leftButtons);
                leftBox.setAlignment(Pos.CENTER);
                leftBox.setPadding(new Insets(20));
            }
            // En el lateral derecho: si currentCard < totalCards, mostrar "siguiente" y, si corresponde, "+10"
            if (currentCard < totalCards) {
                List<Button> rightButtons = new ArrayList<>();
                rightButtons.add(btnPosterior);
                if (currentCard <= totalCards - 10) {
                    rightButtons.add(btnMasDiez);
                }
                rightBox = new VBox(10);
                rightBox.getChildren().addAll(rightButtons);
                rightBox.setAlignment(Pos.CENTER);
                rightBox.setPadding(new Insets(20));
            }
            setLeft(leftBox);
            setRight(rightBox);
        }

        // Alterna entre mostrar el frente y el reverso de la ficha actual
        private void toggleFichaSide() {
            if (frontImage == null) return;
            if (showingBack) {
                updateFichaDisplay(frontImage);
                showingBack = false;
            } else {
                if (backImage != null) {
                    updateFichaDisplay(backImage);
                    showingBack = true;
                }
            }
            updateFichaInfo();
        }

        // Direcciones de navegación para este panel
        private enum NavigationDirection {
            PREVIOUS, NEXT, BACK_TEN, FORWARD_TEN
        }

        // Navega a otra ficha según la dirección indicada
        private void navigateFicha(NavigationDirection direction) {
            switch (direction) {
                case PREVIOUS:
                    if (currentCard > minCard) {
                        currentCard--;
                    }
                    break;
                case NEXT:
                    if (currentCard < totalCards) {
                        currentCard++;
                    }
                    break;
                case BACK_TEN:
                    currentCard = Math.max(currentCard - 10, minCard);
                    break;
                case FORWARD_TEN:
                    currentCard = Math.min(currentCard + 10, totalCards);
                    break;
            }
            loadFicha();
            updateFichaInfo();
            updateNavigationButtons();
        }

        // Muestra una alerta informativa (local)
        private void showAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
            alert.setGraphic(null);
            alert.showAndWait();
        }
    } // Fin de FichaPane

    // Devuelve el tab principal para integrarlo en la aplicación
    public Tab getFicheroTab() {
        return mainTab;
    }
}
