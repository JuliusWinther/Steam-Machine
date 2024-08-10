package com.application.steammachine.utils;

import com.application.steammachine.*;
import com.application.steammachine.settings.GeneralSettings;
import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ini4j.Wini;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamUtils {


    public static boolean isCurrentGameArchivePresent(){

        if(new File(
                Controller.objectGameList.get(Controller.selectedGame.getName()).getInstallPath() + "/"
                        +  Controller.objectGameList.get(Controller.selectedGame.getName()).getArchiveName()).exists()){
            return true;
        }else{
            return false;
        }

    }



    public static boolean isGameArchivePresent(String gameName){

        if(new File(
                Controller.objectGameList.get(gameName).getInstallPath() + "/"
                        +  Controller.objectGameList.get(gameName).getArchiveName()).exists()){
            return true;
        }else{
            return false;
        }

    }



    public static void logError(Exception e){
        Main.log.error(Throwables.getStackTraceAsString(e));
    }



    public static boolean isCurrentGameExePresent(){

        if(new File(
                Controller.objectGameList.get(Controller.selectedGame.getName()).getInstallPath() + "/"
                        +  Controller.objectGameList.get(Controller.selectedGame.getName()).getGameFolderName() + "/"
                        + Controller.objectGameList.get(Controller.selectedGame.getName()).getExePath()).exists()){
            return true;
        }else{
            return false;
        }

    }



    public static boolean isGameExePresent(String gameName){

        if(new File(
                Controller.objectGameList.get(gameName).getInstallPath()
                        +  Controller.objectGameList.get(gameName).getGameFolderName()
                        + Controller.objectGameList.get(gameName).getExePath()).exists()){
            return true;
        }else{
            return false;
        }

    }



    public static String convertSavesPath(Game game){

        String path = game.getSaveFilesPath();
        String finalPath = "";
        String env = "";

        try{
            env = path.split("%")[1];
        } catch (Exception e) {

        }

        if(!env.equals("")){
            if(path.contains("Users")){
                try{
                    finalPath = (path.split("%")[0] +
                            System.getenv(path.split("%")[1].toUpperCase()) + path.split("%")[2] + "/").replace("%", "");
                }catch(Exception e){
                    logError(e);
                }
            }else if(path.charAt(0) == '%'){
                try{
                    finalPath = (System.getenv(path.split("%")[1].toUpperCase())
                            + path.split("%")[2] + "/").replace("%", "");
                }catch(Exception e){
                    logError(e);
                }
            }
        }else{
            try{
                finalPath = ( path.charAt(0) == '^' ? game.getInstallPath() +
                        path.replace("^", "") + "/" : path + "/");
            }catch(Exception e){
                logError(e);
            }
        }

        Main.log.info("Generated saves path: " + finalPath);
        return finalPath;

    }



    public static void addKeyToGameData(Game game, String key, String value) throws IOException {

        Wini data = new Wini(new File("data.ini"));

        data.load();
        if(data.containsKey(game.getName())){
            if(data.get(game.getName()).containsKey(key)) {
                data.get(game.getName()).replace(key, value);
            }else {
                data.put(game.getName(), key, value);
            }
        }else {
            data.put(game.getName(), key, value);
        }

        data.store();

    }



    public static double getFileSize(String path){

        try {
            if (new File(path).exists())
                return new File(path).length();
            else
                throw new FileNotFoundException();

        }catch (Exception e){
            logError(e);
        }

        return 0;

    }



    public static String formatFileDimension(double fileSize){

        if (fileSize >= 1073741824) {
            double temp = ((fileSize / 1e+6) / 1024);
            return round(temp, 2) + " GB";
        } else {
            double temp = (fileSize / 1e+6);
            return round(temp, 2) + " MB";
        }

    }



    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }



    public static void safelyRemoveFromData(Game game) throws IOException {

        String keep_LocalBackupData = "";
        String keep_DriveBackupID = "";
        String keep_DriveBackupData = "";
        String keep_Bookmarked = "";



        Wini data = new Wini(new File("data.ini"));

        data.load();

        if(data.containsKey(game.getName())) {

            if (data.get(game.getName()).containsKey("local-backup-date"))
                keep_LocalBackupData = data.get(game.getName()).get("local-backup-date");

            if (data.get(game.getName()).containsKey("drive-backup-id"))
                keep_DriveBackupID = data.get(game.getName()).get("drive-backup-id");

            if (data.get(game.getName()).containsKey("drive-backup-date"))
                keep_DriveBackupData = data.get(game.getName()).get("drive-backup-date");

            if (data.get(game.getName()).containsKey("bookmarked"))
                keep_Bookmarked = data.get(game.getName()).get("bookmarked");







            data.remove(game.getName());

        }

        data.store();
        data.load();

        if(!keep_LocalBackupData.equalsIgnoreCase("") ||
                !keep_DriveBackupID.equalsIgnoreCase("")||
                !keep_DriveBackupData.equalsIgnoreCase("") ||
                !keep_Bookmarked.equalsIgnoreCase("")


                ){

            data.add(game.getName());

            if(!keep_LocalBackupData.equalsIgnoreCase(""))
                data.put(game.getName(), "local-backup-date", keep_LocalBackupData);
            if(!keep_DriveBackupID.equalsIgnoreCase(""))
                data.put(game.getName(), "drive-backup-id", keep_DriveBackupID);
            if(!keep_DriveBackupData.equalsIgnoreCase(""))
                data.put(game.getName(), "drive-backup-date", keep_DriveBackupData);
            if(!keep_Bookmarked.equalsIgnoreCase(""))
                data.put(game.getName(), "bookmarked", keep_Bookmarked);





        }

        data.store();

    }

    public static void addDestinationDiskToData(String gameName, String disk) throws IOException {

        Wini data = new Wini(new File("data.ini"));

        data.load();

        data.put(gameName, "destination-disk", disk);

        data.store();
    }

    public static String getDestinationDiskFromData(String gameName) throws IOException {

        Wini data = new Wini(new File("data.ini"));

        data.load();
        if(data.containsKey(gameName))
            if(!data.get(gameName).containsKey("destination-disk"))
                return null;
            else
                return data.get(gameName).get("destination-disk");
        else
            return null;

    }



    public static void openInNewWindow(String fxmlFile, String title, double width, double height){

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(fxmlFile)));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(Main.mainStage);
            stage.setTitle(title);
            stage.getIcons().add(new Image(Objects.requireNonNull(Objects.requireNonNull(SteamUtils.class).getResourceAsStream("/com/application/steammachine/images/logo.png"))));
            stage.setScene(new Scene(root, width, height));

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset[0] = event.getSceneX();
                    yOffset[0] = event.getSceneY();
                }
            });
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset[0]);
                    stage.setY(event.getScreenY() - yOffset[0]);
                }
            });

            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);

            stage.show();

        } catch (IOException e) {
            logError(e);
        }

    }



    public static double calculateNecessaryFreeSpace(Game game) throws MalformedURLException {

        ArrayList<String> url = new ArrayList<String>();

        for(String s : game.getDownloadLink()){
            url.add(s);
        }

        double dwnDim = 0;
        double downloaded = 0;
        double offset = 0;

        if(game.getDownloadDimension().contains("MB"))
            dwnDim = (Double.parseDouble(game.getDownloadDimension().split(" ")[0])) * 1048576.0;
        else if(game.getDownloadDimension().contains("GB"))
            dwnDim = (Double.parseDouble(game.getDownloadDimension().split(" ")[0])) * 1073741824.0;

        if(!Controller.objectGameList.get(game.getName()).getDownloadedParts().isEmpty() &&
                Controller.objectGameList.get(game.getName()).getDownloadedParts().size() > 0){

            downloaded += Controller.objectGameList.get(game.getName()).getDownloadedParts().size() * (5 * InternalVar.bytesInGB);

            if(new File(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" +
                    (Controller.objectGameList.get(game.getName()).getDownloadedParts().size() + 1) + ".rar").exists()){

                downloaded += getFileSize(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" +
                        (Controller.objectGameList.get(game.getName()).getDownloadedParts().size() + 1) + ".rar");

            }

        }else {

            if (new File(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part1.rar").exists()) {
                downloaded += getFileSize(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0]
                        + ".part1.rar");

            }

        }

        if(isGameArchivePresent(game.getName())){

            downloaded += getFileSize(game.getInstallPath() + "/" + game.getArchiveName());

        }

        if(url.size() == 1){
            offset = 100;
        }else{
            if (GeneralSettings.isDeleteExtractedParts())
                offset = 20;
            else
                offset = 100;
        }

        double result = (dwnDim + ((dwnDim*offset)/100) + (url.size() == 1 ? 0 : (5 * InternalVar.bytesInGB))) - (downloaded);
        Main.log.info(" - Occupato:" + formatFileDimension(dwnDim) + ", Scaricato: " + formatFileDimension(downloaded)
                + ", Ancora Richiesto: " + formatFileDimension(result));
        return result;

    }



    public static String searchFile(File folder, String fileName) {

        try {

            if(folder.exists()) {


                for (File file : Objects.requireNonNull(folder.listFiles())) {


                    if (file.isDirectory()) {
                        String percorsoFile = searchFile(file, fileName);


                        if (percorsoFile != null) {
                            return percorsoFile;
                        }
                    }

                    else if (file.getName().equals(fileName)) {
                        return file.getAbsolutePath();
                    }
                }

            }

        }catch(Exception e){
            logError(e);
        }


        return null;
    }



    public static void launchSecondaryNotification(String text){


        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported! Interrupting notification launch.");
            return;
        }


        SystemTray tray = SystemTray.getSystemTray();


        java.awt.Image image = Toolkit.getDefaultToolkit().createImage("com/application/steammachine/images/logo.png");


        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("System tray icon demo");


        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Main.log.error("TrayIcon could not be added.");
            return;
        }





        trayIcon.displayMessage("Steam-Machine", text + " Installato!", TrayIcon.MessageType.INFO);






















































    }


    public static boolean needsEmulator(Game game){

        String type = game.getTypeOfFile().replaceAll(" ", "");

        if(type.equalsIgnoreCase("N64") || type.equalsIgnoreCase("GBA") || type.equalsIgnoreCase("NDS")
        || type.equalsIgnoreCase("3DS") || type.equalsIgnoreCase("wii") || type.equalsIgnoreCase("switch")
        || type.equalsIgnoreCase("PS1") || type.equalsIgnoreCase("PS2")){
            return true;
        }else {
            return false;
        }

    }



    public static boolean isFolderExcluded(String folderPath) throws IOException, InterruptedException {

    String command = "powershell.exe";
    String script = "$exclusionPaths = (Get-MpPreference).ExclusionPath; $exclusionPaths -contains '" + folderPath + "'";


    ProcessBuilder processBuilder = new ProcessBuilder(command, script);


    processBuilder.redirectErrorStream(true);


    Process process = processBuilder.start();


    int exitCode = process.waitFor();


    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }


        Main.log.info("Output:\n" + output);


        return exitCode == 0 && output.toString().trim().equalsIgnoreCase("True");
    }
}



    public static void addFolderExclusion(String folderPath) throws IOException, InterruptedException {

        String command = "powershell.exe";
        String script = "$folderPath='" + folderPath + "'; Add-MpPreference -ExclusionPath $folderPath";


        ProcessBuilder processBuilder = new ProcessBuilder(command, script);


        processBuilder.redirectErrorStream(true);


        Process process = processBuilder.start();


        int exitCode = process.waitFor();


        java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
        String output = s.hasNext() ? s.next() : "";


        Main.log.info("Output:\n" + output);
        Main.log.info("Exit Code: " + exitCode);


        if (exitCode != 0) {
            throw new IOException("Error adding folder exclusion. Exit code: " + exitCode);
        }
    }



    public static boolean fixPartionedDownloadWeightError(Game game) throws IOException {

        long minWeight = 4800000000L;

        if(game.getName().replace(" ", "").equalsIgnoreCase("SteamWorldDig"))
            minWeight = 0L;

        boolean deletedSomething = false;

        ArrayList<String> toRemove = new ArrayList<>();

        if(game.getDownloadLink().size() > 1){

            for (String part : game.getDownloadedParts()) {

                if(!(Integer.valueOf(part) == game.getDownloadedParts().size())) {

                    Main.log.info("Verifying weight of part: " + part);
                    Main.log.info("Part path: " + Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" + (part) + ".rar");

                    if (getFileSize(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" + (part) + ".rar")
                            <= minWeight) {

                        if (new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" + (part) + ".rar").exists()) {
                            new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + game.getArchiveName().split("\\.")[0] + ".part" + (part) + ".rar").delete();
                            Main.log.warn("Attention, part " + part + " has not been downloaded completely, proceeding to removal");
                        }

                        Wini data = new Wini(new File("data.ini"));

                        StringBuilder downloadedPartsDataString = new StringBuilder();

                        for(int count = 1; count <= Controller.objectGameList.get(game.getName()).getDownloadedParts().size(); count++) {
                            if (!new File(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0]
                                    + ".part" + (count) + ".rar").exists()){

                                if (!toRemove.contains(String.valueOf(count)))
                                    toRemove.add(String.valueOf(count));

                            }else{
                                if(downloadedPartsDataString.toString().equalsIgnoreCase(""))
                                    downloadedPartsDataString.append(count);
                                else
                                    downloadedPartsDataString.append(",").append(count);
                            }
                        }

                        data.load();
                        if(data.containsKey(game.getName()))
                            if(data.get(game.getName()).containsKey("downloaded-parts"))
                                data.get(game.getName()).replace("downloaded-parts", downloadedPartsDataString.toString());
                        data.store();

                        if (!toRemove.contains(part))
                            toRemove.add(part);

                        Controller.sendNotification("", "");
                        deletedSomething = true;

                    }
                }

            }

            for (String part : toRemove){
                game.getDownloadedParts().remove(part);
            }

        }

        Controller.updateListViewColors();
        Controller.updateTable();
        Controller.updateInstallButtonText();

        return deletedSomething;

    }


    public static String normalizeString(String input) {

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);


        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized;
    }


    public static boolean correctDownloadedParts(Game game) throws IOException {

        if(game.getDownloadLink().size() > 1) {

            Wini data = new Wini(new File("data.ini"));

            boolean somethingRemoved = false;

            StringBuilder downloadedPartsDataString = new StringBuilder();
            ArrayList<String> partsToRemove = new ArrayList<String>();

            for (int count = 1; count <= Controller.objectGameList.get(game.getName()).getDownloadedParts().size(); count++) {
                if (!new File(game.getInstallPath() + "/" + game.getArchiveName().split("\\.")[0]
                        + ".part" + (count) + ".rar").exists()) {

                    partsToRemove.add(String.valueOf(count));

                } else {
                    if (downloadedPartsDataString.toString().equalsIgnoreCase(""))
                        downloadedPartsDataString.append(count);
                    else
                        downloadedPartsDataString.append(",").append(count);
                }
            }

            if (!partsToRemove.isEmpty()) {
                Controller.objectGameList.get(game.getName()).getDownloadedParts().removeAll(partsToRemove);
                somethingRemoved = true;
            }

            data.load();
            if (data.containsKey(game.getName()))
                if (data.get(game.getName()).containsKey("downloaded-parts"))
                    data.get(game.getName()).replace("downloaded-parts", downloadedPartsDataString.toString());
            data.store();

            return somethingRemoved;
        }
            return false;

    }


    public static String greatStrip(String input) {
        input = input.strip().replace(" ", "").toLowerCase();
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\s]");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }


    public static boolean hasHeaderCache(String gameName){
        String[] imageFormats = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

        File folder = new File("./cache/images");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("hasCache() func failed: ./images folder does not exist or is empty.");
            return false;
        }

        for (String format : imageFormats) {
            File imageFile = new File(folder, SteamUtils.greatStrip(gameName) + format);
            if (imageFile.exists()) {
                return true;
            }
        }

        return false;
    }


    public static Image getHeaderCache(String gameName){
        String[] imageFormats = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

        File folder = new File("./cache/images");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("hasCache() func failed: ./cache/images folder does not exist or is empty.");
            return null;
        }

        for (String format : imageFormats) {
            File imageFile = new File(folder, SteamUtils.greatStrip(gameName) + format);
            if (imageFile.exists()) {
                return new Image("file:" + imageFile.getAbsolutePath());
            }
        }

        return null;
    }


    public static File getDetailsFromCache(String gameId) {

        File folder = new File("./cache/details");
        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println("hasCache() func failed: ./cache/details folder does not exist or is empty.");
            return null;
        }

        if(gameId.equalsIgnoreCase("0"))
        {
            return null;
        }

        if (new File("./cache/details/"+gameId+".json").exists()) {

            File jsonFile = new File("./cache/details/"+gameId+".json");
            if (jsonFile.exists()) {
                return new File(jsonFile.getAbsolutePath());
            } else {
                return null;
            }
        }

        return null;
    }

    public static boolean isDetailCachingSuccess(String gameName) throws FileNotFoundException {

        File jsonFile = new File("./cache/details/"+Controller.objectGameList.get(gameName).getId()+".json");

        String jsonData = "";
        Scanner myReader = new Scanner(jsonFile);
        while (myReader.hasNextLine()) {
            jsonData = myReader.nextLine();
        }
        myReader.close();


        JSONObject jsonResponse = new JSONObject(jsonData);


        return jsonResponse.getJSONObject(Controller.objectGameList.get(gameName).getId()).getBoolean("success");
    }

    public static HashMap<String, ArrayList<String>> readDetailsJson(String gameName) throws FileNotFoundException {

        HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>();

        File jsonFile = new File("./cache/details/"+Controller.objectGameList.get(gameName).getId()+".json");

        String jsonData = "";
        Scanner myReader = new Scanner(jsonFile);
        while (myReader.hasNextLine()) {
            jsonData = myReader.nextLine();
        }
        myReader.close();


        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(jsonData);
        }catch(Exception e){
            jsonFile.delete();
            return null;
        }


        JSONObject data = null;
        try {
            data = jsonResponse.getJSONObject(Controller.objectGameList.get(gameName).getId()).getJSONObject("data");
        }catch(JSONException noData){
            jsonFile.delete();
            return null;
        }
        ArrayList<String> tmpA = new ArrayList<String>();
        tmpA.add(data.getString("short_description"));
        temp.putIfAbsent("shortDesc", tmpA);

        tmpA = new ArrayList<String>();
        tmpA.add(data.getString("about_the_game"));
        temp.putIfAbsent("desc", tmpA);

        tmpA = new ArrayList<String>();
        tmpA.add(data.getJSONObject("pc_requirements").getString("minimum"));
        temp.putIfAbsent("req", tmpA);

        tmpA = new ArrayList<String>();
        try {
            tmpA.add(data.getJSONObject("price_overview").getString("final_formatted"));
        }catch(Exception ignored){}
        tmpA.add("");
        temp.putIfAbsent("price", tmpA);

        tmpA = new ArrayList<String>();
        String tmp = "";
        try {
            tmp += data.getJSONArray("genres").getJSONObject(0).getString("description");
            tmp += ", " + data.getJSONArray("genres").getJSONObject(1).getString("description");
        }catch (Exception ignored){}
        tmpA.add(tmp);
        temp.putIfAbsent("genres", tmpA);

        tmpA = new ArrayList<String>();
        tmpA.add(data.getString("header_image").split("\\?")[0]);
        temp.putIfAbsent("small-header", tmpA);

        tmpA = new ArrayList<String>();
        try {
            for(int i = 0; i < 30; i++) {
                String value = data.getJSONArray("screenshots").getJSONObject(i).getString("path_thumbnail");
                tmpA.add(value);
            }
        }catch (Exception ignored){}
        temp.putIfAbsent("imgs_thumbs", tmpA);

        tmpA = new ArrayList<String>();
        try {
            for(int i = 0; i < 30; i++) {
                String value = data.getJSONArray("screenshots").getJSONObject(i).getString("path_full");
                tmpA.add(value);
            }
        }catch (Exception ignored){}
        temp.putIfAbsent("imgs", tmpA);

        tmpA = new ArrayList<String>();
        try {
            for(int i = 0; i < 30; i++) {
                String value = data.getJSONArray("movies").getJSONObject(i).getString("thumbnail");
                tmpA.add(value);
            }
        }catch (Exception ignored){}
        temp.putIfAbsent("movies_thumbs", tmpA);

        tmpA = new ArrayList<String>();
        try {
            for(int i = 0; i < 30; i++) {
                String value = data.getJSONArray("movies").getJSONObject(i).getJSONObject("mp4").getString("max");
                tmpA.add(value);
            }
        }catch (Exception ignored){}
        temp.putIfAbsent("movies", tmpA);
        tmpA = new ArrayList<String>();


        return temp;

    }

    public static File downloadDetails(String gameId) {

        HashMap<String, ArrayList<String>> temp = new HashMap<String, ArrayList<String>>();

        String apiUrl = "https:
        String fileName = gameId + ".json";
        File outputFolder = new File("./cache/details");
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        File outputFile = new File(outputFolder, fileName);


        try (InputStream inputStream = new URL(apiUrl).openStream()) {
            byte[] newJsonBytes = IOUtils.toByteArray(inputStream);


            FileUtils.writeByteArrayToFile(outputFile, newJsonBytes);
            Main.log.info("JSON for game ID " + gameId + " downloaded successfully (on request).");
            return new File("./cache/details/"+gameId+".json");
        } catch (Exception e) {
            Main.log.error("Error while caching JSON for game ID " + gameId + ": " + e.getMessage());
            return null;
        }
    }



    public static File searchFolder(File directory, String folderName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && Objects.equals(file.getName(), folderName)) {
                    return file;
                } else if (file.isDirectory()) {
                    File found = searchFolder(file, folderName);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }


    public static void overrideForceLanguageFiles(File directory, String text) throws IOException {
        Files.walk(Paths.get(directory.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .filter(path -> path.toFile().getName().equals("force_language.txt"))
                .forEach(path -> {
                    try (FileWriter writer = new FileWriter(path.toFile())) {
                        writer.write(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }


    public static void openConsole(){

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("console.fxml")));
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(Main.mainStage);
            stage.setTitle("Console");
            stage.getIcons().add(new Image(Objects.requireNonNull(Objects.requireNonNull(SteamUtils.class).getResourceAsStream("/com/application/steammachine/images/logo.png"))));
            stage.setScene(new Scene(root, 900, 800));

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset[0] = event.getSceneX();
                    yOffset[0] = event.getSceneY();
                }
            });
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset[0]);
                    stage.setY(event.getScreenY() - yOffset[0]);
                }
            });

            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);

            stage.show();

        } catch (IOException e) {
            logError(e);
        }

    }

    public static void openHome(){

        final double[] xOffset = {0};
        final double[] yOffset = {0};

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("new-games-view-v2.fxml")));
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(Main.mainStage);
            stage.setTitle("Novità");
            stage.getIcons().add(new Image(Objects.requireNonNull(Objects.requireNonNull(SteamUtils.class).getResourceAsStream("/com/application/steammachine/images/logo.png"))));
            stage.setScene(new Scene(root, 1280, 510));

            root.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    xOffset[0] = event.getSceneX();
                    yOffset[0] = event.getSceneY();
                }
            });
            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stage.setX(event.getScreenX() - xOffset[0]);
                    stage.setY(event.getScreenY() - yOffset[0]);
                }
            });

            stage.setResizable(false);
            stage.initStyle(StageStyle.UNDECORATED);

            stage.show();

        } catch (IOException e) {
            logError(e);
        }

    }


    public static String removeLink(String input) {

        Document doc = Jsoup.parse(input);

        Element link = doc.selectFirst("a");
        if (link != null) {

            link.remove();

            return doc.body().html();
        } else {

            return input;
        }
    }

    public static void shutdownThreads(){

        if(Controller.downloader != null) {
            Controller.downloader.setInterrupted(true);
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
                Controller.downloadThread.cancel(true);
            }
        }

        StatusUpdater.interrupt = true;

        Controller.downloaderThread.shutdown();
        Controller.interfaceUpdaterThread.shutdown();
        Controller.queueThread.shutdownNow();
        Controller.imageCacherThread.shutdown();
        Controller.detailsCacherThread.shutdown();
        Controller.galleryLoaderThread.shutdown();
        Controller.descriptionLoaderThread.shutdown();

        Controller.downloaderThread.shutdownNow();
        Controller.interfaceUpdaterThread.shutdownNow();
        Controller.queueThread.shutdownNow();
        Controller.imageCacherThread.shutdownNow();
        Controller.detailsCacherThread.shutdownNow();
        Controller.galleryLoaderThread.shutdownNow();
        Controller.descriptionLoaderThread.shutdownNow();

    }

    public static boolean areSimilar(String str1, String str2) {

        String cleanStr1 = cleanString(str1);
        String cleanStr2 = cleanString(str2);


        boolean containsDigit1 = containsDigit(cleanStr1);
        boolean containsDigit2 = containsDigit(cleanStr2);
        if ((containsDigit1 && !containsDigit2) || (!containsDigit1 && containsDigit2)) {
            return false;
        }


        if (Math.abs(cleanStr1.length() - cleanStr2.length()) > 1) {
            return false;
        }


        int differences = 0;
        int length = Math.min(cleanStr1.length(), cleanStr2.length());
        boolean digitDifferenceFound = false;
        for (int i = 0; i < length; i++) {
            boolean isDigit1 = Character.isDigit(cleanStr1.charAt(i));
            boolean isDigit2 = Character.isDigit(cleanStr2.charAt(i));

            if (isDigit1 != isDigit2) {
                return false;
            }

            if (isDigit1 && isDigit2) {
                if (cleanStr1.charAt(i) != cleanStr2.charAt(i)) {
                    if (digitDifferenceFound) {
                        return false;
                    }
                    digitDifferenceFound = true;
                }
            } else {
                if (cleanStr1.charAt(i) != cleanStr2.charAt(i)) {
                    differences++;
                }
                if (differences > 1) {
                    return false;
                }
            }
        }


        return true;
    }


    private static boolean containsDigit(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }



    private static String cleanString(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }


    public static boolean levenshtein(String str1, String str2) {

        str1 = str1.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        str2 = str2.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();


        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(str1.charAt(i - 1), str2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[str1.length()][str2.length()] <= 1;
    }


    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }


    private static int min(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

}
