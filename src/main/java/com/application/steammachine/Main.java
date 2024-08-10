package com.application.steammachine;

import com.application.steammachine.settings.GeneralSettings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main extends Application {

    public static double ACTUAL_VERSION = 1.00;

    public static Logger log = Logger.getLogger(Main.class.getName());

    public static Stage mainStage;
    private static Scene scene;

    private double xOffset = 0;
    private double yOffset = 0;

    public static String applicationDisk = "C:\\";

    @Override
    public void start(Stage stage) throws IOException {

        applicationDisk = getCurrentDisk();

        if (GeneralSettings.isApplyDnsOnStartup()) {
            if (GeneralSettings.getEmbeddedDNS().equalsIgnoreCase("google")) {
                Process processBuilder = new ProcessBuilder("./elevate.exe", "-c", "DNS-setter-google.ps1").start();
            } else {
                Process processBuilder = new ProcessBuilder("./elevate.exe", "-c", "DNS-setter-cloudflare.ps1").start();
            }
        }

        mainStage = stage;

        if(GeneralSettings.isHd()) {

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("first-view-1920.fxml")));


            scene = new Scene(root, 1920, 1040);

            if (GeneralSettings.getOpenFullscreen()) {

                maximize();
            } else {

                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                mainStage.setX((primaryScreenBounds.getWidth() / 2) - 960);
                mainStage.setY((primaryScreenBounds.getHeight() / 2) - 520);
            }
        }else{

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("first-view.fxml")));


            scene = new Scene(root, 1280, 720);

            if (GeneralSettings.getOpenFullscreen()) {

                maximize();
            } else {

                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                mainStage.setX((primaryScreenBounds.getWidth() / 2) - 640);
                mainStage.setY((primaryScreenBounds.getHeight() / 2) - 360);
            }
        }

        stage.setTitle("Steam-Machine");
        stage.setScene(scene);


        stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/com/application/steammachine/images/logo.png"))));

        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);

        scene.getStylesheets().add(getClass().getResource("/com/application/steammachine/styles/customs.css").toExternalForm());

        stage.show();

    }

    public static void maximize() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        mainStage.setX(primaryScreenBounds.getMinX());
        mainStage.setY(primaryScreenBounds.getMinY());
        mainStage.setWidth(primaryScreenBounds.getWidth());
        mainStage.setHeight(primaryScreenBounds.getHeight());

        double newX = 0;
        double newY = 0;

        if(GeneralSettings.isHd()) {
            newX = mainStage.getWidth() / 1920;
            newY = mainStage.getHeight() / 1040;
        }else {
            newX = mainStage.getWidth() / 1280;
            newY = mainStage.getHeight() / 720;
        }

        Scale scale = new Scale(newX, newY);
        scale.setPivotX(mainStage.getX());
        scale.setPivotY(mainStage.getY());
        scene.getRoot().getTransforms().setAll(scale);

    }

    public static void standardize() {

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        if(GeneralSettings.isHd()) {
            mainStage.setX((primaryScreenBounds.getWidth() / 2) - 960);
            mainStage.setY((primaryScreenBounds.getHeight() / 2) - 520);
            mainStage.setWidth(1920);
            mainStage.setHeight(1040);
        }else {
            mainStage.setX((primaryScreenBounds.getWidth() / 2) - 640);
            mainStage.setY((primaryScreenBounds.getHeight() / 2) - 360);
            mainStage.setWidth(1280);
            mainStage.setHeight(720);
        }

        Scale scale = new Scale(1, 1);
        scale.setPivotX(mainStage.getX());
        scale.setPivotY(mainStage.getY());
        scene.getRoot().getTransforms().setAll(scale);

    }

    public static void main(String[] args) {
        launch();
    }

    private String getCurrentDisk() {
        Path currentRelativePath = Paths.get("");
        String currentPath = currentRelativePath.toAbsolutePath().toString();
        File currentFile = new File(currentPath);
        return currentFile.getAbsolutePath().substring(0, 3);
    }
}