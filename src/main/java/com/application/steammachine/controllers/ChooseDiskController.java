package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.DownloaderQueue;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class ChooseDiskController {

    @FXML private FontIcon closeButton;
    @FXML private Button proceedButton;
    private final Game selectedGame;

    @FXML private ChoiceBox<String> selector;

    public ChooseDiskController() {
        this.selectedGame = Controller.objectGameList.get(Controller.selectedGame.getName());
    }

    public void initialize() throws IOException {
        List<String> diskList = getLargeDisks();
        String defaultDisk = Main.applicationDisk + " (Default)";
        selector.getItems().addAll(diskList);
        selector.setValue(defaultDisk);
        selector.setTooltip(new Tooltip(defaultDisk));
        Main.log.info("Disks found: " + diskList);
    }

    private List<String> getLargeDisks() {
        List<String> largeDisks = new ArrayList<>();
        File[] roots = File.listRoots();
        for (File root : roots) {
            long totalSpace = root.getTotalSpace();
            long totalSpaceInGB = totalSpace / (1024 * 1024 * 1024);
            if (totalSpaceInGB > 10) {
                largeDisks.add(root.getAbsolutePath().equalsIgnoreCase(Main.applicationDisk) ? Main.applicationDisk + " (Default)" : root.getAbsolutePath());
            }
        }
        return largeDisks;
    }

    @FXML
    protected void mouseClickProceedButton(){

        try {

            if (!selector.getValue().equalsIgnoreCase(Main.applicationDisk + " (Default)")){
                Controller.objectGameList.get(selectedGame.getName()).setInstallPath(selector.getValue().split(":")[0]+":"+ GeneralSettings.getSecondaryFolderName());
                if(!new File(selector.getValue().split(":")[0]+":"+ GeneralSettings.getSecondaryFolderName()).exists()){
                    new File(selector.getValue().split(":")[0]+":"+ GeneralSettings.getSecondaryFolderName()).mkdirs();
                }
                SteamUtils.addDestinationDiskToData(selectedGame.getName(), selector.getValue().split(":")[0]);
            }else {
                Controller.objectGameList.get(selectedGame.getName()).setInstallPath(GeneralSettings.getInstallPath());
                SteamUtils.addDestinationDiskToData(selectedGame.getName(), "default");
            }
            DownloaderQueue.addToQueue(selectedGame, false);


        } catch (Exception ex) {
            Main.log.error(Throwables.getStackTraceAsString (ex));
        }finally {
            onCloseAction();
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