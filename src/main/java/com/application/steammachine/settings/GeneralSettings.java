package com.application.steammachine.settings;

public class GeneralSettings {



    private static String installPath = "./Games";

    private static boolean diskChoiceEnabled = true;
    private static String secondaryFolderName = "/Steam-Machine-Games";
    private static boolean useVPN;
    private static boolean openFullscreen;
    private static String initialFilter;
    private static boolean silentUnpacking;
    private static boolean applyDnsOnStartup;
    private static boolean autoGenerateShortcuts;
    private static boolean showInstallNot;
    private static boolean launchOnStart;

    private static boolean hd = true;

    private static boolean deleteExtractedParts;

    private static String embeddedDNS;

    public GeneralSettings(String installPath, String secondaryFolderName, boolean useVPN, boolean openFullscreen, String initialFilter,
                           boolean silentUnpacking, boolean applyDnsOnStartup, boolean autoGenerateShortcuts,
                           boolean showInstallNot, boolean launchOnStart, String embeddedDNS, boolean deleteExtractedParts,
                           boolean diskChoiceEnabled, boolean hd) {

        GeneralSettings.installPath = installPath;
        GeneralSettings.secondaryFolderName = secondaryFolderName;
        GeneralSettings.useVPN = useVPN;
        GeneralSettings.openFullscreen = openFullscreen;
        GeneralSettings.initialFilter = initialFilter;
        GeneralSettings.silentUnpacking = silentUnpacking;
        GeneralSettings.applyDnsOnStartup = applyDnsOnStartup;
        GeneralSettings.autoGenerateShortcuts = autoGenerateShortcuts;
        GeneralSettings.showInstallNot = showInstallNot;
        GeneralSettings.launchOnStart = launchOnStart;
        GeneralSettings.embeddedDNS = embeddedDNS;
        GeneralSettings.deleteExtractedParts = deleteExtractedParts;
        GeneralSettings.diskChoiceEnabled = diskChoiceEnabled;
        GeneralSettings.hd = hd;

    }





    public static String getInstallPath() {
        return installPath;
    }

    public static void setInstallPath(String x) {
        installPath = x;
    }

    public static boolean getUseVPN() {
        return useVPN;
    }

    public static void setUseVPN(boolean useVPN) {
        GeneralSettings.useVPN = useVPN;
    }

    public static boolean getOpenFullscreen() {
        return openFullscreen;
    }

    public static void setOpenFullscreen(boolean openFullscreen) {
        GeneralSettings.openFullscreen = openFullscreen;
    }

    public static String getInitialFilter() {
        return initialFilter;
    }

    public static void setInitialFilter(String initialFilter) {
        GeneralSettings.initialFilter = initialFilter;
    }

    public static boolean isSilentUnpacking() {
        return silentUnpacking;
    }

    public static void setSilentUnpacking(boolean silentUnpacking) {
        GeneralSettings.silentUnpacking = silentUnpacking;
    }

    public static boolean isApplyDnsOnStartup() {
        return applyDnsOnStartup;
    }

    public static void setApplyDnsOnStartup(boolean applyDnsOnStartup) {
        GeneralSettings.applyDnsOnStartup = applyDnsOnStartup;
    }

    public static boolean isAutoGenerateShortcuts() {
        return autoGenerateShortcuts;
    }

    public static void setAutoGenerateShortcuts(boolean autoGenerateShortcuts) {
        GeneralSettings.autoGenerateShortcuts = autoGenerateShortcuts;
    }

    public static boolean isShowInstallNot() {
        return showInstallNot;
    }

    public static void setShowInstallNot(boolean showDownloadNot) {
        GeneralSettings.showInstallNot = showDownloadNot;
    }

    public static boolean isLaunchOnStart() {
        return launchOnStart;
    }

    public static void setLaunchOnStart(boolean launchOnStart) {
        GeneralSettings.launchOnStart = launchOnStart;
    }

    public static String getEmbeddedDNS() {
        return embeddedDNS;
    }

    public static void setEmbeddedDNS(String embeddedDNS) {
        GeneralSettings.embeddedDNS = embeddedDNS;
    }

    public static boolean isDeleteExtractedParts() {
        return deleteExtractedParts;
    }

    public static void setDeleteExtractedParts(boolean deleteExtractedParts) {
        GeneralSettings.deleteExtractedParts = deleteExtractedParts;
    }

    public static String getSecondaryFolderName() {
        return secondaryFolderName;
    }

    public static void setSecondaryFolderName(String secondaryFolderName) {
        GeneralSettings.secondaryFolderName = secondaryFolderName;
    }

    public static boolean isDiskChoiceEnabled() {
        return diskChoiceEnabled;
    }

    public static void setDiskChoiceEnabled(boolean diskChoiceEnabled) {
        GeneralSettings.diskChoiceEnabled = diskChoiceEnabled;
    }

    public static boolean isHd() {
        return hd;
    }

    public static void setHd(boolean hd) {
        GeneralSettings.hd = hd;
    }
}
