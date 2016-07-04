package com.enjin.bungee;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.enjin.bungee.command.CommandBank;
import com.enjin.bungee.command.commands.CoreCommands;
import com.enjin.bungee.command.commands.PointCommands;
import com.enjin.bungee.sync.BungeeInstructionHandler;
import com.enjin.bungee.sync.RPCPacketManager;
import com.enjin.bungee.util.Log;
import com.enjin.bungee.util.io.EnjinErrorReport;
import com.enjin.common.config.GenericEnjinConfig;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import com.enjin.core.EnjinServices;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class EnjinMinecraftPlugin extends Plugin implements EnjinPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private InstructionHandler instructionHandler = new BungeeInstructionHandler();
    @Getter
    private boolean firstRun = true;

    @Getter @Setter
    private boolean unableToContactEnjin = false;
    @Getter @Setter
    private boolean authKeyInvalid = false;
    @Getter @Setter
    private EnjinErrorReport lastError = null;

    @Getter @Setter
    private String newVersion = "";
    @Getter @Setter
    private boolean hasUpdate = false;
    @Getter @Setter
    private boolean updateFailed = false;

    @Override
    public void onEnable() {
        instance = this;
        Enjin.setPlugin(instance);
        init();
    }

    @Override
    public void onDisable() {
        disableTasks();
    }

    public void init() {
        if (authKeyInvalid) {
            return;
        }

        if (firstRun) {
            Log log = new Log(getDataFolder());
            Enjin.setLogger(log);

            firstRun = false;
            initConfig();
            log.configure();

            Enjin.getLogger().debug("Init config done.");

            initCommands();
            Enjin.getLogger().debug("Init commands done.");

            if (Enjin.getConfiguration().getAuthKey().length() == 50) {
                Optional<Integer> port = getPort();
                RPCData<Boolean> data = EnjinServices.getService(PluginService.class).auth(Optional.<String>absent(), port.isPresent() ? port.get() : null, true);
                if (data == null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Data could not be retrieved.");
                    return;
                } else if (data.getError() != null) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. " + data.getError().getMessage());
                    return;
                } else if (!data.getResult()) {
                    authKeyInvalid = true;
                    Enjin.getLogger().debug("Auth key is invalid. Failed to authenticate.");
                    return;
                }
            } else {
                authKeyInvalid = true;
                Enjin.getLogger().debug("Auth key is invalid. Must be 50 characters in length.");
                return;
            }
        }

        initTasks();
        Enjin.getLogger().debug("Init tasks done.");
    }

    public void initConfig() {
        File configFile = new File(getDataFolder(), "config.json");
        GenericEnjinConfig configuration = JsonConfig.load(configFile, GenericEnjinConfig.class);
        Enjin.setConfiguration(configuration);

        if (!configFile.exists()) {
            configuration.save(configFile);
        }

        if (!configuration.getApiUrl().endsWith("/")) {
            configuration.setApiUrl(configuration.getApiUrl().concat("/"));
        }
    }

    public static void saveConfiguration() {
        Enjin.getConfiguration().save(new File(instance.getDataFolder(), "config.json"));
    }

    public void initCommands() {
        CommandBank.setup(this);
        CommandBank.register(CoreCommands.class, PointCommands.class);
    }

    public void initTasks() {
        Enjin.getLogger().debug("Starting tasks.");
        ProxyServer.getInstance().getScheduler().schedule(this, new RPCPacketManager(this), 60L, 60L, TimeUnit.SECONDS);
    }

    public void disableTasks() {
        Enjin.getLogger().debug("Stopping tasks.");
        ProxyServer.getInstance().getScheduler().cancel(this);
    }


    public static Optional<Integer> getPort() {
        if (ProxyServer.getInstance().getConfig().getListeners().size() > 0) {
            return Optional.fromNullable(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getHost().getPort());
        }

        return Optional.absent();
    }
}
