package com.enjin.bukkit;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import com.enjin.bukkit.command.CommandBank;
import com.enjin.bukkit.command.commands.*;
import com.enjin.bukkit.compatibility.PlayerGetter;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.managers.VaultManager;
import com.enjin.bukkit.permlisteners.*;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsServer;
import com.enjin.bukkit.stats.StatsUtils;
import com.enjin.bukkit.stats.WriteStats;
import com.enjin.bukkit.sync.BukkitInstructionHandler;
import com.enjin.bukkit.sync.RPCPacketManager;
import com.enjin.bukkit.tickets.TicketListener;
import com.enjin.bukkit.tpsmeter.MonitorTPS;
import com.enjin.bukkit.util.PacketUtilities;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.rpc.mappings.services.TicketService;
import lombok.Getter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

import com.enjin.bukkit.compatibility.OnlinePlayerGetter;
import com.enjin.bukkit.listeners.EnjinStatsListener;
import com.enjin.bukkit.listeners.NewPlayerChatListener;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.bukkit.threaded.AsyncToSyncEventThrower;
import com.enjin.bukkit.threaded.BanLister;
import com.enjin.bukkit.threaded.DelayedCommandExecuter;
import com.enjin.bukkit.threaded.PeriodicVoteTask;
import com.enjin.bukkit.threaded.Updater;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.vexsoftware.votifier.Votifier;

