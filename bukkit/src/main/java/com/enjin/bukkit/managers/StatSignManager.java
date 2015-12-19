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
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

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

    public static void fetchStats() {
        RPCData<Stats> data = EnjinServices.getService(PluginService.class).getStats(Optional.ofNullable(items));

        if (data.getError() != null) {
            Enjin.getPlugin().debug(data.getError().getMessage());
        } else {
            stats = data.getResult();
        }
    }

    public static void add(SignData data) {
        if (config != null) {
            config.getSigns().add(data);
            config.save(file);

            if (data.getSubType() != null && data.getSubType() == SignType.SubType.ITEMID && data.getItemId() != null) {
                updateItems();
                Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), () -> {
                    fetchStats();
                    update(data);
                });
            } else {
                update(data);
            }
        }
    }

    public static void remove(SerializableLocation location) {
        if (config != null && config.getSigns().removeIf((data) -> data.getLocation().equals(location))) {
            config.save(file);
            updateItems();
        }
    }

    public static void update() {
        new ArrayList<>(config.getSigns()).forEach(StatSignManager::update);
    }

    public static void update(SignData data) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), () -> {
            Location location = data.getLocation().toLocation();
            Block block = location.getBlock();

            if (block.getState() == null || !(block.getState() instanceof Sign)) {
                config.getSigns().remove(data);
                return;
            }

            Sign sign = (Sign) block.getState();
            String name = null;
            switch (data.getType()) {
                case DONATION:
                    name = StatSignProcessor.setPurchaseSign(sign, data, stats);
                    break;
                case TOPVOTER:
                    name = StatSignProcessor.setTopVoterSign(sign, data, stats);
                    break;
                case VOTER:
                    name = StatSignProcessor.setVoterSign(sign, data, stats);
                    break;
                case TOPPLAYER:
                    name = StatSignProcessor.setTopPlayerSign(sign, data, stats);
                    break;
                case TOPPOSTER:
                    name = StatSignProcessor.setTopPosterSign(sign, data, stats);
                    break;
                case TOPLIKES:
                    name = StatSignProcessor.setTopLikesSign(sign, data, stats);
                    break;
                case NEWMEMBER:
                    name = StatSignProcessor.setNewMemberSign(sign, data, stats);
                    break;
                case TOPPOINTS:
                    name = StatSignProcessor.setTopPointsSign(sign, data, stats);
                    break;
                case POINTSSPENT:
                    name = StatSignProcessor.setPointsSpentSign(sign, data, stats);
                    break;
                case MONEYSPENT:
                    name = StatSignProcessor.setMoneySpentSign(sign, data, stats);
                    break;
                default:
                    break;
            }
            sign.update();

            if (name != null) {
                updateHead(sign, data, name);
            }
        });
    }

    public static void updateItems() {
        new ArrayList<>(config.getSigns()).stream()
                .filter(data -> data.getType() == SignType.DONATION && data.getSubType() != null && data.getSubType().equals(SignType.SubType.ITEMID) && data.getItemId() != null)
                .filter(data -> !items.contains(data.getItemId())).forEach(data -> items.add(data.getItemId()));
    }

    public static void updateHead(Sign sign, SignData data, String name) {
        Block block = null;
        if (data.getHeadLocation() != null) {
            block = data.getHeadLocation().toLocation().getBlock();

            if (block.getType() != Material.SKULL) {
                block = null;
                data.setHeadLocation(null);
            } else {
                updateHead(block, data, name);
                return;
            }
        }

        block = sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace()).getRelative(0, 1, 0);
        if (block.getType() == Material.SKULL) {
            updateHead(block, data, name);
            return;
        }

        block = sign.getBlock().getRelative(0, 2, 0);
        if (block.getType() == Material.SKULL) {
            updateHead(block, data, name);
            return;
        }
    }

    public static void updateHead(Block block, SignData data, String name) {
        SerializableLocation location = new SerializableLocation(block.getLocation());
        for (SignData d : new ArrayList<>(config.getSigns())) {
            if (d != data && d.getHeadLocation() != null && d.getHeadLocation().equals(location)) {
                return;
            }
        }

        if (block.getType() != Material.SKULL) {
            return;
        }

        Skull skull = (Skull) block.getState();
        skull.setSkullType(SkullType.PLAYER);
        skull.setOwner(name != null ? (name.isEmpty() ? "MHF_Steve" : name) : "MHF_Steve");
        skull.update();
        data.setHeadLocation(location);
    }
}
