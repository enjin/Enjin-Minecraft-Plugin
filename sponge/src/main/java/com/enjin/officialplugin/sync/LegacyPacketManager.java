package com.enjin.officialplugin.sync;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.sync.data.Packet12ExecuteCommand;
import com.enjin.officialplugin.sync.data.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.sync.data.Packet1ECommandsReceived;
import com.enjin.officialplugin.utils.ConnectionUtil;
import com.enjin.officialplugin.utils.WebAPI;
import com.enjin.officialplugin.utils.commands.CommandWrapper;
import com.enjin.officialplugin.utils.packet.PacketUtilities;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LegacyPacketManager implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private boolean startup = true;
    private int pluginsReportDelay = 60;

    public LegacyPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (startup && plugin.getConfig().isHttps()) {
            if (!ConnectionUtil.testHTTPSconnection()) {
                plugin.getConfig().setHttps(false);
                plugin.debug("SSL connection test failed. The plugin will use http without SSL. This may be less secure.");
            }
        }

        boolean successful = false;
        StringBuilder builder = new StringBuilder();

        try {
            plugin.debug("Connecting to Enjin...");
            URL url = getUrl();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "Mozilla/4.0");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            builder.append("authkey=" + encode(plugin.getAuthKey()));

            if (startup) {
                builder.append("&maxplayers=" + encode(String.valueOf(plugin.getGame().getServer().getMaxPlayers())));
                builder.append("&mc_version=" + encode(plugin.getGame().getPlatform().getMinecraftVersion().getName()));
            }

            builder.append("&players=" + encode(String.valueOf(plugin.getGame().getServer().getOnlinePlayers().size())));
            // TODO: Look into existing permissions plugins in development for sponge
            builder.append("&hasranks=" + encode("FALSE"));
            builder.append("&pluginversion=" + encode(plugin.getContainer().getVersion()));

            if (plugin.getProcessedCommands().size() > 0) {
                builder.append("&executed_commands=" + encode(getCommands()));
            }

            if (++pluginsReportDelay >= 59) {
                builder.append("&mods=" + encode(getMods()));
                pluginsReportDelay = 0;
            }

            builder.append("&playerlist=" + encode(getPlayers()));
            builder.append("&worlds=" + encode(getWorlds()));
            builder.append("&tps=" + encode(getTPS()));
            builder.append("&time=" + encode(getTimes()));
            // TODO: Banned, Unbanned, Groups, Stats

            connection.setRequestProperty("Content-Length", String.valueOf(builder.length()));
            plugin.debug("Sending content to " + url.toString() + ":\n" + builder.toString());
            connection.getOutputStream().write(builder.toString().getBytes());

            InputStream input = connection.getInputStream();
            String success = handleInput(input);

            if (success.equalsIgnoreCase("ok")) {
                successful = true;
            } else if (success.equalsIgnoreCase("auth_error")) {
                // TODO
            } else if (success.equalsIgnoreCase("bad_data")) {
                // TODO
            } else if (success.equalsIgnoreCase("retry_later")) {
                // TODO
            } else if (success.equalsIgnoreCase("invalid_op")) {
                // TODO
            } else {
                plugin.getLogger().warn("Something happened on sync, if you continue to see this error please report it to Enjin.");
                plugin.getLogger().warn("Response code: " + success);
            }
        } catch (MalformedURLException e) {
            plugin.debug("Invalid URL when connecting to minecraft-sync.");
        } catch (IOException e) {
            plugin.debug("Failed to connect to Enjin API when connecting to minecraft-sync.");
        }

        if (successful) {
            startup = false;
            plugin.debug("Sync Successful.");
        }
    }

    private URL getUrl() throws MalformedURLException {
        return new URL((plugin.getConfig().isHttps() ? "https" : "http") + WebAPI.getApiUrl() + "minecraft-sync");
    }

    private String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8");
    }

    private String getCommands() {
        StringBuilder sb = new StringBuilder();
        List<CommandWrapper> processed = new ArrayList<CommandWrapper>(EnjinMinecraftPlugin.getProcessedCommands());
        Iterator<CommandWrapper> iterator = processed.iterator();

        while (iterator.hasNext() && sb.length() < 40 * 1024) {
            CommandWrapper wrapper = iterator.next();

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(wrapper.getId() + ":" + wrapper.getHash());
        }

        return sb.toString();
    }

    private String getMods() {
        StringBuilder builder = new StringBuilder();

        for (PluginContainer container : plugin.getGame().getPluginManager().getPlugins()) {
            builder.append(",")
                    .append(container.getName());
        }

        if (builder.length() > 2) {
            builder.deleteCharAt(0);
        }

        return builder.toString();
    }

    private String getPlayers() {
        StringBuilder builder = new StringBuilder();

        for (Player player : plugin.getGame().getServer().getOnlinePlayers()) {
            builder.append(",")
                    // TODO: Look into existing vanish plugins in development for sponge
                    .append(player.getUniqueId().toString() + ":" + player.getName() + ":" + "FALSE");
        }

        if (builder.length() > 2) {
            builder.deleteCharAt(0);
        }

        return builder.toString();
    }

    private String getWorlds() {
        StringBuilder builder = new StringBuilder();

        for (World world : plugin.getGame().getServer().getWorlds()) {
            builder.append(",")
                    .append(world.getName());
        }

        if (builder.length() > 2) {
            builder.deleteCharAt(0);
        }

        return builder.toString();
    }

    private String getTPS() {
        return String.valueOf(20.0);
    }

    private String getTimes() {
        StringBuilder builder = new StringBuilder();

        // TODO: Check if World API supports time

        return builder.toString();
    }

    private String handleInput(InputStream in) throws IOException {
        String result = "Unknown Error";
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(Integer.MAX_VALUE);

        int i;
        StringBuilder input = new StringBuilder();
        while ((i = bin.read()) != -1) {
            input.append((char) i);
            switch (i) {
                case 0x12:
                    plugin.debug("Packet [0x12] (Execute Command) received.");
                    Packet12ExecuteCommand.handle(bin, plugin);
                    break;
                case 0x13:
                    plugin.debug("Packet [0x13] (Execute Command As Player) received.");
                    Packet13ExecuteCommandAsPlayer.handle(bin, plugin);
                    break;
                case 0x1E:
                    plugin.debug("Packet [0x1E] (Commands Received) received.");
                    Packet1ECommandsReceived.handle(bin, plugin);
                    break;
                case 0x19:
                    plugin.debug("Packet [0x19] (Enjin Status) received.");
                    result = PacketUtilities.readString(bin);
                    break;
                case 0x10:
                case 0x11:
                case 0x14:
                case 0x15:
                case 0x17:
                case 0x18:
                case 0x1A:
                case 0x1B:
                case 0x1D:
                case 0x1F:
                    plugin.debug("Packet [" + Integer.toHexString(i) + "] received but not yet supported. Skipping packet.");
                    break;
                default:
                    plugin.debug("Received an invalid opcode: [" + Integer.toHexString(i) + "]");

                    bin.reset();
                    input = new StringBuilder();
                    while ((i = bin.read()) != -1) {
                        input.append((char) i);
                    }

                    plugin.debug("Raw data received:\n" + input.toString());
                    return "invalid_op";
            }
        }

        plugin.debug("No more packets. End of stream. Update concluded.");
        bin.close();

        plugin.debug("Raw data received:\n" + input.toString());
        return result;
    }
}
