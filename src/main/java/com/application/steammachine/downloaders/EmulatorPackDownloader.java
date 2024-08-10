package com.application.steammachine.downloaders;

import com.application.steammachine.*;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.DownloadCountingOutputStream;
import com.application.steammachine.utils.InternalVar;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ini4j.Wini;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static com.application.steammachine.Controller.*;


public class EmulatorPackDownloader extends Task<Void> {

    {
        updateMessage(" , ");
        updateProgress(0,0);
    }

    private boolean majorUpdate = true;
    private final URL url;
    private DownloadCountingOutputStream dcount = null;
    private OutputStream os = null;
    private InputStream is = null;
    private URL dl = null;
    private File fl = null;
    private boolean interrupted = false;
    private String fileName = "EmulatorPack.rar";



    public EmulatorPackDownloader(URL url, boolean majorUpdate) throws MalformedURLException {

        this.url = url;
        this.majorUpdate = majorUpdate;
        this.interrupted = false;

    }


    
    public class ProgressListener implements ActionListener {

        private double fileSize = 0;
        private double lastMB = 0;
        private long initialTime;
        private double speed = 0;

        public ProgressListener(double fileSize){
            this.fileSize = fileSize;
            initialTime = System.nanoTime();
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            double bytes = ((DownloadCountingOutputStream) e.getSource()).getByteCount();
            updateProgress(bytes, fileSize);
            double mbDownloaded = SteamUtils.round(bytes / 1e+6, 2);

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

            updateMessage(String.valueOf(speed)+"MB/s - " + ((speed  > 0) ? formattedTime : "??:??:??") + ","+"Emulator Update: "+String.valueOf(downloadedText + "/" + sizeText));
        }
    }


    
    @Override
    protected Void call() throws Exception {

        if(majorUpdate)
            emulatorPackDownload();
        else
            emulatorPackConfigUpdate();
        return null;
    }


    

