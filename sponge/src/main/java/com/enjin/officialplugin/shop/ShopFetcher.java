package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.data.Shop;
import com.enjin.officialplugin.utils.WebAPI;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.List;
import java.util.UUID;

public class ShopFetcher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private UUID uuid;

    public ShopFetcher(Player player) {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        final Optional<Player> p = plugin.getGame().getServer().getPlayer(uuid);

        if (!p.isPresent()) {
            return;
        }

        Player player = p.get();
        StringBuilder builder = new StringBuilder();
        InputStream in = null;
        try {
            plugin.debug("Connecting to Enjin for shop data for player...");
            URL enjinurl = getUrl();
            HttpURLConnection con = (HttpURLConnection) enjinurl.openConnection();
            con.setRequestMethod("POST");
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "Mozilla/4.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            builder.append("authkey=" + encode(plugin.getAuthKey()));
            builder.append("&player=" + encode(player.getName()));
            con.setRequestProperty("Content-Length", String.valueOf(builder.length()));

            plugin.debug("Sending content: \n" + builder.toString());
            con.getOutputStream().write(builder.toString().getBytes());
            in = con.getInputStream();
            String json = parseInput(in);

            final List<Shop> shops = ShopUtil.getShopsFromJSON(json);
            plugin.debug("# of Shops: " + shops.size());

            if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
                PlayerShopInstance.getInstances().put(player.getUniqueId(), new PlayerShopInstance(shops));
            } else {
                PlayerShopInstance.getInstances().get(player.getUniqueId()).update(shops);
            }

            PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());
            ShopUtil.sendTextShop(player, instance, -1);
            return;
        } catch (SocketTimeoutException e) {
            p.get().sendMessage(Texts.builder("The request to the Enjin API timed out, please try again later.").color(TextColors.RED).build());
            return;
        } catch (Throwable t) {
            plugin.getLogger().error("", t);
            return;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static String parseInput(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        StringBuilder builder = new StringBuilder();

        while (bytesRead > 0) {
            builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
            bytesRead = in.read(buffer);
        }

        return builder.toString();
    }

    private URL getUrl() throws Throwable {
        return new URL((plugin.getConfig().isHttps() ? "https" : "http") + WebAPI.getApiUrl() + "minecraft-shop");
    }

    private String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8");
    }

}
