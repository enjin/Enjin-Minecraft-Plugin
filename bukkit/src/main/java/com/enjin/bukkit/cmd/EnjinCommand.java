package com.enjin.bukkit.cmd;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.enums.CommandProcess;
import com.enjin.bukkit.enums.MessageAction;
import com.enjin.bukkit.enums.Usage;
import com.enjin.bukkit.enums.VeryifyRequirements;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.util.text.MessageUtil;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class EnjinCommand implements CommandExecutor, TabCompleter {

    protected EnjinMinecraftPlugin plugin;
    protected Optional<EnjinCommand> parent;
    protected List<String> aliases;
    protected List<EnjinCommand> subCommands;
    protected List<String> requiredArgs;
    protected List<String> optionalArgs;
    protected CommandRequirements requirements;

    public EnjinCommand(EnjinMinecraftPlugin plugin, EnjinCommand parent) {
        this.plugin = plugin;
        this.parent = Optional.ofNullable(parent);
        this.aliases = new ArrayList<>();
        this.subCommands = new ArrayList<>();
        this.requiredArgs = new ArrayList<>();
        this.optionalArgs = new ArrayList<>();
        this.requirements = CommandRequirements.builder(plugin)
                .withAllowedSenderTypes(SenderType.ANY)
                .build();
    }

    public EnjinCommand(EnjinCommand parent) {
        this(parent.plugin, parent);
    }

    public EnjinCommand(EnjinMinecraftPlugin plugin) {
        this(plugin, null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        process(new CommandContext(sender, new ArrayList<>(Arrays.asList(args)), label), CommandProcess.EXECUTE);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        CommandContext context = new CommandContext(sender, new ArrayList<>(Arrays.asList(args)), label);
        process(context, CommandProcess.TAB);
        return context.tabCompletionResult;
    }

    public abstract void execute(CommandContext context);

    public abstract Translation getUsageTranslation();

    public List<String> tab(CommandContext context) {
        return new ArrayList<>();
    }

    private List<String> tab0(CommandContext context) {
        List<String> tabResults = new ArrayList<>();

        if (!subCommands.isEmpty()) {
            List<String> als = subCommands.stream()
                    .filter(c -> c.requirements.areMet(context, MessageAction.OMIT))
                    .map(c -> c.aliases.get(0).toLowerCase())
                    .collect(Collectors.toList());

            if (!context.args.isEmpty()) {
                tabResults.addAll(als.stream()
                        .filter(a -> a.startsWith(context.args.get(0).toLowerCase()))
                        .collect(Collectors.toList()));
            }
        } else {
            tabResults.addAll(tab(context));
        }

        return tabResults;
    }

    public void showHelp(CommandSender sender) {
        showHelp(sender, VeryifyRequirements.YES, Usage.ALL);
    }

    public void showHelp(CommandSender sender, VeryifyRequirements verifyRequirements, Usage usage) {
        if (getUsageTranslation() == null)
            return;
        if (verifyRequirements == VeryifyRequirements.YES && !requirements.areMet(sender, MessageAction.OMIT))
            return;
        MessageUtil.sendString(sender, getUsage(SenderType.type(sender), usage));
    }

    public String getUsage(SenderType type, Usage usage) {
        String output = buildUsage(type, usage);
        if (type != SenderType.PLAYER)
            output = output.replaceFirst("/", "");
        return output;
    }

    public String buildUsage(SenderType type, Usage usage) {
        StringBuilder builder = new StringBuilder();

        builder.append("&6/");

        List<EnjinCommand> commandStack = CommandContext.createCommandStackAsList(this);
        for (int i = 0; i < commandStack.size(); i++) {
            EnjinCommand command = commandStack.get(i);
            if (i > 0)
                builder.append(' ');
            builder.append(TextUtils.concat(command.aliases, ","));
        }

        builder.append("&e");

        if (!requiredArgs.isEmpty()) {
            builder.append(' ')
                    .append(TextUtils.concat(requiredArgs.stream()
                            .map(s -> String.format("<%s>", s))
                            .collect(Collectors.toList()), " "));
        }

        if (!optionalArgs.isEmpty()) {
            builder.append(' ')
                    .append(TextUtils.concat(optionalArgs.stream()
                            .map(s -> String.format("[%s]", s))
                            .collect(Collectors.toList()), " "));
        }

        if (usage == Usage.ALL)
            builder.append(" &f").append(TextUtils.colorize(getUsageTranslation().translation(type)));

        return builder.toString();
    }

    public void process(CommandContext context, CommandProcess process) {
        try {
            if (!isValid(context, process.getMessageAction()))
                return;

            if (!context.args.isEmpty()) {
                for (EnjinCommand subCommand : subCommands) {
                    if (!subCommand.aliases.contains(context.args.get(0).toLowerCase()))
                        continue;
                    context.args.remove(0);
                    context.commandStack.push(this);
                    subCommand.process(context, process);
                    return;
                }
            }

            if (process == CommandProcess.EXECUTE) {
                execute(context);
            } else {
                context.tabCompletionResult = tab0(context);
            }
        } catch (Exception ex) {
            Enjin.getLogger().log(ex);
            Translation.Errors_Exception.send(context.sender, ex.getMessage());
        }

    }

    protected void addSubCommand(EnjinCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    private boolean isValid(CommandContext context, MessageAction action) {
        return requirements.areMet(context, action) && validArgs(context, action);
    }

    private boolean validArgs(CommandContext context, MessageAction action) {
        boolean result = context.args.size() >= requiredArgs.size();

        if (action == MessageAction.SEND && !result) {
            Translation.CommandApi_InvalidUsage.send(context.sender);
            Translation.CommandApi_Usage.send(context.sender, getUsage(context.senderType, Usage.COMMAND_ONLY));
        }

        return result;
    }

    protected void register(String name, List<String> aliases) {
        name = name.toLowerCase();

        try {
            Map<String, Command> known = getKnownCommands();
            Command command = known.get(name);

            if (command != null) {
                if (!(command instanceof CommandProxy))
                    return;
                known.remove(name);
            }

            command = new CommandProxy(this, name, aliases);
            known.put(name, command);
        } catch (Throwable t) {
            Enjin.getLogger().log(t);
        }
    }

    public static SimpleCommandMap getCommandMap() throws ReflectiveOperationException {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (SimpleCommandMap) field.get(Bukkit.getServer());
    }

    public static Map<String, Command> getKnownCommands() throws ReflectiveOperationException {
        SimpleCommandMap map = getCommandMap();
        Field field = getField(map.getClass(), "knownCommands");
        field.setAccessible(true);
        return (Map<String, Command>) field.get(map);
    }

    public static Field getField(Class<?> clazz, String name) {
        if (clazz == null)
            return null;

        Field field;

        try {
            field = clazz.getDeclaredField("knownCommands");
        } catch (ReflectiveOperationException ex) {
            field = getField(clazz.getSuperclass(), name);
        }

        return field;
    }

    public static void unregisterAll() {
        try {
            Map<String, Command> known = getKnownCommands();
            for (Map.Entry<String, Command> entry : new HashSet<>(known.entrySet())) {
                if (entry.getValue() instanceof CommandProxy)
                    known.remove(entry.getKey());
            }
        } catch (Throwable t) {
            Enjin.getLogger().log(t);
        }
    }

}
