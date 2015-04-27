package io.puharesource.mc.titlemanager.backend.updatechecker;

import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateManager {

    private String latestVersion;

    public UpdateManager() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(TitleManager.getInstance(), new Checker(), 0, 20 * 60 * 60);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return TitleManager.getInstance().getDescription().getVersion();
    }

    public boolean isUpdateAvailable() {
        return latestVersion != null && !getCurrentVersion().equalsIgnoreCase(latestVersion);
    }

    private class Checker implements Runnable {
        @Override
        public void run() {
            Logger logger = TitleManager.getInstance().getLogger();
            logger.info("Searching for updates.");

            try {
                HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=1049").getBytes("UTF-8"));
                latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            } catch (IOException ex) {
                logger.info("Failed to get check for updates.");
            }

            logger.info(isUpdateAvailable() ? "An update was found!" : "No update was found.");
        }
    }
}
