package com.application.steammachine.controllers;

import com.application.steammachine.*;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.ProgressCallback;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;


public class DownloadBackupController {

    @FXML private FontIcon closeButton;
    @FXML private Button localButton;
    @FXML private Button driveButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressLabel;
    @FXML private Label lastLocal;
    @FXML private Label lastDrive;

    private final Game selectedGame;
    private String finalSavePath = "";

    public DownloadBackupController() {
        this.selectedGame = Controller.objectGameList.get(Controller.selectedGame.getName());

    }

    @FXML
    public void initialize() throws IOException {

        finalSavePath = SteamUtils.convertSavesPath(selectedGame);

        if(!new File("./Saves/" + selectedGame.getName().replaceAll(" ", "") + "-saves.rar").exists())
            localButton.setDisable(true);
        else
            localButton.setDisable(false);

        Wini data = new Wini(new File("data.ini"));
        data.load();

        for (Profile.Section sec : data.values()) {
            if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                if(sec.containsKey("drive-backup-id"))
                    driveButton.setDisable(false);
                else
                    driveButton.setDisable(true);
                break;
            }
        }

        data.store();

        data.load();
        for (Profile.Section sec : data.values()) {
            if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                lastLocal.setText(sec.getOrDefault("local-backup-date", "No Local Backups"));
                lastDrive.setText(sec.getOrDefault("drive-backup-date", "No Drive Backups"));
                break;
            }
        }
        data.store();

    }


    @FXML
    protected void mouseClickLocalButton(){


        try {

            Main.log.info("Recover saves of " + selectedGame.getName() + " from LOCAL");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    localButton.setVisible(false);
                    localButton.setDisable(true);
                    driveButton.setVisible(false);
                    driveButton.setDisable(true);

                    progressIndicator.setVisible(true);
                    progressLabel.setVisible(true);
                }
            });

            class ActualRecoverer implements Runnable {
                public void run() {

                    try{

                        
                        RARExtractor.extractBasic(
                                "./Saves/" + selectedGame.getName().replaceAll(" ", "") + "-saves.rar",
                                finalSavePath,
                                true, false);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Stage stage = (Stage) closeButton.getScene().getWindow();
                                stage.close();
                            }
                        });

                        Thread.currentThread().interrupt();

                    }catch(Exception e){
                        SteamUtils.logError(e);
                        Thread.currentThread().interrupt();
                    }

                }
            }

            ActualRecoverer recoverer = new ActualRecoverer();
            Thread recovererThread = new Thread(recoverer);
            recovererThread.start();
            Main.log.info("Started Recoverer Thread");

        } catch (Exception e) {

            SteamUtils.logError(e);

        }

    }


    @FXML
    protected void mouseClickDriveButton(){

        try {

            Main.log.info("Upload saves of " + selectedGame.getName() + " to DRIVE");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    localButton.setVisible(false);
                    localButton.setDisable(true);
                    driveButton.setVisible(false);
                    driveButton.setDisable(true);

                    progressIndicator.setVisible(true);
                    progressLabel.setVisible(true);
                }
            });

            class ActualRecoverer implements Runnable {
                public void run() {

                    try {

                        File archive = new File(selectedGame.getInstallPath() + selectedGame.getName().replaceAll(" ", "") + "-saves.rar");

                        String IDofExisting = "";

                        Wini data = new Wini(new File("data.ini"));
                        data.load();

                        for (Profile.Section sec : data.values()) {
                            if (selectedGame.getName().equalsIgnoreCase(sec.getName())) {
                                if(sec.containsKey("drive-backup-id"))
                                    IDofExisting = sec.get("drive-backup-id");
                                break;
                            }
                        }

                        GDriveManager.downloadSave(archive, IDofExisting);

                       
                        RARExtractor.extractBasic(
                                selectedGame.getInstallPath() + selectedGame.getName().replaceAll(" ", "") + "-saves.rar",
                                finalSavePath,
                                true, false);

                        if (archive.exists())
                            archive.delete();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Stage stage = (Stage) closeButton.getScene().getWindow();
                                stage.close();
                            }
                        });

                        Thread.currentThread().interrupt();

                    }catch(Exception e){
                        SteamUtils.logError(e);
                        Thread.currentThread().interrupt();
                    }

                }
            }

            ActualRecoverer recoverer = new ActualRecoverer();
            Thread recovererThread = new Thread(recoverer);
            recovererThread.start();
            Main.log.info("Started Recoverer Thread");


        } catch (Exception e) {

            SteamUtils.logError(e);

        }

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