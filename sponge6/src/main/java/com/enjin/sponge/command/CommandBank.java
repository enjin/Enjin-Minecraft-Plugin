package com.enjin.sponge.command;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CommandBank {
    @Getter
    private static Map<String, CommandNode> nodes = Maps.newHashMap();
    private static CommandBank              instance;

    /**
     * Prepares the parent bank for operation.
     *
     * @param plugin The plugin registered to this command bank.
     */
    public static void setup(EnjinMinecraftPlugin plugin) {
        if (instance != null) {
            Enjin.getLogger().debug("Command bank has already been initialized.");
            return;
        }

        plugin.getGame().getEventManager().registerListeners(plugin, instance = new CommandBank());
        Enjin.getLogger().debug("Command bank initialized.");
    }

    /**
     * Registers the provided handles.
     *
     * @param handles the parent handles to be processed
     */
    public static void register(Class<?>... handles) {
        for (Class<?> clazz : handles) {
            Enjin.getLogger().debug("Registering commands and directives for " + clazz.getSimpleName());
            List<Method> methods = Lists.newArrayList();
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
                if (!CommandSource.class.isAssignableFrom(types[0])) {
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

            List<CommandNode>   root = Lists.newArrayList();
            List<DirectiveNode> sub  = Lists.newArrayList();
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
            registerCommandAlias(node.getData().value(), node.getData().aliases());
        }
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
            }
        }
    }

    public static void replaceCommandWithAlias(String command, String... alias) {
        registerCommandAlias(command, alias);
        nodes.remove(command.toLowerCase());
    }

    public static boolean isCommandRegistered(String command) {
        return nodes.containsKey(command.toLowerCase());
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

    @Listener
    public void onSendCommand(SendCommandEvent event) {
        Object object = event.getCause().root();

        if (object instanceof CommandSource) {
            event.setCancelled(handle((CommandSource) object, event.getCommand() + " " + event.getArguments()));
        }
    }

    private boolean handle(CommandSource sender, String c) {
        if (nodes.size() == 0) {
            return false;
        }

        String[] parts = c.startsWith("/") ? c.replaceFirst("/", "").split(" ") : c.split(" ");
        if (parts.length == 0) return false;
        String command = parts[0].toLowerCase();

        Optional<CommandNode> w = Optional.fromNullable(nodes.get(command));
        if (w.isPresent()) {
            CommandNode wrapper = w.get();
            String[]    args    = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[] {};
            wrapper.invoke(sender, args);
            return true;
        }

        return false;
    }
}
