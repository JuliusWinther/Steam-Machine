package com.application.steammachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.ProgressCallback;
import com.application.steammachine.utils.SteamUtils;

public class RARExtractor {

    public static long totalSize = 0;
    public static long extractedSize = 0;

    private static Process process;

    private static String getRegexGroup(String pattern, String input, int group) {
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(group);
        } else {
            return null;
        }
    }


    public static void extractBasic(String archive, String archiveDest, boolean verbose, boolean unsafe) {

        Main.mainStage.setOnCloseRequest(event -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        }));

        List<String> finishedArchives = new ArrayList<>();
        String currentArchive = null;
        String currentFile = null;
        String finishedFile = null;

        totalSize = 0;
        extractedSize = 0;

        if (new File(archive.split("\\.")[0] + ".part1.rar").exists()){
            archive = archive.split("\\.")[0] + ".part1.rar";
        }
        if (new File(archive.split("\\.")[0] + ".part01.rar").exists()){
            archive = archive.split("\\.")[0] + ".part01.rar";
        }


        totalSize = new File(archive).length();


        List<String> command = new ArrayList<>();
        command.add("UnRAR.exe");
        command.add("x");
        command.add("-o+");
        command.add("");//-pPassword if the archive is protected by one
        command.add(archive);
        command.add(archiveDest);

        System.out.println("unrar command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();




                if (line.isEmpty()) {
                    continue;
                }


                if (verbose) {
                    System.out.println("unrar: \"" + line + "\"");
                }


                if (line.matches("^.*Corrupt\\s+file\\s+or\\s+wrong\\s+password\\.\\s*$") ||
                        line.matches("^\\s*The\\s+specified\\s+password\\s+is\\s+incorrect\\.\\s*$")) {
                    System.out.println("ERROR: Wrong password! Aborting...");
                    process.destroy();
                    System.exit(1);
                }


                String currentArchiveMatch = getRegexGroup("^\\s*Extracting\\s+from\\s+(.*?)$", line, 1);
                if (currentArchiveMatch != null && !currentArchiveMatch.equals(currentArchive)) {
                    if (unsafe) {
                        if (currentArchive != null) {
                            System.out.println("Extracting from " + currentArchiveMatch + ", deleting " + currentArchive);


                        } else {
                            System.out.println("Extracting from " + currentArchiveMatch);
                        }
                    } else {
                        if (currentArchive != null) {
                            finishedArchives.add(currentArchive);
                        }
                        System.out.println("Extracting from " + currentArchiveMatch);
                        System.out.println("Finished parts: " + String.join(", ", finishedArchives));
                    }
                    currentArchive = currentArchiveMatch;
                }


                String currentFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\[\\b])+\\d+%)+(?:\\s|\\[\\b])*$", line, 1);
                if (currentFileMatch != null && !currentFileMatch.equals(currentFile)) {
                    currentFile = currentFileMatch;
                    System.out.println("Extracting " + currentFile);
                }


                String finishedFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\u0008)+\\d+%)*(?:\\s|\\u0008)*OK\\s*$", line, 1);
                if (!unsafe && finishedFileMatch != null) {
                    finishedFile = finishedFileMatch;
                    System.out.println("Finished extracting " + finishedFile);
                    if (!finishedArchives.isEmpty() && GeneralSettings.isDeleteExtractedParts()) {
                        System.out.println("Deleting parts: " + String.join(", ", finishedArchives));

                        finishedArchives.forEach(file -> new File(file).delete());
                        finishedArchives.clear();
                    } else {
                        System.out.println("Nothing to delete");
                    }
                }

            }

            finishedArchives.add(currentArchive);
            System.out.println("Extraction finished");
            System.out.println("Deleting remaining parts: " + String.join(", ", finishedArchives));

            try {
                finishedArchives.forEach(file -> new File(file).delete());
            }catch(NullPointerException np){}

        } catch (IOException e) {

            e.printStackTrace();

        }finally {
            return;
        }

    }
    public static void extractWithCallback(String archive, String archiveDest, boolean verbose, boolean unsafe, ProgressCallback progressCallback) {

        Main.mainStage.setOnCloseRequest(event -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        }));

        List<String> finishedArchives = new ArrayList<>();
        String currentArchive = null;
        String currentFile = null;
        String finishedFile = null;

        double totalFiles = 0;
        int extractedFiles = 0;
        double progress = 0;

        if (new File(archive.split("\\.")[0] + ".part1.rar").exists()){
            archive = archive.split("\\.")[0] + ".part1.rar";
        }
        if (new File(archive.split("\\.")[0] + ".part01.rar").exists()){
            archive = archive.split("\\.")[0] + ".part01.rar";
        }

        boolean checkingParts = true;
        int numberOfParts = 1;
        if(archive.contains(".part")) {
            int partsCounter = 2;
            while (checkingParts) {
                if (new File(archive.split(".part")[0]+".part"+partsCounter+".rar").exists()){
                    partsCounter ++;
                }else{
                    checkingParts = false;
                }
            }
            numberOfParts = partsCounter-1;
        }else{
            checkingParts = false;
        }

        while(numberOfParts>0) {

            int partitionFiles = 0;

            List<String> listCommand = new ArrayList<>();
            listCommand.add("UnRAR.exe");
            listCommand.add("l");
            listCommand.add("-pImperoCanziani1933");
            if(archive.contains(".part")) {
                listCommand.add(archive.split(".part")[0]+".part"+numberOfParts+".rar");
            }else{
                listCommand.add(archive);
            }

            System.out.println("Used UnRAR File List Command: " + String.join(" ", listCommand));

            ProcessBuilder listProcessBuilder = new ProcessBuilder(listCommand);
            listProcessBuilder.redirectErrorStream(true);

            try {
                process = listProcessBuilder.start();
                BufferedReader listReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";

                while ((line = listReader.readLine()) != null) {
                    if (line.startsWith("*")) {
                        partitionFiles ++;
                        totalFiles++;
                    }
                }

                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }finally{
                if(archive.contains(".part")) {
                    Main.log.info("Files found in partition: " + partitionFiles);
                    partitionFiles = 0;
                }
                numberOfParts --;
            }

        }

        Main.log.info("Total files in archive: " + totalFiles);


        List<String> command = new ArrayList<>();
        command.add("UnRAR.exe");
        command.add("x");
        command.add("-o+");
        command.add("-pImperoCanziani1933");
        command.add(archive);
        command.add(archiveDest);

        System.out.println("Used UnRAR Command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();




                if (line.isEmpty()) {
                    continue;
                }


                if (verbose) {
                    System.out.println("unrar: \"" + line + "\"");
                }


                if (line.matches("^.*Corrupt\\s+file\\s+or\\s+wrong\\s+password\\.\\s*$") ||
                        line.matches("^\\s*The\\s+specified\\s+password\\s+is\\s+incorrect\\.\\s*$")) {
                    Main.log.error("ERROR: Wrong password! Aborting...");
                    process.destroy();
                    System.exit(1);
                }


                String currentArchiveMatch = getRegexGroup("^\\s*Extracting\\s+from\\s+(.*?)$", line, 1);
                if (currentArchiveMatch != null && !currentArchiveMatch.equals(currentArchive)) {
                    if (unsafe) {
                        if (currentArchive != null) {
                            System.out.println("Extracting from " + currentArchiveMatch + ", deleting " + currentArchive);


                        } else {
                            System.out.println("Extracting from " + currentArchiveMatch);
                        }
                    } else {
                        if (currentArchive != null) {
                            finishedArchives.add(currentArchive);
                        }
                        System.out.println("Extracting from " + currentArchiveMatch);
                        System.out.println("Finished parts: " + String.join(", ", finishedArchives));
                    }
                    currentArchive = currentArchiveMatch;
                }


                String currentFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\[\\b])+\\d+%)+(?:\\s|\\[\\b])*$", line, 1);
                if (currentFileMatch != null && !currentFileMatch.equals(currentFile)) {
                    currentFile = currentFileMatch;
                    System.out.println("Extracting " + currentFile);
                }


                String finishedFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\u0008)+\\d+%)*(?:\\s|\\u0008)*OK\\s*$", line, 1);
                if (!unsafe && finishedFileMatch != null) {
                    finishedFile = finishedFileMatch;
                    extractedFiles++;
                    progress = SteamUtils.round((extractedFiles * 100.00)/totalFiles, 3);
                    System.out.println("Finished extracting " + finishedFile + " - progress: " + String.valueOf(progress) + "%");
                    Main.log.info("<> " + extractedFiles + " -- Finished extracting " + finishedFile + " - progress: " + String.valueOf(progress) + "%");
                    progressCallback.onProgressUpdate(progress);
                    if (!finishedArchives.isEmpty() && GeneralSettings.isDeleteExtractedParts()) {
                        System.out.println("Deleting parts: " + String.join(", ", finishedArchives));

                        finishedArchives.forEach(file -> new File(file).delete());
                        finishedArchives.clear();
                    } else {
                        System.out.println("Nothing to delete");
                    }
                }

            }


            finishedArchives.add(currentArchive);
            System.out.println("Extraction finished");
            System.out.println("Deleting remaining parts: " + String.join(", ", finishedArchives));

            try {
                finishedArchives.forEach(file -> new File(file).delete());
            }catch(NullPointerException np){}

        } catch (IOException e) {

            e.printStackTrace();
            Main.log.error(e.getMessage());

        }finally {
            progressCallback = null;
            return;
        }

    }
    public static void extractGame(Game game, String archive, String archiveDest, boolean verbose, boolean unsafe, ProgressCallback progressCallback) {


        Main.mainStage.setOnCloseRequest(event -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            if (process != null) {
                process.destroy();
                process.destroyForcibly();
            }
        }));

        List<String> finishedArchives = new ArrayList<>();
        String currentArchive = null;
        String currentFile = null;
        String finishedFile = null;

        double totalFiles = 0;
        int extractedFiles = 0;
        double progress = 0;

        if (new File(archive.split("\\.")[0] + ".part1.rar").exists()){
            archive = archive.split("\\.")[0] + ".part1.rar";
        }
        if (new File(archive.split("\\.")[0] + ".part01.rar").exists()){
            archive = archive.split("\\.")[0] + ".part01.rar";
        }

        boolean checkingParts = true;
        int numberOfParts = 1;
        if(archive.contains(".part")) {
            int partsCounter = 2;
            while (checkingParts) {
                if (new File(archive.split(".part")[0]+".part"+partsCounter+".rar").exists()){
                    partsCounter ++;
                }else{
                    checkingParts = false;
                }
            }
            numberOfParts = partsCounter-1;
        }else{
            checkingParts = false;
        }

        while(numberOfParts>0) {

            int partitionFiles = 0;

            List<String> listCommand = new ArrayList<>();
            listCommand.add("UnRAR.exe");
            listCommand.add("l");
            listCommand.add("");//-Password if the archive is protected by one
            if(archive.contains(".part")) {
                listCommand.add(archive.split(".part")[0]+".part"+numberOfParts+".rar");
            }else{
                listCommand.add(archive);
            }

            System.out.println("Used UnRAR File List Command: " + String.join(" ", listCommand));

            ProcessBuilder listProcessBuilder = new ProcessBuilder(listCommand);
            listProcessBuilder.redirectErrorStream(true);

            try {
                process = listProcessBuilder.start();
                BufferedReader listReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";

                while ((line = listReader.readLine()) != null) {
                    if (line.startsWith("*")) {
                        partitionFiles ++;
                        totalFiles++;
                    }
                }

                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }finally{
                if(archive.contains(".part")) {
                    Main.log.info("Files found in partition: " + partitionFiles);
                    partitionFiles = 0;
                }
                numberOfParts --;
            }

        }

        Main.log.info("Total files in archive: " + totalFiles);


        List<String> command = new ArrayList<>();
        command.add("UnRAR.exe");
        command.add("x");
        command.add("-o+");
        command.add(""); //-pPassword if the archive is protected by one
        command.add(archive);
        command.add(archiveDest);

        System.out.println("Used UnRAR Command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();




                if (line.isEmpty()) {
                    continue;
                }


                if (verbose) {
                    System.out.println("unrar: \"" + line + "\"");
                }


                if (line.matches("^.*Corrupt\\s+file\\s+or\\s+wrong\\s+password\\.\\s*$") ||
                        line.matches("^\\s*The\\s+specified\\s+password\\s+is\\s+incorrect\\.\\s*$")) {
                    Main.log.error("ERROR: Wrong password! Aborting...");
                    process.destroy();
                    System.exit(1);
                }


                String currentArchiveMatch = getRegexGroup("^\\s*Extracting\\s+from\\s+(.*?)$", line, 1);
                if (currentArchiveMatch != null && !currentArchiveMatch.equals(currentArchive)) {
                    if (unsafe) {
                        if (currentArchive != null) {
                            System.out.println("Extracting from " + currentArchiveMatch + ", deleting " + currentArchive);


                        } else {
                            System.out.println("Extracting from " + currentArchiveMatch);
                        }
                    } else {
                        if (currentArchive != null) {
                            finishedArchives.add(currentArchive);
                        }
                        System.out.println("Extracting from " + currentArchiveMatch);
                        System.out.println("Finished parts: " + String.join(", ", finishedArchives));
                    }
                    currentArchive = currentArchiveMatch;
                }


                String currentFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\[\\b])+\\d+%)+(?:\\s|\\[\\b])*$", line, 1);
                if (currentFileMatch != null && !currentFileMatch.equals(currentFile)) {
                    currentFile = currentFileMatch;
                    System.out.println("Extracting " + currentFile);
                }


                String finishedFileMatch = getRegexGroup("^\\s*(?:Extracting|\\.+)+\\s+(.*?)(?:(?:\\s|\\u0008)+\\d+%)*(?:\\s|\\u0008)*OK\\s*$", line, 1);
                if (!unsafe && finishedFileMatch != null) {
                    finishedFile = finishedFileMatch;
                    extractedFiles++;
                    progress = SteamUtils.round((extractedFiles * 100.00) /totalFiles, 3);
                    System.out.println("Finished extracting " + finishedFile + " - progress: " + String.valueOf(progress) + "%");
                    Main.log.info("<> " + extractedFiles + " -- Finished extracting " + finishedFile + " - progress: " + String.valueOf(progress) + "%");
                    progressCallback.onProgressUpdate(progress);
                    if (!finishedArchives.isEmpty() && GeneralSettings.isDeleteExtractedParts()) {
                        System.out.println("Deleting parts: " + String.join(", ", finishedArchives));

                        finishedArchives.forEach(file -> new File(file).delete());
                        SteamUtils.correctDownloadedParts(game);
                        finishedArchives.clear();
                    } else {
                        System.out.println("Nothing to delete");
                    }
                }

            }


            finishedArchives.add(currentArchive);
            System.out.println("Extraction finished");
            System.out.println("Deleting remaining parts: " + String.join(", ", finishedArchives));

            try {
                finishedArchives.forEach(file -> new File(file).delete());
            }catch(NullPointerException np){}

        } catch (IOException e) {

            e.printStackTrace();
            Main.log.error(e.getMessage());

        }finally {
            progressCallback = null;
            return;
        }

    }

}