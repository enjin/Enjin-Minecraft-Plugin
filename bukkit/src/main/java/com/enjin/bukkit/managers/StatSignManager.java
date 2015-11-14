package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.StatSignConfig;
import com.enjin.bukkit.statsigns.SignData;
import com.enjin.bukkit.statsigns.SignType;
import com.enjin.bukkit.statsigns.StatSignProcessor;
import com.enjin.bukkit.listeners.SignListener;
import com.enjin.bukkit.util.serialization.SerializableLocation;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Stats;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatSignManager {
    @Getter
    private static File file;
    @Getter
    private static StatSignConfig config;
    @Getter
    private static Stats stats;
    @Getter
    private static List<Integer> items = Lists.newArrayList();

    public static void init(EnjinMinecraftPlugin plugin) {
        file = new File(plugin.getDataFolder(), "stat-signs.json");
        config = JsonConfig.load(file, StatSignConfig.class);

        if (!file.exists()) {
            config.save(file);
        }

        Bukkit.getPluginManager().registerEvents(new SignListener(), plugin);
        schedule(plugin, false);
    }

    public static void disable() {
        if (config != null) {
            config.save(file);
        }
    }

    public static void schedule(EnjinMinecraftPlugin plugin, boolean delayed) {
        Runnable runnable = () -> {
            updateItems();
            fetchStats();
            update();
            schedule(plugin, true);
        };

        if (plugin.isEnabled()) {
            if (delayed) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, 20 * 60);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            }
        }
    }

    private static void fetchStats() {
        RPCData<Stats> data = EnjinServices.getService(PluginService.class).getStats(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), Optional.ofNullable(items));

        if (data.getError() != null) {
            Enjin.getPlugin().debug(data.getError().getMessage());
        } else {
            stats = data.getResult();
        }
    }

    public static void add(SignData data) {
        if (config != null) {
            config.getHeads().add(data);
            config.save(file);

            if (data.getSubType() != null && data.getSubType() == SignType.SubType.ITEMID && data.getItemId() != null) {
                updateItems();
                Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), () -> {
                    fetchStats();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), () -> update(data));
                });
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), () -> update(data));
            }
        }
    }

    public static void remove(SerializableLocation location) {
        if (config != null) {
            new ArrayList<>(config.getHeads()).stream().filter(data -> data.getLocation().equals(location)).forEach(data -> config.getHeads().remove(data));
            config.save(file);

            updateItems();
        }
    }

    public static void update() {
        new ArrayList<>(config.getHeads()).forEach(StatSignManager::update);
    }

    public static void update(SignData data) {
        Location location = data.getLocation().toLocation();
        Block block = location.getBlock();

        if (block.getState() == null || !(block.getState() instanceof Sign)) {
            config.getHeads().remove(data);
            return;
        }

        Sign sign = (Sign) block.getState();
        switch (data.getType()) {
            case DONATION:
                StatSignProcessor.setPurchaseSign(sign, data, stats);
                break;
            case TOPVOTER:
                StatSignProcessor.setTopVoterSign(sign, data, stats);
                break;
            case VOTER:
                StatSignProcessor.setVoterSign(sign, data, stats);
                break;
            case TOPPLAYER:
                StatSignProcessor.setTopPlayerSign(sign, data, stats);
                break;
            case TOPPOSTER:
                StatSignProcessor.setTopPosterSign(sign, data, stats);
                break;
            case TOPLIKES:
                StatSignProcessor.setTopLikesSign(sign, data, stats);
                break;
            case NEWMEMBER:
                StatSignProcessor.setNewMemberSign(sign, data, stats);
                break;
            case TOPPOINTS:
                StatSignProcessor.setTopPointsSign(sign, data, stats);
                break;
            case POINTSSPENT:
                StatSignProcessor.setPointsSpentSign(sign, data, stats);
                break;
            case MONEYSPENT:
                StatSignProcessor.setMoneySpentSign(sign, data, stats);
                break;
            default:
                break;
        }
        sign.update();
    }

    public static void updateItems() {
        new ArrayList<>(config.getHeads()).stream()
                .filter(data -> data.getType() == SignType.DONATION && data.getSubType() != null && data.getSubType().equals(SignType.SubType.ITEMID) && data.getItemId() != null)
                .filter(data -> !items.contains(data.getItemId())).forEach(data -> items.add(data.getItemId()));
    }
}
