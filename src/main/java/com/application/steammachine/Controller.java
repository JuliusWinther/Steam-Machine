package com.application.steammachine;

import com.application.steammachine.downloaders.EmulatorPackDownloader;
import com.application.steammachine.downloaders.SoftwareUpdateDownloader;
import com.application.steammachine.downloaders.WorkshopDownloader;
import com.application.steammachine.settings.DesignSettings;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.*;
import com.google.common.base.Throwables;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import mslinks.ShellLink;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static com.application.steammachine.Main.ACTUAL_VERSION;

public class Controller {


    protected static class ListItem {
        public ListItem(String text) {
            this.text = text;
        }

        private final String text;
        final ObjectProperty<Color> color = new SimpleObjectProperty(Paint.valueOf("#C2C9D2"));

        public String getText() {
            return text;
        }
    }

    @FXML private AnchorPane mainPanel;
    @FXML private Pane titlePanel;
    @FXML private ListView<ListItem> gameList;
    @FXML private TextField searchBar;
    @FXML private Label previewDesc;
    @FXML private ImageView previewImage;
    @FXML private FontIcon closeButton;
    @FXML private FontIcon homeButton;
    @FXML private FontIcon collapseButton;
    @FXML private FontIcon basicWindowButton;
    @FXML private FontIcon fullScreenButton;
    @FXML private FontIcon settingsButton;
    @FXML private FontIcon consoleButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label downloadSpeed;
    @FXML private Button playButton;
    @FXML private Button uninstallPartsButton;
    @FXML private Button uninstallButton;
    @FXML private Button bookmarkButton1;
    @FXML private Button bookmarkButton2;
    @FXML private FontIcon bookmarkIcon1;
    @FXML private FontIcon bookmarkIcon2;
    @FXML private Button findInstalledButton;
    @FXML private Button uploadSaveButton;
    @FXML private Button downloadSaveButton;
    @FXML private AnchorPane saveButtons;
    @FXML private MenuButton gameSettingsButton;
    @FXML private Button installButton;
    @FXML private Label dimensionLabel;
    @FXML private TextArea gameDetails;
    @FXML private Text version;
    @FXML private Text gameVersionLabel;
    @FXML private MenuItem menuAction1;
    @FXML private MenuItem menuAction2;
    @FXML private MenuItem menuAction3;
    @FXML private MenuItem menuAction4;
    @FXML private MenuItem menuAction5;
    @FXML private MenuItem menuRestoreDataINI;
    @FXML private MenuItem filterInstalled;
    @FXML private MenuItem filterBookmarked;
    @FXML private MenuItem filterPartial;
    @FXML private MenuItem filterNone;
    @FXML private MenuItem filterSteam;
    @FXML private MenuItem filterEpic;
    @FXML private MenuItem filterN64;
    @FXML private MenuItem filterGba;
    @FXML private MenuItem filterNds;
    @FXML private MenuItem filter3ds;
    @FXML private MenuItem filterWii;
    @FXML private MenuItem filterSwitch;
    @FXML private MenuItem filterPs1;
    @FXML private MenuItem filterPs2;
    @FXML private MenuItem filterSoftware;
    @FXML private MenuItem filterOther;
    @FXML private MenuItem filterSingleplayer;
    @FXML private MenuItem filterOnline;
    @FXML private MenuItem filterLan;
    @FXML private MenuItem filterLocal;
    @FXML private Label DownloadLabel;
    @FXML private TableView<TableInfo> infoTable;
    @FXML private Menu languageSelector;
    @FXML private FontIcon pauseButton;
    @FXML private FontIcon rapidFilter;
    @FXML private FontIcon filterButton;
    @FXML private FontIcon filterButtonDecoration;
    @FXML private MediaView previewVideo;
    @FXML private Text gameTitleLabel;
    @FXML private AnchorPane galleryPane;
    @FXML private ImageView headerImage;
    @FXML private HBox gallery;
    @FXML private WebView detailedDescription;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Pane videoPane;
    @FXML private ScrollPane galleryScroller;
    @FXML private WebView minReqHtml;
    @FXML private FontIcon bigVideoPause;
    @FXML private Label cacheLabel;




    private HBox controls;
    private Slider volumeSlider;
    private Slider timeSlider;
    private Button playPauseButton;
    private Media media;
    private MediaPlayer player;
    private StackPane lastSelectedThumbnail;


    public static boolean isFullscreen = false;
    public static boolean isQueueOpen = false;


    public static boolean isPaused = false;
    public static URL storedLink = null;
    public static SimpleBooleanProperty isDownloading = new SimpleBooleanProperty(false);
    public static SimpleBooleanProperty isUnpacking = new SimpleBooleanProperty(false);

    public static boolean isEmulatorPackageInstalled = false;
    public static double emulatorPackageVersion = 0;

    public static ExecutorService downloaderThread = Executors.newSingleThreadExecutor();
    public static Downloader downloader;
    public static Future downloadThread;


    public static Future updateFuture;
    public static boolean updating = false;


    public static Future emuPackFuture;
    public static boolean downloadingEmuPack = false;

    public static DownloaderQueue queueClass = null;
    public static LinkedHashMap<String, Game> queue = new LinkedHashMap<String, Game>();
    public static ExecutorService queueThread = Executors.newSingleThreadExecutor();
    public static ExecutorService imageCacherThread = Executors.newSingleThreadExecutor();
    public static ExecutorService detailsCacherThread = Executors.newSingleThreadExecutor();
    public static ExecutorService galleryLoaderThread = Executors.newSingleThreadExecutor();
    public static ExecutorService descriptionLoaderThread = Executors.newSingleThreadExecutor();


    public static ExecutorService interfaceUpdaterThread = Executors.newSingleThreadExecutor();
    private final StatusUpdater interfaceStatusUpdater = new StatusUpdater();

    public static HashMap<String, Game> objectGameList = new HashMap<String, Game>();
    public static ObservableList<ListItem> observableGameList = FXCollections.observableArrayList();

    public static Game selectedGame;

    public static String notificationTitle = "";
    public static String notificationText = "";
    public static ArrayList<String> unknownGames = new ArrayList<String>();

    public static ArrayList<String> updatedGames = new ArrayList<String>();
    public static ObservableList<TableInfo> tableData = FXCollections.observableArrayList();

    public static String currentFilter = "Tutti";

    static StringProperty installButtonTextProperty = new SimpleStringProperty("INSTALLA");
    public static int presentCache = 0;
    public static int totalCache = 0;

    public static boolean openNews = false;

    public static SimpleStringProperty changeSelection = new SimpleStringProperty("");

