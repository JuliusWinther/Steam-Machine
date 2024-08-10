package com.application.steammachine.downloaders;

import com.application.steammachine.*;
import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.DownloadCountingOutputStream;
import com.application.steammachine.utils.InternalVar;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Objects;

public class SoftwareUpdateDownloader extends Task<Void> {

    {
        updateMessage("- , -");
    }

    public static boolean interrupt;

    private final URL url;

    public SoftwareUpdateDownloader(URL url) {
        this.url = url;
        interrupt = false;
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

            updateMessage(String.valueOf(speed)+"MB/s - " + ((speed  > 0) ? formattedTime : "??:??:??") + ","+"Software Update: "+String.valueOf(downloadedText + "/" + sizeText));
        }
    }

    @Override
    protected Void call() throws Exception {

        URL dl;
        File fl;
        OutputStream os = null;
        InputStream is = null;

        try {

            updateMessage("Software Update Available,Searching files...");


            if ((double) new File(GeneralSettings.getInstallPath()).getUsableSpace() < (InternalVar.bytesInGB)) {

                Controller.sendNotification("", "");

                Thread.sleep(20000);

                SteamUtils.shutdownThreads();

                Platform.exit();
                System.exit(1);


                throw new IOException("Not enough space for proceeding with the installation!");

            }

            dl = new URL(Objects.requireNonNull(CrawlerManager.searchForDirectLink(String.valueOf(this.url))));
            fl = new File("./UpdatedVersion.rar");
            os = new FileOutputStream(fl);
            is = dl.openStream();

            DownloadCountingOutputStream dcount = new DownloadCountingOutputStream(os);

            double fileSize = Double.parseDouble(dl.openConnection().getHeaderField("Content-Length"));

            ProgressListener progressListener = new ProgressListener(fileSize);
            dcount.setListener(progressListener);

            IOUtils.copy(is, dcount, 512000 );

            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);

            updateMessage("Concluding update...,Almost finished");



            try {

                ProcessBuilder processBuilder = new ProcessBuilder("./elevate.exe", "-c", "Mechanic.exe", DatabaseManager.getLastSoftwareVersion());
                Process process = processBuilder.start();
            }catch (Exception e){
                SteamUtils.logError(e);
            }

            Thread.sleep(1000);

            SteamUtils.shutdownThreads();

            Platform.exit();
            System.exit(1);

            updateMessage(" , ");

        } catch (Exception e) {

            SteamUtils.logError(e);
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
            updateMessage("UPDATING ERROR, UPDATING ERROR");
            new File("./UpdatedVersion.rar").delete();
            this.cancel(true);
            this.done();

        } finally {

            new File("/.UpdatedVersion.rar").delete();

            updateProgress(0, 0);
            this.cancel(true);
            this.done();

        }

        return null;
    }

}