package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.application.steammachine.GDriveManager;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class UploadBackupController {

    @FXML private FontIcon closeButton;
    @FXML private Button localButton;
    @FXML private Button driveButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label progressLabel;
    @FXML private Label lastLocal;
    @FXML private Label lastDrive;

    private final Game selectedGame;
    private String finalSavePath = "";

    public UploadBackupController() {
        this.selectedGame = Controller.objectGameList.get(Controller.selectedGame.getName());
    }

    @FXML
    public void initialize() throws IOException {

        finalSavePath = SteamUtils.convertSavesPath(selectedGame);

        Wini data = new Wini(new File("data.ini"));
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

            Main.log.info("Upload saves of " + selectedGame.getName() + " to LOCAL");

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

            class ActualBackupper implements Runnable {
                public void run() {

                    try{
                        File savesFolder = new File("Saves");
                        if (!savesFolder.exists()){
                            Main.log.info("Generating default saves folder...");
                            savesFolder.mkdirs();
                        }

                        Process process = null;

                        process = new ProcessBuilder("./elevate.exe", "-c", "-w", "RAR.bat",
                                "./Saves/" + selectedGame.getName().replaceAll(" ", "") + "-saves.rar", finalSavePath).start();

                        Thread.sleep(1500);
                        int code = process.waitFor();

                        SteamUtils.addKeyToGameData(selectedGame,"local-backup-date",
                                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));

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

            ActualBackupper backupper = new ActualBackupper();
            Thread backupperThread = new Thread(backupper);
            backupperThread.start();
            Main.log.info("Started Backupper Thread");

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

            class ActualBackupper implements Runnable {
                public void run() {

                    try {

                        File savesFolder = new File("Saves");
                        if (!savesFolder.exists()){
                            Main.log.info("Generating default saves folder...");
                            savesFolder.mkdirs();
                        }

                        Process process = null;

                        process = new ProcessBuilder("./elevate.exe", "-c", "-w", "RAR.bat",
                                selectedGame.getInstallPath() + selectedGame.getName().replaceAll(" ", "") + "-saves.rar", finalSavePath).start();

                        Thread.sleep(1500);
                        int code = process.waitFor();

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

                        data.store();

                        String driveID = GDriveManager.uploadFile(archive, IDofExisting);

                        if (archive.exists())
                            archive.delete();

                        SteamUtils.addKeyToGameData(selectedGame, "drive-backup-id", driveID);
                        SteamUtils.addKeyToGameData(selectedGame, "drive-backup-date",
                                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));

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

            ActualBackupper backupper = new ActualBackupper();
            Thread backupperThread = new Thread(backupper);
            backupperThread.start();
            Main.log.info("Started Backupper Thread");


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