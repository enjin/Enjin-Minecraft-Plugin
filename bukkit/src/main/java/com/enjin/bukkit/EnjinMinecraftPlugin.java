package com.enjin.bukkit;

import com.enjin.bukkit.cmd.CmdEnjin;
import com.enjin.bukkit.cmd.legacy.CommandBank;
import com.enjin.bukkit.cmd.legacy.commands.BuyCommand;
import com.enjin.bukkit.cmd.legacy.commands.ConfigCommand;
import com.enjin.bukkit.cmd.legacy.commands.CoreCommands;
import com.enjin.bukkit.cmd.legacy.commands.HeadCommands;
import com.enjin.bukkit.cmd.legacy.commands.PointCommands;
import com.enjin.bukkit.cmd.legacy.commands.SupportCommands;
import com.enjin.bukkit.cmd.legacy.commands.VoteCommands;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.i18n.Locale;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.listeners.BanListeners;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.listeners.perm.processors.BPermissionsListener;
import com.enjin.bukkit.listeners.perm.processors.GroupManagerListener;
import com.enjin.bukkit.listeners.perm.processors.PermissionsBukkitListener;
import com.enjin.bukkit.listeners.perm.processors.PexListener;
import com.enjin.bukkit.listeners.perm.processors.ZPermissionsListener;
import com.enjin.bukkit.modules.ModuleManager;
import com.enjin.bukkit.modules.impl.SignStatsModule;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.storage.Database;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.CommandExecutor;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tasks.BanLister;
import com.enjin.bukkit.tasks.ConfigSaver;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.bukkit.util.Log;
import com.enjin.bukkit.util.Plugins;
import com.enjin.bukkit.util.ui.MenuAPI;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin   instance;
    @Getter
    private        InstructionHandler     instructionHandler = new BukkitInstructionHandler();
    @Getter
    private        boolean                firstRun           = true;
    @Getter
    private        MenuAPI                menuAPI;

    @Getter
    private String mcVersion;

    @Getter
    private boolean                  tuxTwoLibInstalled    = false;
    @Getter
    private boolean                  globalGroupsSupported = true;
    @Getter
    private StatsServer              serverStats           = new StatsServer(this);
    @Getter
    private Map<String, StatsPlayer> playerStats           = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that banned the player or blank if the console banned
     */
    @Getter
    private Map<String, String>      bannedPlayers         = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that pardoned the player or blank if the console pardoned
     */
    @Getter
    private Map<String, String>      pardonedPlayers       = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private String                   newVersion            = "";
    @Getter
    @Setter
    private boolean                  hasUpdate             = false;
    @Getter
    @Setter
    private boolean                  updateFailed          = false;
    @Getter
    @Setter
    private boolean                  authKeyInvalid        = false;
    @Getter
    @Setter
    private boolean                  unableToContactEnjin  = false;
    @Getter
    @Setter
    private boolean                  permissionsNotWorking = false;

    @Getter
    private PermissionListener permissionListener;

    @Getter
    private ModuleManager moduleManager = null;

    @Getter
    private long serverId = -1;

    private Database database;
    @Getter
    private CommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        instance = this;
        Enjin.setPlugin(instance);
        init();
    }

    @Override
    public void onDisable() {
        disableTasks();
        disableManagers();

        try {
            saveConfiguration();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            database.commit();
            database.backup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initVersion() {
        String   bukkitVersion = Bukkit.getBukkitVersion();
        String[] versionParts  = bukkitVersion.split("-");
        mcVersion = versionParts.length >= 1 ? versionParts[0] : "UNKNOWN";
    }

    public void init() {

        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + "   ___    _  _        _    ___    _  _ ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + "  | __|  | \\| |    _ | |  |_ _|  | \\| |");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + "  | _|   | .` |   | || |   | |   | .` |");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + "  |___|  |_|\\_|    \\__/   |___|  |_|\\_|");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "  Enjin Minecraft Plugin " + ChatColor.GOLD + getDescription().getVersion());

        if (firstRun) {
            Log log = new Log(getDataFolder());
            Enjin.setLogger(log);

            Enjin.getLogger().info("Initializing for the first time.");
            initConfigs();
            log.configure();
            log.setDebug(Enjin.getConfiguration().isDebug());

            loadLocales();

            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {
                Enjin.getLogger().debug("Failed to start metrics.");
            }

            menuAPI = new MenuAPI(this);
            Enjin.getLogger().debug("Init gui api done.");

            initVanishPredicate();

            moduleManager = new ModuleManager(this);

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                RPCData<Auth> data = EnjinServices.getService(PluginService.class)
                                                  .auth(Optional.<String>absent(), Bukkit.getPort(), true, true);
                if (data == null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Data could not be retrieved.");
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. " + data.getError().getMessage());
                } else if (data.getResult() == null || !data.getResult().isAuthed()) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Failed to authenticate.");
                } else {
                    Auth auth = data.getResult();
                    if (auth.getServerId() > 0) {
                        serverId = auth.getServerId();
                    }
                }
            } else {
                authKeyInvalid = true;
                Enjin.getLogger().debug("Auth key is invalid. Must be 50 characters in length.");
            }

            initVersion();
            Enjin.getLogger().debug("Init version done.");
//            initCommands();
            new CmdEnjin(this);
//            Enjin.getLogger().debug("Init commands done.");
            initListeners();
            Enjin.getLogger().debug("Init listeners done.");

            firstRun = false;
        }

        if (authKeyInvalid) {
            Enjin.getLogger().debug("Auth key is invalid. Stopping initialization.");
            return;
        }

        commandExecutor = new CommandExecutor(this);
        commandExecutor.runTaskTimerAsynchronously(this, 20, 20);

        moduleManager.init();
        Enjin.getLogger().debug("Init modules done.");
        initPlugins();
        Enjin.getLogger().debug("Init plugins done.");

        if (Plugins.isEnabled("Vault")) {
            initPermissions();
            Enjin.getLogger().debug("Init permissions done.");
        }

        initTasks();
        Enjin.getLogger().debug("Init tasks done.");
    }

    public void initConfigs() {
        initConfig();

        try {
            database = new Database(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initConfig() {
        try {
            File      configFile    = new File(getDataFolder(), "config.json");
            EMPConfig configuration = JsonConfig.load(configFile, EMPConfig.class);
            Enjin.setConfiguration(configuration);

            if (configuration.getSyncDelay() < 0) {
                configuration.setSyncDelay(0);
            } else if (configuration.getSyncDelay() > 10) {
                configuration.setSyncDelay(10);
            }

            configuration.save(configFile);
        } catch (Exception e) {
            Enjin.getLogger().warning("Error occurred while initializing enjin configuration.");
            Enjin.getLogger().log(e);
        }
    }

    public static void saveConfiguration() {
        if (Enjin.getConfiguration() == null) {
            instance.initConfig();
        }

        synchronized (Enjin.getConfiguration()) {
            Enjin.getConfiguration().save(new File(instance.getDataFolder(), "config.json"));
        }
    }

    private void initCommands() {
        CommandBank.register(CoreCommands.class, BuyCommand.class, // StatCommands.class,
                             HeadCommands.class, SupportCommands.class, PointCommands.class, ConfigCommand.class);

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier") && Enjin.getConfiguration(EMPConfig.class).getEnabledComponents().isVoteListener()) {
            CommandBank.register(VoteCommands.class);
        }

        String buyCommand = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
        if (buyCommand != null && !buyCommand.isEmpty() && !CommandBank.isCommandRegistered(buyCommand)) {
            CommandBank.replaceCommandWithAlias("buy", buyCommand);
        }
    }

    private void disableManagers() {
        SignStatsModule signStats = moduleManager.getModule(SignStatsModule.class);

        if (signStats != null) {
            signStats.disable();
        }
    }

    public void loadLocales() {
        Translation.setServerLocale(Locale.en_US);
        Translation.loadLocales(this);
    }

    public void initTasks() {
        Enjin.getLogger().debug("Starting tasks.");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new RPCPacketManager(this), 20L * 60L, 20L * 60L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TPSMonitor(), 20L * 2L, 20L * 2L);
        ConfigSaver.schedule(this);

        if (Enjin.getConfiguration(EMPConfig.class).isListenForBans()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BanLister(this), 20L * 2L, 20L * 90L);
        }
    }

    public void disableTasks() {
        Enjin.getLogger().debug("Stopping tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void initListeners() {
        Enjin.getLogger().debug("Initializing Listeners");
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    private void initPlugins() {
        if (Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxTwoLibInstalled = true;
            Enjin.getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
            getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
        }
    }

    private void initPermissions() {
        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            Enjin.getLogger().debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PexListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            Enjin.getLogger().debug("bPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new BPermissionsListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("zPermissions")) {
            Enjin.getLogger().debug("zPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new ZPermissionsListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
            Enjin.getLogger().debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(permissionListener = new PermissionsBukkitListener(), this);
        } else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager") || Bukkit.getPluginManager()
                                                                                      .isPluginEnabled("GroupManagerX")) {
            Enjin.getLogger().debug("GroupManager found, hooking custom events.");
            globalGroupsSupported = false;
            Bukkit.getPluginManager().registerEvents(permissionListener = new GroupManagerListener(), this);
        } else {
            Enjin.getLogger()
                 .debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            Enjin.getLogger().debug("You might want to switch to PermissionsEx or bPermissions.");
        }
    }

    private void initVanishPredicate() {
        Enjin.getApi().registerVanishPredicate(new Predicate<UUID>() {
            @Override
            public boolean apply(@Nullable UUID input) {
                boolean vanished = false;
                if (input != null) {
                    Player player = Bukkit.getPlayer(input);
                    if (player != null) {
                        List<MetadataValue> values = player.getMetadata("vanished");
                        for (MetadataValue value : values) {
                            try {
                                if (value.asBoolean()) {
                                    vanished = true;
                                    break;
                                }
                            } catch (Exception e) {
                                Enjin.getLogger()
                                     .debug("Vanished metadata from " + value.getOwningPlugin()
                                                                             .getName() + " is not of type boolean.");
                            }
                        }
                    }
                }
                return vanished;
            }
        });
    }

    public static void dispatchConsoleCommand(String command) {
        Enjin.getLogger().debug("Dispatching command: " + command);
        Bukkit.getScheduler()
                .scheduleSyncDelayedTask((Plugin) Enjin.getPlugin(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    public static void dispatchConsoleCommand(String command, Runnable callback, boolean async) {
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin) Enjin.getPlugin(), () -> {
            Enjin.getLogger().debug("Dispatching command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            if (async)
                Bukkit.getScheduler().runTaskAsynchronously((Plugin) Enjin.getPlugin(), callback);
            else
                callback.run();
        });
    }

    public boolean isUpdateFromCurseForge() {
        return getDescription().getVersion().endsWith("-bukkit");
    }

    public Database db() {
        return database;
    }

}
