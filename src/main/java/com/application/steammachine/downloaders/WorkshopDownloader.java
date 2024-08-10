package com.application.steammachine.downloaders;

import com.application.steammachine.Game;
import com.application.steammachine.Main;
import com.google.common.base.Throwables;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkshopDownloader extends Task<Void>{

    private final String gameID;
    private ArrayList<String> modIDs = new ArrayList<String>();
    private final Game game;
    private WebDriver driver;

    public WorkshopDownloader(Game game) {
        this.gameID = String.valueOf(game.getId());
        this.game = game;
    }


    protected Void call() throws Exception {

        try {

            try {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--incognito");
                options.addArguments("--disable-extensions");
                options.addArguments("--disable-infobars");
                options.addArguments("--remote-debugging-port=9222");
                options.addArguments("--use-fake-ui-for-media-stream");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-gpu");

                driver = new ChromeDriver(options);
            }catch (Exception e){
                Main.log.error(Throwables.getStackTraceAsString (e));
            }

            JavascriptExecutor js = (JavascriptExecutor) driver;

            driver.manage().window().maximize();

            driver.get("https:

            Main.log.info("Opening the Steam Workshop for the game: " + game.getName());

            class WholePageUpdate implements Runnable {
                public void run() {
                    while (true) {
                        
                        try {
                            js.executeScript("let element = document.querySelector(\"#global_header > div > div.supernav_container\");\n" +
                                    "                               element.style.display = \"none\";\n" +
                                    "                               element.disabled = true;");
                            js.executeScript("let element = document.querySelector(\"#global_actions\");\n" +
                                    "                                element.style.display = \"none\";\n" +
                                    "                                element.disabled = true;");
                            js.executeScript("let element = document.querySelector(\"#logo_holder > a\");\n" +
                                    "                                element.removeAttribute(\"href\");");
                            js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_sectionTabs.responsive_hidden\"); " +
                                    "                                element.style.visibility = \"hidden\";\n" +
                                    "                                element.disabled = true;");
                            js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div:nth-child(4) > div > div.workshop_browse_menu_area > div:nth-child(3) > a\");\n" +
                                    "                                element.style.visibility = \"hidden\";\n" +
                                    "                                element.removeAttribute(\"href\");");
                            js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div:nth-child(4) > div > div.workshop_browse_menu_area > div:nth-child(4) > a\");\n" +
                                    "                                element.style.visibility = \"hidden\";\n" +
                                    "                                element.removeAttribute(\"href\");");
                        }catch (Exception ex1){
                            try{
                                js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_sectionTabs.responsive_hidden\"); " +
                                        "                                element.style.visibility = \"hidden\";\n" +
                                        "                                element.disabled = true;");
                                js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > center > div > div.workshop_browse_menu_area > div:nth-child(3) > a\"); " +
                                        "                                element.style.visibility = \"hidden\";\n" +
                                        "                                element.disabled = true;");
                                js.executeScript("let element = document.querySelector(\"#responsive_page_template_content > center > div > div.workshop_browse_menu_area > div:nth-child(4) > a\"); " +
                                        "                                element.style.visibility = \"hidden\";\n" +
                                        "                                element.disabled = true;");
                            }catch(Exception ex2) {
                                try {
                                    js.executeScript("let element = document.querySelector(\"#ig_bottom > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_sectionTabs.responsive_hidden\"); " +
                                            "                                element.style.visibility = \"hidden\";\n" +
                                            "                                element.disabled = true;");
                                } catch (Exception ex3) {}
                            }
                        }


                        
                        try {
                            js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\").style.backgroundColor = \"red\";");
                            js.executeScript("var link = document.querySelector(\"#responsive_page_template_content > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\");\n" +
                                    "link.href = \"about:blank\";");
                            js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").innerHTML = \"Close Workshop\"");
                            js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").style.color = \"white\";");

                        } catch (Exception e1) {
                            try {
                                js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\").style.backgroundColor = \"red\";");
                                js.executeScript("var link = document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\");\n" +
                                        "link.href = \"about:blank\";");
                                js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").innerHTML = \"Close Workshop\"");
                                js.executeScript("document.querySelector(\"#responsive_page_template_content > div.apphub_background > div.apphub_HomeHeader > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").style.color = \"white\";");
                            } catch (Exception e2) {
                                try {
                                    js.executeScript("document.querySelector(\"#ig_bottom > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\").style.backgroundColor = \"red\";");
                                    js.executeScript("var link = document.querySelector(\"#ig_bottom > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a\");\n" +
                                            "link.href = \"about:blank\";");
                                    js.executeScript("document.querySelector(\"#ig_bottom > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").innerHTML = \"Close Workshop\"");
                                    js.executeScript("document.querySelector(\"#ig_bottom > div.apphub_HomeHeaderContent > div.apphub_HeaderTop.workshop > div.apphub_OtherSiteInfo.responsive_hidden > a > span\").style.color = \"white\";");
                                }catch (Exception e3){}
                            }
                        }

                        
                        try{
                            if (!modIDs.isEmpty() && modIDs.contains(driver.getCurrentUrl().substring(55))) {
                                js.executeScript("document.querySelector(\"#SubscribeItemOptionAdd\").innerHTML = \"Already added to SteamMachine\"");
                            } else {
                                js.executeScript("document.querySelector(\"#SubscribeItemOptionAdd\").innerHTML = \"Add to SteamMachine\"");
                            }
                        }catch (Exception e){}

                    }
                }
            }

            class ModPageListener implements Runnable {
                public void run() {

                    while (true) {

                        try {
                            WebElement buttonClicked = new WebDriverWait(driver, Duration.ofSeconds(9999999))
                                    .until(ExpectedConditions.visibilityOfElementLocated(By.className("notLoggedInButtons")));

                            driver.findElement(By.xpath("

                            if (!modIDs.isEmpty() || !modIDs.contains(driver.getCurrentUrl().substring(55))) {
                                modIDs.add(driver.getCurrentUrl().substring(55));
                                Main.log.info("Added the mod with ID: " + driver.getCurrentUrl().substring(55) + " to the download list");
                            }

                            driver.get("https:
                        }catch (Exception e){}

                    }

                }
            }


            ModPageListener modPageListener = new ModPageListener();
            Thread modPageThread = new Thread(modPageListener);
            modPageThread.start();
            Main.log.info("Started ModPageListener");


            WholePageUpdate pageUpdate = new WholePageUpdate();
            Thread pageThread = new Thread(pageUpdate);
            pageThread.start();
            Main.log.info("Started PageUpdateThread");


            Boolean modPageOpened = new WebDriverWait(driver, Duration.ofSeconds(9999999))
                    .until(ExpectedConditions.urlToBe("about:blank"));

            modPageThread.interrupt();
            pageThread.interrupt();
            driver.quit();

            Main.log.info("Workshop session terminated");

            if(!modIDs.isEmpty()) {

                Main.log.info("Starting SteamCMD");

                ArrayList<String> cmdList = new ArrayList<String>();
                cmdList.add("./elevate.exe");
                cmdList.add("-c");
                cmdList.add("-w");
                cmdList.add("ModDownloader.bat");

                
                String modList = "";
                for (String s : modIDs) {
                    cmdList.add("+workshop_download_item");
                    cmdList.add(gameID);
                    cmdList.add(s);
                }

                cmdList.add("+quit");


                String[] cmd = cmdList.toArray(new String[cmdList.size()]);


                Process process = new ProcessBuilder(cmd).start();
                Thread.sleep(1500);
                int code = process.waitFor();

                Main.log.info("SteamCMD terminated");

                Main.log.info("Moving mods to destination directory");


                for (String s : modIDs) {
                    try {
                        FileUtils.moveDirectory(new File("./SteamCMD/steamapps/workshop/content/" + gameID + "/" + s), new File(game.getInstallPath() + game.getModInstallPath() + s));
                    }catch(Exception e){
                        Main.log.error(Throwables.getStackTraceAsString (e));
                    }
                }

            }

            this.cancel();

        }catch(Exception e){

            e.printStackTrace();
            this.cancel();

        }finally {

            this.cancel();
            return null;

        }

    }



    protected static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}