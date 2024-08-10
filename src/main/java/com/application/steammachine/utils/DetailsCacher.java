package com.application.steammachine.utils;

import com.application.steammachine.Controller;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetailsCacher extends Task<Void> {

    private static final String DETAILS_FOLDER = "./cache/details";
    private static final long DOWNLOAD_DELAY_MS = 1550;

    public static int presentCache = 0;

    @Override
    protected Void call() throws Exception {
        Main.log.info("-> DetailsCacher Process Started");
        cacheDetails();
        return null;
    }

    private void cacheDetails() {

        List<Game> gameList = new ArrayList<>(Controller.objectGameList.values());


        List<Game> uncachedGames = new ArrayList<>();
        List<Game> cachedGames = new ArrayList<>();


        for (Game game : gameList) {
            String gameId = game.getId();
            if (gameId != null && !gameId.strip().equals("0")) {
                if (SteamUtils.getDetailsFromCache(gameId) == null) {
                    uncachedGames.add(game);
                } else {
                    cachedGames.add(game);
                }
            }
        }


        Collections.shuffle(cachedGames);


        List<Game> shuffledGameList = new ArrayList<>();
        shuffledGameList.addAll(uncachedGames);
        shuffledGameList.addAll(cachedGames);


        for (Game game : shuffledGameList) {
            try {
                String gameId = game.getId();
                if (gameId != null && !gameId.strip().equals("0")) {
                    cacheDetail(gameId, game.getName());
                    ImageCacher.downloadHeaderImage(game.getName(), Controller.objectGameList.get(game.getName()).getImgURL());

                    TimeUnit.MILLISECONDS.sleep(DOWNLOAD_DELAY_MS);
                }
            } catch (InterruptedException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Main.log.info("<- DetailsCacher Process Ended");
        this.done();
    }

    private void cacheDetail(String gameId, String gameName) throws IOException {
        String apiUrl = "https:
        String fileName = gameId + ".json";
        File outputFolder = new File(DETAILS_FOLDER);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        File outputFile = new File(outputFolder, fileName);


        try (InputStream inputStream = new URL(apiUrl).openStream()) {
            byte[] newJsonBytes = IOUtils.toByteArray(inputStream);


            String newHash = computeHash(newJsonBytes);


            if (outputFile.exists()) {
                byte[] existingJsonBytes = FileUtils.readFileToByteArray(outputFile);
                String existingHash = computeHash(existingJsonBytes);
                if (newHash.equals(existingHash)) {

                    return;
                }
            }


            if (!outputFile.exists()) {
                FileUtils.writeByteArrayToFile(outputFile, newJsonBytes);
                presentCache ++;
                Main.log.info("JSON for game ID " + gameId + " downloaded successfully.");
            }else{
                FileUtils.writeByteArrayToFile(outputFile, newJsonBytes);

            }

            if(!SteamUtils.isDetailCachingSuccess(gameName)) {
                SteamUtils.downloadDetails(Controller.objectGameList.get(gameName).getId());
                TimeUnit.MILLISECONDS.sleep(1000);
            }

            if(!SteamUtils.isDetailCachingSuccess(gameName)){
                outputFile.delete();
                return;
            }

            Controller.applyDetailsFromCache(gameName, SteamUtils.readDetailsJson(gameName));
        } catch (Exception e) {
            Main.log.error("Error while caching JSON for game ID " + gameId + ": " + e.getMessage());
        }
    }

    private String computeHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Main.log.error("Error computing hash: " + e.getMessage());
            return null;
        }
    }
}
