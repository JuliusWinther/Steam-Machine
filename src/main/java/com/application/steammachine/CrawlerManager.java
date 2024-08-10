package com.application.steammachine;

import com.application.steammachine.settings.GeneralSettings;
import com.application.steammachine.utils.SteamUtils;
import com.google.common.base.Throwables;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.*;
import java.util.Scanner;


public class CrawlerManager {

    static Process process = null;
    static PrintWriter resetter = null;

    static boolean isSearchingLink = false;
    static private boolean killSearch = false;


    public static String executeSteamTrain(String URL) throws IOException, InterruptedException {

        String link = null;


        resetter = new PrintWriter("railroad.txt");
        resetter.print("");
        resetter.close();

        Main.log.info("Initial railroad.txt reset");


        if(!new File("railroad.txt").exists()){

            new File("raildroad.txt").createNewFile();
        }


        try {
            FileWriter writer = new FileWriter("railroad.txt");
            writer.write(URL);
            writer.close();
            Main.log.info("Wrote " + URL + " to railroad.txt");
        } catch (IOException e) {
            SteamUtils.logError(e);
        }


        try {

            try {
                ProcessBuilder processBuilder = new ProcessBuilder("SteamTrain.exe", GeneralSettings.getUseVPN() ? "true" : "false");
                process = processBuilder.start();
                int code = process.waitFor();
            }catch (InterruptedException interrupted){

                Main.log.warn("SteamTrain Interrupted");
            }

        }catch (Exception e) {

            SteamUtils.logError(e);

        }finally {


            try {

                File railroadFile = new File("railroad.txt");
                Scanner reader = new Scanner(railroadFile);

                while (reader.hasNextLine()) {
                    link = reader.nextLine();
                    if(!link.equalsIgnoreCase(URL)) {

                        Main.log.info("Got " + link + " from railroad.txt");
                    }else{

                        Main.log.error("Link not changed in railroad.txt");
                        link = null;

                    }
                }

                reader.close();

            } catch (FileNotFoundException e) {

                SteamUtils.logError(e);
            }

            resetter = new PrintWriter("railroad.txt");
            resetter.print("");
            resetter.close();

            Main.log.info("Second railroad.txt reset");

        }


        return link;

    }

    public static String searchForDirectLink(String contentId) {

        isSearchingLink = true;


        String token = "";

        try {
            if (contentId.contains("https")) {
                Main.log.info("Found direct link (not via API): " + contentId);
                return contentId;
            }

            URL url = new URL("https:
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();


            String responseString = content.toString();
            int startIndex = responseString.indexOf("\"directLink\":") + 14;
            int endIndex = responseString.indexOf("\",", startIndex);
            String directLink = responseString.substring(startIndex, endIndex);


            if(!killSearch) {
                Main.log.info("Found direct link: " + directLink);
                return directLink;
            }else {
                Main.log.warn("Returned NULL because search for direct link was Killed");
                killSearch = false;
                return null;
            }

        } catch (Exception e) {
            Main.log.error("Error Occurred while Searching for the Direct Link");
            killSearch = false;
            e.printStackTrace();
            return null;
        }finally{
            isSearchingLink = false;
            killSearch = false;
        }
    }


    public static boolean isCrawlerAlive_dep(){
        if(process.isAlive()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCrawlerAlive(){
        if(isSearchingLink) {
            return true;
        } else {
            return false;
        }
    }


    public static void killCrawler_dep() throws FileNotFoundException {

        if(process != null)
            if(process.isAlive())
                process.destroyForcibly();

        resetter = new PrintWriter("railroad.txt");
        resetter.print("");
        resetter.close();

        Main.log.info("Resetted railroad.txt with process kill");

    }

    public static void killCrawler() throws FileNotFoundException {

        if(isSearchingLink) {
            killSearch = true;
        } else {
            killSearch = false;
        }

        Main.log.info("Search for Direct Link Killed");

    }

}
