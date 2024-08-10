package com.application.steammachine;

import com.application.steammachine.utils.InternalVar;
import com.application.steammachine.utils.SteamUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class DownloaderQueue extends Task<Void> {

    protected SimpleDoubleProperty progress = new SimpleDoubleProperty();
    protected SimpleStringProperty message = new SimpleStringProperty(" , ");

    public DownloaderQueue() {
    }

    @Override
    protected Void call() throws Exception {

        while(true) {


            LinkedHashMap<String, Game> localQueue = Controller.queue;

            if(!localQueue.isEmpty()) {


                try {

                    String key = localQueue.entrySet().stream().findFirst().get().getKey();
                    Game game = localQueue.entrySet().stream().findFirst().get().getValue();

                    if(!Controller.isPaused) {

                        if (Controller.downloadThread == null) {


                            Main.log.info("Beginning " + game.getName() + " installation process, (thread == null)");

                            Controller.downloader =
                                    new Downloader(game, key.contains(" - Update"));

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progress.bind(Controller.downloader.progressProperty());
                                    message.bind(Controller.downloader.messageProperty());
                                }
                            });


                            Controller.downloadThread = Controller.downloaderThread.submit(Controller.downloader);

                        } else if (Controller.downloadThread.isDone() || Controller.downloadThread.isCancelled()) {


                            Controller.downloaderThread = Executors.newSingleThreadExecutor();

                            Main.log.info("Beginning " + game.getName() + " installation process, (thread.isDone())");

                            Controller.downloader =
                                    new Downloader(game, key.contains(" - Update"));

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progress.bind(Controller.downloader.progressProperty());
                                    message.bind(Controller.downloader.messageProperty());
                                }
                            });


                            Controller.downloadThread = Controller.downloaderThread.submit(Controller.downloader);

                        } else {

                        }

                    }

                }catch (Exception e) {
                    SteamUtils.logError(e);
                }

            }else {
                if(progress.isBound())
                    progress.unbind();
                if(message.isBound())
                    message.unbind();
                updateProgress(0, 0);
                updateMessage(" , ");

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progress.set(0);
                        message.set(" , ");}
                });

            }


            Thread.sleep(75);

        }

    }

    


    public static synchronized void addToQueue(Game game, boolean onlyMP){

        if(Controller.queue.size() < InternalVar.QUEUE_SIZE) {


            if(!onlyMP) {


                if(Controller.queue.containsKey(game.getName()))
                    Controller.sendNotification("", "");

                Controller.queue.putIfAbsent(game.getName(), game);
            }else{


                if(Controller.queue.containsKey(game.getName() + " - Update"))
                    Controller.sendNotification("", "");

                Controller.queue.putIfAbsent(game.getName() + " - Update", game);
            }

        }else {


            Controller.sendNotification("", "");

        }

    }


    public static synchronized void removeFromQueue(String key){
        Controller.queue.remove(key);
    }

    public static synchronized void removeFromQueue(Game game){
        Controller.queue.remove(game.getName());
    }


    public static synchronized void removeFirst(){
        String key = Controller.queue.entrySet().stream().findFirst().get().getKey();
        removeFromQueue(key);
    }


    public static synchronized String getFirst(){
        String key = Controller.queue.entrySet().stream().findFirst().get().getKey();
        return key;
    }


    public static synchronized void moveDown(String key){
        Iterator<Map.Entry<String, Game>> it = Controller.queue.entrySet().iterator();
        Map.Entry<String, Game> next = null;
        while (it.hasNext()) {
            Map.Entry<String, Game> entry = it.next();
            if (entry.getKey().equalsIgnoreCase(key)) {
                if (it.hasNext()) {
                    next = it.next();
                    break;
                }
            }
        }

        LinkedHashMap<String, Game> tempMap = new LinkedHashMap<String, Game>();

        if (next != null) {
            for(String s: Controller.queue.keySet()){
                if(next.getKey().equalsIgnoreCase(s)){

                    continue;
                }
                if(key.equalsIgnoreCase(s)){
                    tempMap.putIfAbsent(next.getKey(), next.getValue());
                }
                tempMap.putIfAbsent(s, Controller.queue.get(s));
            }
        }

        Controller.queue = tempMap;


    }


    public static synchronized void moveUp(String key){
        Iterator<Map.Entry<String, Game>> it = Controller.queue.entrySet().iterator();
        Map.Entry<String, Game> previous = null;
        while (it.hasNext()) {
            Map.Entry<String, Game> entry = it.next();
            if (entry.getKey().equalsIgnoreCase(key)) {
                break;
            }
            previous = entry;
        }

        LinkedHashMap<String, Game> tempMap = new LinkedHashMap<String, Game>();

        if (previous != null) {
            for(String s : Controller.queue.keySet()){
                if(key.equalsIgnoreCase(s)){
                    continue;
                }
                if(s.equalsIgnoreCase(previous.getKey())){
                    tempMap.putIfAbsent(key, Controller.queue.get(key));
                }
                tempMap.putIfAbsent(s, Controller.queue.get(s));
            }
        }

        Controller.queue = tempMap;

    }

}


