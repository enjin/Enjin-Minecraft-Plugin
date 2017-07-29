package com.enjin.sponge.managers;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Stats;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.config.StatSignConfig;
import com.enjin.sponge.listeners.SignListener;
import com.enjin.sponge.statsigns.EnjinSignData;
import com.enjin.sponge.statsigns.EnjinSignType;
import com.enjin.sponge.statsigns.StatSignProcessor;
import com.enjin.sponge.utils.SkullUtil;
import com.enjin.sponge.utils.serialization.SerializableLocation;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        try {
            file = new File(plugin.getConfigDir(), "stat-signs.json");
            config = JsonConfig.load(file, StatSignConfig.class);

            if (!file.exists()) {
                config.save(file);
            }

            Sponge.getEventManager().registerListeners(Enjin.getPlugin(), new SignListener());
            schedule(plugin, false);
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }

    public static void disable() {
        if (config != null) {
            config.save(file);
        }
    }

    public static void schedule(final EnjinMinecraftPlugin plugin, boolean delayed) {
        Runnable runnable = () -> {
            updateItems();
            if (fetchStats()) {
                update();
                schedule(plugin, true);
            }
        };

        if (delayed) {
            EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
            int delay = config == null ? 5 : config.getSendStatsInterval();
            Sponge.getScheduler().createTaskBuilder().async()
                    .delay(delay, TimeUnit.MINUTES)
                    .execute(runnable)
                    .submit(Enjin.getPlugin());
        } else {
            Sponge.getScheduler().createTaskBuilder().async()
                    .execute(runnable)
                    .submit(Enjin.getPlugin());
        }
    }

    public static boolean fetchStats() {
        RPCData<Stats> data = EnjinServices.getService(PluginService.class).getStats(Optional.fromNullable(items));

        if (data == null) {
            Enjin.getLogger().debug("Failed to fetch stats from Enjin web services.");
        } else if (data.getError() != null) {
            Enjin.getLogger().debug(data.getError().getMessage());
        } else {
            stats = data.getResult();
            return true;
        }

        return false;
    }

    public static void add(final EnjinSignData data) {
        if (config != null) {
            config.getSigns().add(data);
            config.save(file);

            if (data.getSubType() != null && data.getSubType() == EnjinSignType.SubType.ITEMID && data.getItemId() != null) {
                Runnable runnable = () -> {
                    if (fetchStats()) {
                        update(data);
                    }
                };

                updateItems();
                Sponge.getScheduler().createTaskBuilder().async().execute(runnable).submit(Enjin.getPlugin());
            } else {
                update(data);
            }
        }
    }

    public static void remove(SerializableLocation location) {
        boolean removed = false;
        for (EnjinSignData data : new ArrayList<>(config.getSigns())) {
            if (data.getLocation().equals(location)) {
                removed = config.getSigns().remove(data);
            }
        }

        if (config != null && removed) {
            config.save(file);
            updateItems();
        }
    }

    public static void update() {
        for (EnjinSignData data : new ArrayList<>(config.getSigns())) {
            StatSignManager.update(data);
        }
    }

    public static void update(final EnjinSignData data) {
        Runnable runnable = () -> {
            Location<World> location = data.getLocation().toLocation();

            if (location == null || !location.hasTileEntity() || !(location.getTileEntity().get() instanceof Sign)) {
                config.getSigns().remove(data);
                return;
            }

            Sign sign = (Sign) location.getTileEntity().get();
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

            if (name != null) {
                updateHead(sign, data, name);
            }
        };

        Sponge.getScheduler().createTaskBuilder().execute(runnable).submit(Enjin.getPlugin());
    }

    public static void updateItems() {
        for (EnjinSignData data : new ArrayList<>(config.getSigns())) {
            if (data.getType() == EnjinSignType.DONATION && data.getSubType() == EnjinSignType.SubType.ITEMID && data.getItemId() != null) {
                if (!items.contains(data.getItemId())) {
                    items.add(data.getItemId());
                }
            }
        }
    }

    public static void updateHead(Sign sign, EnjinSignData data, String name) {
        if (data.getHeadLocation() != null) {
            Location<World> loc = data.getHeadLocation().toLocation();

            if (loc.getBlock().getType() != BlockTypes.SKULL) {
                data.setHeadLocation(null);
            } else {
                updateHead(loc.getBlock(), loc, data, name);
                return;
            }
        }

        BlockState state = sign.getBlock();
        if (state.supports(ImmutableDirectionalData.class)) {
            ImmutableDirectionalData directional = state.get(ImmutableDirectionalData.class).get();
            if (directional != null) {
                Direction direction = directional.direction().get();
                Location<World> loc = sign.getLocation().getRelative(direction.getOpposite()).getRelative(Direction.UP);

                if (loc != null && loc.getBlockType().equals(BlockTypes.SKULL)) {
                    updateHead(loc.getBlock(), loc, data, name);
                    return;
                }
            }
        }

        Location<World> loc = sign.getLocation().getRelative(Direction.UP);
        if (loc.getBlock().getType() == BlockTypes.SKULL) {
            updateHead(loc.getBlock(), loc, data, name);
            return;
        }

        loc = sign.getLocation().add(0, 2, 0);
        if (loc.getBlock().getType() == BlockTypes.SKULL) {
            updateHead(loc.getBlock(), loc, data, name);
            return;
        }
    }

    public static void updateHead(BlockState block, Location<World> location, EnjinSignData data, String name) {
        SerializableLocation loc = new SerializableLocation(location);
        for (EnjinSignData d : new ArrayList<>(config.getSigns())) {
            if (d != data && d.getHeadLocation() != null && d.getHeadLocation().equals(loc)) {
                return;
            }
        }

        if (block.getType() != BlockTypes.SKULL && !location.hasTileEntity() && !(location.getTileEntity().get() instanceof Skull)) {
            return;
        }

        SkullUtil.updateSkullOwner(location, (name == null || name.isEmpty()) ? "MHF_Steve" : name);
        data.setHeadLocation(loc);
    }
}
