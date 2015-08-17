package com.enjin.officialplugin;

import com.enjin.core.config.JsonConfig;
import com.enjin.officialplugin.commands.EnjinCommand;
import com.enjin.officialplugin.commands.configuration.SetKeyCommand;
import com.enjin.officialplugin.commands.store.BuyCommand;
import com.enjin.officialplugin.config.EnjinConfig;
import com.enjin.officialplugin.shop.ShopListener;
import com.enjin.officialplugin.threaded.IncomingPacketManager;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(id = "EnjinMinecraftPlugin", name = "Enjin Minecraft Plugin", version = "2.8.2-sponge")
public class EnjinMinecraftPlugin {
    @Getter
    private static EnjinMinecraftPlugin instance;
    @Getter
    private static List<CommandSpec> commands = Lists.newArrayList();
    @Inject
    @Getter
    private PluginContainer container;
    @Inject
    @Getter
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    @Getter
    private File configDir;
    @Getter
    private EnjinConfig config;
    @Inject
    @Getter
    private Game game;

    @Getter
    private Task syncTask;

    public EnjinMinecraftPlugin() {
        instance = this;
    }

    @Subscribe
    public void initialization(InitializationEvent event) {
        logger.info("Initializing Enjin Minecraft Plugin");
        initConfig();
        initCommands();
        initListeners();
        initTasks();
    }

    private void initConfig() {
        logger.info("Initializing EMP Config");
        config = JsonConfig.load(new File(configDir, "config.json"), EnjinConfig.class);
    }

    public void saveConfig() {
        logger.info("Saving EMP Config");
        config.save(new File(configDir, "config.json"));
    }

    private void initCommands() {
        logger.info("Initializing EMP Commands");
        if (!commands.isEmpty()) {
            commands.clear();
        }

        CommandSpec.Builder enjinCommandBuilder = CommandSpec.builder()
                .description(Texts.of("/enjin"))
                .executor(new EnjinCommand());
        enjinCommandBuilder.child(CommandSpec.builder()
                .description(Texts.of("Set the authentication key for this server"))
                .permission("enjin.setkey")
                .arguments(GenericArguments.string(Texts.of("key")))
                .executor(new SetKeyCommand()).build(), "setkey", "key", "sk");

        CommandSpec.Builder buyCommandBuilder = CommandSpec.builder()
                .description(Texts.of("/buy"))
                .permission("enjin.buy")
                .arguments(GenericArguments.optional(GenericArguments.integer(Texts.of("#"))))
                .executor(new BuyCommand());
        buyCommandBuilder.child(CommandSpec.builder()
                .description(Texts.of("/buy shop <#>"))
                .permission("enjin.buy")
                .arguments(GenericArguments.integer(Texts.of("#")))
                .executor(new BuyCommand.ShopCommand())
                .build(), "shop");

        CommandSpec buySpec = buyCommandBuilder.build();
        enjinCommandBuilder.child(buySpec, "buy");

        CommandSpec enjinSpec = enjinCommandBuilder.build();
        commands.add(enjinSpec);
        commands.add(buySpec);

        game.getCommandDispatcher().register(this, enjinSpec, "enjin", "emp", "e");
        game.getCommandDispatcher().register(this, buySpec, "buy");
    }

    private void initListeners() {
        game.getEventManager().register(this, new ShopListener());
    }

    public void initTasks() {
        if (syncTask != null) {
            stopTasks();
        }

        // TODO: Look for alternative as the scheduler does not appear reliable
        syncTask = game.getScheduler().createTaskBuilder()
                .execute(new IncomingPacketManager(this))
                .interval(60, TimeUnit.SECONDS)
                .submit(this);
    }

    public void stopTasks() {
        syncTask.cancel();
        syncTask = null;
    }

    public String getAuthKey() {
        if (config == null) {
            return "";
        }

        return config.getAuthkey();
    }

    public int getPort() {
        return game.getServer().getBoundAddress().get().getPort();
    }

    public void debug(String ... messages) {
        if (config.isDebug()) {
            for (String message : messages) {
                logger.info("[Debug] " + message);
            }
        }
    }
}
