package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;


public class UninstallController {

    @FXML private FontIcon closeButton;
    @FXML private Button denyButton;
    @FXML private Button proceedButton;
    @FXML private Label uninstallLabel;
    @FXML private ProgressIndicator uninstallationProgress;
    @FXML private AnchorPane uninstallationProgressLabel;

    private final Game selectedGame;

    public UninstallController() {
        this.selectedGame = Controller.objectGameList.get(Controller.selectedGame.getName());
    }

    @FXML
    public void initialize() throws IOException {

        this.uninstallLabel.setText("Sei sicuro di voler disinstallare " + this.selectedGame.getName() + "?");

        if(selectedGame.getGameVersion().replaceAll(" ", "").equalsIgnoreCase("Not from SteamMachine".replaceAll(" ", ""))) {

            Controller.sendNotification("",
                    "");

        }

    }



    @FXML
    protected void mouseClickProceedButton(){

        try {

            Main.log.info("Beginning " + selectedGame.getName() + " uninstallation...");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    proceedButton.setVisible(false);
                    proceedButton.setDisable(true);
                    denyButton.setVisible(false);
                    denyButton.setDisable(true);

                    uninstallationProgress.setVisible(true);
                    uninstallationProgressLabel.setVisible(true);
                }
            });

            class GameActualUninstaller implements Runnable {
                public void run() {

                    try {

                        if(!selectedGame.getGameVersion().replaceAll(" ", "").equalsIgnoreCase("Not from SteamMachine".replaceAll(" ", ""))) {
                            Main.log.info("Beginning " + selectedGame.getName() + " uninstallation...");
                            Main.log.info("Deleting " + selectedGame.getGameFolderName() + " as requested by the downloader class.");
                            try {
                                FileUtils.deleteDirectory(new File(selectedGame.getInstallPath() + "/" + selectedGame.getGameFolderName().split("/")[1]));
                            }catch (Exception failed){
                                SteamUtils.logError(failed);
                            }
                        }

                        if (new Wini(new File("data.ini")).containsKey(selectedGame.getName())) {
                            Main.log.info("Safely removing " + selectedGame.getName() + " from data.ini");
                            SteamUtils.safelyRemoveFromData(selectedGame);
                        }

                        Controller.objectGameList.get(selectedGame.getName()).setInstalled(false);
                        Controller.objectGameList.get(selectedGame.getName()).getDownloadedParts().clear();

                        Controller.updateListViewColors();
                        Controller.updateTable();
                        Controller.updateInstallButtonText();

                        Main.log.info("Game uninstalled...");

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Stage stage = (Stage) closeButton.getScene().getWindow();
                                stage.close();
                            }
                        });

                        Thread.currentThread().interrupt();

                    }catch(IOException e){
                        SteamUtils.logError(e);
                        Thread.currentThread().interrupt();
                    }

                }
            }

            GameActualUninstaller uninstaller = new GameActualUninstaller();
            Thread uninstallerThread = new Thread(uninstaller);
            uninstallerThread.start();
            Main.log.info("Started Uninstaller Thread");

        }catch (Exception e){
            SteamUtils.logError(e);
        }

    }


    @FXML
    protected void mouseClickDenyButton(){

        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();

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