package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.StatsModule;
import com.enjin.bukkit.stats.StatsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class EnjinStatsListener implements Listener {
    EnjinMinecraftPlugin plugin;

    public EnjinStatsListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
        if (module != null) {
            module.getPlayerStats(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnityExplode(final EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (event.getEntity() != null && event.getEntityType() == EntityType.CREEPER) {
                    plugin.getServerStats().addCreeperExplosion();
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerKickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                plugin.getServerStats().addKick(event.getPlayer().getName());
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                if (module != null) {
                    StatsPlayer player = module.getPlayerStats(event.getPlayer());
                    player.addBrokenBlock(event.getBlock());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                if (module != null) {
                    StatsPlayer player = module.getPlayerStats(event.getPlayer());
                    player.addPlacedBlock(event.getBlock());
                }
            }
        });
    }

    //Listener to increment player deaths
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                if (module != null) {
                    Player p = event.getEntity();
                    StatsPlayer player = module.getPlayerStats(p);
                    player.addDeath();
                }
            }
        });
    }

    //Listener to increment player kills
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();

            if (damageEvent.getDamager() instanceof Player) {
                Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                        if (module != null) {
                            Player p = (Player) damageEvent.getDamager();
                            StatsPlayer player = module.getPlayerStats(p);

                            player.addKilled();
                            //Let's add to the player's pvp or pve kills
                            if (event.getEntityType() == EntityType.PLAYER) {
                                player.addPvpkill();
                            } else {
                                player.addPvekill(event.getEntityType());
                            }
                        }
                    }
                });
            }
        }
    }

    //Listener to add distance traveled to players
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                if (module != null) {
                    StatsPlayer player = module.getPlayerStats(event.getPlayer());
                    Player p = event.getPlayer();

                    Location from = event.getFrom();
                    Location to = event.getTo();

                    //Make sure we didn't teleport between two worlds or something
                    if (!from.getWorld().getName().equals(to.getWorld().getName())) {
                        return;
                    }

                    double distance = (to.getX() - from.getX()) * (to.getX() - from.getX()) + (to.getY() - from.getY()) * (to.getY() - from.getY()) + (to.getZ() - from.getZ()) * (to.getZ() - from.getZ());

                    //You can't go over 4 blocks before a teleport event happens, otherwise you are using hacks to move.
                    if (distance > 36) {
                        return;
                    }

                    if (p.isInsideVehicle()) {
                        Entity vehicle = p.getVehicle();
                        if (vehicle instanceof Minecart) {
                            player.addMinecartdistance(distance);
                        } else if (vehicle instanceof Boat) {
                            player.addBoatdistance(distance);
                        } else if (vehicle instanceof Pig) {
                            player.addPigdistance(distance);
                        } else {
                            //They are riding an entity we can't identify
                            //In the future we should allow custom entities
                        }
                    } else {
                        //The player could also be flying, so we need to add that
                        player.addFootdistance(distance);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerExpChange(final PlayerExpChangeEvent event) {
        if (event.getAmount() == 0) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                StatsModule module = plugin.getModuleManager().getModule(StatsModule.class);
                if (module != null) {
                    StatsPlayer player = module.getPlayerStats(event.getPlayer());
                    Player p = event.getPlayer();

                    if (player != null && p != null) {
                        player.setXpLevel(p.getLevel());
                        player.setTotalXp(p.getTotalExperience());
                    }
                }
            }
        });
    }
}
