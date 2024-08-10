package com.application.steammachine.controllers;

import com.application.steammachine.*;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.FileNotFoundException;
import java.io.IOException;


public class ShutdownController {

    @FXML private FontIcon closeButton;
    @FXML private Button denyButton;
    @FXML private Button proceedButton;
    @FXML private Label uninstallLabel;

    @FXML
    public void initialize() throws IOException { }


    @FXML
    protected void mouseClickProceedButton(){

        try {

            SteamUtils.shutdownThreads();

            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();

            Platform.exit();

        }catch (Exception e){
            SteamUtils.logError(e);
        }

        System.exit(1);

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