package com.enjin.bukkit.util;

import com.enjin.core.Enjin;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

public class VersionUtil {
    public static boolean validate(String hash) {
        int cbDistance = getCraftBukkitDistance();
        Enjin.getLogger().debug("cbDistance: " + cbDistance);
        int minDistance = getDistance("craftbukkit", hash);
        Enjin.getLogger().debug("minDistance: " + minDistance);

        return !(minDistance == -1 || cbDistance == -1) && minDistance >= cbDistance;

    }

    private static int getCraftBukkitDistance() {
        String version = Bukkit.getVersion();
        if (version == null) version = "Custom";
        if (version.startsWith("git-Spigot-")) {
            String[] parts = version.substring("git-Spigot-".length()).split("-");
            int cbVersions = getDistance("craftbukkit", parts[1].substring(0, parts[1].indexOf(' ')));
            if (cbVersions == -1) {
                return -1;
            } else {
                if (cbVersions == 0) {
                    return 0;
                } else {
                    return cbVersions;
                }
            }
        } else if (version.startsWith("git-Bukkit-")) {
            version = version.substring("git-Bukkit-".length());
            int cbVersions = getDistance("craftbukkit", version.substring(0, version.indexOf(' ')));
            if (cbVersions == -1) {
                return -1;
            } else {
                if (cbVersions == 0) {
                    return 0;
                } else {
                    return cbVersions;
                }
            }
        } else {
            return -1;
        }
    }

    public static int getDistance(String repo, String hash) {
        try {
            BufferedReader reader = Resources.asCharSource(
                    new URL("https://hub.spigotmc.org/stash/rest/api/1.0/projects/SPIGOT/repos/" + repo + "/commits?since=" + URLEncoder.encode(hash, "UTF-8") + "&withCounts=true"),
                    Charsets.UTF_8
            ).openBufferedStream();
            try {
                JSONObject obj = (JSONObject) new JSONParser().parse(reader);
                return ((Number) obj.get("totalCount")).intValue();
            } catch (ParseException ex) {
                ex.printStackTrace();
                return -1;
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
