package com.application.steammachine.settings;

public class DesignSettings {



    private static String installedGamesColor;
    private static String partiallyInstalledGamesColor;
    private static String favoriteGamesColor;
    private static String progressBarColor;

    public DesignSettings(String installedGamesColor, String partiallyInstalledGamesColor, String favoriteGamesColor, String progressBarColor) {

        DesignSettings.installedGamesColor = installedGamesColor;
        DesignSettings.partiallyInstalledGamesColor = partiallyInstalledGamesColor;
        DesignSettings.favoriteGamesColor = favoriteGamesColor;
        DesignSettings.progressBarColor = progressBarColor;

    }





    public static String getInstalledGamesColor() {
        return installedGamesColor;
    }

    public static void setInstalledGamesColor(String installedGamesColor) {
        DesignSettings.installedGamesColor = installedGamesColor;
    }

    public static String getPartiallyInstalledGamesColor() {
        return partiallyInstalledGamesColor;
    }

    public static void setPartiallyInstalledGamesColor(String partiallyInstalledGamesColor) {
        DesignSettings.partiallyInstalledGamesColor = partiallyInstalledGamesColor;
    }

    public static String getFavoriteGamesColor() {
        return favoriteGamesColor;
    }

    public static void setFavoriteGamesColor(String favoriteGamesColor) {
        DesignSettings.favoriteGamesColor = favoriteGamesColor;
    }

    public static String getProgressBarColor() {
        return progressBarColor;
    }

    public static void setProgressBarColor(String progressBarColor) {
        DesignSettings.progressBarColor = progressBarColor;
    }

}
