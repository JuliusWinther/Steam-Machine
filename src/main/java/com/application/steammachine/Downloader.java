package com.application.steammachine;

import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.DownloadCountingOutputStream;
import com.application.steammachine.utils.ProgressCallback;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.concurrent.Task;
import mslinks.ShellLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ini4j.Wini;

import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Objects;


public class Downloader extends Task<Void> {

    {
        updateMessage(" , ");
        updateProgress(0,0);
    }

    private final ArrayList<String> url = new ArrayList<String>();
    private final String fileName;
    private final Game game;
    private DownloadCountingOutputStream dcount = null;
    private OutputStream os = null;
    private InputStream is = null;
    private RandomAccessFile outputFile = null;
    private HttpURLConnection connection = null;
    private long fileOffset = 0;
    private URL dl = null;
    private File fl = null;
    private boolean interrupted = false;
    private boolean paused = false;
    private boolean mpError = false;
    private boolean exError = false;
    private boolean downloadOnlyMP = false;

    private boolean isTimeOut = false;

    private ProgressCallback progressCallback = null;

    public Downloader(Game game, boolean downloadOnlyMP) throws MalformedURLException {

        this.game = game;
        this.downloadOnlyMP = downloadOnlyMP;

        this.interrupted = false;
        this.mpError = false;

        this.fileName = game.getArchiveName();

        for(String s : game.getDownloadLink()){
            this.url.add(s);
        }

    }


    
    public class ProgressListener implements ActionListener {

        private double offset = 0;
        private double fileSize = 0;
        private int currentPart = 0;
        private int numberOfParts = 0;
        private double lastMB = 0;
        private long initialTime;
        private double speed = 0;

        public ProgressListener(double fileSize, int currentPart, int numberOfParts, double offset){
            this.fileSize = fileSize;
            this.currentPart = currentPart;
            this.numberOfParts = numberOfParts;
            this.offset = offset;
            initialTime = System.nanoTime();
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            double bytes = ((DownloadCountingOutputStream) e.getSource()).getByteCount();
            updateProgress(offset + bytes, fileSize);
            double mbDownloaded =  SteamUtils.round((offset + bytes) / 1e+6, 2);

            String sizeText = SteamUtils.formatFileDimension(fileSize);

            String downloadedText = "";
            if(mbDownloaded >= 1024){
                downloadedText = String.valueOf(SteamUtils.round(mbDownloaded /1024,2));
            }else{
                downloadedText = String.valueOf(mbDownloaded);
            }

            if((System.nanoTime() - initialTime) >= (Math.pow(10, 9))){
                speed = SteamUtils.round((mbDownloaded - lastMB), 3);
                initialTime = System.nanoTime();
                lastMB = mbDownloaded;
            }

            long totalSec = (long) (SteamUtils.round((fileSize-bytes)/1e+6, 2) / speed);
            long seconds = totalSec % 60;
            long minutes = (totalSec % 3600) / 60;
            long hours = totalSec / 3600;

            String formattedTime = (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":"
                    + (seconds < 10 ? "0" : "") + seconds;

            updateMessage(String.valueOf(speed)+"MB/s" + (numberOfParts > 1 ? " - Part " + currentPart + "/" + numberOfParts : " - " + ((speed  > 0) ? formattedTime : "??:??:??")) +
                    "," + (numberOfParts > 1 ? ((speed  > 0) ? formattedTime : "??:??:??") + " - ": "") + String.valueOf(downloadedText + "/" + sizeText));
        }
    }

    
    @Override
    protected Void call() throws Exception {

        progressCallback = new ProgressCallback() {
            @Override
            public void onProgressUpdate(double progress) {

                updateProgress(progress, 100.00);
            }
        };

        if(!downloadOnlyMP){
            if(this.url.size() == 1) {
                classicDownload();
            }else {

                boolean somethingCorrected = SteamUtils.correctDownloadedParts(game);
                Controller.updateInstallButtonText();
                if (somethingCorrected)
                    Controller.sendNotification("","");

                if(!Controller.objectGameList.get(game.getName()).getDownloadedParts().isEmpty() &&
                    Controller.objectGameList.get(game.getName()).getDownloadedParts().size() == url.size()){
                    launchExtractionOnly();
                }
                multiPartedDownload();
            }
        }else{
            onlyMPDownload();
        }

        return null;
    }

