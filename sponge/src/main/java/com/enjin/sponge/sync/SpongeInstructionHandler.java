package com.enjin.sponge.sync;

import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.google.common.base.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongeInstructionHandler implements InstructionHandler {
	@Override
	public void addToWhitelist (String player) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "whitelist add " + player);
	}

	@Override
	public void removeFromWhitelist (String player) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "whitelist remove " + player);
	}

	@Override
	public void ban(String player) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "ban " + player);
	}

	@Override
	public void pardon(String player) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "pardon " + player);
	}


	@Override
	public void addToGroup (String player, String group, String world) {
		// TODO
	}

	@Override
	public void removeFromGroup (String player, String group, String world) {
		// TODO
	}

	@Override
	public void execute (long id, String command, long delay, Optional<Boolean> requireOnline, Optional<String> name, Optional<String> uuid) {
		for (ExecutedCommand c : EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()) {
			if (Long.parseLong(c.getId()) == id) {
				return;
			}
		}

		Runnable runnable = () -> {
			if (requireOnline.isPresent() && requireOnline.get().booleanValue()) {
				if (uuid.isPresent() || name.isPresent()) {
					Player player = (uuid.isPresent() && !uuid.get().isEmpty()) ? Sponge.getServer().getPlayer(UUID.fromString(uuid.get())).get() : Sponge.getServer().getPlayer(name.get()).get();
					if (player == null || !player.isOnline()) {
						return;
					}
				} else {
					return;
				}
			}

			Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
			if (id > -1) {
				EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands().add(new ExecutedCommand(Long.toString(id), command, Enjin.getLogger().getLastLine()));
				EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
			}
		};

		if (delay <= 0) {
			Sponge.getScheduler().createTaskBuilder().execute(runnable)
					.submit(Enjin.getPlugin());
		} else {
			Sponge.getScheduler().createTaskBuilder().execute(runnable)
					.delay(delay, TimeUnit.SECONDS)
					.submit(Enjin.getPlugin());
		}
	}

	@Override
	public void commandConfirmed (List<Long> executed) {
		for (ExecutedCommand command : new ArrayList<>(EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands())) {
			for (long id : executed) {
				Enjin.getPlugin().debug("Confirming Command ID: " + id);
				if (Long.parseLong(command.getId()) == id) {
					EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands().remove(command);
				}
			}
		}

		EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
	}

	@Override
	public void configUpdated (Object update) {
		EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
		if (config != null) {
			config.update(new File(EnjinMinecraftPlugin.getInstance().getConfigDir(), "config.json"), update);
			EnjinMinecraftPlugin.getInstance().initConfig();
			EnjinMinecraftPlugin.saveConfiguration();
		}
	}

	@Override
	public void statusReceived (String status) {
		Enjin.getLogger().debug("Enjin Status: " + status);
	}

	@Override
	public void clearInGameCache (String player, int id, String price) {
		// TODO
	}

	@Override
	public void notify (List<String> players, String message, long time) {
		for (String player : players) {
			java.util.Optional<Player> p = Sponge.getServer().getPlayer(player);
			if (p.isPresent()) {
				p.get().sendMessage(Text.of(message));
			}
		}
	}

	@Override
	public void version (String version) {
		// TODO
	}
}
