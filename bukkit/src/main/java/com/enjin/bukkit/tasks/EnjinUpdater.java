package com.enjin.bukkit.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;

public class EnjinUpdater implements Runnable {
    String downloadlocation = "";
    File destination;
    EnjinMinecraftPlugin plugin;
    String versionnumber;
    String updatejar = "http://resources.guild-hosting.net/1/downloads/emp/";

    public EnjinUpdater(String downloadlocation, String versionnumber, EnjinMinecraftPlugin plugin) {
        this.downloadlocation = downloadlocation;
        this.versionnumber = versionnumber;
        this.destination = new File(downloadlocation, "EnjinMinecraftPlugin.jar");
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File part = new File(downloadlocation + File.separator + "EnjinMinecraftPlugin.jar.part");

        try {
            URL website;

            Enjin.getLogger().debug("Connecting to url " + updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");
            website = new URL(updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");

            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(part);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();

            if (destination.exists()) {
                destination.delete();
            }

            if (part.renameTo(destination)) {
                plugin.setHasUpdate(true);
                plugin.setNewVersion(versionnumber);
                Enjin.getLogger().warning("Enjin Minecraft plugin was updated to version " + versionnumber + ". Please restart your server.");
                return;
            } else {
                plugin.setUpdateFailed(true);
                Enjin.getLogger().warning("Unable to update to new version. Please update manually!");
            }
        } catch (IOException e) {
            Enjin.getLogger().warning("Unable to update to new version. Please update manually!");
        }

        plugin.setHasUpdate(false);
    }
}
