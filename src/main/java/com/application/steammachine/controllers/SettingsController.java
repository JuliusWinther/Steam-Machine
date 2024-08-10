package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.Main;
import com.application.steammachine.settings.DesignSettings;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.ini4j.Wini;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.List;


public class SettingsController {

    @FXML private FontIcon closeButton;
    @FXML private FontIcon fileExplorer_InstallPath;
    @FXML private TextField installPathLabel;


    @FXML private TabPane tabPane;
    @FXML private ColorPicker installedGamesColor;
    @FXML private ColorPicker partiallyInstalledGamesColor;
    @FXML private ColorPicker favoriteGamesColor;
    @FXML private ColorPicker progressBarColor;

    @FXML private ToggleButton deleteExtractedParts;
    @FXML private ToggleButton openFullscreen;
    @FXML private ToggleButton applyDnsOnStartup;
    @FXML private ToggleButton autoGenerateShortcuts;
    @FXML private ToggleButton showInstallNot;
    @FXML private ToggleButton launchOnStart;
    @FXML private ChoiceBox<String> initialFilterSelector;

    @FXML private ToggleButton enableDiskChoice;
    @FXML private ToggleButton hdButton;

    DirectoryChooser installPathChooser;
    String installPath;
    String secondaryInstallPath;
    String[] filters = { "Tutti", "Installati", "Preferiti", "Parziali", "Steam",
            "N64", "GBA", "NDS", "3DS", "Wii", "Switch",
            "PS1", "PS2", "Software", "Altro"};