    private Void notInternalCall() throws Exception {

        progressCallback = new ProgressCallback() {
            @Override
            public void onProgressUpdate(double progress) {

                updateProgress(progress, 100.00);
            }
        };

        if(!downloadOnlyMP){
            if(this.url.size() == 1) {
                classicDownload();
            }else {
                if(!Controller.objectGameList.get(game.getName()).getDownloadedParts().isEmpty() &&
                        Controller.objectGameList.get(game.getName()).getDownloadedParts().size() == url.size()){
                    launchExtractionOnly();
                }
                multiPartedDownload();
            }
        }else{
            onlyMPDownload();
        }

        return null;
    }

    private void launchExtractionOnly() throws IOException{

        int numberOfParts = this.url.size();

        Main.log.info("Launching extraction only process for: " + game.getName());

        updateMessage("Verifying downloaded parts...,Please wait");
        if (SteamUtils.fixPartionedDownloadWeightError(game)){
            Controller.storedLink = null;
            Controller.updateTable();
            updateMessage(" , ");
            updateProgress(0,0);
            this.done();
        }

        updateMessage("Extracting the game...,It's not stuck but may take a while");


        try {

            Main.log.info("Extracting the archive");
            updateProgress(-1,-1);
            Controller.setUnpackingProperty(true);

            if(numberOfParts > 1) {
                RARExtractor.extractGame(game, (Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + ".part1.rar"), Controller.objectGameList.get(game.getName()).getInstallPath(), true, false, progressCallback);
            }else{
                RARExtractor.extractGame(game, (Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName), Controller.objectGameList.get(game.getName()).getInstallPath(), true, false, progressCallback);
            }
            Controller.setUnpackingProperty(false);

        } catch (Exception e) {

            Controller.setUnpackingProperty(false);
            SteamUtils.logError(e);
            errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true, 0, true, true);

        }finally{
            resetProgressCallback();
        }

        updateMessage("Concluding...,Almost finished");

        try{

            Wini data = new Wini(new File("data.ini"));

            data.load();
            if (!data.containsKey(game.getName())) {
                data.add(game.getName());
            }
            data.put(game.getName(), "install-path", Controller.objectGameList.get(game.getName()).getInstallPath());
            data.put(game.getName(), "main-folder", game.getGameFolderName());
            data.put(game.getName(), "exe-path", game.getExePath());
            data.put(game.getName(), "saves-path", game.getSaveFilesPath());
            data.put(game.getName(), "game-version", game.getGameVersion());
            data.store();

            Controller.objectGameList.get(game.getName()).setInstalled(true);

            if(!SteamUtils.isGameExePresent(game.getName())) {
                exError = true;
                Controller.sendNotification("", "");
                Controller.objectGameList.get(game.getName()).setInstalled(false);
                SteamUtils.safelyRemoveFromData(game);
            }else{
                Main.log.info("Removing data.ini downloaded parts string from " + game.getName());
                data.load();
                data.get(game.getName()).remove("downloaded-parts");
                data.get(game.getName()).remove("destination-disk");
                data.store();
            }

            for (int part = 1; part <= numberOfParts; part++)
                deleteArchives(false, part, true);

            updateMessage(" , ");
            updateProgress(0,0);

            if(GeneralSettings.isAutoGenerateShortcuts())
                createShortcut(game);

            Controller.storedLink = null;
            Controller.updateTable();

            if(GeneralSettings.isShowInstallNot())
                SteamUtils.launchSecondaryNotification(game.getName());

            this.done();

        }catch (Exception e) {

            errorProcedure(e, "ERROR,ERROR", true, true, true, false, 0, true, true);

        }finally {

            Controller.updateListViewColors();

            if(!paused)
                DownloaderQueue.removeFirst();

        }

    }

    

