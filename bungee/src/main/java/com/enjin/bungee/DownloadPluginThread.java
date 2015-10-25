package com.enjin.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DownloadPluginThread implements Runnable {

    String downloadlocation = "";
    File destination;
    EnjinPlugin plugin;
    String versionnumber;
    String updatejar = "http://resources.guild-hosting.net/1/downloads/emp/";

    public DownloadPluginThread(String downloadlocation, String versionnumber, File destination, EnjinPlugin plugin) {
        this.downloadlocation = downloadlocation;
        this.versionnumber = versionnumber;
        this.destination = destination;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File tempfile = new File(downloadlocation + File.separator + "EnjinMinecraftPlugin.jar.part");
        try {
            URL website;

            EnjinPlugin.debug("Connecting to url " + updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");
            website = new URL(updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(tempfile);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            if (destination.delete() && tempfile.renameTo(destination)) {
                plugin.hasupdate = true;
                plugin.newversion = versionnumber;
                plugin.getLogger().warning("[Enjin Minecraft Plugin] Enjin Minecraft plugin was updated to version " + versionnumber + ". Please restart your server.");
                return;
            } else {
                plugin.updatefailed = true;
                plugin.getLogger().warning("[Enjin Minecraft Plugin] Unable to update to new version. Please update manually!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.hasupdate = false;
    }

}
