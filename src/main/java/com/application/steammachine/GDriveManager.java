package com.application.steammachine;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GDriveManager {

    private static final String APPLICATION_NAME = "";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    
    private static final List<String> SCOPES =
            Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY, DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/com/application/steammachine/credentials.json";

    private static final String FOLDER_NAME = "Steam-Machine Save Files";
    private static final String MIME_FOLDER = "application/vnd.google-apps.folder";
    private static final String MIME_FILE = "application/octet-stream";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        try {
            InputStream in = DatabaseManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }catch(Exception ex){
            if(new java.io.File(TOKENS_DIRECTORY_PATH).exists()){
                new java.io.File(TOKENS_DIRECTORY_PATH).delete();
            }
            return null;
        }
    }

    public static String uploadFile(java.io.File archive, String id) throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();


        String folderId = null;
        FileList result = service.files().list()
                .setQ("mimeType='" + MIME_FOLDER + "' and trashed = false and name='" + FOLDER_NAME + "'")
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        for (File file : result.getFiles()) {
            if (file.getName().equals(FOLDER_NAME)) {
                folderId = file.getId();
                break;
            }
        }


        if (folderId == null) {
            File folderMetadata = new File();
            folderMetadata.setName(FOLDER_NAME);
            folderMetadata.setMimeType(MIME_FOLDER);

            File folder = service.files().create(folderMetadata)
                    .setFields("id")
                    .execute();
            folderId = folder.getId();
        }


        File fileMetadata = new File();
        fileMetadata.setName(archive.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));

        java.io.File filePath = new java.io.File(archive.getPath());
        FileContent mediaContent = new FileContent("application/x-rar-compressed", filePath);
        File file;
        if(id.isEmpty() || id.replaceAll(" ", "").equalsIgnoreCase(""))
            file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        else {
            service.files().delete(id).execute();
            file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
        Main.log.info("Backup File Drive ID: " + file.getId());

        return file.getId();
    }

    public static void downloadSave(java.io.File archive, String id) throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();


        String fileId = id;


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream);


        java.io.File file = archive;
        OutputStream fileOutputStream = new FileOutputStream(file);
        outputStream.writeTo(fileOutputStream);
        fileOutputStream.close();
    }


}