    private void classicDownload() throws IOException {

        try {

            Controller.setDownloadingProperty(false);


            if ((double) new File(Controller.objectGameList.get(game.getName()).getInstallPath()).getUsableSpace() < (SteamUtils.calculateNecessaryFreeSpace(game))) {

                Controller.sendNotification("", "");

                throw new IOException("Not enough space for proceeding with the installation!");

            }

            updateProgress(-1,-1);
            if(isTimeOut)
                    updateMessage("Trying to restore connection,Please Wait");
                else
                    updateMessage("Searching files...,Please Wait");



            try {
                if (Controller.storedLink != null) {
                    dl = Controller.storedLink;
                    Controller.storedLink = null;
                } else {
                    dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(this.url.get(0)))));
                }
            }catch (NullPointerException np) {
                if (interrupted)
                    throw new InterruptedException();
                else
                    errorProcedure(np, "ERROR,ERROR", true, true, true, false, 1, true, true);
            }

            fl = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName);

            os = new BufferedOutputStream(new FileOutputStream(fl, true));

            Controller.updateListViewColors();

            
            fileOffset = 0;
            if (fl.exists()) {
                fileOffset = fl.length();
            }
            outputFile = new RandomAccessFile(fl, "rw");
            outputFile.seek(fileOffset);

            try {

                connection = (HttpURLConnection) dl.openConnection();
                connection.setRequestProperty("Range", "bytes=" + fileOffset + "-");

                is = connection.getInputStream();

            }catch(SocketTimeoutException sok){
                isTimeOut = true;
                Main.log.warn("CONNECTION TIME OUT - link search - SocketTimeoutException");
                updateMessage("Connection Time Out,Retrying in 5 sec");
                Thread.sleep(5000);
                try {
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
            }catch(UnknownHostException unho){
                isTimeOut = true;
                Main.log.warn("CONNECTION TIME OUT - link search - UnknownHostException");
                updateMessage("Connection Time Out,Retrying in 5 sec");
                Thread.sleep(5000);
                try {
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
            }

            dcount = new DownloadCountingOutputStream(os);
            


            double fileSize = 0;
            try {
                fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));
            }catch (Exception ignored){}

            ProgressListener progressListener = new ProgressListener(fileSize, 1, 1, (double)fileOffset);
            dcount.setListener(progressListener);

            Controller.setDownloadingProperty(true);


            try{
                isTimeOut = false;
                IOUtils.copyLarge(is, dcount);
            }catch(SocketTimeoutException sok){
               isTimeOut = true;
               Main.log.warn("CONNECTION TIME OUT - download");
               updateMessage("Connection Time Out,Retry in progress");
               Thread.sleep(5000);
               try{
                   dcount.close();
                   outputFile.close();
                   IOUtils.closeQuietly(os);
                   IOUtils.closeQuietly(is);
               }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
               this.notInternalCall();
            }catch (IOException ioex) {
                if (interrupted) {
                    throw new InterruptedException();
                } else {
                    throw new IOException();
                }
            }

            if(!paused)
                Controller.setDownloadingProperty(false);


            if(interrupted){

                throw new InterruptedException();
            }

            dcount.close();
            outputFile.close();
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            updateMessage("Extracting the game...,It's not stuck but may take a while");



            try {

                Main.log.info("Extracting the archive");
                updateProgress(-1,-1);





                Controller.setUnpackingProperty(true);
                
                RARExtractor.extractGame(game, Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName, Controller.objectGameList.get(game.getName()).getInstallPath(), true, false, progressCallback);

                Controller.setUnpackingProperty(false);

            } catch (Exception e) {
                Controller.setUnpackingProperty(false);
                SteamUtils.logError(e);
                errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true, 1, true, true);
            }finally{
                resetProgressCallback();
            }

            updateMessage("Concluding...,Almost finished");



                Wini data = new Wini(new File("data.ini"));

                data.load();
                if (!data.containsKey(game.getName())) {
                    data.add(game.getName());
                }

                data.put(game.getName(), "install-path", Controller.objectGameList.get(game.getName()).getInstallPath());
                data.put(game.getName(), "main-folder", game.getGameFolderName());
                data.put(game.getName(), "exe-path", game.getExePath());
                data.put(game.getName(), "saves-path", game.getSaveFilesPath());
                data.put(game.getName(), "game-version", game.getGameVersion());

                data.store();

                Controller.objectGameList.get(game.getName()).setInstalled(true);

            if(!SteamUtils.isGameExePresent(game.getName())) {
                Controller.sendNotification("", "");
                Controller.objectGameList.get(game.getName()).setInstalled(false);
                SteamUtils.safelyRemoveFromData(game);
            }

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            deleteArchives(false, 1, true);

            updateMessage(" , ");
            updateProgress(0,0);

            if(GeneralSettings.isAutoGenerateShortcuts())
                createShortcut(game);

            Controller.storedLink = null;

            Controller.updateTable();

            if(GeneralSettings.isShowInstallNot())
                SteamUtils.launchSecondaryNotification(game.getName());

            Main.log.info(game.getName() + " has been installed correctly.");

            this.done();

        }catch (InterruptedException interrupted){

            errorProcedure(interrupted, "", false, false, false, false, 0, true, true);

        }catch (Exception e) {

            errorProcedure(e, "ERROR,ERROR", true, true, true, false, 1, true, true);

        }finally {

            Controller.updateListViewColors();

            dcount.close();
            outputFile.close();

            if(!paused)
                DownloaderQueue.removeFirst();

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);


        }
    }


    private void multiPartedDownload() throws IOException {

        int numberOfParts = this.url.size();

        try {

            ProgressListener progressListener;
            double fileSize = 0;

            Controller.setDownloadingProperty(false);

            if ((double) new File(Controller.objectGameList.get(game.getName()).getInstallPath()).getUsableSpace()
                    <= (SteamUtils.calculateNecessaryFreeSpace(game))) {

                    Controller.sendNotification("");

                throw new IOException("Not enough space for proceeding with the installation!");

            }

            for(int i = 1; i <= numberOfParts; i++){

                try {

                    updateProgress(-1, -1);
                    if(isTimeOut)
                        updateMessage("Trying to restore connection,Please Wait");
                    else
                        updateMessage("Searching part " + i + " files...,Please Wait");

                    Wini data = new Wini(new File("data.ini"));

                    SteamUtils.correctDownloadedParts(game);

                    if(!Controller.objectGameList.get(game.getName()).getDownloadedParts().isEmpty() &&
                            Controller.objectGameList.get(game.getName()).getDownloadedParts().contains(String.valueOf(i))){
                        continue;
                    }

                    try {
                        if(Controller.storedLink != null) {
                            dl = Controller.storedLink;
                            Controller.storedLink = null;
                        }else {
                            dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(this.url.get(i - 1)))));
                        }
                    } catch (NullPointerException np) {
                        if (interrupted) {
                            throw new InterruptedException();
                        }else {
                            int finalI = i;

                            Controller.sendNotification("", "");

                            errorProcedure(np, "ERROR,ERROR", true, false, true, false, i, true, false);
                            if (finalI == this.url.size())
                                return;
                            else
                                continue;
                        }
                    }

                    fl = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + ".part" + (i) + ".rar");

                    os = new BufferedOutputStream(new FileOutputStream(fl, true));

                    Controller.updateListViewColors();

                    
                    fileOffset = 0;
                    if (fl.exists()) {
                        fileOffset = fl.length();
                    }
                    outputFile = new RandomAccessFile(fl, "rw");
                    outputFile.seek(fileOffset);

                    try{
                        connection = (HttpURLConnection) dl.openConnection();
                        connection.setRequestProperty("Range", "bytes=" + fileOffset + "-");
                        is = connection.getInputStream();
                    }catch(SocketTimeoutException sok){
                        isTimeOut = true;
                        Main.log.warn("CONNECTION TIME OUT - link search - SocketTimeoutException");
                        break;
                    }catch(UnknownHostException unho){
                        isTimeOut = true;
                        Main.log.warn("CONNECTION TIME OUT - link search - UnknownHostException");
                        break;
                    }

                    dcount = new DownloadCountingOutputStream(os);
                    


                    try {
                        fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));
                    } catch (Exception ex) {}

                    progressListener = new ProgressListener(fileSize, i, numberOfParts, (double)fileOffset);
                    dcount.setListener(progressListener);

                    if (interrupted)
                        throw new InterruptedException();

                    Controller.setDownloadingProperty(true);


                    try{
                        isTimeOut = false;
                        IOUtils.copyLarge(is, dcount);
                    }catch(SocketTimeoutException sok){
                        isTimeOut = true;
                        Main.log.warn("CONNECTION TIME OUT - download");
                        updateMessage("Connection Time Out,Retry in progress");
                        Thread.sleep(5000);
                        try {
                            dcount.close();
                            outputFile.close();
                            IOUtils.closeQuietly(os);
                            IOUtils.closeQuietly(is);
                        }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                        this.notInternalCall();
                        return;
                    }catch (IOException ioex){
                        if(interrupted){
                            throw new InterruptedException();
                        }else{
                            throw new IOException();
                        }
                    }

                    if(!paused)
                        Controller.setDownloadingProperty(false);


                    if (interrupted) {
                        throw new InterruptedException();
                    }

                    updateMessage("Saving part " + i + "...,Proceeding...");




                    data.load();
                    if(!data.containsKey(game.getName()))
                        data.add(game.getName());
                    if(data.get(game.getName()).containsKey("downloaded-parts"))
                        data.put(game.getName(), "downloaded-parts", data.get(game.getName(), "downloaded-parts") + "," + i);
                    else
                        data.put(game.getName(), "downloaded-parts", i);
                    data.store();

                    Controller.objectGameList.get(game.getName()).getDownloadedParts().add(String.valueOf(i));

                    Controller.updateListViewColors();

                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);

                    Controller.storedLink = null;

                    Controller.updateTable();

                    if ((double) new File(Controller.objectGameList.get(game.getName()).getInstallPath()).getUsableSpace()
                            <= (SteamUtils.calculateNecessaryFreeSpace(game))) {

                        Controller.sendNotification("", "");

                        throw new InterruptedException("Not enough space for proceeding with the installation!");

                    }

                }catch (InterruptedException interrupted){

                    errorProcedure(interrupted, "", false, false, false, false, 0, true, false);
                    break;

                }catch (Exception e) {

                    errorProcedure(e, "ERROR,ERROR", true, false, true, false, i, true, false);
                    if(!interrupted) {
                        int finalI = i;
                        Controller.sendNotification("", "");
                    }
                   if (i >= game.getDownloadLink().size()){
                        break;
                    }

                }

            }

            if(isTimeOut){
                updateMessage("Connection Time Out,Retrying in 5 sec");
                Thread.sleep(5000);
                try {
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
                return;
            }

            if (interrupted) {
                throw new InterruptedException();
            }

            if(game.getDownloadedParts().size() == numberOfParts) {

                dcount.close();
                outputFile.close();
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);

                updateMessage("Verifying downloaded parts...,Please wait");
                if (SteamUtils.fixPartionedDownloadWeightError(game)){
                    Controller.storedLink = null;
                    Controller.updateTable();
                    updateMessage(" , ");
                    updateProgress(0,0);
                    this.done();
                }

                updateMessage("Extracting the game...,It's not stuck but may take a while");


                try {

                    Main.log.info("Extracting the archive");
                    updateProgress(-1,-1);
                    Controller.setUnpackingProperty(true);

                   
                    RARExtractor.extractGame(game, (Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + ".part1.rar"), Controller.objectGameList.get(game.getName()).getInstallPath(), true, false, progressCallback);
                    Controller.setUnpackingProperty(false);

                } catch (Exception e) {

                    Controller.setUnpackingProperty(false);
                    SteamUtils.logError(e);
                    errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true, 0, true, true);

                }finally{
                    resetProgressCallback();
                }

                updateMessage("Concluding...,Almost finished");


                Wini data = new Wini(new File("data.ini"));

                data.load();
                if (!data.containsKey(game.getName())) {
                    data.add(game.getName());
                }
                data.put(game.getName(), "install-path", Controller.objectGameList.get(game.getName()).getInstallPath());
                data.put(game.getName(), "main-folder", game.getGameFolderName());
                data.put(game.getName(), "exe-path", game.getExePath());
                data.put(game.getName(), "saves-path", game.getSaveFilesPath());
                data.put(game.getName(), "game-version", game.getGameVersion());
                data.store();

                Controller.objectGameList.get(game.getName()).setInstalled(true);

                if(!SteamUtils.isGameExePresent(game.getName())) {
                    exError = true;
                    Controller.sendNotification("", "");
                    Controller.objectGameList.get(game.getName()).setInstalled(false);
                    SteamUtils.safelyRemoveFromData(game);
                }else{
                    Main.log.info("Removing data.ini downloaded parts string from " + game.getName());
                    data.load();
                    data.get(game.getName()).remove("downloaded-parts");
                    data.get(game.getName()).remove("destination-disk");
                    data.store();
                }

                dcount.close();
                outputFile.close();
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);

                for (int part = 1; part <= numberOfParts; part++)
                    deleteArchives(false, part, true);

            }else{

                Controller.sendNotification("", "");

            }

            updateMessage(" , ");
            updateProgress(0,0);

            if(GeneralSettings.isAutoGenerateShortcuts())
                createShortcut(game);

            Controller.storedLink = null;

            Controller.updateTable();

            if(GeneralSettings.isShowInstallNot())
                SteamUtils.launchSecondaryNotification(game.getName());

            Main.log.info(game.getName() + " has been installed correctly.");

            this.done();

        } catch (InterruptedException interrupted){

            errorProcedure(interrupted, "", false, false, false, false, 0, true, true);

        }catch (Exception e) {

            errorProcedure(e, "ERROR,ERROR", true, true, true, false, 0, true, true);

        }finally {

            Controller.updateListViewColors();
            Controller.updateInstallButtonText();

            dcount.close();
            outputFile.close();

            if(!paused)
                DownloaderQueue.removeFirst();

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

        }
    }


    private void onlyMPDownload() throws IOException {

        try {


            if ((double) new File(Controller.objectGameList.get(game.getName()).getInstallPath()).getUsableSpace() < (4e+8)) {

                Controller.sendNotification("", "");

                throw new IOException("Not enough space for proceeding with the installation!");

            }

            updateProgress(-1,-1);
            if(isTimeOut)
                updateMessage("Trying to restore connection,Please Wait");
            else
                updateMessage("Searching fix files...,Please Wait");



            try {
                dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(game.getMultiplayerType()))));
            }catch (NullPointerException np) {
                if (interrupted)
                    throw new InterruptedException();
                else
                    errorProcedure(np, "FIX ERROR,FIX ERROR", true, true, true, false, 0, true, true);
            }

            fl = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + (this.fileName.split("\\.")[0]) + "-fix.rar");

            os = new BufferedOutputStream(new FileOutputStream(fl, true));

            Controller.updateListViewColors();

            
            fileOffset = 0;
            if (fl.exists()) {
                fileOffset = fl.length();
            }
            outputFile = new RandomAccessFile(fl, "rw");
            outputFile.seek(fileOffset);

            try {

                connection = (HttpURLConnection) dl.openConnection();
                connection.setRequestProperty("Range", "bytes=" + fileOffset + "-");

                is = connection.getInputStream();

            }catch(SocketTimeoutException sok){
                isTimeOut = true;
                Main.log.warn("CONNECTION TIME OUT - fix link search - SocketTimeoutException");
                updateMessage("Connection Time Out,Retrying in 5 sec");
                Thread.sleep(5000);
                try {
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
            }catch(UnknownHostException unho){
                isTimeOut = true;
                Main.log.warn("CONNECTION TIME OUT - fix link search - UnknownHostException");
                updateMessage("Connection Time Out,Retrying in 5 sec");
                Thread.sleep(5000);
                try {
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
            }

            dcount = new DownloadCountingOutputStream(os);
            


            double fileSize = 0;
            try {
                fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));
            }catch (Exception ignored){}

            ProgressListener progressListener = new ProgressListener(fileSize, 1, 1, (double)fileOffset);
            dcount.setListener(progressListener);

            Controller.setDownloadingProperty(true);


            try{
                isTimeOut = false;
                IOUtils.copyLarge(is, dcount);
            }catch(SocketTimeoutException sok){
                isTimeOut = true;
                Main.log.warn("CONNECTION TIME OUT - fix download");
                updateMessage("Connection Time Out,Retry in progress");
                Thread.sleep(5000);
                try{
                    dcount.close();
                    outputFile.close();
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }catch(NullPointerException nullpointer){SteamUtils.logError(nullpointer);}
                this.notInternalCall();
            }catch (IOException ioex) {
                if (interrupted) {
                    throw new InterruptedException();
                } else {
                    throw new IOException();
                }
            }

            if(!paused)
                Controller.setDownloadingProperty(false);


            if(interrupted){

                throw new InterruptedException();
            }

            dcount.close();
            outputFile.close();
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);


            if (!game.getFixInstallPath().equalsIgnoreCase("/")) {
                updateMessage("Applying fix...,Please wait");

                try {

                    Main.log.info("Extracting the fix archive");
                    updateProgress(-1,-1);

                    Controller.setUnpackingProperty(true);

                    RARExtractor.extractWithCallback(
                            (Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + "-fix.rar"),
                            Controller.objectGameList.get(game.getName()).getInstallPath() + game.getFixInstallPath(),
                            true, false, progressCallback);

                    Controller.setUnpackingProperty(false);

                } catch (Exception e) {
                    Controller.setUnpackingProperty(false);
                    SteamUtils.logError(e);
                    errorProcedure(e, "FIX EX ERROR,FIX EX ERROR", true, true, true, false, 0, true, true);
                }finally{
                    resetProgressCallback();
                }


            }

            updateMessage("Concluding...,Almost finished");

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            deleteArchives(false, 0, true);

            updateMessage(" , ");
            updateProgress(0,0);

            Controller.updateTable();

            Main.log.info(game.getName() + " has been fixed correctly.");

            this.done();

        }catch (InterruptedException interrupted){

            errorProcedure(interrupted, "", false, false, false, false, 0, true, true);

        }catch (Exception e) {

            errorProcedure(e, "ERROR,ERROR", true, true, true, false, 1, true, true);

        }finally {

            Controller.updateListViewColors();

            dcount.close();
            outputFile.close();

            if(!paused)
                DownloaderQueue.removeFirst();

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

        }

    }


    

    

    private void errorProcedure(Exception e, String txt, boolean killCrawler, boolean removeFirst, boolean showError, boolean deleteFolder, int deleteGamePart, boolean deleteMPArchive, boolean killThread) throws IOException {

        if(!paused)
            Main.log.warn("-> Error Procedure Started");
        else
            Main.log.warn("-> Pause Procedure Started");

        if(killCrawler)
            CrawlerManager.killCrawler();

        if(removeFirst)
            DownloaderQueue.removeFirst();

        if (showError)
            SteamUtils.logError(e);

        deleteArchives(deleteFolder, deleteGamePart, deleteMPArchive);

        updateMessage(txt);

        if(!paused)
            updateProgress(0, 0);

        if(killThread) {
            this.cancel(true);
            Thread.currentThread().interrupt();
        }

    }

    public void closeInputStream() throws IOException {
        IOUtils.close(is);
    }

    private void deleteArchives(boolean deleteFolder, int deleteGamePart, boolean deleteMPArchive) throws IOException {

        if (deleteFolder)
            if(FileUtils.getFile(new File(game.getInstallPath() + game.getGameFolderName())).exists()) {
                Main.log.info("Deleting " + game.getGameFolderName() + " as requested by the downloader class.");
                FileUtils.deleteDirectory(new File(game.getInstallPath() + game.getGameFolderName()));
            }

        if(deleteGamePart != 0) {
            File gameArchive;
            if(game.getDownloadLink().size() == 1) {
                gameArchive = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName);
            }else{
                gameArchive = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + ".part" + (deleteGamePart) + ".rar");
            }
            if (gameArchive.exists())
                gameArchive.delete();
        }


        if(deleteMPArchive) {
            if (!game.getFixInstallPath().equalsIgnoreCase("/")) {
                File mpArchive = new File(Controller.objectGameList.get(game.getName()).getInstallPath() + "/" + this.fileName.split("\\.")[0] + "-fix.rar");
                if (mpArchive.exists())
                    mpArchive.delete();
            }
        }
    }

    private void createShortcut(Game game){
        FileSystemView view = FileSystemView.getFileSystemView();
        File file = view.getHomeDirectory();
        String path = file.getPath();

        try {
            ShellLink.createLink(game.getInstallPath() + game.getGameFolderName() + game.getExePath(),
                    path + "/" + game.getName().replaceAll("[^a-zA-Z0-9]", " ") + ".lnk");
        }catch (Exception e){
            Main.log.error(Throwables.getStackTraceAsString(e));
        }
    }



    

    public ArrayList<String> getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public Game getGame() {
        return game;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public URL getDirectLink() {
        return dl;
    }

    public void setDirectLink(URL dl) {
        this.dl = dl;
    }

    public void resetProgressCallback(){
        progressCallback = new ProgressCallback() {
            @Override
            public void onProgressUpdate(double progress) {

                updateProgress(progress, 100.00);
            }
        };
    }





}