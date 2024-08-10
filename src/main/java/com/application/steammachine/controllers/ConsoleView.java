package com.application.steammachine.controllers;

import com.application.steammachine.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleView {

    @FXML
    private FontIcon closeButton;
    @FXML
    private TextArea consoleText;

    private Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @FXML
    public void initialize() {
        start();
    }

    private void start() {

        if (logger != null) {
            redirectLoggerOutput();
        }


        redirectSystemOutAndErr();
    }

    private void redirectLoggerOutput() {

        logger.addAppender(new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {

                if (consoleText != null && event.getMessage() != null) {
                    Platform.runLater(() -> consoleText.appendText(event.getMessage().toString() + "\n"));
                }
            }

            @Override
            public void close() {

            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
        });
    }

    private void redirectSystemOutAndErr() {

        PrintStream customOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {

                System.out.write(b);

                appendText(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {

                System.out.write(b, off, len);

                appendText(new String(b, off, len));
            }

            private void appendText(String text) {
                Platform.runLater(() -> consoleText.appendText(text));
            }
        }, true);


        System.setOut(customOut);
        System.setErr(customOut);
    }

    @FXML
    protected void onCloseAction() {

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