public class EnjinMinecraftPlugin extends JavaPlugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static EnjinConfig configuration;
    @Getter
    private InstructionHandler instructionHandler = new BukkitInstructionHandler();

    private VanishManager vanishmanager = null;
    private static boolean isMcMMOloaded = false;
    private boolean mcMMOSupported = false;
    public boolean supportsglobalgroups = true;
    public boolean votifierinstalled = false;
    protected boolean votifiererrored = false;
    public int xpversion = 0;
    private static boolean supportsuuid = false;
    @Getter
    private static boolean mcmmoOutdated = false;
    private boolean listenforbans = true;
    @Getter
    private boolean tuxtwolibinstalled = false;
    public final static Logger enjinLogger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    private EnjinLogInterface mcLogListener = new EnjinLogAppender();
    public StatsServer serverstats = new StatsServer(this);
    public Map<String, StatsPlayer> playerstats = new ConcurrentHashMap<String, StatsPlayer>();
    /**
     * Key is banned player, value is admin that banned the player or blank if the console banned
     */
    public Map<String, String> bannedplayers = new ConcurrentHashMap<String, String>();
    /**
     * Key is banned player, value is admin that pardoned the player or blank if the console pardoned
     */
    public Map<String, String> pardonedplayers = new ConcurrentHashMap<String, String>();
    public String newversion = "";
    public boolean hasupdate = false;
    public boolean updatefailed = false;
    public boolean authkeyinvalid = false;
    public boolean unabletocontactenjin = false;
    public boolean permissionsnotworking = false;
    public static boolean vaultneedsupdating = false;
    public boolean gmneedsupdating = false;
    private LinkedList<String> keywords = new LinkedList<String>();
    public OnlinePlayerGetter playergetter = new PlayerGetter();
    public AsyncToSyncEventThrower eventthrower = new AsyncToSyncEventThrower(this);
    public final EMPListener listener = new EMPListener(this);

    //------------Threaded tasks---------------
    public Runnable task;
    public PeriodicVoteTask votetask;
    public BanLister banlistertask;
    public DelayedCommandExecuter commexecuter = new DelayedCommandExecuter(this);
    //Initialize in the onEnable
    public MonitorTPS tpstask;

    //-------------Thread IDS-------------------
    private int synctaskid = -1;
    private int votetaskid = -1;
    private int banlisttask = -1;
    private int commandexecutertask = -1;
    private int headsupdateid = -1;
    private int updatethread = -1;
    public static final ExecutorService exec = Executors.newCachedThreadPool();
    public static boolean usingSSL = true;
    public Map<String, String> playerperms = new ConcurrentHashMap<String, String>();
    //Player, lists voted on.
    public Map<String, List<String>> playervotes = new ConcurrentHashMap<String, List<String>>();
    private Map<String, CommandWrapper> commandids = new ConcurrentHashMap<String, CommandWrapper>();
    public EnjinErrorReport lasterror = null;
    public EnjinStatsListener esl = null;

    //-------------Ticket Service---------------
    @Getter
    private static Map<Integer, Module> modules = new HashMap<Integer, Module>();
    @Getter
    private static long modulesLastPolled = 0;
    @Getter
    private static TicketListener ticketListener;

    @Override
    public void debug(String s) {
        if (configuration.isDebug()) {
            System.out.println("Enjin Debug: " + s);
        }
        if (configuration.isLoggingEnabled()) {
            enjinLogger.fine(s);
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

        try {
            initConfig();

            EnjinRPC.setLogger(getLogger());
            EnjinRPC.setDebug(configuration.isDebug());

            VaultManager.init(this);

            initCommands();

            task = new RPCPacketManager(this);
            votetask = new PeriodicVoteTask(this);

            debug("Initializing internal logger");
            enjinLogger.setLevel(Level.FINEST);
            File logsfolder = new File(getDataFolder().getAbsolutePath() + File.separator + "logs");
            if (!logsfolder.exists()) {
                logsfolder.mkdirs();
            }
            File logsfile = new File(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log");
            if (logsfile.exists()) {
                //Max file size of the enjin log should be less than 5MB.
                if (logsfile.length() > 1024 * 1024 * 5) {
                    logsfile.delete();
                }
            }
            FileHandler fileTxt = new FileHandler(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
            EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
            fileTxt.setFormatter(formatterTxt);
            enjinLogger.addHandler(fileTxt);
            enjinLogger.setUseParentHandlers(false);
            debug("Logger initialized.");
            /*
            Append MC log listener to the root logger.
             */
            org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
            log.addAppender((Appender) mcLogListener);

            debug("Get the ban list");
            banlistertask = new BanLister(this);
            debug("Ban list loaded");
            loadCommandIDs();
            debug("Init files done.");
            initPlugins();
            debug("Init plugins done.");
            setupPermissions();
            debug("Setup permissions integration");
            setupVotifierListener();
            debug("Setup Votifier integration");

            //------We should do TPS even if we have an invalid auth key
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, tpstask = new MonitorTPS(this), 40, 40);

            //Thread configthread = new Thread(new ConfigSender(this));
            //configthread.start();

            if (configuration.isCollectPlayerStats()) {
                startStatsCollecting();
                File stats = new File("enjin-stats.json");
                if (stats.exists()) {
                    //Let's not error when we fail to parse the stats.
                    try {
                        String content = readFile(stats, Charset.forName("UTF-8"));
                        StatsUtils.parseStats(content, this);
                    } catch (Exception e) {

                    }
                }
                //XP handling and chat event handling changed at 1.3, so we can use the same variable. :D
                if (xpversion < 1) {
                    //We only keep this around for backwards compatibility with tekkit as it is still on 1.2.5
                    getLogger().severe("This version of the Enjin Minecraft Plugin does not support Tekkit Classic! Please downgrade to version 2.4.0.");
                } else {
                    Bukkit.getPluginManager().registerEvents(new NewPlayerChatListener(this), this);
                }
            }
            //debug("Checking key valid.");
            //Bypass key checking, but only if the key looks valid
            registerEvents();
            if (configuration.getAuthKey().length() == 50) {
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(configuration.getAuthKey(), Bukkit.getPort(), true);
                if (data == null) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. Data could not be retrieved.");
                } else if (data.getError() != null) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. " + data.getError().getMessage());
                } else if (!data.getResult()) {
                    authkeyinvalid = true;
                    debug("Auth key is invalid. Failed to authenticate.");
                } else {
                    debug("Starting periodic tasks.");
                    startTask();
                }
            } else {
                authkeyinvalid = true;
                debug("Auth key is invalid. Must be 50 characters in length.");
            }

            // Adding in metrics
            try {
                MetricsLite metrics = new MetricsLite(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
            enjinLogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
            t.printStackTrace();
            this.setEnabled(false);
        }

        prepareTicketService();
    }

    private void prepareTicketService() {
        ticketListener = new TicketListener();
        Bukkit.getPluginManager().registerEvents(ticketListener, this);

        pollModules();
    }

    public OnlinePlayerGetter getPlayerGetter() {
        return playergetter;
    }

    public void stopStatsCollecting() {
        HandlerList.unregisterAll(esl);
    }

    static String readFile(File path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path.getAbsolutePath()));
        return new String(encoded, encoding);
    }

    public static boolean isMcMMOEnabled() {
        return isMcMMOloaded;
    }

    public void startStatsCollecting() {
        if (mcMMOSupported) {
            Plugin mcmmo = this.getServer().getPluginManager().getPlugin("mcMMO");
            if (mcmmo != null) {
                try {
                    //Let's try to load the method, if it fails, catch it.
                    List<SkillType> skills = SkillType.NON_CHILD_SKILLS;
                    isMcMMOloaded = true;
                    debug("mcMMO found, hooking custom stats.");
                } catch (NoSuchFieldError e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                } catch (NoClassDefFoundError e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                } catch (Error e) {
                    mcmmoOutdated = true;
                    isMcMMOloaded = false;
                    getLogger().warning("Your version of mcMMO is outdated! Please update here: http://dev.bukkit.org/bukkit-plugins/mcmmo/");
                }
            } else {
                isMcMMOloaded = false;
            }
        }

        if (esl == null) {
            esl = new EnjinStatsListener(this);
        }

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(esl, this);

        if (!configuration.getStatsCollected().getPlayer().isTravel()) {
            PlayerMoveEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isBlocksBroken()) {
            BlockBreakEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isBlocksPlaced()) {
            BlockPlaceEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isKills()) {
            EntityDeathEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isDeaths()) {
            PlayerDeathEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getPlayer().isXp()) {
            PlayerExpChangeEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getServer().isCreeperExplosions()) {
            EntityExplodeEvent.getHandlerList().unregister(esl);
        }
        if (!configuration.getStatsCollected().getServer().isPlayerKicks()) {
            PlayerKickEvent.getHandlerList().unregister(esl);
        }
    }

    @Override
    public void onDisable() {
        stopTask();
        if (configuration.isCollectPlayerStats()) {
            new WriteStats(this).write("enjin-stats.json");
            debug("Stats saved to enjin-stats.json.");
        }
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        EnjinMinecraftPlugin.configuration = JsonConfig.load(configFile, EnjinConfig.class);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }

        if (!configuration.getApiUrl().endsWith("/")) {
            configuration.setApiUrl(configuration.getApiUrl().concat("/"));
        }

        String rpcApiUrl = (configuration.isHttps() ? "https" : "http") + configuration.getApiUrl() + "v1/";
        EnjinRPC.setHttps(configuration.isHttps());
        EnjinRPC.setApiUrl(rpcApiUrl);
        debug("RPC API Url: " + rpcApiUrl);
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

    public static void saveConfiguration() {
        configuration.save(new File(instance.getDataFolder(), "config.json"));
    }

    public void startTask() {
        debug("Starting tasks.");
        BukkitScheduler scheduler = Bukkit.getScheduler();
        synctaskid = scheduler.runTaskTimerAsynchronously(this, task, 1200L, 1200L).getTaskId();
        banlisttask = scheduler.runTaskTimerAsynchronously(this, banlistertask, 40L, 1800L).getTaskId();
        //execute the value executer task every 10 ticks, which should vary between .5 and 1 second on servers.
        commandexecutertask = scheduler.runTaskTimer(this, commexecuter, 20L, 10L).getTaskId();
        commexecuter.loadCommands(Bukkit.getConsoleSender());
        //Only start the vote task if votifier is installed.

        if (votifierinstalled) {
            debug("Starting votifier task.");
            votetaskid = scheduler.runTaskTimerAsynchronously(this, votetask, 80L, 80L).getTaskId();
        }

        updatethread = scheduler.runTaskTimerAsynchronously(this, new Updater(this, 44560, this.getFile(), Updater.UpdateType.DEFAULT, true), 20 * 60 * 5, 20 * 60 * 5).getTaskId();
    }

    public void registerEvents() {
        debug("Registering events.");
        Bukkit.getPluginManager().registerEvents(listener, this);

        if (listenforbans) {
            Bukkit.getPluginManager().registerEvents(new BanListeners(this), this);
        }

        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public void stopTask() {
        debug("Stopping tasks.");
        if (synctaskid != -1) {
            Bukkit.getScheduler().cancelTask(synctaskid);
        }
        if (commandexecutertask != -1) {
            Bukkit.getScheduler().cancelTask(commandexecutertask);
            commexecuter.saveCommands();
        }
        if (votetaskid != -1) {
            Bukkit.getScheduler().cancelTask(votetaskid);
        }
        if (banlisttask != -1) {
            Bukkit.getScheduler().cancelTask(banlisttask);
        }
        if (headsupdateid != -1) {
            Bukkit.getScheduler().cancelTask(headsupdateid);
        }
        if (updatethread != -1) {
            Bukkit.getScheduler().cancelTask(updatethread);
        }
        //Bukkit.getScheduler().cancelTasks(this);
    }

    public void stopUpdateTask() {
        debug("Stopping update task.");
        if (updatethread != -1) {
            Bukkit.getScheduler().cancelTask(updatethread);
            updatethread = -1;
        }
        //Bukkit.getScheduler().cancelTasks(this);
    }

    private void setupVotifierListener() {
        if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
            System.out.println("[Enjin Minecraft Plugin] Votifier plugin found, enabling Votifier support.");
            enjinLogger.info("Votifier plugin found, enabling Votifier support.");
            Bukkit.getPluginManager().registerEvents(new VotifierListener(this), this);
            votifierinstalled = true;

            Plugin vobject = Bukkit.getPluginManager().getPlugin("Votifier");
            if (vobject != null && vobject instanceof Votifier) {
                Votifier votifier = (Votifier) vobject;
                votifiererrored = false;
                if (votifier.getVoteReceiver() == null) {
                    votifiererrored = true;
                }
            }

        }


    }

    private void initPlugins() throws Throwable {
        if (Bukkit.getPluginManager().isPluginEnabled("TuxTwoLib")) {
            tuxtwolibinstalled = true;
            enjinLogger.info("TuxTwoLib is installed. Offline players can be given items.");
            getLogger().info("TuxTwoLib is installed. Offline players can be given items.");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            enjinLogger.warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
            return;
        }

        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        boolean vaultupdated = false;

        if (vault != null) {
            String[] version = vault.getDescription().getVersion().split("\\.");
            int majorver = Integer.parseInt(version[0]);
            int minorver = Integer.parseInt(version[1]);
            if (majorver > 1) {
                vaultupdated = true;
            } else if (minorver > 3) {
                vaultupdated = true;
            }
        }

        if (!vaultupdated) {
            vaultneedsupdating = true;
            enjinLogger.severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
            enjinLogger.severe("Disabling vault integration until Vault is updated.");
            getLogger().severe("This version of vault doesn't support UUID! Please update to the latest version here: http://dev.bukkit.org/bukkit-plugins/vault/files/");
            getLogger().severe("Disabling vault integration until Vault is updated.");
            return;
        }

        setupVanishNoPacket();
    }

    private void setupVanishNoPacket() {
        Plugin vanish = Bukkit.getPluginManager().getPlugin("VanishNoPacket");
        if (vanish != null && vanish instanceof VanishPlugin) {
            vanishmanager = ((VanishPlugin) vanish).getManager();
        }
    }

    private void setupPermissions() {
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

            Plugin plugin = Bukkit.getPluginManager().getPlugin("GroupManager");
            if (supportsUUID()) {
                boolean gmupdated = false;
                Pattern devbuild = Pattern.compile("\\(Dev(\\d+)\\.(\\d+)\\.(\\d+)\\)");
                Matcher devmatch = devbuild.matcher(plugin.getDescription().getVersion());

                if (devmatch.find()) {
                    int majorver = Integer.parseInt(devmatch.group(1));
                    int minorver = Integer.parseInt(devmatch.group(2));
                    int buildver = Integer.parseInt(devmatch.group(3));

                    if (majorver > 2) {
                        gmupdated = true;
                    } else if (majorver == 2) {
                        if (minorver == 14 && buildver > 49) {
                            gmupdated = true;
                        } else if (buildver > 14) {
                            gmupdated = true;
                        }
                    }

                    debug("GroupManager dev version: " + majorver + "." + minorver + "." + buildver);
                } else {
                    String[] version = plugin.getDescription().getVersion().split("\\.");
                    int majorver = Integer.parseInt(version[0]);
                    Pattern numberpattern = Pattern.compile("\\d+");
                    int minorver = 0;

                    if (version.length > 1) {
                        Matcher match = numberpattern.matcher(version[1]);

                        if (match.find()) {
                            minorver = Integer.parseInt(match.group());
                        }
                    }

                    int revver = 0;

                    if (version.length > 2) {
                        Matcher match = numberpattern.matcher(version[2]);

                        try {
                            if (match.find()) {
                                revver = Integer.parseInt(match.group());
                            }
                        } catch (Exception e) {}
                    }

                    debug("GroupManager version: " + majorver + "." + minorver + "." + revver);

                    if (majorver > 2) {
                        gmupdated = true;
                    } else if (majorver == 2 && minorver > 1) {
                        gmupdated = true;
                    } else if (majorver == 2 && minorver == 1 && revver > 10) {
                        gmupdated = true;
                    }
                }

                if (!gmupdated) {
                    gmneedsupdating = true;
                    enjinLogger.severe("This version of GroupManager doesn't support UUID! Please update to the latest version here: http://tiny.cc/EssentialsGMZip");
                    enjinLogger.severe("Disabling GroupManager integration until GroupManager is updated.");
                    getLogger().severe("This version of GroupManager doesn't support UUID! Please update to the latest version here: http://tiny.cc/EssentialsGMZip");
                    getLogger().severe("Disabling GroupManager integration until GroupManager is updated.");
                    return;
                }
            }

            supportsglobalgroups = false;
            Bukkit.getPluginManager().registerEvents(new GroupManagerListener(this), this);
            return;
        } else {
            debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
            debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        }
    }

    public void noEnjinConnectionEvent() {
        if (!unabletocontactenjin) {
            unabletocontactenjin = true;
            Player[] players = getPlayerGetter().getOnlinePlayers();
            for (Player player : players) {
                if (player.hasPermission("enjin.notify.connectionstatus")) {
                    player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
                    player.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin report");
                }
            }
        }
    }

    public boolean testHTTPSconnection() {
        try {
            URL url = new URL("https://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SSLHandshakeException e) {
            if (configuration.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (configuration.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (configuration.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public boolean testWebConnection() {
        try {
            URL url = new URL("http://google.com");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (configuration.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (configuration.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public boolean testHTTPconnection() {
        try {
            URL url = new URL("http://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (configuration.isDebug()) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (configuration.isDebug()) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public static boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Runnable getTask() {
        return task;
    }

    public void addCommandID(CommandWrapper command) {
        if (command.getId().equals("")) {
            return;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(command.getCommand().getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            command.setHash(hashtext);
            commandids.put(command.getId(), command);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Map<String, CommandWrapper> getCommandIDs() {
        return commandids;
    }

    public void removeCommandID(String id) {
        commandids.remove(id);
    }

    public void saveCommandIDs() {
        File dataFolder = getDataFolder();
        File headsfile = new File(dataFolder, "newexecutedcommands.yml");
        YamlConfiguration headsconfig = new YamlConfiguration();
        Iterator<Entry<String, CommandWrapper>> thecodes = commandids.entrySet().iterator();
        while (thecodes.hasNext()) {
            Entry<String, CommandWrapper> code = thecodes.next();
            headsconfig.set(code.getKey() + ".hash", code.getValue().getHash());
            headsconfig.set(code.getKey() + ".result", code.getValue().getResult());
            headsconfig.set(code.getKey() + ".command", code.getValue().getCommand());
        }
        try {
            headsconfig.save(headsfile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadCommandIDs() {
        commandids.clear();
        File dataFolder = getDataFolder();
        File headsfile = new File(dataFolder, "executedcommands.yml");
        File newheadsfile = new File(dataFolder, "newexecutedcommands.yml");
        if (headsfile.exists()) {
            YamlConfiguration commandconfig = new YamlConfiguration();
            try {
                commandconfig.load(headsfile);
                Set<String> keys = commandconfig.getValues(false).keySet();
                for (String key : keys) {
                    String hash = commandconfig.getString(key);
                    CommandWrapper comm = new CommandWrapper(Bukkit.getConsoleSender(), "", key);
                    comm.setHash(hash);
                    commandids.put(key, comm);
                }
                headsfile.delete();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (InvalidConfigurationException e) {
            }
        } else if (newheadsfile.exists()) {
            YamlConfiguration commandconfig = new YamlConfiguration();
            try {
                commandconfig.load(newheadsfile);
                Set<String> keys = commandconfig.getValues(false).keySet();
                for (String key : keys) {
                    String hash = commandconfig.getString(key + ".hash");
                    String command = commandconfig.getString(key + ".command");
                    String result = commandconfig.getString(key + ".result");
                    CommandWrapper comm = new CommandWrapper(Bukkit.getConsoleSender(), command, key);
                    comm.setHash(hash);
                    comm.setResult(result);
                    commandids.put(key, comm);
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (InvalidConfigurationException e) {
            }
        }
    }

    public EnjinLogInterface getMcLogListener() {
        return mcLogListener;
    }

    public String getLastLogLine() {
        return mcLogListener.getLine();
    }

    public static boolean supportsUUID() {
        return supportsuuid;
    }

    public boolean isVanished(Player player) {
        if (vanishmanager != null) {
            try {
                return vanishmanager.isVanished(player);
                //Make sure old versions of Vanish don't mess up the plugin.
            } catch (Throwable e) {
                return vanishmanager.isVanished(player.getName());
            }
        } else {
            return false;
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

    public void pollModules() {
        if (System.currentTimeMillis() - modulesLastPolled > 10 * 60 * 1000) {
            modulesLastPolled = System.currentTimeMillis();
            RPCData<Map<Integer, Module>> data = EnjinServices.getService(TicketService.class).getModules(EnjinMinecraftPlugin.getConfiguration().getAuthKey());

            if (data == null || data.getError() != null) {
                debug(data == null ? "Could not retrieve support modules." : data.getError().getMessage());
                modules.clear();
                return;
            }

            if (data.getResult().size() == 0) {
                modules.clear();
            }

            for (Entry<Integer, Module> entry : data.getResult().entrySet()) {
                modules.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
