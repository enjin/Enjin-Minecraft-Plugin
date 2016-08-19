package com.enjin.sponge.listeners;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.managers.StatsManager;
import com.enjin.sponge.stats.StatsPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.animal.Pig;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent.Break;
import org.spongepowered.api.event.block.ChangeBlockEvent.Place;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent.Death;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.event.world.ExplosionEvent.Detonate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EnjinStatsListener {
    private EnjinMinecraftPlugin plugin;

    public EnjinStatsListener (EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

	@Listener(order = Order.LAST)
    public void onPlayerJoin(final Join event) {
        StatsManager.getPlayerStats(event.getTargetEntity());
    }

	@Listener(order = Order.LAST)
    public void onEnityExplode(final Detonate event) {
		if (event.isCancelled() || !event.getCause().containsType(Creeper.class)) {
			return;
		}

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			plugin.getServerStats().addCreeperExplosion();
		}).async().submit(Enjin.getPlugin());
    }

	@Listener(order = Order.LAST)
    public void onPlayerKick(final KickPlayerEvent event) {
        Sponge.getScheduler().createTaskBuilder().execute(() -> {
			plugin.getServerStats().addKick(event.getTargetEntity().getName());
		}).async().submit(Enjin.getPlugin());
    }

	@Listener(order = Order.LAST)
    public void onBlockBreak(final Break event) {
        if (event.isCancelled() || !event.getCause().containsType(Player.class)) {
            return;
        }

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
			StatsPlayer player = StatsManager.getPlayerStats(event.getCause().first(Player.class).get());

			for (Transaction<BlockSnapshot> snapshot : event.getTransactions()) {
				player.addBrokenBlock(snapshot.getOriginal().getState().getType());
			}
		}).async().submit(Enjin.getPlugin());
    }

	@Listener(order = Order.LAST)
    public void onBlockPlace(final Place event) {
		if (event.isCancelled() || !event.getCause().containsType(Player.class)) {
			return;
		}

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
			StatsPlayer player = StatsManager.getPlayerStats(event.getCause().first(Player.class).get());

			for (Transaction<BlockSnapshot> snapshot : event.getTransactions()) {
				player.addPlacedBlock(snapshot.getFinal().getState().getType());
			}
		}).async().submit(Enjin.getPlugin());
    }

    //Listener to increment player deaths
	@Listener(order = Order.LAST)
    public void onPlayerDeath(final Death event) {
		if (!(event.getTargetEntity() instanceof Player)) {
			return;
		}

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
			Player p = (Player) event.getTargetEntity();
			StatsPlayer player = StatsManager.getPlayerStats(p);
			player.addDeath();
		}).async().submit(Enjin.getPlugin());
    }

    //Listener to increment player kills
	@Listener(order = Order.LAST)
    public void onEntityDeath(final Death event) {
		if (!event.getCause().containsType(Player.class)) {
			return;
		}

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			Player p = event.getCause().first(Player.class).get();
			StatsPlayer player = StatsManager.getPlayerStats(p);

			player.addKilled();
			//Let's add to the player's pvp or pve kills
			if (event.getTargetEntity() instanceof Player) {
				player.addPvpkill();
			} else {
				player.addPvekill(event.getTargetEntity().getType());
			}
		}).async().submit(Enjin.getPlugin());
    }

    //Listener to add distance traveled to players
	@Listener(order = Order.LAST)
    public void onPlayerMove(final MoveEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getTargetEntity() instanceof Player)) {
        	return;
		}

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			Player p = (Player) event.getTargetEntity();
			StatsPlayer player = StatsManager.getPlayerStats(p);

			Location<World> from = event.getFromTransform().getLocation();
			Location<World> to = event.getToTransform().getLocation();

			//Make sure we didn't teleport between two worlds or something
			if (!from.getExtent().getName().equals(to.getExtent().getName())) {
				return;
			}

			double distance = (to.getX() - from.getX()) * (to.getX() - from.getX()) + (to.getY() - from.getY()) * (to.getY() - from.getY()) + (to.getZ() - from.getZ()) * (to.getZ() - from.getZ());

			//You can't go over 4 blocks before a teleport event happens, otherwise you are using hacks to move.
			if (distance > 36) {
				return;
			}

			if (p.getVehicle().isPresent()) {
				Entity vehicle = p.getVehicle().get();
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
		}).async().submit(Enjin.getPlugin());
    }

	@Listener(order = Order.LAST)
    public void onPlayerExpChange(final ChangeEntityExperienceEvent event) {
		if (event.isCancelled() || !(event.getTargetEntity() instanceof Player)) {
			return;
		}

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			Player p = (Player) event.getTargetEntity();
			StatsPlayer player = StatsManager.getPlayerStats(p);

			if (player != null && p != null) {
				ExperienceHolderData data = p.get(ExperienceHolderData.class).get();
				if (data != null) {
					player.setXpLevel(data.level().get());
					player.setTotalXp(data.totalExperience().get());
				}
			}
		}).async().submit(Enjin.getPlugin());
    }
}
