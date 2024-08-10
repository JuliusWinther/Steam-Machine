package com.application.steammachine;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseManager {

    public static List<List<String>> GameList = new ArrayList<List<String>>();

    private static final String APPLICATION_NAME = "5";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    
    private static final List<String> SCOPES =
            Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/com/application/steammachine/credentials.json";

    
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws Exception {

        InputStream in = DatabaseManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        GoogleAuthorizationCodeFlow flow = null;
        try {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        }catch (AccessDeniedException ex){
            Main.log.error("Error encountered while trying to access Google credentials");
            FileUtils.deleteDirectory(new java.io.File(TOKENS_DIRECTORY_PATH));
            getCredentials(HTTP_TRANSPORT);
        }
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    
    public static void fillGameList() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Games_v2!A2:V";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("No data found in SteamTrain");
        } else {
            int r = 0;
            for (List row : values) {
                GameList.add(new ArrayList<String>());
                for(int col = 0; col < 22; col++) {
                    GameList.get(r).add((String) row.get(col));
                }
                r ++;
            }
        }
    }

    public static String getLastSoftwareVersion() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B1";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.warn("No data found in version sheet, returning current version");
            return Launcher.softwareVersion;
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static String getLastSoftwareVersionLink() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B2";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("New version link not found");
            return "";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static String getLastEmuPackVersion() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B7";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.warn("No data found in version sheet, returning current version");
            return "1.0";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static String getEmuPackDownloadLink() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B8";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("Emulator pack link not found");
            return "";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static String getEmuPackConfigDownloadLink() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B9";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("Emulator pack config link not found");
            return "";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static String getEmuPackFirstDownloadLink() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B10";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("Emulator pack first link not found");
            return "";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }


    public static String getSecretFunction() throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "";
        final String range = "Version!B3";
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            Main.log.error("Secret Function Value not Found");
            return "NO";
        } else {
            for (List v : values) {
                return String.valueOf(v.get(0));
            }
        }
        return null;
    }

    public static ArrayList<String> selectColumnFromDB(int columnIndex) {

            ArrayList<String> temp = new ArrayList<String>();
            for(int i = 0; i < GameList.size(); i++)
                temp.add(GameList.get(i).get(columnIndex));

            return temp;

    }

}

