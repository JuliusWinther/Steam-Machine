package com.application.steammachine;

import com.application.steammachine.utils.SteamUtils;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Game {



    private String id;
    private String name;
    private String imgURL;
    private String gameDesc;
    private Image img;
    private String installPath;
    private String gameFolderName;
    private String saveFilesPath;
    private String exePath;

    private String archiveName;
    private ArrayList<String> downloadLink = new ArrayList<>();
    private String multiplayerType;

    private String fixInstallPath = "/";
    private String gameVersion;
    private String latestGameVersion;
    private String modInstallPath = "/";
    private String downloadDimension;
    private String typeOfFile;
    private String customImageUrl;
    private String extraDetails;
    private boolean isBookmarked;
    private String steamWorkshopLink;
    private ArrayList<String> downloadedParts = new ArrayList<String>();
    private boolean isInstalled = false;
    private String storeLink = "";
    private String extraTags = "";
    private String status = "";

    private ArrayList<String> videoGallery = new ArrayList<String>();
    private ArrayList<String> videoGalleryThumbs = new ArrayList<String>();

    private ArrayList<String> imageGallery = new ArrayList<String>();
    private ArrayList<String> imageGalleryThumbs = new ArrayList<String>();

    private String price = "";
    private String long_desc = "";
    private String requirements = "";

    private String smallHeader = "";

    private String steamGridDBId = "";

    private boolean hasCustomImage = false;






    public Game() throws IOException {

        id = "0";
        imgURL = "https:
        gameDesc = "/";
        name = "";
        gameFolderName = "/Games/GameFolder";
        installPath = "/Games";
        saveFilesPath = "C:
        exePath = "";
        archiveName = this.name.replaceAll(" ", "") + ".rar";
        gameVersion ="0";
        isInstalled = false;
        steamWorkshopLink = "https:
        downloadDimension = "Undefined";
        typeOfFile = "Undefined";
        extraDetails = "/";
        isBookmarked = false;


        

    }


    public Game(String id, String name, String gameFolderName, String exePath, String saveFilesPath,
                ArrayList<String> downloadLink, String multiplayerType,
                String gameVersion, String typeOfFile, String downloadDimension,
                String extraDetails, String status, String gameDesc, String customImageUrl,
                String storeLink, String extraTags, String customImgs, String customVideos,
                String price, String customLongDesc, String requirements, String steamGridDBId,
                String installPath, boolean isInstalled) throws IOException {

        this.id = id.strip();

        this.imgURL =
               (customImageUrl.strip().equalsIgnoreCase("") || customImageUrl.strip().equalsIgnoreCase("/")) ?
               "https:
               : customImageUrl.strip();

        this.smallHeader =  (customImageUrl.strip().equalsIgnoreCase("") || customImageUrl.strip().equalsIgnoreCase("/")) ?
                "https:
                : customImageUrl.strip();

        if(customImageUrl.strip() != null && !customImageUrl.strip().equalsIgnoreCase("/") && !customImageUrl.strip().equalsIgnoreCase(""))
            this.hasCustomImage = true;

        this.gameDesc = gameDesc.strip();
        this.name = name.strip();
        this.gameFolderName = gameFolderName.strip();
        this.exePath = exePath.strip();
        this.saveFilesPath = saveFilesPath.strip();
        this.downloadLink = downloadLink;
        this.multiplayerType = multiplayerType.strip();
        this.fixInstallPath = fixInstallPath.strip();

        this.gameVersion = gameVersion.strip();
        this.latestGameVersion = gameVersion.strip();

        this.modInstallPath = modInstallPath.strip();
        this.downloadDimension = downloadDimension.strip();
        this.typeOfFile = typeOfFile.strip();
        this.extraDetails = extraDetails.strip();

        this.steamWorkshopLink = "https:


        this.archiveName = SteamUtils.greatStrip(this.name.strip().replaceAll("[^A-Za-z0-9]", "")) + ".rar";

        this.installPath = installPath;
        this.isInstalled = isInstalled;

        this.isBookmarked = false;

        if(storeLink.contains("http")){
            this.storeLink = storeLink.strip();
        }else if(!this.id.equalsIgnoreCase("0")){
            this.storeLink = "https:
        }else{
            this.storeLink = "No Store Link Found";
        }

        if(!customImgs.strip().equalsIgnoreCase("/") && !customImgs.strip().equalsIgnoreCase("")) {
            this.imageGallery = new ArrayList<>(List.of(customImgs.strip().split("@")));
            this.imageGalleryThumbs = new ArrayList<>(List.of(customImgs.strip().split("@")));
        }

        if(!customVideos.strip().equalsIgnoreCase("/") && !customVideos.strip().equalsIgnoreCase("")) {
            this.videoGallery = new ArrayList<>(List.of(customVideos.strip().split("@")));
            for(int i = 0; i < this.videoGallery.size(); i++){
                this.videoGalleryThumbs.add("com/application/steammachine/images/video_thumb.jpg");
            }
        }

        this.long_desc = customLongDesc.strip();
        this.requirements = requirements.strip();
        this.price = price.strip();
        this.extraTags = extraTags.strip();
        this.steamGridDBId = steamGridDBId.strip();
        this.status = status.strip();

    }

    public boolean hasExtraDetails(){
        if(this.extraDetails.equalsIgnoreCase("/") || this.extraDetails == null || this.extraDetails.equalsIgnoreCase(""))
            return false;
        else
            return true;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public String getSaveFilesPath() {
        return saveFilesPath;
    }

    public void setSaveFilesPath(String saveFilesPath) {
        this.saveFilesPath = saveFilesPath;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public String getExePath() { return exePath; }

    public void setExePath(String exePath) { this.exePath = exePath; }

    public ArrayList<String> getDownloadLink() { return downloadLink; }

    public void setDownloadLink(ArrayList<String> downloadLink) { this.downloadLink = downloadLink; }

    public String getMultiplayerType() { return multiplayerType; }

    public void setMultiplayerType(String multiplayerType) { this.multiplayerType = multiplayerType; }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getFixInstallPath() {
        return fixInstallPath;
    }

    public void setFixInstallPath(String fixInstallPath) {
        this.fixInstallPath = fixInstallPath;
    }

    public String getGameFolderName() {
        return gameFolderName;
    }

    public void setGameFolderName(String gameFolder) {
        this.gameFolderName = gameFolder;
    }

    public String getModInstallPath() {
        return modInstallPath;
    }

    public void setModInstallPath(String modInstallPath) {
        this.modInstallPath = modInstallPath;
    }

    public String getSteamWorkshopLink() {
        return steamWorkshopLink;
    }

    public void setSteamWorkshopLink(String steamWorkshopLink) {
        this.steamWorkshopLink = steamWorkshopLink;
    }

    public String getGameDesc() {
        return gameDesc;
    }

    public void setGameDesc(String gameDesc) {
        this.gameDesc = gameDesc;
    }

    public ArrayList<String> getDownloadedParts() {
        return downloadedParts;
    }

    public void setDownloadedParts(ArrayList<String> downloadedParts) {
        this.downloadedParts = downloadedParts;
    }

    public String getDownloadDimension() {
        return downloadDimension;
    }

    public void setDownloadDimension(String downloadDimension) {
        this.downloadDimension = downloadDimension;
    }

    public String getTypeOfFile() {
        return typeOfFile;
    }

    public void setTypeOfFile(String typeOfFile) {
        this.typeOfFile = typeOfFile;
    }

    public String getCustomImageUrl() {
        return customImageUrl;
    }

    public void setCustomImageUrl(String customImageUrl) {
        this.customImageUrl = customImageUrl;
    }

    public String getExtraDetails() {
        return extraDetails;
    }

    public void setExtraDetails(String extraDetails) {
        this.extraDetails = extraDetails;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public String getLatestGameVersion() {
        return latestGameVersion;
    }

    public void setLatestGameVersion(String latestGameVersion) {
        this.latestGameVersion = latestGameVersion;
    }

    public String getStoreLink() {
        return storeLink;
    }

    public void setStoreLink(String storeLink) {
        this.storeLink = storeLink;
    }

    public String getExtraTags() {
        return extraTags;
    }

    public void setExtraTags(String extraTags) {
        this.extraTags = extraTags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLong_desc() {
        return long_desc;
    }

    public void setLong_desc(String long_desc) {
        this.long_desc = long_desc;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public ArrayList<String> getVideoGallery() {
        return videoGallery;
    }

    public void setVideoGallery(ArrayList<String> videoGallery) {
        this.videoGallery = videoGallery;
    }

    public ArrayList<String> getVideoGalleryThumbs() {
        return videoGalleryThumbs;
    }

    public void setVideoGalleryThumbs(ArrayList<String> videoGalleryThumbs) {
        this.videoGalleryThumbs = videoGalleryThumbs;
    }

    public ArrayList<String> getImageGallery() {
        return imageGallery;
    }

    public void setImageGallery(ArrayList<String> imageGallery) {
        this.imageGallery = imageGallery;
    }

    public ArrayList<String> getImageGalleryThumbs() {
        return imageGalleryThumbs;
    }

    public void setImageGalleryThumbs(ArrayList<String> imageGalleryThumbs) {
        this.imageGalleryThumbs = imageGalleryThumbs;
    }

    public String getSmallHeader() {
        return smallHeader;
    }

    public void setSmallHeader(String smallHeader) {
        this.smallHeader = smallHeader;
    }

    public boolean isHasCustomImage() {
        return hasCustomImage;
    }

    public void setHasCustomImage(boolean hasCustomImage) {
        this.hasCustomImage = hasCustomImage;
    }

    public String getSteamGridDBId() {
        return steamGridDBId;
    }

    public void setSteamGridDBId(String steamGridDBId) {
        this.steamGridDBId = steamGridDBId;
    }
}
