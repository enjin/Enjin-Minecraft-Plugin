package com.enjin.bukkit;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.bukkit.command.CommandBank;
import com.enjin.bukkit.command.commands.*;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.managers.StatsManager;
import com.enjin.bukkit.managers.TicketManager;
import com.enjin.bukkit.managers.VaultManager;
import com.enjin.bukkit.managers.VotifierManager;
import com.enjin.bukkit.util.Log;
import com.enjin.bukkit.util.io.EnjinErrorReport;
import com.enjin.bukkit.listeners.*;
import com.enjin.bukkit.listeners.perm.*;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import lombok.Getter;

import lombok.Setter;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;

import com.enjin.bukkit.tasks.BanLister;
import com.enjin.bukkit.tasks.CurseUpdater;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static EnjinConfig configuration;
    @Getter
    private InstructionHandler instructionHandler = new BukkitInstructionHandler();
    @Getter
    private boolean firstRun = true;

    @Getter
    private boolean tuxTwoLibInstalled = false;
    @Getter
    private boolean globalGroupsSupported = true;
    @Getter
    private StatsServer serverStats = new StatsServer(this);
    @Getter
    private Map<String, StatsPlayer> playerStats = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that banned the player or blank if the console banned
     */
    @Getter
    private Map<String, String> bannedPlayers = new ConcurrentHashMap<>();
    /**
     * Key is banned player, value is admin that pardoned the player or blank if the console pardoned
     */
    @Getter
    private Map<String, String> pardonedPlayers = new ConcurrentHashMap<>();
    @Getter @Setter
    private String newVersion = "";
    @Getter @Setter
    private boolean hasUpdate = false;
    @Getter @Setter
    private boolean updateFailed = false;
    @Getter @Setter
    private boolean authKeyInvalid = false;
    @Getter @Setter
    private boolean unableToContactEnjin = false;
    @Getter @Setter
    private boolean permissionsNotWorking = false;
    @Getter
    private LinkedList<String> keywords = new LinkedList<String>();

    //-------------Thread IDS-------------------
    @Getter
    private Map<String, String> playerPerms = new ConcurrentHashMap<>();
    //Player, lists voted on.
    @Getter
    private Map<String, List<String>> playerVotes = new ConcurrentHashMap<>();
    @Getter @Setter
    private EnjinErrorReport lastError = null;

    @Override
    public void debug(String s) {
        if (configuration.isDebug()) {
            getLogger().info("Enjin Debug: " + s);
        }

        if (configuration.isLoggingEnabled()) {
            Log.debug(s);
        }
    }

    @Override
    public void onEnable() {
        Enjin.setPlugin(this);
        instance = this;

        //Add keywords for item giving
        keywords.add("-name");
        keywords.add("-color");
        keywords.add("-repairxp");
        keywords.add("--n");
        keywords.add("--c");
        keywords.add("--r");

        init();
    }

    @Override
    public void onDisable() {
        disableTasks();
        disableManagers();
    }

    public void init() {
        if (firstRun) {
            firstRun = false;
            initConfig();

            EnjinRPC.setLogger(getLogger());
            EnjinRPC.setDebug(configuration.isDebug());
            Log.init();
            debug("Init config done.");

            initCommands();
            debug("Init commands done.");

            if (configuration.getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(configuration.getAuthKey(), Bukkit.getPort(), true);
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
        initPlugins();
        debug("Init plugins done.");
        initPermissions();
        debug("Init permissions done.");
        initListeners();
        debug("Init listeners done.");
        initTasks();
        debug("Init tasks done.");

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            debug("Failed to start metrics.");
        }
    }

    private void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EnjinMinecraftPlugin.configuration = JsonConfig.load(configFile, EnjinConfig.class);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }

        if (!configuration.getApiUrl().endsWith("/")) {
            configuration.setApiUrl(configuration.getApiUrl().concat("/"));
        }

        EnjinRPC.setHttps(configuration.isHttps());
        EnjinRPC.setApiUrl(configuration.getApiUrl());
        debug("RPC API Url: " + configuration.getApiUrl());
    }

    public static void saveConfiguration() {
        configuration.save(new File(instance.getDataFolder(), "config.json"));
    }

    private void initCommands() {
        CommandBank.setup(this);
        CommandBank.register(BuyCommand.class, CoreCommands.class, StatCommands.class, HeadCommands.class, SupportCommands.class);

        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            CommandBank.register(VoteCommands.class);
        }

        if (configuration.getBuyCommand() != null && !configuration.getBuyCommand().isEmpty()) {
            CommandBank.registerCommandAlias("buy", configuration.getBuyCommand());
        }
    }

    private void initManagers() {
        VaultManager.init(this);
        VotifierManager.init(this);
        TicketManager.init(this);
        StatsManager.init(this);
    }

    private void disableManagers() {
        StatsManager.disable(this);
    }

    public void initTasks() {
        debug("Starting tasks.");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new RPCPacketManager(this), 20L * 60L, 20L * 60L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new BanLister(this), 20L * 2L, 20L * 90L).getTaskId();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TPSMonitor(), 20L * 2L, 20L * 4L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new CurseUpdater(this, 44560, this.getFile(), CurseUpdater.UpdateType.DEFAULT, true), 20L * 60L * 5L, 20L * 60L * 5L).getTaskId();
    }

    public void disableTasks() {
        debug("Stopping tasks.");
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void initListeners() {
        debug("Initializing Listeners");
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    private void initPlugins() {
        if (Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxTwoLibInstalled = true;
            Log.info("TuxTwoLib is installed. Offline players can be given items.");
            getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Log.warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            return;
        }
    }

    private void initPermissions() {
        if (Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PexChangeListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
            debug("bPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new BPermissionsListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("zPermissions")) {
            debug("zPermissions found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new ZPermissionsListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
            debug("PermissionsBukkit found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PermissionsBukkitChangeListener(this), this);
            return;
        } else if (Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
            debug("GroupManager found, hooking custom events.");
            globalGroupsSupported = false;
            Bukkit.getPluginManager().registerEvents(new GroupManagerListener(), this);
            return;
        } else {
            debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        }
    }

    public void addCustomData(ItemStack is, String[] args, OfflinePlayer reciever, int startingpos) {
        for (int i = startingpos; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-name") || args[i].equalsIgnoreCase("--n")) {
                boolean noflags = true;
                i++;
                StringBuilder name = new StringBuilder();
                while (noflags && i < args.length) {
                    if (keywords.contains(args[i].toLowerCase())) {
                        noflags = false;
                        i--;
                    } else {
                        name.append(args[i] + " ");
                        i++;
                    }
                }
                addName(is, ChatColor.translateAlternateColorCodes('&', name.toString().trim()));
            } else if (args[i].equalsIgnoreCase("-color") || args[i].equalsIgnoreCase("--c")) {
                i++;
                if (args.length > i) {
                    try {
                        String[] rgb = args[i].split(",");
                        int r = 0;
                        int g = 0;
                        int b = 0;
                        for (String col : rgb) {
                            col = col.toLowerCase();
                            if (col.startsWith("r")) {
                                r = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("g")) {
                                g = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("b")) {
                                b = Integer.parseInt(col.substring(1));
                            }
                        }
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof LeatherArmorMeta) {
                            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(r, g, b));
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            } else if (args[i].equalsIgnoreCase("-repairxp") || args[i].equalsIgnoreCase("--r")) {
                i++;
                if (args.length > i) {
                    try {
                        int repaircost = Integer.parseInt(args[i]);
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof Repairable) {
                            ((Repairable) meta).setRepairCost(repaircost);
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
    }

    public ItemStack addName(ItemStack is, String name) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);
        return is;
    }
}
