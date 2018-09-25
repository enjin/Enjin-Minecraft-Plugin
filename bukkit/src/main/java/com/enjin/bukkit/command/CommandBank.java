package com.enjin.bukkit.command;

import com.enjin.core.Enjin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CommandBank {
    @Getter
    protected static Map<String, CommandNode> nodes           = new HashMap<>();
    private static   DispatchCommand          dispatchCommand = new DispatchCommand();

    /**
     * Registers the provided handles.
     *
     * @param handles the parent handles to be processed
     */
    public static void register(Class<?>... handles) {
        for (Class<?> clazz : handles) {
            Enjin.getLogger().debug("Registering commands and directives for " + clazz.getSimpleName());
            List<Method> methods = new ArrayList<>();
            for (Method method : clazz.getMethods()) {
                if (!(method.isAnnotationPresent(Command.class) || method.isAnnotationPresent(Directive.class))) {
                    continue;
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    Enjin.getLogger().debug(method.getName() + " is not static.");
                    continue;
                }

                if (method.getParameterTypes().length != 2) {
                    Enjin.getLogger().debug(method.getName() + " does not have 2 parameters.");
                    continue;
                }

                Class<?>[] types = method.getParameterTypes();
                if (!CommandSender.class.isAssignableFrom(types[0])) {
                    Enjin.getLogger()
                         .debug(method.getName() + "'s first argument is not assignable from CommandSender.");
                    continue;
                }

                if (!String[].class.isAssignableFrom(types[1])) {
                    Enjin.getLogger().debug(method.getName() + "'s second argument is not assignable from String[].");
                    continue;
                }

                methods.add(method);
            }

            List<CommandNode>   root = new ArrayList<>();
            List<DirectiveNode> sub  = new ArrayList<>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    root.add(method.isAnnotationPresent(Permission.class)
                                     ? new CommandNode(method.getAnnotation(Command.class),
                                                       method,
                                                       method.getAnnotation(Permission.class))
                                     : new CommandNode(method.getAnnotation(Command.class), method));
                }

                if (method.isAnnotationPresent(Directive.class)) {
                    sub.add(method.isAnnotationPresent(Permission.class)
                                    ? new DirectiveNode(method.getAnnotation(Directive.class),
                                                        method,
                                                        method.getAnnotation(Permission.class))
                                    : new DirectiveNode(method.getAnnotation(Directive.class), method));
                }
            }

            registerCommandNodes(root.toArray(new CommandNode[root.size()]));
            registerDirectiveNodes(sub.toArray(new DirectiveNode[sub.size()]));
        }
    }

    /**
     * Registers value nodes.
     *
     * @param nodes The command nodes to be registered.
     */
    private static void registerCommandNodes(CommandNode... nodes) {
        for (CommandNode node : nodes) {
            String key = node.getData().value().toLowerCase();
            if (CommandBank.nodes.containsKey(key)) {
                continue;
            }

            Enjin.getLogger().debug("Registering command: " + node.getData().value());
            CommandBank.nodes.put(key, node);
            registerCommand(key);
            registerCommandAlias(node.getData().value(), node.getData().aliases());
        }
    }

    public static void registerCommandAlias(String command, String... alias) {
        CommandNode node = nodes.get(command.toLowerCase());
        if (node != null) {
            for (String a : alias) {
                String key = a.toLowerCase();
                if (nodes.containsKey(key)) {
                    Enjin.getLogger().debug("That alias has already been registered by another command.");
                    continue;
                }

                nodes.put(key, node);
                registerCommand(key);
            }
        }
    }

    public static void replaceCommandWithAlias(String command, String... alias) {
        registerCommandAlias(command, alias);
        nodes.remove(command.toLowerCase());
        unregisterCommand(command.toLowerCase());
    }

    public static boolean isCommandRegistered(String command) {
        return nodes.containsKey(command.toLowerCase());
    }

    /**
     * Registers directives.
     *
     * @param nodes The directive nodes to be registered.
     */
    private static void registerDirectiveNodes(DirectiveNode... nodes) {
        for (DirectiveNode node : nodes) {
            CommandNode command = CommandBank.getNodes().get(node.getData().parent().toLowerCase());

            if (command != null) {
                String key = node.getData().value().toLowerCase();
                if (command.getDirectives().containsKey(key)) {
                    continue;
                }

                Enjin.getLogger()
                     .debug("Registering directive: " + node.getData().value() + " for command: " + node.getData()
                                                                                                        .parent());
                command.getDirectives().put(key, node);
                registerDirectiveAlias(node.getData().parent(), node.getData().value(), node.getData().aliases());
            }
        }
    }

    public static void registerDirectiveAlias(String command, String directive, String... alias) {
        CommandNode node = nodes.get(command.toLowerCase());
        if (node != null) {
            DirectiveNode n = node.getDirectives().get(directive.toLowerCase());
            if (n != null) {
                for (String a : alias) {
                    String key = a.toLowerCase();
                    if (node.getDirectives().containsKey(key)) {
                        Enjin.getLogger().debug("That alias has already been registered by another directive.");
                        continue;
                    }

                    node.getDirectives().put(key, n);
                }
            }
        }
    }

    private static void registerCommand(String command) {
        try {
            SimpleCommandMap commandMap = getCommandMap();
            commandMap.register(command.toLowerCase(),
                                command.equalsIgnoreCase("enjin") ? "" : "enjin",
                                dispatchCommand);
        } catch (ReflectiveOperationException e) {
            Enjin.getLogger().log(e);
        }
    }

    private static void unregisterCommand(String command) {
        try {
            SimpleCommandMap                        commandMap    = getCommandMap();
            Map<String, org.bukkit.command.Command> knownCommands = getServerCommands(commandMap);

            knownCommands.remove(command);
        } catch (ReflectiveOperationException e) {
            Enjin.getLogger().log(e);
        }
    }

    private static SimpleCommandMap getCommandMap() throws ReflectiveOperationException {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (SimpleCommandMap) field.get(Bukkit.getServer());
    }

    private static Map<String, org.bukkit.command.Command> getServerCommands(SimpleCommandMap commandMap) throws ReflectiveOperationException {
        Field field = commandMap.getClass().getDeclaredField("knownCommands");
        field.setAccessible(true);
        return (Map<String, org.bukkit.command.Command>) field.get(commandMap);
    }
}
