package me.pafias.pffa.tasks;

import me.pafias.pffa.pFFA;
import me.pafias.pffa.util.Tasks;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class AutoUpdaterTask extends BukkitRunnable {

    private final pFFA plugin;

    public AutoUpdaterTask(pFFA plugin) {
        this.plugin = plugin;
    }

    public void run() {
        try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(new URI("https://pafias.me/minecraft/pFFA/pFFA.jar"));
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet, new BasicHttpContext());
            File jarFileOnPluginFolder = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarFileOnPluginFolder.length() != httpResponse.getEntity().getContentLength()) {
                FileOutputStream fileOutputStream = new FileOutputStream(jarFileOnPluginFolder);
                BufferedInputStream bufferedInputStream = null;
                try {
                    plugin.getLogger().info("Found a new version and downloading the new update!");
                    bufferedInputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
                    byte[] bytes = new byte[1024];
                    int bytesCounter;
                    while ((bytesCounter = bufferedInputStream.read(bytes, 0, 1024)) != -1)
                        fileOutputStream.write(bytes, 0, bytesCounter);
                    plugin.getLogger().info("Updated to latest version successfully. Restarting in 5 seconds...");
                    Tasks.runLaterSync(5 * 20, () -> plugin.getServer().shutdown());
                } finally {
                    httpResponse.getEntity().getContent().close();
                    fileOutputStream.close();
                    if (bufferedInputStream != null)
                        bufferedInputStream.close();
                }
            }
        } catch (IOException | java.net.URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
}