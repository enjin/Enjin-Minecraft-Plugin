package com.enjin.bungee.tasks;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class EnjinUpdater implements Runnable {
    private String               downloadLocation = "";
    private File                 destination;
    private EnjinMinecraftPlugin plugin;
    private String               versionNumber;
    private String               updateJar        = "http://resources.guild-hosting.net/1/downloads/emp/";

    public EnjinUpdater(String downloadLocation, String versionNumber, File destination, EnjinMinecraftPlugin plugin) {
        this.downloadLocation = downloadLocation;
        this.versionNumber = versionNumber;
        this.destination = destination;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File tempfile = new File(downloadLocation + File.separator + "EnjinMinecraftPlugin.jar.part");

        try {
            Enjin.getLogger().debug("Connecting to url " + updateJar + versionNumber + "/EnjinMinecraftPlugin.jar");
            URL website = new URL(updateJar + versionNumber + "/EnjinMinecraftPlugin.jar");

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream    fos = new FileOutputStream(tempfile);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();

            if (destination.delete() && tempfile.renameTo(destination)) {
                plugin.setHasUpdate(true);
                plugin.setNewVersion(versionNumber);
                plugin.setUpdateFailed(false);
                Enjin.getLogger()
                     .info("Enjin Minecraft Plugin was updated to version " + versionNumber + ". Please restart your server.");
                return;
            } else {
                plugin.setUpdateFailed(true);
                Enjin.getLogger().warning("Unable to update to new version. Please update manually!");
            }
        } catch (IOException e) {
            Enjin.getLogger().log(e);
        }

        plugin.setHasUpdate(false);
    }

}
