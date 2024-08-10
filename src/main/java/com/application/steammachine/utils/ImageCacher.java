package com.application.steammachine.utils;

import com.application.steammachine.Controller;
import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.google.common.base.Throwables;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ImageCacher extends Task<Void> {

    public static final String IMAGES_FOLDER = "./cache/images";
    private static final long DOWNLOAD_DELAY_MS = 1550;

    @Override
    protected Void call() throws Exception {
        Main.log.info("-> CustomImageCacher Process Started");
        downloadCustomImages();
        return null;
    }

    private void downloadCustomImages() {
        deleteFilesWithoutExtension(IMAGES_FOLDER);
        for (Game game : Controller.objectGameList.values()) {
            try {
                if(game.isHasCustomImage()) {

                    downloadHeaderImage(game.getName(), game.getImgURL());
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Main.log.info("<- CustomImageCacher Process Ended");
        this.done();
    }

    public static void downloadHeaderImage(String gameName, String imageUrl) throws IOException {
        File outputFolder = new File(IMAGES_FOLDER);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        String fileName = "";
        if(!Controller.objectGameList.get(gameName).isHasCustomImage()) {
            String originalFileName = getFileName(imageUrl);
            fileName = SteamUtils.greatStrip(gameName) + getFileExtension(originalFileName);
        }else{
            fileName = SteamUtils.greatStrip(gameName) + ".png";
        }
        File outputFile = new File(outputFolder, fileName);
        deleteFilesWithSameName(IMAGES_FOLDER, outputFile);


        if (outputFile.exists()) {
            try (InputStream inputStream = new URL(imageUrl).openStream()) {
                byte[] newImageBytes = IOUtils.toByteArray(inputStream);
                byte[] existingImageBytes = FileUtils.readFileToByteArray(outputFile);
                if (!Arrays.equals(newImageBytes, existingImageBytes)) {
                    updateImage(outputFile, newImageBytes, gameName);
                } else {

                    return;
                }
            } catch (Exception e) {
                Main.log.error("Error while updating image for (" + gameName + "): " + e.getMessage() + " - starting second attempt");

                Controller.objectGameList.get(gameName).setImgURL(Controller.objectGameList.get(gameName).getSmallHeader());
                imageUrl = Controller.objectGameList.get(gameName).getImgURL();


                try (InputStream inputStream = new URL(imageUrl).openStream()) {
                    byte[] newImageBytes = IOUtils.toByteArray(inputStream);
                    byte[] existingImageBytes = FileUtils.readFileToByteArray(outputFile);
                    if (!Arrays.equals(newImageBytes, existingImageBytes)) {
                        updateImage(outputFile, newImageBytes, gameName);
                        Main.log.warn("Updated cache image of using SmallHeader (" + gameName + ")");
                    } else {

                        return;
                    }
                }catch (Exception e2) {
                    Main.log.error("Definitive Error while updating cache for image of (" + gameName + "): " + e2.getMessage());
                }

            }
        } else {
            Main.log.warn("Downloading cache image of " + gameName + " from: " + imageUrl);
            try (InputStream inputStream = new URL(imageUrl).openStream();
                 OutputStream outputStream = new FileOutputStream(outputFile)) {
                IOUtils.copy(inputStream, outputStream);
            } catch (Exception e) {
                Main.log.error("Error while caching image for (" + gameName + "): " + e.getMessage() + " - starting second attempt");
                Controller.objectGameList.get(gameName).setImgURL(Controller.objectGameList.get(gameName).getSmallHeader());
                imageUrl = Controller.objectGameList.get(gameName).getImgURL();
                Main.log.warn("Downloading cache image of using SmallHeader (" + gameName + ")");
                try (InputStream inputStream = new URL(imageUrl).openStream();
                     OutputStream outputStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(inputStream, outputStream);
                }catch (Exception e2) {
                    Main.log.error("Definitive Error while caching image for (" + gameName + "): " + e2.getMessage());
                }
            }
        }









    }

    private static void updateImage(File outputFile, byte[] newImageBytes, String gameName) throws IOException {

        if (outputFile.exists()) {
            FileUtils.delete(outputFile);
        }


        FileUtils.writeByteArrayToFile(outputFile, newImageBytes);

        Main.log.info("Image for " + gameName + " updated successfully.");
    }

    public static String getFileName(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private void deleteFilesWithoutExtension(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().contains(".")) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("Deleted: " + file.getName());
                    } else {
                        System.err.println("Failed to delete: " + file.getName());
                    }
                }
            }
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return "." + fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static void deleteFilesWithSameName(String folderPath, File file) {
        String fileName = file.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String name = f.getName();
                    String ext = name.substring(name.lastIndexOf("."));
                    if (name.startsWith(fileName.substring(0, fileName.lastIndexOf("."))) && !ext.equals(fileExtension)) {
                        if (f.delete()) {
                            System.out.println("Deleted: " + name);
                        } else {
                            System.out.println("Failed to delete: " + name);
                        }
                    }
                }
            }
        }
    }

}