    private final ObservableList<Object> observableOptionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws IOException {







        installPathChooser = new DirectoryChooser();

        installedGamesColor.setValue(Color.valueOf(DesignSettings.getInstalledGamesColor()));
        partiallyInstalledGamesColor.setValue(Color.valueOf(DesignSettings.getPartiallyInstalledGamesColor()));
        favoriteGamesColor.setValue(Color.valueOf(DesignSettings.getFavoriteGamesColor()));
        progressBarColor.setValue(Color.valueOf(DesignSettings.getProgressBarColor()));

        deleteExtractedParts.setSelected(GeneralSettings.isDeleteExtractedParts());
        deleteExtractedParts.setText(GeneralSettings.isDeleteExtractedParts() ? "On" : "Off");

        openFullscreen.setSelected(GeneralSettings.getOpenFullscreen());
        openFullscreen.setText(GeneralSettings.getOpenFullscreen() ? "On" : "Off");

        applyDnsOnStartup.setSelected(GeneralSettings.isApplyDnsOnStartup());
        applyDnsOnStartup.setText(GeneralSettings.isApplyDnsOnStartup() ? "On" : "Off");

        autoGenerateShortcuts.setSelected(GeneralSettings.isAutoGenerateShortcuts());
        autoGenerateShortcuts.setText(GeneralSettings.isAutoGenerateShortcuts() ? "On" : "Off");

        showInstallNot.setSelected(GeneralSettings.isShowInstallNot());
        showInstallNot.setText(GeneralSettings.isShowInstallNot() ? "On" : "Off");

        launchOnStart.setSelected(GeneralSettings.isLaunchOnStart());
        launchOnStart.setText(GeneralSettings.isLaunchOnStart() ? "On" : "Off");

        enableDiskChoice.setSelected(GeneralSettings.isDiskChoiceEnabled());
        enableDiskChoice.setText(GeneralSettings.isDiskChoiceEnabled() ? "On" : "Off");

        hdButton.setSelected(GeneralSettings.isHd());
        hdButton.setText(GeneralSettings.isHd() ? "On" : "Off");

        initialFilterSelector.setItems(FXCollections.observableArrayList(filters));
        initialFilterSelector.setValue(GeneralSettings.getInitialFilter());
        initialFilterSelector.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {


            public void changed(ObservableValue ov, Number value, Number new_value)
            {
                String selectedChoice = filters[new_value.intValue()];
                initialFilterSelector.setValue(selectedChoice);
                GeneralSettings.setInitialFilter(selectedChoice);
                try {
                    Wini config = new Wini(new File("config.ini"));
                    config.load();
                    config.put("GENERAL", "initial-filter", selectedChoice);
                    config.store();
                }catch(Exception e){
                    SteamUtils.logError(e);
                }
            }

        });

    }

    

    @FXML
    protected void fileExplorer_InstallPath_open() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        File selectedDirectory = installPathChooser.showDialog(new Stage());
        if(selectedDirectory != null) {
            installPath = (String) selectedDirectory.getAbsolutePath().replaceAll("\\\\", "/");
            GeneralSettings.setInstallPath(installPath);
            installPathLabel.setText(installPath);
            config.load();
            config.put("GENERAL", "default-install-path", installPath);
            config.store();
        }
    }

















    
    @FXML
    protected void changeDNS(){
        try {

            Main.log.info("Trying to change DNS to Google");

            Process processBuilder = new ProcessBuilder("./elevate.exe", "-c", "-w", "DNS-setter-google.ps1").start();

        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void changeDNSCloudflare(){
        try {

            Main.log.info("Trying to change DNS to Cloudflare");

            Process processBuilder = new ProcessBuilder("./elevate.exe", "-c", "-w", "DNS-setter-cloudflare.ps1").start();

        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void removeEmulatorClicked(){
        try {

            Main.log.info("Trying to remove emulator");

            Wini data = new Wini(new File("data.ini"));
            data.load();
            data.remove("EMULATOR PACKAGE");
            data.store();

            Controller.sendNotification("", "");

        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void createWindowsDefenderExceptionClicked(){
        try {

            Main.log.info("Trying to add Windows Defender Exception");

            String folderPath = System.getProperty("user.dir");

            try {
                if (!SteamUtils.isFolderExcluded(folderPath)) {
                    SteamUtils.addFolderExclusion(folderPath);
                    Main.log.info("Folder exclusion added successfully.");
                } else {
                    Main.log.info("Folder is already excluded.");
                }
            } catch (IOException | InterruptedException e) {
                Main.log.error("Error adding or checking folder exclusion: " + e.getMessage());
            }

        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void clearDetailsCache(){
        try {

            Main.log.info("Deleting Details Cache ...");

            Path folderPath = Paths.get("./cache/details");

            if (!Files.isDirectory(folderPath)) {
                throw new IllegalArgumentException("The provided path is not a directory.");
            }


            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                    if (!dir.equals(folderPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            Main.log.info("Deleted Details Cache ...");


        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void clearImageCache(){
        try {

            Main.log.info("Deleting Image Cache ...");

            Path folderPath = Paths.get("./cache/images");

            if (!Files.isDirectory(folderPath)) {
                throw new IllegalArgumentException("The provided path is not a directory.");
            }


            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                    if (!dir.equals(folderPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            Main.log.info("Deleted Image Cache ...");


        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }

    @FXML
    protected void forceUpdateClicked(){
        try {

            Main.log.info("Trying to forcefully change to version 1.0");
            updateFile("versioning.txt");
            Controller.sendNotification("", "");

        }catch(Exception e){
            SteamUtils.logError(e);
        }
    }



    private static void updateFile(String filePath) {
        try {

            FileWriter writer = new FileWriter(filePath, false);


            writer.write("omega-1.00");


            writer.close();

            Main.log.info("File updated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    protected void onDeleteExtractedParts() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setDeleteExtractedParts(deleteExtractedParts.isSelected());
        deleteExtractedParts.setText(GeneralSettings.isDeleteExtractedParts() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "delete-extracted-parts", deleteExtractedParts.isSelected());
        config.store();

    }

    @FXML
    protected void onOpenFullscreenAction() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setOpenFullscreen(openFullscreen.isSelected());
        openFullscreen.setText(GeneralSettings.getOpenFullscreen() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "open-fullscreen", openFullscreen.isSelected());
        config.store();

    }

    @FXML
    protected void onApplyDnsOnStartupAction() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setApplyDnsOnStartup(applyDnsOnStartup.isSelected());
        applyDnsOnStartup.setText(GeneralSettings.isApplyDnsOnStartup() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "apply-dns-on-startup", applyDnsOnStartup.isSelected());
        config.store();

    }

    @FXML
    protected void onAutoGenerateShortcutsAction() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setAutoGenerateShortcuts(autoGenerateShortcuts.isSelected());
        autoGenerateShortcuts.setText(GeneralSettings.isAutoGenerateShortcuts() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "auto-generate-shortcuts", autoGenerateShortcuts.isSelected());
        config.store();

    }

    @FXML
    protected void onShowInstallNotAction() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setShowInstallNot(showInstallNot.isSelected());
        showInstallNot.setText(GeneralSettings.isShowInstallNot() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "show-installed-notification", showInstallNot.isSelected());
        config.store();

    }

    @FXML
    protected void onLaunchOnStartAction() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setLaunchOnStart(launchOnStart.isSelected());
        launchOnStart.setText(GeneralSettings.isLaunchOnStart() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "launch-on-startup", launchOnStart.isSelected());
        config.store();


        File batch = new File(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Steam-Machine.bat");
        if(GeneralSettings.isLaunchOnStart()){

            if(!batch.exists())
                batch.createNewFile();

            FileWriter writer = new FileWriter(batch);
            writer.write("@echo off\n start \"\" \"" + System.getProperty("user.dir") + "\\SteamMachine.exe\"");
            writer.close();


        }else{

            if(batch.exists())
                batch.delete();

        }

    }

    @FXML
    protected void onHdButton() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setHd(hdButton.isSelected());
        hdButton.setText(GeneralSettings.isHd() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "hd", hdButton.isSelected());
        config.store();

        Controller.sendNotification("","");

    }

    @FXML
    protected void onEnableDiskChoice() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        GeneralSettings.setDiskChoiceEnabled(enableDiskChoice.isSelected());
        enableDiskChoice.setText(GeneralSettings.isDiskChoiceEnabled() ? "On" : "Off");
        config.load();
        config.put("GENERAL", "disk-choice-enabled", enableDiskChoice.isSelected());
        config.store();

    }

    

    @FXML
    protected void mouseOverFileExplorerButton() {
        fileExplorer_InstallPath.setIconColor(Color.valueOf("#e7d42f"));
    }

    @FXML
    protected void mouseExitsFileExplorerButton() {
        fileExplorer_InstallPath.setIconColor(Color.valueOf("#ffffff"));
    }

    @FXML
    protected void mouseOverFileExplorerButton2() {
        fileExplorer_InstallPath.setIconColor(Color.valueOf("#e7d42f"));
    }

    @FXML
    protected void mouseExitsFileExplorerButton2() {
        fileExplorer_InstallPath.setIconColor(Color.valueOf("#ffffff"));
    }


    

    @FXML
    protected void installedGamesColorManager() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        DesignSettings.setInstalledGamesColor(toHexString(installedGamesColor.getValue()));




        config.load();
        config.put("APPEARANCE", "installed-games-color", DesignSettings.getInstalledGamesColor());
        config.store();

        Controller.updateListViewColors();

    }

    @FXML
    protected void partiallyInstalledGamesColorManager() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        DesignSettings.setPartiallyInstalledGamesColor(toHexString(partiallyInstalledGamesColor.getValue()));

        config.load();
        config.put("APPEARANCE", "partially-installed-games-color", DesignSettings.getPartiallyInstalledGamesColor());
        config.store();

        Controller.updateListViewColors();

    }


    @FXML
    protected void favoriteGamesColorManager() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        DesignSettings.setFavoriteGamesColor(toHexString(favoriteGamesColor.getValue()));

        config.load();
        config.put("APPEARANCE", "favorite-games-color", DesignSettings.getFavoriteGamesColor());
        config.store();

        Controller.updateListViewColors();

    }


    @FXML
    protected void progressBarColorManager() throws IOException {

        Wini config = new Wini(new File("config.ini"));

        DesignSettings.setProgressBarColor(toHexString(progressBarColor.getValue()));

        config.load();
        config.put("APPEARANCE", "progress-bar-color", DesignSettings.getProgressBarColor());
        config.store();

    }


    


    private String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity()))
                .toUpperCase();
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