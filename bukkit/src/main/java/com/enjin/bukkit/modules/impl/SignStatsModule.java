package com.enjin.bukkit.modules.impl;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.config.StatSignConfig;
import com.enjin.bukkit.listeners.SignListener;
import com.enjin.bukkit.modules.Module;
import com.enjin.bukkit.statsigns.SignData;
import com.enjin.bukkit.statsigns.SignType;
import com.enjin.bukkit.statsigns.StatSignProcessor;
import com.enjin.bukkit.util.serialization.SerializableLocation;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Stats;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Module(name = "SignStats")
public class SignStatsModule {
    private EnjinMinecraftPlugin plugin;
    @Getter
    private File                 file;
    @Getter
    private StatSignConfig       config;
    @Getter
    private Stats                stats;
    @Getter
    private List<Integer>        items = new ArrayList<>();

    public SignStatsModule() {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        init();
    }

    public void init() {
        try {
            file = new File(plugin.getDataFolder(), "stat-signs.json");
            config = JsonConfig.load(file, StatSignConfig.class);

            if (!file.exists()) {
                config.save(file);
            }

            Bukkit.getPluginManager().registerEvents(new SignListener(), plugin);
            schedule(plugin, false);
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }

    public void disable() {
        if (config != null) {
            config.save(file);
        }
    }

    public void schedule(final EnjinMinecraftPlugin plugin, boolean delayed) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateItems();
                fetchStats();
                update();
                schedule(plugin, true);
            }
        };

        if (plugin.isEnabled()) {
            EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
            if (delayed) {
                int delay = config == null ? 5 : config.getSendStatsInterval();
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, 20 * 60 * delay);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            }
        }
    }

    public void fetchStats() {
        /**
         * TODO Optimize
         */
        RPCData<Stats> data = EnjinServices.getService(PluginService.class).getStats(Optional.fromNullable(items));

        if (data == null) {
            Enjin.getLogger().debug("Failed to fetch stats from Enjin web services.");
        } else if (data.getError() != null) {
            Enjin.getLogger().debug(data.getError().getMessage());
        } else {
            stats = data.getResult();
        }
    }

    public void add(final SignData data) {
        if (config != null) {
            config.getSigns().add(data);
            config.save(file);

            if (data.getSubType() != null && data.getSubType() == SignType.SubType.ITEMID && data.getItemId() != null) {
                updateItems();
                Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        fetchStats();
                        update(data);
                    }
                });
            } else {
                update(data);
            }
        } else {
            Enjin.getLogger().debug("Heads configuration file is null!");
        }
    }

    public void remove(SerializableLocation location) {
        if (config != null) {
            boolean removed = false;
            for (SignData data : new ArrayList<>(config.getSigns())) {
                if (data.getLocation().equals(location)) {
                    removed = config.getSigns().remove(data);
                }
            }

            if (removed) {
                config.save(file);
                updateItems();
            }
        } else {
            Enjin.getLogger().debug("Heads configuration file is null!");
        }
    }

    public void update() {
        if (config != null) {
            SignStatsModule module = plugin.getModuleManager().getModule(SignStatsModule.class);
            if (module != null) {
                for (SignData data : new ArrayList<>(config.getSigns())) {
                    module.update(data);
                }
            }
        } else {
            Enjin.getLogger().debug("Heads configuration file is null!");
        }
    }

    public void update(final SignData data) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                Location location = data.getLocation().toLocation();
                Block    block    = location.getBlock();

                if (block.getState() == null || !(block.getState() instanceof Sign)) {
                    config.getSigns().remove(data);
                    return;
                }

                Sign   sign = (Sign) block.getState();
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

                if (name != null && !name.isEmpty()) {
                    updateHead(sign, data, name);
                }
            }
        });
    }

    public void updateItems() {
        for (SignData data : new ArrayList<>(config.getSigns())) {
            if (data.getType() == SignType.DONATION && data.getSubType() == SignType.SubType.ITEMID && data.getItemId() != null) {
                if (!items.contains(data.getItemId())) {
                    items.add(data.getItemId());
                }
            }
        }
    }

    public void updateHead(Sign sign, SignData data, String name) {
        Block block = null;
        if (data.getHeadLocation() != null) {
            block = data.getHeadLocation().toLocation().getBlock();

            if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
                data.setHeadLocation(null);
            } else {
                updateHead(block, data, name);
                return;
            }
        }

        BlockData blockData = sign.getBlock().getBlockData();
        Optional<BlockFace> blockFaceOptional = getOppositeFace(blockData);

        if (!blockFaceOptional.isPresent()) return;

        BlockFace face = blockFaceOptional.get();

        block = sign.getBlock()
                    .getRelative(face)
                    .getRelative(0, 1, 0);
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            updateHead(block, data, name);
            return;
        }

        block = sign.getBlock().getRelative(0, 1, 0);
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            updateHead(block, data, name);
            return;
        }

        block = sign.getBlock().getRelative(0, -1, 0);
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            updateHead(block, data, name);
            return;
        }

        block = sign.getBlock().getRelative(0, 2, 0);
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            updateHead(block, data, name);
            return;
        }
    }

    public Optional<BlockFace> getOppositeFace(BlockData data) {
        Optional<BlockFace> result = Optional.absent();

        if (data instanceof org.bukkit.block.data.type.Sign) {
            org.bukkit.block.data.type.Sign sign = (org.bukkit.block.data.type.Sign) data;
            result = Optional.of(sign.getRotation().getOppositeFace());
        } else if (data instanceof WallSign) {
            WallSign sign = (WallSign) data;
            result = Optional.of(sign.getFacing().getOppositeFace());
        }

        return result;
    }

    public void updateHead(Block block, SignData data, String name) {
        SerializableLocation location = new SerializableLocation(block.getLocation());
        for (SignData d : new ArrayList<>(config.getSigns())) {
            if (d != data && d.getHeadLocation() != null && d.getHeadLocation().equals(location)) {
                return;
            }
        }

        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) {
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (player == null) {
            return;
        }

        Skull skull = (Skull) block.getState();
        skull.setOwningPlayer(player);
        skull.update();
        data.setHeadLocation(location);
    }
}
