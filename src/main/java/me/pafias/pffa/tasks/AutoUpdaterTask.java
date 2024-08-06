package me.pafias.pffa.tasks;

import me.pafias.pffa.pFFA;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

public class AutoUpdaterTask extends BukkitRunnable {

    private final pFFA plugin;

    public AutoUpdaterTask(pFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(new URI("https://pafias.me/minecraft/pFFA/pFFA.jar"));
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet, new BasicHttpContext());

            File tempJarFile = new File(plugin.getDataFolder(), "pFFA_update.jar");
            File jarFileOnPluginFolder = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            if (jarFileOnPluginFolder.length() != httpResponse.getEntity().getContentLength()) {
                plugin.getLogger().info("Found a new version and downloading the update!");

                try (BufferedInputStream in = new BufferedInputStream(httpResponse.getEntity().getContent());
                     FileOutputStream out = new FileOutputStream(tempJarFile)) {

                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    plugin.getLogger().info("Downloaded update successfully.");

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getLogger().info("Server restarting to apply update...");
                        Bukkit.getPluginManager().disablePlugins();

                        try {
                            Files.move(tempJarFile.toPath(), jarFileOnPluginFolder.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            plugin.getLogger().info("Updated to the latest version successfully. Restarting server...");
                        } catch (IOException e) {
                            plugin.getLogger().severe("Failed to replace the old JAR file with the new one.");
                            e.printStackTrace();
                        }

                        Bukkit.shutdown();
                    }, 60);
                }
            }
        } catch (IOException | java.net.URISyntaxException ex) {
            plugin.getLogger().severe("Failed to check for updates.");
            ex.printStackTrace();
        }
    }
}
