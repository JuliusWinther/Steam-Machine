package com.application.steammachine;

import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.DetailsCacher;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;


public class StatusUpdater extends Task<Void> {

    public static boolean interrupt = false;

    public SimpleBooleanProperty isInstalled = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty showBackupButtons = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty showPartsUninstaller = new SimpleBooleanProperty(false);
    public SimpleIntegerProperty numberOfInstalledParts = new SimpleIntegerProperty(0);

    public SimpleDoubleProperty tableHeight = new SimpleDoubleProperty(206.0);
    public SimpleIntegerProperty tableOffset = new SimpleIntegerProperty(0);
    public SimpleIntegerProperty minReqOffset = new SimpleIntegerProperty(0);

    public SimpleStringProperty cacheStatus = new SimpleStringProperty("");
    public SimpleBooleanProperty showCacheStatus = new SimpleBooleanProperty(false);

    public SimpleBooleanProperty showExtraDetails = new SimpleBooleanProperty(false);


    public StatusUpdater() {
        interrupt = false;
    }


    @Override
    protected Void call() throws Exception {

        while(!interrupt) {

            Thread.sleep(20);

            try {
                if (Controller.objectGameList.get(Controller.selectedGame.getName()).isInstalled()) {


                    this.isInstalled.set(true);

                    if(GeneralSettings.isHd()) {
                        this.tableHeight.set(362.0);
                        this.tableOffset.set(50);
                    }else {
                        this.tableHeight.set(282.0);
                        this.minReqOffset.set(135);
                    }

                    this.showExtraDetails.set(Controller.objectGameList.get(Controller.selectedGame.getName()).hasExtraDetails());


                    if(!Controller.selectedGame.getSaveFilesPath().isEmpty()
                            && !Controller.selectedGame.getSaveFilesPath().replaceAll(" ", "").equalsIgnoreCase("/")
                            && !Controller.selectedGame.getSaveFilesPath().replaceAll(" ", "").equalsIgnoreCase("")){
                        this.showBackupButtons.set(true);
                    }else{
                        this.showBackupButtons.set(false);
                    }

                } else {


                    this.isInstalled.set(false);
                    this.showBackupButtons.set(false);

                    if(GeneralSettings.isHd()) {
                        this.tableHeight.set(256.0);
                        this.tableOffset.set(0);
                    }else {
                        this.minReqOffset.set(0);
                        this.tableHeight.set(206.0);
                    }

                    this.showExtraDetails.set(false);

                    if(!Controller.objectGameList.get(Controller.selectedGame.getName()).getDownloadedParts().isEmpty() &&
                            Controller.objectGameList.get(Controller.selectedGame.getName()).getDownloadedParts().size() > 0) {

                            this.showPartsUninstaller.set(true);
                            this.numberOfInstalledParts.set(Controller.objectGameList.get(Controller.selectedGame.getName()).getDownloadedParts().size());
                    }else{

                        if(SteamUtils.isCurrentGameArchivePresent() ||
                                (new File(Controller.objectGameList.get(Controller.selectedGame.getName()).getInstallPath() + "/" +
                                        Controller.objectGameList.get(Controller.selectedGame.getName())
                                                .getArchiveName().split("\\.")[0] + ".part1.rar").exists())){
                            this.showPartsUninstaller.set(true);
                        }else {
                            this.showPartsUninstaller.set(false);
                        }

                    }

                }

                Platform.runLater(() -> {
                    if ((Controller.presentCache+DetailsCacher.presentCache) == Controller.totalCache) {
                        this.showCacheStatus.set(false);
                    } else {
                        this.showCacheStatus.set(true);
                    }
                    this.cacheStatus.set("Caching: " + (Controller.presentCache + DetailsCacher.presentCache) + "/" + Controller.totalCache);
                });

            }catch(Exception e){

            }

        }

        return null;
    }

}
