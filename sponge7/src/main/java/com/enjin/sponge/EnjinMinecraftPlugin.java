package com.enjin.sponge;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.command.CommandBank;
import com.enjin.sponge.command.commands.BuyCommand;
import com.enjin.sponge.command.commands.ConfigCommand;
import com.enjin.sponge.command.commands.CoreCommands;
import com.enjin.sponge.command.commands.HeadCommands;
import com.enjin.sponge.command.commands.PointCommands;
import com.enjin.sponge.command.commands.SupportCommands;
import com.enjin.sponge.command.commands.VoteCommands;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.config.ExecutedCommandsConfig;
import com.enjin.sponge.config.RankUpdatesConfig;
import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.managers.PurchaseManager;
import com.enjin.sponge.managers.StatSignManager;
import com.enjin.sponge.managers.TicketManager;
import com.enjin.sponge.managers.VotifierManager;
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
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Plugin(id = "enjin-minecraft-plugin", dependencies = {
        @Dependency(id = "ninja.leaping.permissionsex", optional = true),
        @Dependency(id = "permissionmanager", optional = true),
        @Dependency(id = "com.vexsoftware", optional = true),
        @Dependency(id = "nuvotifier", optional = true)
})
public class EnjinMinecraftPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin   instance;
    @Getter
    private static List<CommandSpec>      commands          = Lists.newArrayList();
    @Getter
    private static List<CommandWrapper>   processedCommands = Lists.newArrayList();
    private static RankUpdatesConfig      rankUpdatesConfiguration;
    private static ExecutedCommandsConfig executedCommandsConfiguration;

    @Inject
    @Getter
    private PluginContainer          container;
    @Inject
    @Getter
    private Logger                   logger;
    @Inject
    @Getter
    private java.util.logging.Logger javaLogger;
    @Inject
    @ConfigDir(sharedRoot = false)
    @Getter
    private File                     configDir;
    @Inject
    @Getter
    private Game                     game;
    @Getter
    private SpongeExecutorService    sync;
    @Getter
    private SpongeExecutorService    async;

    @Getter
    private Task syncTask;

    @Getter
    private InstructionHandler instructionHandler = new SpongeInstructionHandler();
    @Getter
    private boolean            firstRun           = true;
    @Getter
    @Setter
    private boolean            authKeyInvalid     = false;

    @Getter
    private StatsServer              serverStats = new StatsServer();
    @Getter
    private Map<String, StatsPlayer> playerStats = new ConcurrentHashMap<>();

    @Getter
    private List<Long> pendingCommands  = Lists.newCopyOnWriteArrayList();
    @Getter
    private List<Long> executedCommands = Lists.newCopyOnWriteArrayList();
    @Getter
    private long       serverId         = -1;

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
            Log log = new Log(configDir);
            Enjin.setLogger(log);

            sync = Sponge.getScheduler().createSyncExecutor(this);
            async = Sponge.getScheduler().createAsyncExecutor(this);

            firstRun = false;
            initConfigs();
            log.configure();
            Enjin.getLogger().debug("Init config done.");

            initCommands();
            Enjin.getLogger().debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Auth> data = EnjinServices.getService(PluginService.class)
                                                  .auth(Optional.absent(), getPort(), true, true);
                if (data == null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (data.getResult() == null || !data.getResult().isAuthed()) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }

                Auth auth = data.getResult();
                if (auth.getServerId() > 0) {
                    serverId = auth.getServerId();
                }
            } else {
                authKeyInvalid = true;
                Enjin.getLogger().debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }

        initManagers();
        Enjin.getLogger().debug("Init managers done.");
        initListeners();
        Enjin.getLogger().debug("Init listeners done.");
        initTasks();
        Enjin.getLogger().debug("Init tasks done.");
    }

    private void initConfigs() {
        initConfig();
        initCommandsConfiguration();
        initRankUpdatesConfiguration();
    }

    public void initConfig() {
        try {
            File      configFile    = new File(configDir, "config.json");
            EMPConfig configuration = JsonConfig.load(configFile, EMPConfig.class);
            Enjin.setConfiguration(configuration);


            if (configuration.getSyncDelay() < 0) {
                configuration.setSyncDelay(0);
            } else if (configuration.getSyncDelay() > 10) {
                configuration.setSyncDelay(10);
            }

            configuration.save(configFile);
        } catch (Exception e) {
            Enjin.getLogger().warning("Error occurred while initializing enjin configuration");
        }
    }

    private void initCommandsConfiguration() {
        try {
            File configFile = new File(configDir, "commands.json");
            EnjinMinecraftPlugin.executedCommandsConfiguration = JsonConfig.load(configFile,
                                                                                 ExecutedCommandsConfig.class);

            if (!configFile.exists()) {
                executedCommandsConfiguration.save(configFile);
            }
        } catch (Exception e) {
            Enjin.getLogger().warning("Error occurred while initializing executed commands configuration.");
            Enjin.getLogger().log(e);
        }
    }

    private void initRankUpdatesConfiguration() {
        try {
            File configFile = new File(configDir, "rankUpdates.json");
            EnjinMinecraftPlugin.rankUpdatesConfiguration = JsonConfig.load(configFile, RankUpdatesConfig.class);

            if (!configFile.exists()) {
                rankUpdatesConfiguration.save(configFile);
            }
        } catch (Exception e) {
            Enjin.getLogger().warning("Error occurred while initializing rank updates configuration.");
            Enjin.getLogger().log(e);
        }
    }

    private void initCommands() {
        Enjin.getLogger().info("Initializing EMP Commands");
        CommandBank.setup(this);

        CommandBank.register(BuyCommand.class, CoreCommands.class, PointCommands.class,
                /* StatCommands.class, */ SupportCommands.class, ConfigCommand.class, HeadCommands.class,
                             VoteCommands.class);

        String buyCommand = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
        if (buyCommand != null && !buyCommand.isEmpty() && !CommandBank.isCommandRegistered(buyCommand)) {
            CommandBank.replaceCommandWithAlias("buy", buyCommand);
        }
    }

    private void initManagers() {
        Enjin.getLogger().info("Initializing EMP Managers");
        PurchaseManager.init();
        //		StatsManager.init(this);
        StatSignManager.init(this);
        VotifierManager.init(this);
        TicketManager.init(this);
    }

    private void initListeners() {
        Enjin.getLogger().info("Initializing EMP Listeners");
        game.getEventManager().registerListeners(this, new ShopListener());
        game.getEventManager().registerListeners(this, new ConnectionListener());
    }

    public void initTasks() {
        if (syncTask != null) {
            stopTasks();
        }

        Enjin.getLogger().debug("Registering Sync Task");
        async.scheduleAtFixedRate(new RPCPacketManager(this), 0, 60, TimeUnit.SECONDS).getTask();

        Enjin.getLogger().debug("Registering TPS Task");
        async.scheduleAtFixedRate(new TPSMonitor(), 0, 2, TimeUnit.SECONDS);
    }

    public void disable() {
        //        StatsManager.disable();
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
