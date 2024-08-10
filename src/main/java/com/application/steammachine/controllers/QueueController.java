package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.utils.QueueCell;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;


public class QueueController {

    @FXML private FontIcon closeButton;
    @FXML private ListView queueListView;

    @FXML
    public void initialize() throws IOException {

        queueListView.setItems(FXCollections.observableArrayList(Controller.queue.keySet()));
        queueListView.setCellFactory(lv -> new QueueCell());

    }



    

    @FXML
    protected void onCloseAction(){
        Controller.isQueueOpen = false;
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