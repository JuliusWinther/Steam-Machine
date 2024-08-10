package com.application.steammachine.utils;

import com.application.steammachine.Controller;
import com.application.steammachine.CrawlerManager;
import com.application.steammachine.DownloaderQueue;
import com.application.steammachine.Main;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

public class QueueCell extends ListCell<String> {




    private final HBox hbox = new HBox();


    private final Label label = new Label();


    private final FontIcon icon1 = FontIcon.of(BootstrapIcons.ARROW_DOWN, 14, Color.WHITE);


    private final FontIcon icon2 = FontIcon.of(BootstrapIcons.ARROW_UP, 14, Color.WHITE);


    private final FontIcon icon3 = FontIcon.of(BootstrapIcons.X, 22, Color.WHITE);



    public QueueCell() {

        label.setTextFill(Color.valueOf("#e4e8ed"));

        HBox hboxLeft = new HBox();
        hboxLeft.getChildren().addAll(label);
        hboxLeft.setAlignment(Pos.CENTER_LEFT);

        HBox hboxRight = new HBox();
        hboxRight.getChildren().addAll(icon1, icon2, icon3);
        hboxRight.setAlignment(Pos.CENTER_RIGHT);
        hboxRight.setSpacing(5);

        HBox.setHgrow(hboxLeft, Priority.ALWAYS);
        HBox.setHgrow(hboxRight, Priority.ALWAYS);
        hboxLeft.setFillHeight(false);
        hboxRight.setFillHeight(false);

        hbox.getChildren().addAll(hboxLeft, hboxRight);
        setContentDisplay(ContentDisplay.CENTER);
        setGraphic(hbox);



        icon1.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {

                Controller.updateListViewColors();

                String key = label.getText();
                DownloaderQueue.moveDown(key);


                getListView().setItems(FXCollections.observableArrayList(Controller.queue.keySet()));

            }
        });
        icon1.setOnMouseEntered(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon1.setIconColor(Color.GREENYELLOW);
            }
        });
        icon1.setOnMouseExited(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon1.setIconColor(Color.WHITE);
            }
        });


        icon2.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                Controller.updateListViewColors();

                String key = label.getText();
                DownloaderQueue.moveUp(key);


                getListView().setItems(FXCollections.observableArrayList(Controller.queue.keySet()));

            }
        });
        icon2.setOnMouseEntered(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon2.setIconColor(Color.GREENYELLOW);
            }
        });
        icon2.setOnMouseExited(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon2.setIconColor(Color.WHITE);
            }
        });


        icon3.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {

                Controller.updateListViewColors();

                String key = label.getText();
                int index = getIndex();

                if(!Controller.isUnpacking.getValue()){

                    if (index == 0) {


                        Main.log.info("-> Remove from Queue by user input");
                        try{
                            DownloaderQueue.removeFirst();
                        }catch(NoSuchElementException nse){
                            Main.log.info("Probably download already finished");
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        Controller.downloader.setInterrupted(true);

                        if (Controller.isPaused) {
                            Controller.isPaused = false;
                            Controller.downloader.setPaused(false);
                            Controller.setDownloadingProperty(false);
                            Controller.storedLink = null;
                        }


                        if (CrawlerManager.isCrawlerAlive()) {


                            try {
                                CrawlerManager.killCrawler();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {

                            try {
                                Controller.downloader.closeInputStream();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } else {

                        DownloaderQueue.removeFromQueue(key);
                    }


                    getListView().setItems(FXCollections.observableArrayList(Controller.queue.keySet()));

                }else {
                    Controller.sendNotification("", "");
                }

            }
        });
        icon3.setOnMouseEntered(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon3.setIconColor(Color.DARKRED);
            }
        });
        icon3.setOnMouseExited(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                icon3.setIconColor(Color.WHITE);
            }
        });

    }


    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            label.setTextFill(Color.valueOf("#e4e8ed"));
            label.setText(item);
            int index = getIndex();
            if (index == 0) {
                label.setTextFill(Color.valueOf("#2f6bb5"));
                icon1.setVisible(false);
                icon2.setVisible(false);
                icon3.setVisible(true);
            }else if (index == 1) {
                if (Controller.queue.size() > 2) {
                    icon1.setVisible(true);
                } else {
                    icon1.setVisible(false);
                }
                icon2.setVisible(false);
                icon3.setVisible(true);
            }  else if (index == getListView().getItems().size() - 1) {
                icon1.setVisible(false);
                icon2.setVisible(true);
                icon3.setVisible(true);
            } else {
                icon1.setVisible(true);
                icon2.setVisible(true);
                icon3.setVisible(true);
            }
            setGraphic(hbox);
        }
    }
}