    @FXML
    public void initialize() throws Exception {

        changeSelection.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectFromName(changeSelection.getValue());
            }
        });

        isFullscreen = GeneralSettings.getOpenFullscreen();
        if(isFullscreen) {
            basicWindowButton.setIconLiteral("bi-fullscreen-exit");
        }else{
            basicWindowButton.setIconLiteral("bi-fullscreen");
        }


        version.setText(String.valueOf(Launcher.softwareVersion));


        Main.log.info("ACTUAL VERSION: " + ACTUAL_VERSION);

        titlePanel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        titlePanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!isFullscreen) {
                    Main.mainStage.setX(event.getScreenX() - xOffset);
                    Main.mainStage.setY(event.getScreenY() - yOffset);
                }
            }
        });

        
        
        

        if(Double.parseDouble(Objects.requireNonNull(DatabaseManager.getLastSoftwareVersion()).toLowerCase().replaceAll(" ", "").split("-")[1]) >
                Double.parseDouble(Launcher.softwareVersion.toLowerCase().replaceAll(" ", "").split("-")[1])){

            try {

                SoftwareUpdateDownloader softwareUpdateDownloader = new SoftwareUpdateDownloader(new URL(Objects.requireNonNull(DatabaseManager.getLastSoftwareVersionLink())));

                progressBar.progressProperty().bind(softwareUpdateDownloader.progressProperty());

                downloadSpeed.textProperty().bind(Bindings.createStringBinding(
                        () -> softwareUpdateDownloader.getMessage().split(",")[0],
                        softwareUpdateDownloader.messageProperty()));

                dimensionLabel.textProperty().bind(Bindings.createStringBinding(
                        () -> softwareUpdateDownloader.getMessage().split(",")[1],
                        softwareUpdateDownloader.messageProperty()));

                updating = true;
                updateFuture = downloaderThread.submit(softwareUpdateDownloader);

            } catch (Exception e) {
                SteamUtils.logError(e);
            }

        }else if (Double.parseDouble(Launcher.softwareVersion.split("-")[1]) != ACTUAL_VERSION) {
            sendNotification("", "");
        }

        
        
        

        File batch = new File(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Steam-Machine.bat");
        if(GeneralSettings.isLaunchOnStart()){

            if(!batch.exists())
                batch.createNewFile();

            FileWriter writer = new FileWriter(batch);
            writer.write("@echo off\nstart \"\" \"" + System.getProperty("user.dir") + "\\SteamMachine.exe\"");
            writer.close();

        }else{

            if(batch.exists())
                batch.delete();

        }



        
        
        


        ArrayList<String> gameNames = DatabaseManager.selectColumnFromDB(1);


        gameList.setCellFactory(cell -> {
            ListCell<ListItem> cel = new ListCell<ListItem>() {
                @Override
                protected void updateItem(ListItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        textFillProperty().bind(item.color);
                        setText(item.text);
                    } else {
                        setText("");
                        textFillProperty().unbind();
                        setTextFill(Paint.valueOf("#C2C9D2"));
                    }
                }
            };
            return cel;
        });


        
        for (int i = 0; i < gameNames.size(); i++){


            String id = DatabaseManager.GameList.get(i).get(0);

            String gameFolderName = DatabaseManager.GameList.get(i).get(2);
            String exePath = DatabaseManager.GameList.get(i).get(3);
            String saveFilesPath = DatabaseManager.GameList.get(i).get(4);
            ArrayList<String> downloadLink = new ArrayList<String>();
            if(DatabaseManager.GameList.get(i).get(5).replaceAll(" ", "").contains("@")) {
                downloadLink = new ArrayList<String>(Arrays.asList(DatabaseManager.GameList.get(i).get(5).replaceAll(" ", "").split("@")));
            }else {
                downloadLink.add(DatabaseManager.GameList.get(i).get(5).replaceAll(" ", ""));
            }
            String multiplayerType = DatabaseManager.GameList.get(i).get(6);
            String gameVersion = DatabaseManager.GameList.get(i).get(7);
            String typeOfFile = DatabaseManager.GameList.get(i).get(8);
            String downloadDimension = DatabaseManager.GameList.get(i).get(9);
            String extraDetails = DatabaseManager.GameList.get(i).get(10);
            String status = DatabaseManager.GameList.get(i).get(11);
            String gameDesc = DatabaseManager.GameList.get(i).get(12);
            String customImageUrl = DatabaseManager.GameList.get(i).get(13).replaceAll(" ", "");
            String storeLink = DatabaseManager.GameList.get(i).get(14);
            String extraTags = DatabaseManager.GameList.get(i).get(15);
            String customImgs = DatabaseManager.GameList.get(i).get(16);
            String customVideos = DatabaseManager.GameList.get(i).get(17);
            String customPrice = DatabaseManager.GameList.get(i).get(18);
            String customLongDesc = DatabaseManager.GameList.get(i).get(19);
            String requirements = DatabaseManager.GameList.get(i).get(20);
            String steamGridDBId = DatabaseManager.GameList.get(i).get(21);


            objectGameList.putIfAbsent(gameNames.get(i), new Game(
                    id, gameNames.get(i), gameFolderName, exePath, saveFilesPath,
                    downloadLink, multiplayerType,
                    gameVersion, typeOfFile, downloadDimension,
                    extraDetails, status, gameDesc, customImageUrl,
                    storeLink, extraTags, customImgs, customVideos,
                    customPrice, customLongDesc, requirements, steamGridDBId,
                    GeneralSettings.getInstallPath(), false));

            observableGameList.add(new ListItem(gameNames.get(i)));

        }


        HashMap<String, String> knownGames = new HashMap<String, String>();

        try {
            Scanner reader = new Scanner(new File("game_list.txt"));
            while (reader.hasNextLine()) {
                String line = reader.nextLine().strip();
                if (!line.isEmpty()) {
                    try {
                        knownGames.putIfAbsent(line.split("\\|\\|")[0].strip(), line.split("\\|\\|")[1].strip());
                    }catch(ArrayIndexOutOfBoundsException e){
                        knownGames.putIfAbsent(line.split("\\|\\|")[0].strip(), "unknown");
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            SteamUtils.logError(e);
        }

        unknownGames.addAll(objectGameList.keySet());

        Set<String> gamesToRemove = new HashSet<>();
        for (String unknownGame : unknownGames) {
            for (String knownGame : knownGames.keySet()) {
                if (SteamUtils.areSimilar(knownGame, unknownGame)) {
                    gamesToRemove.add(unknownGame);

                }
            }
        }
        unknownGames.removeAll(gamesToRemove);

        for (Game game : objectGameList.values()) {
            for (String knownGame : knownGames.keySet()) {
                if(game.getName().equalsIgnoreCase(knownGame)){
                    if (!game.getGameVersion().equalsIgnoreCase(knownGames.get(knownGame))) {
                        updatedGames.add(game.getName());
                    }
                }
            }
        }
        Collections.sort(updatedGames,new Comparator<String>() {

            @Override
            public int compare(String lhs, String rhs) {

                return lhs.compareTo(rhs);

            }
        });

        Collections.sort(unknownGames,new Comparator<String>() {

            @Override
            public int compare(String lhs, String rhs) {

                return lhs.compareTo(rhs);

            }
        });

        if(!unknownGames.isEmpty())
            openNews = true;


        Wini news = new Wini(new File("news.ini"));
        news.load();


        if(unknownGames.size() <= 6)
            if(news.containsKey("news"))
                if(news.get("news").containsKey("added"))
                    if(!news.get("news").get("added").equalsIgnoreCase("[]") && !news.get("news").get("added").equalsIgnoreCase("")){
                        List<String> iniGames = List.of(news.get("news").get("added").replace("[", "").replace("]", "").split(","));
                        if(iniGames.size() <= 6)
                            unknownGames.addAll(iniGames);
                    }

        if(!unknownGames.isEmpty()) {
            news.remove("news");
            news.put("news", "added", unknownGames.toString());
            news.put("news", "updated", updatedGames.toString());
        }

        news.store();

        if(unknownGames.isEmpty()){
            if(news.containsKey("news"))
                if(news.get("news").containsKey("added"))
                    if(!news.get("news").get("added").equalsIgnoreCase("[]") && !news.get("news").get("added").equalsIgnoreCase(""))
                        unknownGames.addAll(List.of(news.get("news").get("added").replace("[", "").replace("]", "").split(",")));
        }

        Main.log.info("Placed in news: " + unknownGames);

        if(updatedGames.isEmpty()){
            if(news.containsKey("news"))
                if(news.get("news").containsKey("updated"))
                    if(!news.get("news").get("updated").equalsIgnoreCase("[]") && !news.get("news").get("updated").equalsIgnoreCase(""))
                        updatedGames.addAll(List.of(news.get("news").get("updated").replace("[", "").replace("]", "").split(",")));
        }

        FileWriter writer = new FileWriter("game_list.txt");

        for (String s : objectGameList.keySet())
            writer.append(s+"||"+Controller.objectGameList.get(s).getGameVersion()).append("\n");
        writer.close();

        for(Game game : Controller.objectGameList.values()){
            if (!game.getName().equalsIgnoreCase("0")){
                if (SteamUtils.getDetailsFromCache(game.getId()) != null){
                    try {
                        applyDetailsFromCache(game.getName(), SteamUtils.readDetailsJson(game.getName()));
                        Controller.presentCache ++;
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        for(Game game : Controller.objectGameList.values()){
            if (!game.getId().equalsIgnoreCase("0")){
                totalCache ++;
            }
        }

        cacheLabel.textProperty().bind(interfaceStatusUpdater.cacheStatus);
        cacheLabel.visibleProperty().bind(interfaceStatusUpdater.showCacheStatus);


        DetailsCacher detailsCacher = new DetailsCacher();
        detailsCacherThread.submit(detailsCacher);
        ImageCacher imageCacher = new ImageCacher();
        imageCacherThread.submit(imageCacher);

        
        
        

        Wini data = new Wini(new File("data.ini"));

        data.load();

        for (Game game : objectGameList.values()) {
            for (Profile.Section sec : data.values()) {
                if(sec.getName().equalsIgnoreCase("EMULATOR PACKAGE")) {
                    isEmulatorPackageInstalled = true;
                    if(data.get("EMULATOR PACKAGE").get("version").replaceAll(" ", "").equalsIgnoreCase("")){
                        emulatorPackageVersion = 0;
                        data.get("EMULATOR PACKAGE").replace("version", "0");
                    }else{
                        emulatorPackageVersion = Double.parseDouble(data.get("EMULATOR PACKAGE").get("version"));
                    }
                }else if(!isEmulatorPackageInstalled){
                    emulatorPackageVersion = 0;
                }
                if(game.getName().equalsIgnoreCase(sec.getName())) {
                    if (!sec.containsKey("downloaded-parts")){
                        if (sec.containsKey("exe-path")) {
                            game.setInstalled(true);
                            game.setInstallPath(data.get(game.getName(), "install-path"));
                            if (!data.get(game.getName(), "main-folder").equalsIgnoreCase("") && !data.get(game.getName(), "main-folder").equalsIgnoreCase("/"))
                                game.setGameFolderName(data.get(game.getName(), "main-folder"));
                            else
                                data.get(game.getName()).replace("main-folder", game.getGameFolderName());
                            if (!data.get(game.getName(), "exe-path").equalsIgnoreCase("") && !data.get(game.getName(), "exe-path").equalsIgnoreCase("/"))
                                game.setExePath(data.get(game.getName(), "exe-path"));
                            else
                                data.get(game.getName()).replace("exe-path", game.getExePath());
                            if (!data.get(game.getName(), "saves-path").equalsIgnoreCase("") && !data.get(game.getName(), "saves-path").equalsIgnoreCase("/"))
                                game.setSaveFilesPath(data.get(game.getName(), "saves-path"));
                            else
                                data.get(game.getName()).replace("saves-path", game.getSaveFilesPath());
                            if (!data.get(game.getName(), "game-version").equalsIgnoreCase("")
                                    && !data.get(game.getName(), "game-version").equalsIgnoreCase("/"))
                                game.setGameVersion(data.get(game.getName(), "game-version"));
                            else
                                data.get(game.getName()).replace("game-version", "Failed to detect");
                        }
                        game.setBookmarked(sec.containsKey("bookmarked"));
                    }else{
                        if(sec.get("downloaded-parts").contains(","))
                            game.setDownloadedParts(new ArrayList<String>(Arrays.asList(data.get(game.getName(), "downloaded-parts").split(","))));
                        else
                            game.getDownloadedParts().add(data.get(game.getName(), "downloaded-parts"));
                        if (!sec.containsKey("destination-disk"))
                            data.get(game.getName()).add("destination-disk", "C");
                        else
                            if(!data.get(game.getName()).get("destination-disk").equalsIgnoreCase("default"))
                                game.setInstallPath(SteamUtils.getDestinationDiskFromData(game.getName())+":"+GeneralSettings.getSecondaryFolderName());
                    }
                }
            }
        }

        data.store();



        
        
        
        if(!updating) {
            if (Double.parseDouble(Objects.requireNonNull(DatabaseManager.getLastEmuPackVersion()).toLowerCase().replaceAll(" ", ""))
                    > emulatorPackageVersion) {

                try {

                    EmulatorPackDownloader emuPackDownloader;

                    if(emulatorPackageVersion != 0) {
                        if (Double.parseDouble((Objects.requireNonNull(DatabaseManager.getLastEmuPackVersion()).toLowerCase().replaceAll(" ", "").split("\\.")[0]))
                                != Double.parseDouble(String.valueOf(emulatorPackageVersion).split("\\.")[0])) {

                            emuPackDownloader = new EmulatorPackDownloader(new URL(Objects.requireNonNull(DatabaseManager.getEmuPackDownloadLink())), true);

                        } else {

                            emuPackDownloader = new EmulatorPackDownloader(new URL(Objects.requireNonNull(DatabaseManager.getEmuPackConfigDownloadLink())), false);

                        }
                    }else{
                            emuPackDownloader = new EmulatorPackDownloader(new URL(Objects.requireNonNull(DatabaseManager.getEmuPackFirstDownloadLink())), true);
                    }

                    progressBar.progressProperty().bind(emuPackDownloader.progressProperty());

                    downloadSpeed.textProperty().bind(Bindings.createStringBinding(
                            () -> emuPackDownloader.getMessage().split(",")[0],
                            emuPackDownloader.messageProperty()));

                    dimensionLabel.textProperty().bind(Bindings.createStringBinding(
                            () -> emuPackDownloader.getMessage().split(",")[1],
                            emuPackDownloader.messageProperty()));

                    downloadingEmuPack = true;
                    emuPackFuture = downloaderThread.submit(emuPackDownloader);

                } catch (Exception e) {
                    Main.log.error(Throwables.getStackTraceAsString(e));
                }

            }
        }


        
        
        

        Collections.sort(observableGameList,new Comparator<ListItem>() {

            @Override
            public int compare(ListItem lhs, ListItem rhs) {

                return lhs.getText().compareTo(rhs.getText());

            }
        });

        gameList.setItems(observableGameList);

        updateListViewColors();

        interfaceUpdaterThread.submit(interfaceStatusUpdater);

        
        searchBar.textProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {



                filterList((String) oldValue, (String) newValue);
            }
        });

        pauseButton.visibleProperty().bind(isDownloading);
        pauseButton.disableProperty().bind(isDownloading.not());

        class PauseButtonUpdater implements Runnable {
            public void run() {
                while(true) {
                    if (Controller.isPaused) {
                        pauseButton.setIconLiteral("bi-play");
                    } else {
                        pauseButton.setIconLiteral("bi-pause");
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        PauseButtonUpdater pauseButtonUpdater = new PauseButtonUpdater();
        Thread pauseButtonUpdaterThread = new Thread(pauseButtonUpdater);
        pauseButtonUpdaterThread.start();

        
        
        

        TableColumn<TableInfo, TableInfo> infoCol = new TableColumn<>("Caratteristica");
        TableColumn<TableInfo, String> valueCol = new TableColumn<>("Valore");

        infoCol.setCellValueFactory(new PropertyValueFactory<>("info"));
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        infoTable.getColumns().clear();
        infoTable.getColumns().addAll(infoCol, valueCol);

        infoTable.setItems(tableData);

        gameList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            private Timeline timeline = new Timeline(new KeyFrame(Duration.millis(60), event -> {


                try {
                    String selectedItem = "";
                    try {
                        selectedItem = (String) gameList.getSelectionModel().getSelectedItem().getText();
                    } catch (NullPointerException e) {
                        gameList.getSelectionModel().select(0);
                        selectedItem = (String) gameList.getSelectionModel().getSelectedItem().getText();
                    }

                    String finalSelectedItem = selectedItem;
                    resetVideoPlayer();
                    gallery.getChildren().clear();

                    selectedGame = (Game) objectGameList.get(finalSelectedItem);


                    if (SteamUtils.getDetailsFromCache(selectedGame.getId()) == null && !selectedGame.getId().equalsIgnoreCase("0")) {
                        File cache = SteamUtils.downloadDetails(selectedGame.getId());
                        try {
                            applyDetailsFromCache(selectedGame.getName(), SteamUtils.readDetailsJson(selectedGame.getName()));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    
                    if (SteamUtils.hasHeaderCache(selectedGame.getName())) {
                        headerImage.setImage(SteamUtils.getHeaderCache(selectedGame.getName()));
                    } else {
                        Task<Void> headerTask = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                CompletableFuture.runAsync(() -> {
                                    Main.log.warn("Missing image cache for " + selectedGame.getName() + ", generating placeholder...");
                                    try {
                                        ImageCacher.downloadHeaderImage(selectedGame.getName(), objectGameList.get(finalSelectedItem).getImgURL());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).thenRun(() -> {
                                    Platform.runLater(() -> {
                                        headerImage.setImage(SteamUtils.getHeaderCache(selectedGame.getName()));
                                    });
                                });

                                return null;
                            }
                        };
                        new Thread(headerTask).start();
                    }

                    gameTitleLabel.setText(selectedGame.getName());

                    if(GeneralSettings.isHd()) {
                        infoTable.setStyle("-fx-font-size: 16px;");
                        gameList.setStyle("-fx-font-size: 13px;");
                    }


                    Task<Void> descriptionTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            String descriptionContent = "<html><head><style>" +
                                    "body { background-color: #383A3F; color: #dde3ed;"+ (GeneralSettings.isHd() ? "font-size: 20px;" : "") +" -fx-font-family: system; overflow-x: hidden; }" +
                                    "img { display: block; margin-left: auto; margin-right: auto; }" +
                                    "</style></head><body>" +
                                    ((Controller.objectGameList.get(selectedGame.getName()).getLong_desc().equalsIgnoreCase("") || Controller.objectGameList.get(selectedGame.getName()).getLong_desc().equalsIgnoreCase("/")) ?
                                            "" : Controller.objectGameList.get(selectedGame.getName()).getLong_desc()) +
                                    "</body></html>";
                            Platform.runLater(() -> {
                                detailedDescription.getEngine().loadContent(descriptionContent);
                                detailedDescription.setStyle("-fx-background-color: #383A3F; -fx-font-size: 14px; -fx-font-family: system;");
                                detailedDescription.setPrefHeight(0);
                                detailedDescription.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        Object resultHeight = detailedDescription.getEngine().executeScript("document.body.scrollHeight");
                                        if (resultHeight instanceof Integer) {
                                            detailedDescription.setPrefHeight((Integer) resultHeight+150);
                                        } else if (resultHeight instanceof Double) {
                                            detailedDescription.setPrefHeight((Double) resultHeight+150.0);
                                        }
                                    }
                                });
                            });
                            return null;
                        }
                    };
                    new Thread(descriptionTask).start();


                    Task<Void> requirementsTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            String requirementsContent = "<html><head><style>body { background-color: #383A3F; color: #dde3ed;"+ (GeneralSettings.isHd() ? "font-size: 20px;" : "") +" -fx-font-family: system; }</style></head><body><br>" +
                                    ((Controller.objectGameList.get(selectedGame.getName()).getRequirements().equalsIgnoreCase("") || Controller.objectGameList.get(selectedGame.getName()).getRequirements().equalsIgnoreCase("/")) ?
                                            "" : Controller.objectGameList.get(selectedGame.getName()).getRequirements()) + "</body></html>";
                            Platform.runLater(() -> {
                                minReqHtml.getEngine().loadContent(requirementsContent);
                                minReqHtml.setStyle("-fx-background-color: #383A3F; -fx-font-size: 14px; -fx-font-family: system;");
                                minReqHtml.setPrefHeight(0);
                                minReqHtml.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        Object result = minReqHtml.getEngine().executeScript("document.body.scrollHeight");
                                        if (result instanceof Integer) {
                                            minReqHtml.setPrefHeight((Integer) result);
                                        } else if (result instanceof Double) {
                                            minReqHtml.setPrefHeight((Double) result);
                                        }
                                    }
                                });
                            });
                            return null;
                        }
                    };
                    new Thread(requirementsTask).start();


                    ArrayList<String> videoLinks = Controller.objectGameList.get(selectedGame.getName()).getVideoGallery();
                    ArrayList<String> imageLinks = Controller.objectGameList.get(selectedGame.getName()).getImageGallery();

                    ArrayList<StackPane> videoThumbnails = new ArrayList<>();
                    ArrayList<StackPane> imageThumbnails = new ArrayList<>();

                    Task<Void> mediaGalleryTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {


                            for (int i = 0; i < videoLinks.size(); i++) {
                                final int videoIndex = i;
                                String videoLink = videoLinks.get(videoIndex);


                                ProgressIndicator videoProgress = new ProgressIndicator();
                                videoProgress.setStyle("-fx-progress-color: white;");
                                videoProgress.setMaxSize(24, 24);


                                StackPane.setAlignment(videoProgress, Pos.CENTER);


                                StackPane videoStackPane = new StackPane();
                                videoStackPane.getChildren().add(videoProgress);


                                ImageView videoThumbnail = new ImageView();
                                FontIcon playIcon = new FontIcon();

                                CompletableFuture.runAsync(() -> {
                                    videoThumbnail.setImage(new Image(Controller.objectGameList.get(selectedGame.getName()).getVideoGalleryThumbs().get(videoIndex), 115, 64, true, true));
                                    videoThumbnail.setFitWidth(115);
                                    videoThumbnail.setFitHeight(64);

                                    playIcon.setIconLiteral("bi-play-btn-fill");
                                    playIcon.setIconSize(28);
                                    playIcon.setIconColor(Color.WHITE);

                                    Platform.runLater(() -> {
                                        videoStackPane.setOnMouseClicked(event -> {
                                            resetVideoPlayer();
                                            previewImage.setVisible(false);
                                            previewVideo.setVisible(true);
                                            try {

                                                media = new Media(new URL(videoLink).toExternalForm());
                                                player = new MediaPlayer(media);
                                                player.setAutoPlay(true);
                                                previewVideo.setSmooth(true);
                                                previewVideo.setMediaPlayer(player);

                                                previewVideo.setPreserveRatio(true);

                                                playPauseButton = new Button("Pause");
                                                playPauseButton.setPrefWidth(60);
                                                playPauseButton.setOnAction(e -> {
                                                    if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                                                        player.pause();
                                                        bigVideoPause.setVisible(true);
                                                        playPauseButton.setText("Play");
                                                    } else {
                                                        player.play();
                                                        bigVideoPause.setVisible(false);
                                                        playPauseButton.setText("Pause");
                                                    }
                                                });

                                                videoPane.setOnMouseClicked(event2 -> {
                                                    if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                                                        player.pause();
                                                        bigVideoPause.setIconSize(87);
                                                        bigVideoPause.setVisible(true);
                                                        playPauseButton.setText("Play");
                                                    } else {
                                                        player.play();
                                                        bigVideoPause.setIconSize(87);
                                                        bigVideoPause.setVisible(false);
                                                        playPauseButton.setText("Pause");
                                                    }
                                                });


                                                timeSlider = new Slider();
                                                timeSlider.setMin(0);
                                                player.setOnReady(() -> timeSlider.setMax(player.getTotalDuration().toSeconds()));
                                                player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                                                    try {
                                                        timeSlider.setValue(newTime.toSeconds());
                                                    } catch (NullPointerException ignored) {
                                                    }
                                                });
                                                timeSlider.setOnMousePressed(e -> {
                                                    player.seek(Duration.seconds(timeSlider.getValue()));
                                                    player.pause();
                                                });
                                                timeSlider.setOnMouseReleased(e -> {
                                                    player.seek(Duration.seconds(timeSlider.getValue()));
                                                    player.play();
                                                });
                                                timeSlider.getStyleClass().add("custom-slider");

                                                HBox volumeBox = new HBox(-5);
                                                volumeBox.setAlignment(Pos.CENTER);


                                                FontIcon volumeIcon = new FontIcon();
                                                volumeIcon.setIconLiteral("bi-speaker-fill");
                                                volumeIcon.setIconSize(16);
                                                volumeIcon.setIconColor(Color.WHITE);


                                                volumeSlider = new Slider();
                                                volumeSlider.setMin(0);
                                                volumeSlider.setMax(100);
                                                volumeSlider.setValue(0);
                                                volumeSlider.setStyle("-fx-background-color: transparent;");
                                                player.volumeProperty().bind(volumeSlider.valueProperty().divide(100));


                                                controls = new HBox(10);
                                                controls.setPadding(new Insets(5));
                                                volumeBox.getChildren().addAll(volumeIcon, volumeSlider);

                                                controls.getChildren().addAll(playPauseButton, timeSlider, volumeBox);
                                                controls.setSpacing(20);
                                                controls.setAlignment(Pos.CENTER);
                                                HBox.setHgrow(timeSlider, Priority.ALWAYS);

                                                controls.prefWidthProperty().bind(previewVideo.fitWidthProperty());

                                                videoPane.getChildren().add(controls);
                                                if(GeneralSettings.isHd())
                                                    controls.setLayoutY(previewVideo.getBoundsInParent().getMaxY() - controls.getHeight() - 65);
                                                else
                                                    controls.setLayoutY(290);
                                                controls.setLayoutX(5);
                                                controls.setVisible(false);


                                                if (lastSelectedThumbnail != null) {
                                                    lastSelectedThumbnail.setStyle(null);
                                                }


                                                videoStackPane.setStyle("-fx-border-color: white; -fx-border-width: 2px;");

                                                lastSelectedThumbnail = videoStackPane;
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    });
                                }).thenRun(() -> {

                                    Platform.runLater(() -> {
                                        videoStackPane.getChildren().remove(videoProgress);

                                        if (!videoStackPane.getChildren().contains(videoThumbnail)) {
                                            videoStackPane.getChildren().addAll(videoThumbnail, playIcon);
                                            StackPane.setAlignment(playIcon, Pos.CENTER);
                                        }


                                        try{
                                            if (!videoLinks.isEmpty()) {
                                                MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                                                        0, 0, 0, 0,
                                                        MouseButton.PRIMARY, 1,
                                                        true, true, true, true,
                                                        true, true, true, true, true,
                                                        true, null);
                                                gallery.getChildren().get(0).fireEvent(clickEvent);
                                            }
                                        }catch(IndexOutOfBoundsException ignored){}
                                    });
                                });

                                videoThumbnails.add(videoStackPane);
                            }







                            for (int i = 0; i < imageLinks.size(); i++) {
                                final int imageIndex = i;
                                String imageLink = imageLinks.get(imageIndex);


                                ProgressIndicator imageProgress = new ProgressIndicator();
                                imageProgress.setStyle("-fx-progress-color: white;");
                                imageProgress.setMaxSize(24, 24);


                                StackPane.setAlignment(imageProgress, Pos.CENTER);


                                StackPane imageStackPane = new StackPane();
                                imageStackPane.getChildren().add(imageProgress);


                                ImageView imageView = new ImageView();

                                CompletableFuture.runAsync(() -> {
                                    imageView.setImage(new Image(imageLink, 115, 64, true, true));
                                    imageView.setFitWidth(115);
                                    imageView.setFitHeight(64);

                                    Platform.runLater(() -> {
                                        imageStackPane.setOnMouseClicked(event -> {
                                            resetVideoPlayer();
                                            previewVideo.setVisible(false);
                                            previewImage.setVisible(true);
                                            previewImage.setImage(new Image(imageLink));


                                            if (lastSelectedThumbnail != null) {
                                                lastSelectedThumbnail.setStyle(null);
                                            }


                                            imageStackPane.setStyle("-fx-border-color: white; -fx-border-width: 2px;");

                                            lastSelectedThumbnail = imageStackPane;
                                        });
                                    });
                                }).thenRun(() -> {

                                    Platform.runLater(() -> {
                                        imageStackPane.getChildren().remove(imageProgress);

                                        if (!imageStackPane.getChildren().contains(imageView)) {
                                            imageStackPane.getChildren().add(imageView);
                                        }

                                        try {

                                            if (videoLinks.isEmpty() && !imageLinks.isEmpty()) {
                                                MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                                                        0, 0, 0, 0,
                                                        MouseButton.PRIMARY, 1,
                                                        true, true, true, true,
                                                        true, true, true, true, true,
                                                        true, null);
                                                gallery.getChildren().get(0).fireEvent(clickEvent);
                                            }
                                        }catch(IndexOutOfBoundsException ignored){}
                                    });
                                });

                                imageThumbnails.add(imageStackPane);
                            }

                            Platform.runLater(() -> {
                                gallery.getChildren().addAll(videoThumbnails);
                                gallery.getChildren().addAll(imageThumbnails);

                            });
                            return null;
                        }
                    };
                    new Thread(mediaGalleryTask).start();



                    previewDesc.setText(Controller.objectGameList.get(selectedGame.getName()).getGameDesc());

                    installButton.textProperty().bind(installButtonTextProperty);

                    Controller.updateInstallButtonText();

                    boolean isBookmarked = Controller.objectGameList.get(selectedGame.getName()).isBookmarked();

                    if (isBookmarked) {
                        bookmarkIcon1.setIconLiteral("bi-star-fill");
                        bookmarkIcon2.setIconLiteral("bi-star-fill");
                    } else {
                        bookmarkIcon1.setIconLiteral("bi-star");
                        bookmarkIcon2.setIconLiteral("bi-star");
                    }

                    menuAction2.setVisible(!SteamUtils.needsEmulator(objectGameList.get(finalSelectedItem)));

                    menuAction3.setVisible(!objectGameList.get(finalSelectedItem).getFixInstallPath().replace(" ", "").equalsIgnoreCase("/"));

                    File firstLevelGameFolder = null;
                    try {
                        firstLevelGameFolder = new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" +
                                Controller.objectGameList.get(selectedGame.getName()).getGameFolderName().split("/")[1]);
                    } catch (NullPointerException np) {
                        SteamUtils.logError(np);
                    }

                    if (SteamUtils.needsEmulator(selectedGame)) {
                        languageSelector.setVisible(false);
                    } else {
                        languageSelector.setVisible(true);
                    }

                    if (!objectGameList.get(finalSelectedItem).getSaveFilesPath().replace(" ", "").equalsIgnoreCase("/"))
                        menuAction4.setVisible(true);
                    else
                        menuAction4.setVisible(false);

                    if (!objectGameList.get(finalSelectedItem).getModInstallPath().replace(" ", "").equalsIgnoreCase("/"))
                        menuAction5.setVisible(true);
                    else
                        menuAction5.setVisible(false);

                    if (Controller.objectGameList.get(selectedGame.getName()).getStatus().equalsIgnoreCase("disabled")) {
                        Main.log.warn("The game " + selectedGame.getName() + " download has been disabled");
                        installButton.setDisable(true);
                    } else {
                        installButton.setDisable(false);
                    }



                    tableData.clear();

                    tableData.add(
                            new TableInfo(
                                    "Multiplayer:",
                                    objectGameList.get(finalSelectedItem).getMultiplayerType().contains("https") ? "steam fix" : objectGameList.get(finalSelectedItem).getMultiplayerType().substring(1)
                            ));






                    tableData.add(
                            new TableInfo(
                                    "Partizioni:",
                                    objectGameList.get(finalSelectedItem).isInstalled() ?
                                            "installed" :
                                            String.valueOf(objectGameList.get(finalSelectedItem).getDownloadedParts().size()) + "/" + String.valueOf(objectGameList.get(finalSelectedItem).getDownloadLink().size())
                            ));
                    if(!Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                        tableData.add(
                                new TableInfo(
                                        "Dim. Download:",
                                        objectGameList.get(finalSelectedItem).getDownloadDimension()
                                ));
                    }
                    if(Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                        tableData.add(
                                new TableInfo(
                                        "Position:",
                                        Controller.objectGameList.get(selectedGame.getName()).getInstallPath().equalsIgnoreCase(GeneralSettings.getInstallPath())
                                                ? Main.applicationDisk : Controller.objectGameList.get(selectedGame.getName()).getInstallPath().split(":")[0]+":\\")
                                        );
                        tableData.add(
                                new TableInfo(
                                        "Dimension:",
                                        objectGameList.get(finalSelectedItem).getDownloadDimension()
                                ));
                        tableData.add(
                                new TableInfo(
                                        "Installed Version:",
                                        objectGameList.get(finalSelectedItem).isInstalled() ?
                                                objectGameList.get(finalSelectedItem).getGameVersion() :
                                                "none"
                                ));
                        tableData.add(
                                new TableInfo(
                                        "Latest Version:",
                                        objectGameList.get(finalSelectedItem).getLatestGameVersion()
                                ));
                    }
                    tableData.add(
                            new TableInfo(
                                    "Categories:",
                                    objectGameList.get(finalSelectedItem).getExtraTags()
                            ));
                    tableData.add(
                            new TableInfo(
                                    "Current Price:",
                                    (objectGameList.get(finalSelectedItem).getPrice() != null && objectGameList.get(finalSelectedItem).getPrice().equalsIgnoreCase("")) ?
                                            "unknown" : objectGameList.get(finalSelectedItem).getPrice()
                            ));
                    tableData.add(
                            new TableInfo(
                                    "Store Link:",
                                    objectGameList.get(finalSelectedItem).getStoreLink().contains("http") ?
                                            "Click to Open" : "unknown"
                            ));

                    infoTable.setOnMouseClicked(tableEvent -> {
                        if (tableEvent.getClickCount() == 1 && infoTable.getSelectionModel().getSelectedItem() != null) {
                            TableInfo selectedTableItem = infoTable.getSelectionModel().getSelectedItem();
                            if (selectedTableItem.getInfo().equals("Store Link:")) {
                                try {
                                    Desktop.getDesktop().browse(new URI(objectGameList.get(finalSelectedItem).getStoreLink()));
                                } catch (IOException | URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    Callback<TableColumn<TableInfo, String>, TableCell<TableInfo, String>> cellFactory = col -> {
                        TableCell<TableInfo, String> cell = new TableCell<>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    setText(item);

                                    if ("Click to Open".equals(item)) {

                                        setStyle("-fx-text-fill: lightblue; -fx-cursor: hand;");

                                    } else if (objectGameList.get(finalSelectedItem).getGameVersion().equals(item)) {

                                        if (objectGameList.get(finalSelectedItem).isInstalled()) {
                                            if (!objectGameList.get(finalSelectedItem).getLatestGameVersion().equalsIgnoreCase(objectGameList.get(finalSelectedItem).getGameVersion())) {

                                                if (!objectGameList.get(finalSelectedItem).getGameVersion().equalsIgnoreCase("Not from SteamMachine"))
                                                    setStyle("-fx-text-fill: rgba(248,96,101,0.90);");
                                                else
                                                    setStyle("-fx-text-fill: rgba(248,149,96,0.9);");
                                            }
                                        }

                                    } else if (objectGameList.get(finalSelectedItem).getLatestGameVersion().equals(item)) {

                                        if (objectGameList.get(finalSelectedItem).isInstalled()) {
                                            if (!objectGameList.get(finalSelectedItem).getLatestGameVersion().equalsIgnoreCase(objectGameList.get(finalSelectedItem).getGameVersion())) {

                                                if (!objectGameList.get(finalSelectedItem).getGameVersion().equalsIgnoreCase("Not from SteamMachine"))
                                                    setStyle("-fx-text-fill: rgba(144,238,144,0.90);");
                                            }
                                        }

                                    } else {
                                        setStyle("-fx-text-fill: #DCE2ED");
                                    }
                                }
                            }
                        };
                        cell.setStyle("-fx-alignment: CENTER;");
                        return cell;
                    };

                    valueCol.setCellFactory(cellFactory);











                            


                    gameVersionLabel.setText("");

                    gameDetails.setText(
                            (selectedGame.getExtraDetails().equalsIgnoreCase("") || selectedGame.getExtraDetails().equalsIgnoreCase("/")) ?
                            "" : selectedGame.getExtraDetails()
                    );


























                    installButton.visibleProperty().bind(interfaceStatusUpdater.isInstalled.not());

                    playButton.disableProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    playButton.visibleProperty().bind(interfaceStatusUpdater.isInstalled);

                    uninstallButton.disableProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    uninstallButton.visibleProperty().bind(interfaceStatusUpdater.isInstalled);

                    uninstallPartsButton.disableProperty().bind(interfaceStatusUpdater.showPartsUninstaller.not());
                    uninstallPartsButton.visibleProperty().bind(interfaceStatusUpdater.showPartsUninstaller);

                    gameSettingsButton.disableProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    gameSettingsButton.visibleProperty().bind(interfaceStatusUpdater.isInstalled);

                    findInstalledButton.disableProperty().bind(interfaceStatusUpdater.isInstalled);
                    findInstalledButton.visibleProperty().bind(interfaceStatusUpdater.isInstalled.not());

                    bookmarkButton1.disableProperty().bind(interfaceStatusUpdater.isInstalled);
                    bookmarkButton1.visibleProperty().bind(interfaceStatusUpdater.isInstalled.not());

                    bookmarkButton2.disableProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    bookmarkButton2.visibleProperty().bind(interfaceStatusUpdater.isInstalled);

                    saveButtons.disableProperty().bind(interfaceStatusUpdater.showBackupButtons.not());
                    saveButtons.visibleProperty().bind(interfaceStatusUpdater.showBackupButtons);

                    galleryPane.visibleProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    galleryPane.disableProperty().bind(interfaceStatusUpdater.isInstalled);
                    galleryPane.managedProperty().bind(interfaceStatusUpdater.isInstalled.not());

                    previewDesc.visibleProperty().bind(interfaceStatusUpdater.isInstalled.not());
                    previewDesc.disableProperty().bind(interfaceStatusUpdater.isInstalled);
                    previewDesc.managedProperty().bind(interfaceStatusUpdater.isInstalled.not());

                    gameDetails.visibleProperty().bind(interfaceStatusUpdater.showExtraDetails);
                    gameDetails.disableProperty().bind(interfaceStatusUpdater.showExtraDetails.not());
                    gameDetails.managedProperty().bind(interfaceStatusUpdater.showExtraDetails);

                    infoTable.prefHeightProperty().bind(interfaceStatusUpdater.tableHeight);
                    infoTable.maxHeightProperty().bind(interfaceStatusUpdater.tableHeight);

                    infoTable.translateYProperty().bind(interfaceStatusUpdater.tableOffset);
                    gameDetails.translateYProperty().bind(interfaceStatusUpdater.tableOffset);
                    if(GeneralSettings.isHd())
                        minReqHtml.translateYProperty().bind(interfaceStatusUpdater.tableOffset.add(45));
                    else
                       minReqHtml.translateYProperty().bind(interfaceStatusUpdater.minReqOffset);
                    detailedDescription.translateYProperty().bind(interfaceStatusUpdater.tableOffset.negate().add(45));


                } catch (Exception ignored) {
                }

            }));

            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {


                mainScrollPane.setVvalue(-50);
                timeline.stop();
                timeline.play();
            }
        });


        if (!gameList.getItems().isEmpty()) {
            gameList.getSelectionModel().select(0);
            selectedGame = Controller.objectGameList.get(gameList.getSelectionModel().getSelectedItem().getText());
        }


        if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Installati"))
            onFilterInstalled();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Preferiti"))
            onFilterBookmarked();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Steam"))
            onFilterSteam();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("N64"))
            onFilterN64();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("GBA"))
            onFilterGba();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("NDS"))
            onFilterNds();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("3DS"))
            onFilter3ds();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Wii"))
            onFilterWii();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Switch"))
            onFilterSwitch();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("PS1"))
            onFilterPs1();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("PS2"))
            onFilterPs2();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Software"))
            onFilterSoftware();
        else if(GeneralSettings.getInitialFilter().equalsIgnoreCase("Altro"))
            onFilterOther();


        queueClass = new DownloaderQueue();

        queueThread.submit(queueClass);

        if(!updating && !downloadingEmuPack) {

            progressBar.progressProperty().bind(queueClass.progress);

            downloadSpeed.textProperty().bind(Bindings.createStringBinding(
                    () -> queueClass.message.getValue().split(",")[0],
                    queueClass.message));

            dimensionLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> {
                        String[] parts = queueClass.message.getValue().split(",");
                        if (parts.length > 1)
                            return parts[1];
                        else
                            return "";
                    },
                    queueClass.message));

        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (openNews) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SteamUtils.openHome();
                }
            }
        });

        if(Launcher.firstLaunch) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sendNotification("", "");
                    sendNotification("", "");
                    sendNotification("", "");
                }
            });
        }

    }

    @FXML
    protected void showControls() {
        if (controls != null)
            controls.setVisible(true);
    }

    @FXML
    protected void hideControls() {
        if (controls != null)
            controls.setVisible(false);
    }

    

    public void filterList(String oldValue, String newValue) {

        ObservableList<String> filteredList = FXCollections.observableArrayList();
        if(searchBar == null || newValue.length() == 0) {
                gameList.setItems(observableGameList);
        } else {
            ObservableList<ListItem> subentries = FXCollections.observableArrayList();
            for (ListItem entry : observableGameList) {
                String entryText = (String) entry.getText();
                if (SteamUtils.normalizeString(entryText.toLowerCase()).contains(SteamUtils.normalizeString(newValue.toLowerCase()))) {
                    subentries.add(entry);
                }
            }
            gameList.setItems(subentries);



        }

    }


    

    @FXML
    protected void onCloseAction() throws FileNotFoundException {

        if(!isUnpacking.getValue()) {

            if ((downloadThread == null || (downloadThread.isDone() || downloadThread.isCancelled())) &&
                    (updateFuture == null || (updateFuture.isDone() || updateFuture.isCancelled())) &&
                    (emuPackFuture == null || (emuPackFuture.isDone() || emuPackFuture.isCancelled()))) {

                SteamUtils.shutdownThreads();

                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();

                Platform.exit();
                System.exit(1);

            } else {

                SteamUtils.openInNewWindow("close-confirm.fxml", "Close", 438, 180);

            }

        }else{

            Controller.sendNotification("", "");

        }
    }

    

    private static double xOffset = 0;
    private static double yOffset = 0;

    @FXML
    protected void onSettingsOpenAction(){

        SteamUtils.openInNewWindow("settings-view.fxml", "Settings", 640, 480);


    }

    @FXML
    protected void onConsoleOpenAction(){

        SteamUtils.openConsole();

    }

    @FXML
    protected void onRandomAction(){


        int listSize = gameList.getItems().size();


        if (listSize > 0) {

            int randomIndex = (int) (Math.random() * listSize);


            gameList.getSelectionModel().select(randomIndex);


            gameList.scrollTo(randomIndex);


            updateListViewColors();


            updateTable();


            updateInstallButtonText();


            gameList.refresh();
        }

    }

    @FXML
    protected void onNewsOpenAction(){

        SteamUtils.openHome();

    }


    

    @FXML
    protected void mouseClickHomeButton() throws IOException{ gameList.getSelectionModel().select(-1); gameList.getSelectionModel().select(0);}

    

    @FXML
    protected void onCollapseClick() throws IOException{
        ((Stage)(collapseButton).getScene().getWindow()).setIconified(true);
    }

    @FXML
    protected void onBasicWindowClick() throws IOException{
        if(isFullscreen) {
            basicWindowButton.setIconLiteral("bi-fullscreen");
            isFullscreen = false;
            Main.standardize();
        }else{
            basicWindowButton.setIconLiteral("bi-fullscreen-exit");
            isFullscreen = true;
            Main.maximize();
        }
    }

    @FXML
    protected void onFullScreenClick() throws IOException{
        if(isFullscreen) {
            basicWindowButton.setIconLiteral("bi-fullscreen");
            isFullscreen = false;
            Main.standardize();
        }else{
            basicWindowButton.setIconLiteral("bi-fullscreen-exit");
            isFullscreen = true;
            Main.maximize();
        }
    }


    

    @FXML
    protected void mouseClickInstallButton() throws IOException{

        if(!Controller.updating && !Controller.downloadingEmuPack) {

            progressBar.progressProperty().bind(queueClass.progress);

            downloadSpeed.textProperty().bind(Bindings.createStringBinding(
                    () -> queueClass.message.getValue().split(",")[0],
                    queueClass.message));

            dimensionLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> {
                        String[] parts = queueClass.message.getValue().split(",");
                        if (parts.length > 1)
                            return parts[1];
                        else
                            return "";
                    },
                    queueClass.message));

        }

        if(!Controller.updating && !Controller.downloadingEmuPack) {
            if(GeneralSettings.isDiskChoiceEnabled()) {
                if(SteamUtils.getDestinationDiskFromData(selectedGame.getName()) == null) {
                    SteamUtils.openInNewWindow("select-install-disk.fxml", "Install Disk Selection", 438, 180);
                } else {

                    try {

                        if (!SteamUtils.getDestinationDiskFromData(selectedGame.getName()).equalsIgnoreCase("default")){
                            Controller.objectGameList.get(selectedGame.getName()).setInstallPath(SteamUtils.getDestinationDiskFromData(selectedGame.getName())+":"+ GeneralSettings.getSecondaryFolderName());
                            if(!new File(SteamUtils.getDestinationDiskFromData(selectedGame.getName())+":"+ GeneralSettings.getSecondaryFolderName()).exists()){
                                new File(SteamUtils.getDestinationDiskFromData(selectedGame.getName())+":"+ GeneralSettings.getSecondaryFolderName()).mkdirs();
                            }
                            SteamUtils.addDestinationDiskToData(selectedGame.getName(), SteamUtils.getDestinationDiskFromData(selectedGame.getName()));
                        }else {
                            Controller.objectGameList.get(selectedGame.getName()).setInstallPath(GeneralSettings.getInstallPath());
                            SteamUtils.addDestinationDiskToData(selectedGame.getName(), "default");
                        }
                        DownloaderQueue.addToQueue(selectedGame, false);

                    } catch (Exception ex) {
                        Main.log.error(Throwables.getStackTraceAsString (ex));
                    }
                }

            }else{

                Controller.objectGameList.get(selectedGame.getName()).setInstallPath(GeneralSettings.getInstallPath());
                SteamUtils.addDestinationDiskToData(selectedGame.getName(), "default");
                DownloaderQueue.addToQueue(selectedGame, false);

            }

        }else {
            Controller.sendNotification("", "");
        }

    }


    

    @FXML
    protected void onPauseClick() throws IOException {

        Controller.isPaused = !Controller.isPaused;

        if(Controller.isPaused){
            pauseButton.setIconLiteral("bi-play");

            Controller.storedLink = Controller.downloader.getDirectLink();

            Controller.downloader.setInterrupted(true);
            Controller.downloader.setPaused(true);

            if(CrawlerManager.isCrawlerAlive()) {


                try {
                    CrawlerManager.killCrawler();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else {


                try {
                    Controller.downloader.closeInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }else{
            Controller.downloader.setInterrupted(false);
            Controller.downloader.setPaused(false);
            pauseButton.setIconLiteral("bi-pause");
        }


    }

    @FXML
    protected void mouseOverPauseButton() throws IOException { pauseButton.setIconColor(Color.valueOf("#00BCD4")); }

    @FXML
    protected void mouseExitsPauseButton() {
        pauseButton.setIconColor(Color.valueOf("#ffffff"));
    }

    

    @FXML
    protected void mouseClickPlayButton() throws IOException{

        try {

            Main.log.info("Trying to start game: " + selectedGame.getName());

            String installPath = Controller.objectGameList.get(selectedGame.getName()).getInstallPath();
            String gameFolderName = Controller.objectGameList.get(selectedGame.getName()).getGameFolderName();
            String exePath = Controller.objectGameList.get(selectedGame.getName()).getExePath();

            String gameFilePath = installPath + gameFolderName + exePath;


            if (!Files.exists(Paths.get(gameFilePath))) {
                sendNotification("", "");
                return;
            }

            Process processBuilder = null;

            boolean isHandSelected = installPath.contains(":") && installPath.length() == 2;
            boolean isOnAnotherDisk = installPath.contains(GeneralSettings.getSecondaryFolderName());
            String otherDisk = "";
            if (isHandSelected){
                isOnAnotherDisk = !Main.applicationDisk.split(":")[0].equalsIgnoreCase(installPath.split(":")[0]);
            }
            if(isOnAnotherDisk){
                otherDisk = installPath.split(":")[0] + ":";
            }

            Main.log.info("Starting game located at: " + (isOnAnotherDisk ? "On Another Disk - " : "On the Default Disk - ") + installPath +
                     gameFolderName + "/" + exePath.split("/")[1]);

            if (selectedGame.getTypeOfFile().equalsIgnoreCase("steam")) {

                processBuilder = new ProcessBuilder( "Execute.bat",
                        (isOnAnotherDisk ? "1" : "0"),
                        isOnAnotherDisk ? otherDisk : (isHandSelected ? "./" : installPath),
                        isHandSelected ? (otherDisk + gameFolderName) : (isOnAnotherDisk ? "." + installPath.split(":")[1] + gameFolderName : "." + gameFolderName),
                        "\"" + exePath.split("/")[1] + "\"").start();

            }else if(SteamUtils.needsEmulator(selectedGame)){

                if (!downloadingEmuPack) {
                    if (isEmulatorPackageInstalled) {

                        if (selectedGame.getTypeOfFile().equalsIgnoreCase("switch")){

                            processBuilder = new ProcessBuilder( 
                                    InternalVar.yuzuPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("N64")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.n64EmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("GBA")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.gbaEmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("NDS")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.ndsEmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("3DS")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.ds3EmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("wii")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.wiiEmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("PS1")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.ps1EmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        } else if(selectedGame.getTypeOfFile().equalsIgnoreCase("PS2")){

                            processBuilder = new ProcessBuilder("Emulator.bat",
                                    InternalVar.RetroArchPath, "-L", InternalVar.ps2EmulatorPath,
                                    "\"" + installPath + gameFolderName + exePath + "\"").start();

                        }else {
                            sendNotification("", "");
                        }

                    }else {
                        sendNotification("", "");
                    }

                } else {
                    sendNotification("", "");
                }

            }else{
                processBuilder = new ProcessBuilder( "Execute.bat",
                        (isOnAnotherDisk ? "1" : "0"),
                        isOnAnotherDisk ? otherDisk : (isHandSelected ? "./" : installPath),
                        isHandSelected ? (otherDisk + gameFolderName) : (isOnAnotherDisk ? "." + installPath.split(":")[1] + gameFolderName : "." + gameFolderName),
                        "\"" + exePath.split("/")[1] + "\"").start();
            }

        }catch(Exception e){
            SteamUtils.logError(e);
        }

        Main.log.info("Game started");

    }


    

    @FXML
    protected void mouseClickUninstallButton() throws IOException{

        SteamUtils.openInNewWindow("uninstall-confirm.fxml", "Uninstall", 438, 180);

    }

    @FXML
    protected void mouseClickUninstallPartsButton() throws IOException{

        SteamUtils.openInNewWindow("uninstall-parts-confirm.fxml", "Uninstall Parts", 438, 180);

    }

    @FXML
    protected void mouseClickFindInstalledButton() throws IOException {


        if(!Controller.isPaused) {
            if (Controller.queue.isEmpty() || (Controller.downloadThread == null || (Controller.downloadThread.isDone() || Controller.downloadThread.isCancelled()))) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select the Executable");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Executable Files", "*.exe"), new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedExe = fileChooser.showOpenDialog(new Stage());
                if (selectedExe != null && !Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                    Wini data = new Wini(new File("data.ini"));

                    data.load();

                    data.add(selectedGame.getName());
                    data.put(selectedGame.getName(), "install-path",
                            selectedExe.getPath().replaceAll("\\\\", "/").split("/")[0]);

                    String temp = "";
                    for (int i = 0; i < selectedExe.getPath().replaceAll("\\\\", "/").split("/").length; i++) {
                        if (i != 0 && i != selectedExe.getPath().replaceAll("\\\\", "/").split("/").length - 1) {
                            temp += "/" + selectedExe.getPath().replaceAll("\\\\", "/").split("/")[i];
                        }
                    }
                    data.put(selectedGame.getName(), "main-folder", temp);

                    data.put(selectedGame.getName(), "exe-path",
                            "/" + selectedExe.getPath().replaceAll("\\\\", "/").split("/")[selectedExe.getPath().replaceAll("\\\\", "/").split("/").length - 1]);
                    data.put(selectedGame.getName(), "saves-path", selectedGame.getSaveFilesPath());
                    data.put(selectedGame.getName(), "game-version", "Not from SteamMachine");

                    data.store();

                    Controller.objectGameList.get(selectedGame.getName()).setInstallPath(data.get(selectedGame.getName()).get("install-path"));
                    Controller.objectGameList.get(selectedGame.getName()).setGameFolderName(data.get(selectedGame.getName()).get("main-folder"));
                    Controller.objectGameList.get(selectedGame.getName()).setExePath(data.get(selectedGame.getName()).get("exe-path"));

                    Controller.objectGameList.get(selectedGame.getName()).setGameVersion("Not from SteamMachine");
                    Controller.objectGameList.get(selectedGame.getName()).setInstalled(true);

                    updateListViewColors();
                    updateTable();

                }
            } else {
                Controller.sendNotification("",
                        "");
            }
        }else {
            Controller.sendNotification("",
                    "");
        }

    }

    @FXML
    protected void mouseClickBookmarkButton() throws IOException {

        Controller.objectGameList.get(selectedGame.getName()).setBookmarked(!Controller.objectGameList.get(selectedGame.getName()).isBookmarked());

        boolean isBookmarked = Controller.objectGameList.get(selectedGame.getName()).isBookmarked();

        if(isBookmarked) {
            bookmarkIcon1.setIconLiteral("bi-star-fill");
            bookmarkIcon2.setIconLiteral("bi-star-fill");

            Wini data = new Wini(new File("data.ini"));

            data.load();

            if(Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                for (Profile.Section sec : data.values()) {
                    if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                        data.put(selectedGame.getName(), "bookmarked", "true");
                    }
                }
            }else {
                if(data.containsKey(selectedGame.getName())){
                    data.put(selectedGame.getName(), "bookmarked", "true");
                }else {
                    data.add(selectedGame.getName());
                    data.put(selectedGame.getName(), "bookmarked", "true");
                }
            }

            data.store();

        }else{
            bookmarkIcon1.setIconLiteral("bi-star");
            bookmarkIcon2.setIconLiteral("bi-star");

            Wini data = new Wini(new File("data.ini"));

            data.load();

            if(Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                for (Profile.Section sec : data.values()) {
                    if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                        sec.remove("bookmarked");
                    }
                }
            }else {
                for (Profile.Section sec : data.values()) {
                    if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                        sec.remove("bookmarked");
                        if(sec.isEmpty())
                            data.remove(selectedGame.getName());
                        break;
                    }
                }
            }

            data.store();

        }

        Controller.updateListViewColors();

    }

    @FXML
    protected void mouseClickUploadSaveButton(){

        SteamUtils.openInNewWindow("upload-backup.fxml", "Upload Backup", 438, 180);

    }

    @FXML
    protected void mouseClickDownloadSaveButton(){

        SteamUtils.openInNewWindow("download-backup.fxml", "Download Backup", 438, 180);

    }

    

    @FXML
    protected void mouseClickGameSettingsButton() throws IOException{



    }

    @FXML
    protected void onMenuAction1() throws IOException {
        try{
            Desktop.getDesktop().open(new File(selectedGame.getInstallPath() + selectedGame.getGameFolderName() + "/"));
        }catch(Exception e){
            Main.log.error(Throwables.getStackTraceAsString(e));
        }
    }

    @FXML
    protected void onMenuAction2() throws IOException{

        FileSystemView view = FileSystemView.getFileSystemView();
        File file = view.getHomeDirectory();
        String path = file.getPath();

        try {
            ShellLink.createLink(selectedGame.getInstallPath() + selectedGame.getGameFolderName() + selectedGame.getExePath(),
                    path + "/" + selectedGame.getName().replaceAll("[^a-zA-Z0-9]", " ") + ".lnk");
        }catch (Exception e){
            Main.log.error(Throwables.getStackTraceAsString(e));
        }
    }

    @FXML
    protected void onMenuAction3() throws IOException{

        try {
            if(!updating)
                DownloaderQueue.addToQueue(selectedGame, true);
            else
                sendNotification("", "");

        } catch (Exception ex) {
            Main.log.error(Throwables.getStackTraceAsString (ex));
        }

    }

    @FXML
    protected void setToItalian() throws IOException {

        File folder = new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" +
                Controller.objectGameList.get(selectedGame.getName()).getGameFolderName().split("/")[1]);

        if(SteamUtils.searchFile(folder, "OnlineFix.ini") != null){

            Wini onlineFix = new Wini(new File(SteamUtils.searchFile(folder, "OnlineFix.ini")));

            onlineFix.load();
            onlineFix.get("Main").replace("Language", "italian");
            onlineFix.store();

        }


        if (folder.exists() && folder.isDirectory()) {

            File steamSettingsFolder = SteamUtils.searchFolder(folder, "steam_settings");
            if (steamSettingsFolder == null) {

                steamSettingsFolder = new File(folder, "steam_settings");
                steamSettingsFolder.mkdirs();
            }


            File forceLanguageFile = new File(steamSettingsFolder, "force_language.txt");
            try (FileWriter writer = new FileWriter(forceLanguageFile)) {
                writer.write("italian");
            }


            SteamUtils.overrideForceLanguageFiles(folder, "italian");
        }

    }

    @FXML
    protected void setToEnglish() throws IOException {

        File folder = new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" +
                Controller.objectGameList.get(selectedGame.getName()).getGameFolderName().split("/")[1]);

        if(SteamUtils.searchFile(folder, "OnlineFix.ini") != null){

            Wini onlineFix = new Wini(new File(SteamUtils.searchFile(folder, "OnlineFix.ini")));

            onlineFix.load();
            onlineFix.get("Main").replace("Language", "english");
            onlineFix.store();

        }


        if (folder.exists() && folder.isDirectory()) {

            File steamSettingsFolder = SteamUtils.searchFolder(folder, "steam_settings");
            if (steamSettingsFolder == null) {

                steamSettingsFolder = new File(folder, "steam_settings");
                steamSettingsFolder.mkdirs();
            }


            File forceLanguageFile = new File(steamSettingsFolder, "force_language.txt");
            try (FileWriter writer = new FileWriter(forceLanguageFile)) {
                writer.write("english");
            }


            SteamUtils.overrideForceLanguageFiles(folder, "english");
        }

    }

    @FXML
    protected void onMenuAction4() throws IOException{

        String path = selectedGame.getSaveFilesPath();
        String env = "";
        try{
            env = path.split("%")[1];
        } catch (Exception e) {

        }

        if(!env.equals("")){
            if(path.contains("Users")){
                try{
                    Desktop.getDesktop().open(new File( (path.split("%")[0] +
                            System.getenv(path.split("%")[1].toUpperCase()) + path.split("%")[2] + "/").replace("%", "")));
                }catch(Exception e){
                    Main.log.error(Throwables.getStackTraceAsString(e));
                }
            }else if(path.charAt(0) == '%'){
                try{
                    Desktop.getDesktop().open(new File( (System.getenv(path.split("%")[1].toUpperCase())
                            + path.split("%")[2] + "/").replace("%", "")));
                }catch(Exception e){
                    Main.log.error(Throwables.getStackTraceAsString(e));
                }
            }
        }else{
            try{
                Desktop.getDesktop().open(new File( path.charAt(0) == '^' ? selectedGame.getInstallPath() + path.replace("^", "") + "/" : path + "/"));
            }catch(Exception e){
                Main.log.error(Throwables.getStackTraceAsString(e));
            }
        }



    }

    @FXML
    protected void onRestoreDataINI() throws IOException {
        Wini data = new Wini(new File("data.ini"));

        data.load();
        if(data.containsKey(selectedGame.getName())){
            data.get(selectedGame.getName()).replace("main-folder", "");
            data.get(selectedGame.getName()).replace("exe-path", "");
        }
        data.store();

        Controller.sendNotification("", "");
    }

    @FXML
    protected void onMenuAction5() throws IOException{

        CompletableFuture.runAsync(new WorkshopDownloader(selectedGame));

    }


    

    @FXML
    protected void onFilterInstalled(){

        if (!currentFilter.equalsIgnoreCase("installati")) {

            Controller.currentFilter = "installati";
            rapidFilter.setIconLiteral("bi-collection-play-fill");
            resetFilterSelection();
            filterInstalled.setDisable(true);
            filterButton.setFill(Color.WHITE);
            filterButtonDecoration.setFill(Color.WHITE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> installed = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.isInstalled()) {
                    installed.add(new ListItem(game.getName()));
                }
            }

            if(installed.isEmpty()){
                Platform.runLater(() -> onFilterNone());
                return;
            }

            Collections.sort(installed, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (installed.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                    observableGameList.clear();
                    observableGameList.addAll(installed);
                    gameList.setItems(observableGameList);

                    for (ListItem li : observableGameList) {
                        if (li.getText().equalsIgnoreCase(selectedGameName)) {
                            gameList.getSelectionModel().select(li);
                            updateListViewColors();
                            return;
                        }
                    }

                    gameList.getSelectionModel().select(0);
                    updateListViewColors();
                    updateTable();
                    updateInstallButtonText();

                    gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }
        }

    }

    @FXML
    protected void onFilterPartial(){

        if (!currentFilter.equalsIgnoreCase("parziali")) {

            Controller.currentFilter = "parziali";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterPartial.setDisable(true);
            filterButton.setFill(Color.SEAGREEN);
            filterButtonDecoration.setFill(Color.SEAGREEN);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> partiallyInstalledGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {

                if (new File(game.getInstallPath() + "/" + game.getArchiveName()).exists() ||
                        (new File(game.getInstallPath() + "/" +
                                Controller.objectGameList.get(game.getName())
                                        .getArchiveName().split("\\.")[0] + ".part1.rar").exists())) {
                    partiallyInstalledGames.add(new ListItem(game.getName()));
                }
            }

            if(partiallyInstalledGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(partiallyInstalledGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (partiallyInstalledGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(partiallyInstalledGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterNone(){

        if (!currentFilter.equalsIgnoreCase("tutti")) {

            Controller.currentFilter = "tutti";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterNone.setDisable(true);
            filterButton.setFill(Color.WHITE);
            filterButtonDecoration.setFill(Color.WHITE);


            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempCompleteGameList = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {

                tempCompleteGameList.add(new ListItem(game.getName()));

            }

            Collections.sort(tempCompleteGameList, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            Platform.runLater(() -> {
            observableGameList.clear();
            observableGameList.addAll(tempCompleteGameList);
            gameList.setItems(observableGameList);

            for (ListItem li : observableGameList) {
                if (li.getText().equalsIgnoreCase(selectedGameName)) {
                    gameList.getSelectionModel().select(li);
                    updateListViewColors();
                    return;
                }
            }

            gameList.getSelectionModel().select(0);
            updateListViewColors();
            updateTable();
            updateInstallButtonText();

            gameList.refresh();
            });
        }

    }

    @FXML
    protected void onFilterBookmarked(){

        if (!currentFilter.equalsIgnoreCase("preferiti")) {

            Controller.currentFilter = "preferiti";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterBookmarked.setDisable(true);
            filterButton.setFill(Color.YELLOW);
            filterButtonDecoration.setFill(Color.YELLOW);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> bookmarkedGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.isBookmarked()) {
                    bookmarkedGames.add(new ListItem(game.getName()));
                }
            }

            if(bookmarkedGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(bookmarkedGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (bookmarkedGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(bookmarkedGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterSteam(){

        if (!currentFilter.equalsIgnoreCase("steam")) {

            Controller.currentFilter = "steam";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterSteam.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> steamGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("steam")) {
                    steamGames.add(new ListItem(game.getName()));
                }
            }

            if(steamGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(steamGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (steamGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(steamGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }
    }

    @FXML
    protected void onFilterEpic(){

        if (!currentFilter.equalsIgnoreCase("epic")) {

            Controller.currentFilter = "epic";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterEpic.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> epicGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("epic")) {
                    epicGames.add(new ListItem(game.getName()));
                }
            }

            if(epicGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(epicGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (epicGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(epicGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }
    }

    @FXML
    protected void onFilterN64(){

        if (!currentFilter.equalsIgnoreCase("n64")) {

            Controller.currentFilter = "n64";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterN64.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);


            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("N64")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterGba(){

        if (!currentFilter.equalsIgnoreCase("gba")) {

            Controller.currentFilter = "gba";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterGba.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("GBA")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterNds(){

        if (!currentFilter.equalsIgnoreCase("nds")) {

            Controller.currentFilter = "nds";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterNds.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("NDS")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                    observableGameList.clear();
                    observableGameList.addAll(tempGames);
                    gameList.setItems(observableGameList);

                    for (ListItem li : observableGameList) {
                        if (li.getText().equalsIgnoreCase(selectedGameName)) {
                            gameList.getSelectionModel().select(li);
                            updateListViewColors();
                            return;
                        }
                    }

                    gameList.getSelectionModel().select(0);
                    updateListViewColors();
                    updateTable();
                    updateInstallButtonText();

                    gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilter3ds(){

        if (!currentFilter.equalsIgnoreCase("3ds")) {

            Controller.currentFilter = "3ds";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filter3ds.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("3DS")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterWii(){

        if (!currentFilter.equalsIgnoreCase("wii")) {

            Controller.currentFilter = "wii";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterWii.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("wii")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterSwitch(){

        if (!currentFilter.equalsIgnoreCase("switch")) {

            Controller.currentFilter = "switch";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterSwitch.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> switchGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("switch")) {
                    switchGames.add(new ListItem(game.getName()));
                }
            }

            if(switchGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(switchGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (switchGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(switchGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterPs1(){

        if (!currentFilter.equalsIgnoreCase("ps1")) {

            Controller.currentFilter = "ps1";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterPs1.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("PS1")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterPs2(){

        if (!currentFilter.equalsIgnoreCase("ps2")) {

            Controller.currentFilter = "ps2";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterPs2.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("PS2")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterSoftware(){

        if (!currentFilter.equalsIgnoreCase("programmi")) {

            Controller.currentFilter = "programmi";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterSoftware.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("software")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterOther(){

        if (!currentFilter.equalsIgnoreCase("altro")) {

            Controller.currentFilter = "altro";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterOther.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getTypeOfFile().equalsIgnoreCase("altro")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterSingleplayer(){

        if (!currentFilter.equalsIgnoreCase("singleplayer")) {

            Controller.currentFilter = "singleplayer";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterSingleplayer.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if ((game.getMultiplayerType().equalsIgnoreCase("/singleplayer"))
                ||(game.getMultiplayerType().equalsIgnoreCase("/non disponibile"))) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterOnline(){

        if (!currentFilter.equalsIgnoreCase("online")) {

            Controller.currentFilter = "online";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterOnline.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if ( (game.getMultiplayerType().equalsIgnoreCase("/steam")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/yuzu rooms")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/xbox app")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/epic games")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/integrato")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/modded")) ||
                        (game.getMultiplayerType().equalsIgnoreCase("/gameranger"))){
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterLan(){

        if (!currentFilter.equalsIgnoreCase("lan")) {

            Controller.currentFilter = "lan";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterLan.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getMultiplayerType().equalsIgnoreCase("/LAN (ZeroTier)")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onFilterLocal(){

        if (!currentFilter.equalsIgnoreCase("local")) {

            Controller.currentFilter = "local";
            rapidFilter.setIconLiteral("bi-collection");
            resetFilterSelection();
            filterLocal.setDisable(true);
            filterButton.setFill(Color.CORNFLOWERBLUE);
            filterButtonDecoration.setFill(Color.CORNFLOWERBLUE);

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempGames = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.getMultiplayerType().equalsIgnoreCase("/locale o parsec")) {
                    tempGames.add(new ListItem(game.getName()));
                }
            }

            if(tempGames.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(tempGames, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });


            if (tempGames.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(tempGames);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }

    }

    @FXML
    protected void onRapidFilter(){

        if(Controller.currentFilter.equalsIgnoreCase("installati")){

            Platform.runLater(() -> {
                        Controller.currentFilter = "tutti";
                        rapidFilter.setIconLiteral("bi-collection");
                        resetFilterSelection();
                        filterNone.setDisable(true);
                        filterButton.setFill(Color.WHITE);
                        filterButtonDecoration.setFill(Color.WHITE);
                    });

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> tempCompleteGameList = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {

                tempCompleteGameList.add(new ListItem(game.getName()));

            }

            Collections.sort(tempCompleteGameList, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });
            Platform.runLater(() -> {
            observableGameList.clear();
            observableGameList.addAll(tempCompleteGameList);
            gameList.setItems(observableGameList);

            for (ListItem li : observableGameList) {
                if (li.getText().equalsIgnoreCase(selectedGameName)) {
                    gameList.getSelectionModel().select(li);
                    updateListViewColors();
                    return;
                }
            }

            gameList.getSelectionModel().select(0);
            updateListViewColors();
            updateTable();
            updateInstallButtonText();

            gameList.refresh();
            });

        }else{

            Controller.currentFilter = "installati";
            Platform.runLater(() -> {
                        rapidFilter.setIconLiteral("bi-collection-play-fill");
                        resetFilterSelection();
                        filterInstalled.setDisable(true);
                        filterButton.setFill(Color.WHITE);
                        filterButtonDecoration.setFill(Color.WHITE);
                    });

            String selectedGameName = selectedGame.getName();
            ArrayList<ListItem> installed = new ArrayList<ListItem>();

            for (Game game : Controller.objectGameList.values()) {
                if (game.isInstalled()) {
                    installed.add(new ListItem(game.getName()));
                }
            }

            if(installed.isEmpty()){
                onFilterNone();
                return;
            }

            Collections.sort(installed, new Comparator<ListItem>() {

                @Override
                public int compare(ListItem lhs, ListItem rhs) {

                    return lhs.getText().compareTo(rhs.getText());

                }
            });

            if (installed.size() != objectGameList.size()) {
                Platform.runLater(() -> {
                observableGameList.clear();
                observableGameList.addAll(installed);
                gameList.setItems(observableGameList);

                for (ListItem li : observableGameList) {
                    if (li.getText().equalsIgnoreCase(selectedGameName)) {
                        gameList.getSelectionModel().select(li);
                        updateListViewColors();
                        return;
                    }
                }

                gameList.getSelectionModel().select(0);
                updateListViewColors();
                updateTable();
                updateInstallButtonText();

                gameList.refresh();
                });
            } else {
                sendNotification("", "");
            }

        }


    }

    
    @FXML
    protected void openQueue() {

        if (!isQueueOpen){

            isQueueOpen = true;
            SteamUtils.openInNewWindow("queue-view.fxml", "Queue", 302, 480);

        }

    }

    @FXML
    protected void queueButtonMouseEntered(){
        DownloadLabel.setText("CLICK TO OPEN QUEUE");
        DownloadLabel.setStyle("-fx-text-fill: #00BCD4; -fx-fill: #00BCD4");
    }

    @FXML
    protected void queueButtonMouseExited(){
        DownloadLabel.setText("DOWNLOADS");
        DownloadLabel.setStyle("-fx-text-fill: #919191; -fx-fill: #919191");
    }

    

    public static void sendNotification(String title, String text) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                    Controller.notificationTitle = title;
                    Controller.notificationText = text;

                    SteamUtils.openInNewWindow("notification.fxml", "Notification", 438, 180);

            }
        });
    }


    public static void setDownloadingProperty(boolean value) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if(value){
                    Controller.isPaused = false;
                }

                Controller.isDownloading.setValue(value);

            }
        });
    }



    public static void setUnpackingProperty(boolean value) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                Controller.isUnpacking.setValue(value);

            }
        });
    }

    public static void updateListViewColors(){

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                for (Game game : Controller.objectGameList.values()) {
                    for (ListItem item : Controller.observableGameList) {
                        if (item.getText().equalsIgnoreCase(game.getName())) {
                            if (game.isInstalled()) {
                                item.color.set(Color.valueOf(DesignSettings.getInstalledGamesColor()));
                            } else {
                                if (!game.getDownloadedParts().isEmpty() && game.getDownloadedParts().size() > 0) {
                                    item.color.set(Color.valueOf(DesignSettings.getPartiallyInstalledGamesColor()));
                                } else if (new File(game.getInstallPath() + "/" + game.getArchiveName()).exists() ||
                                            (new File(game.getInstallPath() + "/" +
                                                Controller.objectGameList.get(game.getName())
                                                        .getArchiveName().split("\\.")[0] + ".part1.rar").exists())) {
                                    item.color.set(Color.valueOf(DesignSettings.getPartiallyInstalledGamesColor()));
                                } else {
                                    item.color.set(Color.valueOf("#C1C8D2"));
                                }
                            }
                            if (game.isBookmarked()) {
                                item.color.set(Color.valueOf(DesignSettings.getFavoriteGamesColor()));
                            }
                        }

                    }
                }

            }

        });

    }

    public static void updateInstallButtonText(){

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                if(!Controller.objectGameList.get(selectedGame.getName()).getDownloadedParts().isEmpty() &&
                        Controller.objectGameList.get(selectedGame.getName()).getDownloadedParts().size() == selectedGame.getDownloadLink().size()){
                    installButtonTextProperty.set("ESTRAI");
                }else{
                    installButtonTextProperty.set("INSTALLA");
                }

            }

        });

    }

    public void resetFilterSelection(){
        filterInstalled.setDisable(false);
        filterBookmarked.setDisable(false);
        filterPartial.setDisable(false);
        filterNone.setDisable(false);
        filterSteam.setDisable(false);
        filterEpic.setDisable(false);
        filterN64.setDisable(false);
        filterGba.setDisable(false);
        filterNds.setDisable(false);
        filter3ds.setDisable(false);
        filterWii.setDisable(false);
        filterSwitch.setDisable(false);
        filterPs1.setDisable(false);
        filterPs2.setDisable(false);
        filterSoftware.setDisable(false);
        filterOther.setDisable(false);
        filterSingleplayer.setDisable(false);
        filterOnline.setDisable(false);
        filterLan.setDisable(false);
        filterLocal.setDisable(false);
    }

    public static void updateTable(){

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                tableData.clear();

                tableData.add(
                        new TableInfo(
                                "Multiplayer:",
                                objectGameList.get(selectedGame.getName()).getMultiplayerType().contains("https") ? "steam fix" : objectGameList.get(selectedGame.getName()).getMultiplayerType().substring(1)
                        ));






                tableData.add(
                        new TableInfo(
                                "Partizioni:",
                                objectGameList.get(selectedGame.getName()).isInstalled() ?
                                        "installed" :
                                        String.valueOf(objectGameList.get(selectedGame.getName()).getDownloadedParts().size()) + "/" + String.valueOf(objectGameList.get(selectedGame.getName()).getDownloadLink().size())
                        ));
                if(!Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                    tableData.add(
                            new TableInfo(
                                    "Dim. Download:",
                                    objectGameList.get(selectedGame.getName()).getDownloadDimension()
                            ));
                }
                if(Controller.objectGameList.get(selectedGame.getName()).isInstalled()) {
                    tableData.add(
                            new TableInfo(
                                    "Position:",
                                    Controller.objectGameList.get(selectedGame.getName()).getInstallPath().equalsIgnoreCase(GeneralSettings.getInstallPath())
                                            ? Main.applicationDisk : Controller.objectGameList.get(selectedGame.getName()).getInstallPath().split(":")[0]+":\\")
                    );
                    tableData.add(
                            new TableInfo(
                                    "Dimension:",
                                    objectGameList.get(selectedGame.getName()).getDownloadDimension()
                            ));
                    tableData.add(
                            new TableInfo(
                                    "Installed Version:",
                                    objectGameList.get(selectedGame.getName()).isInstalled() ?
                                            objectGameList.get(selectedGame.getName()).getGameVersion() :
                                            "none"
                            ));
                    tableData.add(
                            new TableInfo(
                                    "Latest Version:",
                                    objectGameList.get(selectedGame.getName()).getLatestGameVersion()
                            ));
                }
                tableData.add(
                        new TableInfo(
                                "Categories:",
                                objectGameList.get(selectedGame.getName()).getExtraTags()
                        ));
                tableData.add(
                        new TableInfo(
                                "Current Price:",
                                (objectGameList.get(selectedGame.getName()).getPrice() != null && objectGameList.get(selectedGame.getName()).getPrice().equalsIgnoreCase("")) ?
                                        "unknown" : objectGameList.get(selectedGame.getName()).getPrice()
                        ));
                tableData.add(
                        new TableInfo(
                                "Store Link:",
                                objectGameList.get(selectedGame.getName()).getStoreLink().contains("http") ?
                                        "Click to Open" : "unknown"
                        ));

            }

        });

    }

    private void resetVideoPlayer() {

        if (player != null) {
            player.stop();
            player.dispose();
        }



        if (controls != null)
            controls.getChildren().clear();


        if (playPauseButton != null)
            playPauseButton.setOnAction(null);
        if (timeSlider != null)
            timeSlider.setOnMousePressed(null);
        if (timeSlider != null)
            timeSlider.setOnMouseReleased(null);
        if (volumeSlider != null)
            volumeSlider.valueProperty().unbind();

        bigVideoPause.setVisible(false);


        player = null;
        playPauseButton = null;
        timeSlider = null;
        volumeSlider = null;
    }

    public static void applyDetailsFromCache(String gameName, HashMap<String, ArrayList<String>> dtl){

        Controller.objectGameList.get(gameName).setSmallHeader(dtl.get("small-header").get(0));

        if(Controller.objectGameList.get(gameName).getExtraTags() == null
                || Controller.objectGameList.get(gameName).getExtraTags().equalsIgnoreCase("")
                || Controller.objectGameList.get(gameName).getExtraTags().equalsIgnoreCase("/"))
        Controller.objectGameList.get(gameName).setExtraTags(dtl.get("genres").get(0));

        if(Controller.objectGameList.get(gameName).getGameDesc() == null
                || Controller.objectGameList.get(gameName).getGameDesc().equalsIgnoreCase("")
                || Controller.objectGameList.get(gameName).getGameDesc().equalsIgnoreCase("/"))
        Controller.objectGameList.get(gameName).setGameDesc(dtl.get("shortDesc").get(0));

        if(Controller.objectGameList.get(gameName).getLong_desc() == null
                || Controller.objectGameList.get(gameName).getLong_desc().equalsIgnoreCase("")
                || Controller.objectGameList.get(gameName).getLong_desc().equalsIgnoreCase("/"))
        Controller.objectGameList.get(gameName).setLong_desc(SteamUtils.removeLink(dtl.get("desc").get(0)).strip());

        if(Controller.objectGameList.get(gameName).getPrice() == null
                || Controller.objectGameList.get(gameName).getPrice().equalsIgnoreCase("")
                || Controller.objectGameList.get(gameName).getPrice().equalsIgnoreCase("/"))
        Controller.objectGameList.get(gameName).setPrice(dtl.get("price").get(0));

        if(Controller.objectGameList.get(gameName).getImageGalleryThumbs() == null
                || Controller.objectGameList.get(gameName).getImageGalleryThumbs().isEmpty())
        Controller.objectGameList.get(gameName).setImageGalleryThumbs(dtl.get("imgs_thumbs"));

        if(Controller.objectGameList.get(gameName).getImageGallery() == null
                || Controller.objectGameList.get(gameName).getImageGallery().isEmpty())
        Controller.objectGameList.get(gameName).setImageGallery(dtl.get("imgs"));

        if(Controller.objectGameList.get(gameName).getVideoGallery() == null
                || Controller.objectGameList.get(gameName).getVideoGallery().isEmpty())
        Controller.objectGameList.get(gameName).setVideoGallery(dtl.get("movies"));

        if(Controller.objectGameList.get(gameName).getVideoGalleryThumbs() == null
                || Controller.objectGameList.get(gameName).getVideoGalleryThumbs().isEmpty())
        Controller.objectGameList.get(gameName).setVideoGalleryThumbs(dtl.get("movies_thumbs"));

        if(Controller.objectGameList.get(gameName).getRequirements() == null
                || Controller.objectGameList.get(gameName).getRequirements().equalsIgnoreCase("")
                || Controller.objectGameList.get(gameName).getRequirements().equalsIgnoreCase("/"))
        Controller.objectGameList.get(gameName).setRequirements(dtl.get("req").get(0));
    }

    public void selectFromName(String gameName) {

        int index = getIndexFromName(gameName);


        gameList.getSelectionModel().select(index);


        gameList.scrollTo(index);


        updateListViewColors();


        updateTable();


        updateInstallButtonText();


        gameList.refresh();
    }

    private int getIndexFromName(String gameName) {



        ObservableList<ListItem> items = gameList.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getText().equalsIgnoreCase(gameName)) {
                return i;
            }
        }
        return -1;
    }

}

