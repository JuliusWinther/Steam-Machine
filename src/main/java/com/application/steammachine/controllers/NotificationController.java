package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;


public class NotificationController {

    @FXML private FontIcon closeButton;
    @FXML private Label notificationTitle;
    @FXML private Label notificationText;


    @FXML
    public void initialize() throws IOException {

        this.notificationTitle.setText(Controller.notificationTitle);
        this.notificationText.setText(Controller.notificationText);

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


    
    public void setTitle(String title){
        this.notificationTitle.setText(title);
    }

    public void setText(String text){
        this.notificationText.setText(text);
    }


}