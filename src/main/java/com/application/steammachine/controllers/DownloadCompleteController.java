package com.application.steammachine.controllers;

import com.application.steammachine.Controller;
import com.application.steammachine.DownloaderQueue;
import com.application.steammachine.Main;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;


public class DownloadCompleteController {

    @FXML private Label installedTitle;
    @FXML private AnchorPane mainWindow;

    public static String SecondaryNotText = "";

    private final int NOTIFICATION_WIDTH = 313;
    private final int NOTIFICATION_HEIGHT = 94;
    private final int NOTIFICATION_DURATION = 4000;


    @FXML
    public void initialize() throws IOException {

        installedTitle.setText(SecondaryNotText);

    }


}