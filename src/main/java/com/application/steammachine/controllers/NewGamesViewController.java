package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.Main;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NewGamesViewController {














    @FXML private FontIcon closeButton;
    @FXML private HBox newGames;

    @FXML private ScrollPane newGamesScrollPane;

    @FXML private Label nothingLabel;

    private final double scrollPaneDragThreshold = 10.0;
    private final DropShadow glowEffect = new DropShadow(20, Color.WHITE);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final String API_KEY = "";

    @FXML
    public void initialize() {
        Main.log.info("Loading home images ...");

        nothingLabel.setVisible(Controller.unknownGames.isEmpty());

        newGamesScrollPane.setPannable(true);


        loadImagesWithProgressIndicator();
    }


    private void addGlowEffectAndTooltip(ImageView imageView, String gameName) {
        imageView.setOnMouseEntered(event -> {
            imageView.setEffect(glowEffect);
            imageView.setCursor(Cursor.HAND);
            Tooltip tooltip = new Tooltip(gameName);
            tooltip.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 5; -fx-border-color: white;");
            tooltip.setShowDelay(Duration.millis(400));
            Tooltip.install(imageView, tooltip);
        });

        imageView.setOnMouseExited(event -> {
            imageView.setEffect(null);
            imageView.setCursor(Cursor.DEFAULT);
            Tooltip.uninstall(imageView, null);
        });
    }


    private String fetchGameId(int steamAppId) throws IOException {
        URL url = new URL("https:
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());

            return String.valueOf(jsonResponse.getJSONObject("data").getNumber("id"));
        }
    }


    private void loadImagesWithProgressIndicator() {
        for (String gameName : Controller.unknownGames) {
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(40, 40);
            progressIndicator.setStyle("-fx-progress-color: white;");
            Platform.runLater(() -> newGames.getChildren().add(progressIndicator));

            CompletableFuture.supplyAsync(() -> {
                try {
                    int steamAppId = Integer.parseInt(Controller.objectGameList.get(gameName.strip()).getId());

                    String gameId;

                    if (Controller.objectGameList.get(gameName.strip()).getSteamGridDBId().isEmpty() ||
                            Controller.objectGameList.get(gameName.strip()).getSteamGridDBId().equals("/") ||
                            Controller.objectGameList.get(gameName.strip()).getSteamGridDBId() == null) {

                        gameId = fetchGameId(steamAppId);

                    } else {
                        gameId = Controller.objectGameList.get(gameName.strip()).getSteamGridDBId();
                    }

                    if (gameId != null) {
                        String imageUrl = fetchImageUrl(gameId);
                        if (imageUrl != null) {
                            return new Image(imageUrl);
                        }
                    }
                } catch (IOException e) {
                    SteamUtils.logError(e);
                }
                return null;
            }).thenAcceptAsync(image -> {
                if (image != null) {
                    Main.log.warn("Applying News Image for: " + gameName.strip());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(210);
                    imageView.setPreserveRatio(true);

                    Platform.runLater(() -> {
                        newGames.getChildren().remove(progressIndicator);
                        newGames.getChildren().add(imageView);
                        addGlowEffectAndTooltip(imageView, gameName.strip());
                        imageView.setOnMouseClicked(event ->
                                Controller.changeSelection.set(gameName.strip())
                        );
                    });
                } else {
                    Main.log.warn("No image found for newly added: " + gameName.strip());
                    Platform.runLater(() -> newGames.getChildren().remove(progressIndicator));
                }
            }, Platform::runLater);
        }
    }

    private String fetchImageUrl(String gameId) throws IOException {
        String style = "material,white_logo";
        String dimensions = "600x900,342x482,660x930";
        String mimes = "image/png,image/jpeg";
        String types = "static";
        String nsfw = "false";

        String url = String.format("https:
                gameId, dimensions, mimes, types, nsfw);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            Main.log.info("Got News Images: " + jsonResponse.getJSONArray("data").getJSONObject(0).getString("thumb"));
            return jsonResponse.getJSONArray("data").getJSONObject(0).getString("thumb");
        }catch (Exception e){
            Main.log.warn("Nothing worrying:");
            SteamUtils.logError(e);
        }
        return null;
    }



    

    @FXML
    protected void onCloseAction(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void mouseOverCloseButton() {
        closeButton.setIconColor(Color.valueOf("#ea4b1f"));
    }

    @FXML
    protected void mouseExitsCloseButton() {
        closeButton.setIconColor(Color.valueOf("#ffffff"));
    }




























}