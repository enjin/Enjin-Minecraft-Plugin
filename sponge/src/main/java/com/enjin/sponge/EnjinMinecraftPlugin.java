package com.enjin.sponge;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.command.CommandBank;
import com.enjin.sponge.command.commands.BuyCommand;
import com.enjin.sponge.command.commands.CoreCommands;
import com.enjin.sponge.command.commands.PointCommands;
import com.enjin.sponge.command.commands.StatCommands;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.config.ExecutedCommandsConfig;
import com.enjin.sponge.config.RankUpdatesConfig;
import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.listeners.SignListener;
import com.enjin.sponge.managers.PurchaseManager;
import com.enjin.sponge.managers.StatSignManager;
import com.enjin.sponge.managers.StatsManager;
import com.enjin.sponge.shop.ShopListener;
import com.enjin.sponge.stats.StatsPlayer;
import com.enjin.sponge.stats.StatsServer;
import com.enjin.sponge.sync.RPCPacketManager;
import com.enjin.sponge.sync.SpongeInstructionHandler;
import com.enjin.sponge.tasks.TPSMonitor;
import com.enjin.sponge.utils.Log;
import com.enjin.sponge.utils.commands.CommandWrapper;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Plugin(id = "com.enjin.sponge", name = "EnjinMinecraftPlugin", description = "Enjin Minecraft Plugin for Sponge", version = "2.8.3-sponge")
public class EnjinMinecraftPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static List<CommandSpec> commands = Lists.newArrayList();
    @Getter
    private static List<CommandWrapper> processedCommands = Lists.newArrayList();
	private static RankUpdatesConfig rankUpdatesConfiguration;
	private static ExecutedCommandsConfig executedCommandsConfiguration;

    @Inject
    @Getter
    private PluginContainer container;
    @Inject
    @Getter
    private Logger logger;
    @Inject
    @Getter
    private java.util.logging.Logger javaLogger;
    @Inject
    @ConfigDir(sharedRoot = false)
    @Getter
    private File configDir;
    @Inject
    @Getter
    private Game game;
	@Getter
	private SpongeExecutorService sync;
	@Getter
	private SpongeExecutorService async;

    @Getter
    private Task syncTask;

	@Getter
	private InstructionHandler instructionHandler = new SpongeInstructionHandler();
    @Getter
    private boolean firstRun = true;
    @Getter @Setter
    private boolean authKeyInvalid = false;

	@Getter
	private StatsServer serverStats = new StatsServer();
	@Getter
	private Map<String, StatsPlayer> playerStats = new ConcurrentHashMap<>();

    public EnjinMinecraftPlugin() {
        instance = this;
        Enjin.setPlugin(this);
    }

    @Listener
    public void initialization(GameInitializationEvent event) {
        init();
    }

	@Listener
	public void stopping(GameStoppingEvent event) {
		disable();
	}

    public void init() {
        if (authKeyInvalid) {
            return;
        }

        if (firstRun) {
			sync = Sponge.getScheduler().createSyncExecutor(this);
			async = Sponge.getScheduler().createAsyncExecutor(this);

			firstRun = false;
            initConfigs();

            Enjin.setLogger(new Log(configDir));
            debug("Init config done.");

            initCommands();
            debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(Optional.absent(), getPort(), true);
                if (data == null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (!data.getResult()) {
                    authKeyInvalid = true;
                    debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }
            } else {
                authKeyInvalid = true;
                debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }

        initManagers();
        debug("Init managers done.");
        initListeners();
        debug("Init listeners done.");
        initTasks();
        debug("Init tasks done.");
    }

	private void initConfigs() {
		initConfig();
		initCommandsConfiguration();
		initRankUpdatesConfiguration();
	}

    public void initConfig() {
        logger.info("Initializing EMP Config");
        EMPConfig config = JsonConfig.load(new File(configDir, "config.json"), EMPConfig.class);
        Enjin.setConfiguration(config);
    }

	private void initCommandsConfiguration() {
		File configFile = new File(configDir, "commands.json");
		EnjinMinecraftPlugin.executedCommandsConfiguration = JsonConfig.load(configFile, ExecutedCommandsConfig.class);

		if (!configFile.exists()) {
			executedCommandsConfiguration.save(configFile);
		}
	}

	private void initRankUpdatesConfiguration() {
		File configFile = new File(configDir, "rankUpdates.json");
		EnjinMinecraftPlugin.rankUpdatesConfiguration = JsonConfig.load(configFile, RankUpdatesConfig.class);

		if (!configFile.exists()) {
			rankUpdatesConfiguration.save(configFile);
		}
	}

    private void initCommands() {
        logger.info("Initializing EMP Commands");
        CommandBank.setup(this);

        CommandBank.register(BuyCommand.class, CoreCommands.class, PointCommands.class,
				StatCommands.class);
    }

	private void initManagers() {
		logger.info("Initializing EMP Managers");
		PurchaseManager.init();
		StatsManager.init(this);
		StatSignManager.init(this);
	}

    private void initListeners() {
		logger.info("Initializing EMP Listeners");
        game.getEventManager().registerListeners(this, new ShopListener());
		game.getEventManager().registerListeners(this, new ConnectionListener());
    }

    public void initTasks() {
        if (syncTask != null) {
            stopTasks();
        }

        syncTask = game.getScheduler().createTaskBuilder()
                .execute(new RPCPacketManager(this))
                .async().interval(60, TimeUnit.SECONDS)
                .submit(this);

		game.getScheduler().createTaskBuilder()
				.execute(new TPSMonitor())
				.async().interval(2, TimeUnit.SECONDS)
				.submit(this);
    }

	public void disable() {
		StatsManager.disable();
	}

    public void stopTasks() {
        syncTask.cancel();
        syncTask = null;
    }

    public Integer getPort() {
        return game.getServer().getBoundAddress().get().getPort();
    }

    @Override
    public InstructionHandler getInstructionHandler() {
        return instructionHandler;
    }

    @Override
    public void debug(String s) {
        Enjin.getLogger().debug(s);
    }

    public static void saveConfiguration() {
        Enjin.getConfiguration().save(new File(instance.configDir, "config.json"));
    }

	public static void saveExecutedCommandsConfiguration() {
		if (executedCommandsConfiguration == null) {
			instance.initCommandsConfiguration();
		}

		executedCommandsConfiguration.save(new File(instance.configDir, "commands.json"));
	}

	public static void saveRankUpdatesConfiguration() {
		if (rankUpdatesConfiguration == null) {
			instance.initRankUpdatesConfiguration();
		}

		rankUpdatesConfiguration.save(new File(instance.configDir, "rankUpdates.json"));
	}

	public static ExecutedCommandsConfig getExecutedCommandsConfiguration() {
		if (executedCommandsConfiguration == null) {
			instance.initCommandsConfiguration();
		}

		return executedCommandsConfiguration;
	}

	public static RankUpdatesConfig getRankUpdatesConfiguration() {
		if (rankUpdatesConfiguration == null) {
			instance.initRankUpdatesConfiguration();
		}

		return rankUpdatesConfiguration;
	}
}