    private void emulatorPackDownload() throws IOException {

        try {

            updateProgress(-1,-1);
            updateMessage("Searching emulator pack files...,Emulator Pack Update Available");


            if ((double) new File(GeneralSettings.getInstallPath()).getUsableSpace() < (2 * InternalVar.bytesInGB)) {

                Controller.sendNotification("", "");

                throw new IOException("Not enough space for proceeding with the installation!");

            }

            try {
                dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(this.url))));
            }catch (NullPointerException np) {
                if (interrupted)
                    throw new InterruptedException();
                else
                    errorProcedure(np, "ERROR,ERROR", true, true, true, true);
            }
            fl = new File(GeneralSettings.getInstallPath() + "/" + this.fileName);
            os = new FileOutputStream(fl);
            is = dl.openStream();

            dcount = new DownloadCountingOutputStream(os);


            double fileSize = 0;
            try {
                fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));
            }catch (Exception ex){}

            ProgressListener progressListener = new ProgressListener(fileSize);
            dcount.setListener(progressListener);


            try{
                IOUtils.copy(is, dcount, 512000);
            }catch (IOException ioex){
                if(interrupted){
                    throw new InterruptedException();
                }else{
                    throw new IOException();
                }
            }

            if(interrupted){
                throw new InterruptedException();
            }

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            updateMessage("Removing old version...,A freeze is normal - be patient");

            if(FileUtils.getFile(new File("./emulators/yuzu")).exists())
                FileUtils.deleteDirectory(new File("./emulators/yuzu"));

            updateMessage("Extracting the emulator package...,Please wait may take a while");



            try {

                Main.log.info("Extracting the archive");
                updateProgress(-1,-1);
                Controller.setUnpackingProperty(true);

                
                RARExtractor.extractBasic(
                        GeneralSettings.getInstallPath() + "/" + this.fileName,
                        "./emulators/",
                        true, false);

                Controller.setUnpackingProperty(false);

            } catch (Exception e) {

                Controller.setUnpackingProperty(false);
                SteamUtils.logError(e);
                errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true);

            }

            updateMessage("Applying custom configs...,Wait a little more");

            File configArchive = new File(System.getenv("appdata") + "/YuzuConfig.rar");
            if (configArchive.exists())
                configArchive.delete();

            try {
                FileUtils.moveFile(new File("./emulators/YuzuConfig.rar"), new File(System.getenv("appdata") + "/YuzuConfig.rar"));
            }catch(Exception e){
                Main.log.error(Throwables.getStackTraceAsString (e));
            }


            try {

                Main.log.info("Extracting the archive");
                updateProgress(-1,-1);
                Controller.setUnpackingProperty(true);

                
                RARExtractor.extractBasic(
                        System.getenv("appdata") + "/YuzuConfig.rar",
                        System.getenv("appdata"),
                        true, false);

                Controller.setUnpackingProperty(false);

            } catch (Exception e) {

                Controller.setUnpackingProperty(false);
                SteamUtils.logError(e);
                errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true);

            }

            configArchive = new File(System.getenv("appdata") + "/YuzuConfig.rar");
            if (configArchive.exists())
                configArchive.delete();

            updateMessage("Concluding...,Almost finished");


            Wini data = new Wini(new File("data.ini"));

            data.load();
            if(Controller.isEmulatorPackageInstalled) {
                data.get("EMULATOR PACKAGE").replace("version", DatabaseManager.getLastEmuPackVersion());
            }else{
                data.add("EMULATOR PACKAGE");
                data.put("EMULATOR PACKAGE", "version", DatabaseManager.getLastEmuPackVersion());
            }
            data.store();

            Controller.isEmulatorPackageInstalled = true;
            Controller.downloadingEmuPack = false;

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            deleteArchives();

            updateMessage(" , ");
            updateProgress(0,0);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Controller.updateListViewColors();
                }
            });

            this.done();

        } catch (Exception interrupted){

            errorProcedure(interrupted, "ERROR,ERROR", true, true, true, true);

        } finally {

            dcount.close();

            DownloaderQueue.removeFirst();

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            Controller.downloadingEmuPack = false;

            updateMessage(" , ");
            updateProgress(0, 0);

            this.cancel(true);

        }
    }

    private void emulatorPackConfigUpdate() throws IOException {

        try {

            updateProgress(-1,-1);
            updateMessage("Searching emulator config files...,Emulator Config Update Available");


            if ((double) new File(GeneralSettings.getInstallPath()).getUsableSpace() < (2 * InternalVar.bytesInGB)) {

                Controller.sendNotification("", "");

                throw new IOException("Not enough space for proceeding with the installation!");

            }

            try {
                dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(this.url))));
            }catch (NullPointerException np) {
                if (interrupted)
                    throw new InterruptedException();
                else
                    errorProcedure(np, "ERROR,ERROR", true, true, true, true);
            }
            fl = new File(GeneralSettings.getInstallPath() + "/YuzuConfig.rar");
            os = new FileOutputStream(fl);
            is = dl.openStream();

            dcount = new DownloadCountingOutputStream(os);


            double fileSize = 0;
            try {
                fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));
            }catch (Exception ex){}

            ProgressListener progressListener = new ProgressListener(fileSize);
            dcount.setListener(progressListener);


            try{
                IOUtils.copy(is, dcount, 512000);
            }catch (IOException ioex){
                if(interrupted){
                    throw new InterruptedException();
                }else{
                    throw new IOException();
                }
            }

            if(interrupted){
                throw new InterruptedException();
            }

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            updateMessage("Applying new custom configs...,Please wait");

            File configArchive = new File(System.getenv("appdata") + "/YuzuConfig.rar");
            if (configArchive.exists())
                configArchive.delete();

            try {
                FileUtils.moveFile(new File(GeneralSettings.getInstallPath() + "/" + "YuzuConfig.rar"), new File(System.getenv("appdata") + "/YuzuConfig.rar"));
            }catch(Exception e){
                Main.log.error(Throwables.getStackTraceAsString (e));
            }


            try {

                Main.log.info("Extracting the archive");
                updateProgress(-1,-1);
                Controller.setUnpackingProperty(true);

                
                RARExtractor.extractBasic(
                        System.getenv("appdata") + "/YuzuConfig.rar",
                        System.getenv("appdata"),
                        true, false);

                Controller.setUnpackingProperty(false);

            } catch (Exception e) {

                Controller.setUnpackingProperty(false);
                SteamUtils.logError(e);
                errorProcedure(e, "EX ERROR,EX ERROR", true, true, true, true);


            }

            configArchive = new File(System.getenv("appdata") + "/YuzuConfig.rar");
            if (configArchive.exists())
                configArchive.delete();

            updateMessage("Concluding...,Almost finished");


            Wini data = new Wini(new File("data.ini"));

            data.load();
            if(Controller.isEmulatorPackageInstalled) {
                data.get("EMULATOR PACKAGE").replace("version", DatabaseManager.getLastEmuPackVersion());
            }else{
                data.add("EMULATOR PACKAGE");
                data.put("EMULATOR PACKAGE", "version", DatabaseManager.getLastEmuPackVersion());
            }
            data.store();

            Controller.isEmulatorPackageInstalled = true;
            Controller.downloadingEmuPack = false;

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            deleteArchives();

            updateMessage(" , ");
            updateProgress(0,0);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Controller.updateListViewColors();
                }
            });

            this.done();

        } catch (Exception interrupted){

            errorProcedure(interrupted, "ERROR,ERROR", true, true, true, true);

        } finally {

            dcount.close();

            DownloaderQueue.removeFirst();

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            Controller.downloadingEmuPack = false;

            updateMessage(" , ");
            updateProgress(0, 0);

            this.cancel(true);

        }
    }

    

    

    private void errorProcedure(Exception e, String txt, boolean killCrawler, boolean removeFirst, boolean showError, boolean killThread) throws IOException {

        Main.log.warn("-> Error Procedure Started");

        if(killCrawler)
            CrawlerManager.killCrawler();

        if(removeFirst)
            DownloaderQueue.removeFirst();

        if (showError)
            SteamUtils.logError(e);

        deleteArchives();

        Controller.isEmulatorPackageInstalled = false;

        updateMessage(txt);
        updateProgress(0, 0);

        if(killThread) {
            this.cancel(true);
            Thread.currentThread().interrupt();
        }

    }

    public void closeInputStream() throws IOException {
        IOUtils.close(is);
    }

    private void deleteArchives() throws IOException {

            File gameArchive = new File(GeneralSettings.getInstallPath() + "/" + this.fileName);
            if (gameArchive.exists())
                gameArchive.delete();

    }


    

    public URL getUrl() {
        return url;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }



}