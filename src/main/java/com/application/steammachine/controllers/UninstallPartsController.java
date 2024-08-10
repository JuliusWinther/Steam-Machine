package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.DownloaderQueue;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;


public class UninstallPartsController {

    @FXML private FontIcon closeButton;
    @FXML private Button denyButton;
    @FXML private Button proceedButton;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private ProgressIndicator uninstallationProgress;
    @FXML private Label uninstallationProgressLabel;

    private final Game selectedGame;

    public UninstallPartsController() {
        this.selectedGame = Controller.objectGameList.get(Controller.selectedGame.getName());
    }

    @FXML
    public void initialize() throws IOException {

        this.titleLabel.setText("Eliminare le partizioni di " + this.selectedGame.getName() + "?");

        if(Controller.objectGameList.get(this.selectedGame.getName()).getDownloadedParts().isEmpty() &&
                Controller.objectGameList.get(this.selectedGame.getName()).getDownloadedParts().size() <= 0) {


                if (SteamUtils.isGameArchivePresent(this.selectedGame.getName())) {

                    double fileSize =
                            SteamUtils.getFileSize(Controller.objectGameList.get(this.selectedGame.getName()).getInstallPath() + "/"
                            + Controller.objectGameList.get(this.selectedGame.getName()).getArchiveName());

                    String sizeText = SteamUtils.formatFileDimension(fileSize);

                    this.titleLabel.setText("Eliminare quanto scaricato di " + this.selectedGame.getName() + "?");
                    this.descriptionLabel.setText("Eliminerai " + sizeText + " fino ad ora scaricati");

                }

        }

    }


    @FXML
    protected void mouseClickProceedButton(){

        try {

            Main.log.info("Beginning " + selectedGame.getName() + " parts uninstallation...");

            if(!Controller.isPaused || !DownloaderQueue.getFirst().equalsIgnoreCase(selectedGame.getName())) {

                if (Controller.queue.isEmpty() || ((Controller.downloadThread == null || (Controller.downloadThread.isDone() || Controller.downloadThread.isCancelled()))
                        || !DownloaderQueue.getFirst().equalsIgnoreCase(selectedGame.getName()))) {

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

                    class PartsActualUninstaller implements Runnable {
                        public void run() {

                            try {
                                for (int part = 1; part <= Controller.objectGameList.get(selectedGame.getName()).getDownloadedParts().size() + 1; part++) {
                                    File gameArchive;
                                    if (Controller.objectGameList.get(selectedGame.getName()).getDownloadLink().size() == 1) {
                                        gameArchive = new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" + Controller.objectGameList.get(selectedGame.getName()).getArchiveName());
                                    } else {
                                        gameArchive = new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" + Controller.objectGameList.get(selectedGame.getName()).getArchiveName().split("\\.")[0] + ".part" + part + ".rar");
                                    }
                                    if (gameArchive.exists()) {
                                        try {
                                            FileUtils.delete(gameArchive);
                                        }catch (Exception failed){
                                            SteamUtils.logError(failed);
                                        }
                                    }else{
                                        Main.log.warn(gameArchive.getAbsolutePath() + " cannot be found!");
                                    }
                                }

                                if (new Wini(new File("data.ini")).containsKey(selectedGame.getName()))
                                    SteamUtils.safelyRemoveFromData(selectedGame);

                                Controller.objectGameList.get(selectedGame.getName()).getDownloadedParts().clear();

                                Controller.updateListViewColors();
                                Controller.updateTable();
                                Controller.updateInstallButtonText();

                                Main.log.info("Game Parts uninstalled...");

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Stage stage = (Stage) closeButton.getScene().getWindow();
                                        stage.close();
                                    }
                                });

                                Thread.currentThread().interrupt();

                            } catch (IOException e) {
                                SteamUtils.logError(e);
                                Thread.currentThread().interrupt();
                            }

                        }
                    }

                    class PartialActualUninstaller implements Runnable {
                        public void run() {

                            try {

                                Main.log.info("Uninstalling " + selectedGame.getName() + " parts...");
                                try {
                                    FileUtils.delete(new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" + selectedGame.getArchiveName()));
                                }catch (Exception failed){
                                    SteamUtils.logError(failed);
                                }

                                if (new Wini(new File("data.ini")).containsKey(selectedGame.getName()))
                                    SteamUtils.safelyRemoveFromData(selectedGame);

                                Main.log.info("Game Partial uninstalled...");

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Stage stage = (Stage) closeButton.getScene().getWindow();
                                        stage.close();
                                    }
                                });

                                Controller.updateListViewColors();

                                Thread.currentThread().interrupt();

                            } catch (IOException e) {
                                SteamUtils.logError(e);
                                Thread.currentThread().interrupt();
                            }

                        }
                    }

                    if (!selectedGame.getDownloadedParts().isEmpty() && selectedGame.getDownloadedParts().size() > 0) {
                        PartsActualUninstaller uninstaller = new PartsActualUninstaller();
                        Thread uninstallerThread = new Thread(uninstaller);
                        uninstallerThread.start();
                        Main.log.info("Started Parts Uninstaller Thread");
                    } else if (SteamUtils.isGameArchivePresent(this.selectedGame.getName())) {
                        PartialActualUninstaller uninstaller = new PartialActualUninstaller();
                        Thread uninstallerThread = new Thread(uninstaller);
                        uninstallerThread.start();
                        Main.log.info("Started Partial Uninstaller Thread");
                    } else if (new File(Controller.objectGameList.get(selectedGame.getName()).getInstallPath() + "/" +
                            Controller.objectGameList.get(Controller.selectedGame.getName())
                                    .getArchiveName().split("\\.")[0] + ".part1.rar").exists()) {
                        PartsActualUninstaller uninstaller = new PartsActualUninstaller();
                        Thread uninstallerThread = new Thread(uninstaller);
                        uninstallerThread.start();
                        Main.log.info("Started Parts Uninstaller Thread (only a partially part1 exists)");
                    }

                } else {
                    Controller.sendNotification("",
                            "");
                }
            }else {
                Controller.sendNotification("",
                        "");
            }

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