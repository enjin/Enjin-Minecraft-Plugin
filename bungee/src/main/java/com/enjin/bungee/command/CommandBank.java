package com.enjin.bungee.command;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class CommandBank implements Listener {
    @Getter
    private static Map<String, CommandNode> nodes = Maps.newHashMap();
    private static CommandBank instance;

    /**
     * Prepares the parent bank for operation.
     * @param plugin The plugin registered to this command bank.
     */
    public static void setup(Plugin plugin) {
        if (instance != null) {
            Enjin.getPlugin().debug("Command bank has already been initialized.");
            return;
        }

        ProxyServer.getInstance().getPluginManager().registerListener(plugin, instance = new CommandBank());
        Enjin.getPlugin().debug("Command bank initialized.");
    }

    /**
     * Registers the provided handles.
     * @param handles the parent handles to be processed
     */
    public static void register(Class<?> ... handles) {
        for (Class<?> clazz : handles) {
            Enjin.getPlugin().debug("Registering commands and directives for " + clazz.getSimpleName());
            List<Method> methods = Lists.newArrayList();
            for (Method method : clazz.getMethods()) {
                if (!(method.isAnnotationPresent(Command.class) || method.isAnnotationPresent(Directive.class))) {
                    continue;
                }

                if (!Modifier.isStatic(method.getModifiers())) {
                    Enjin.getPlugin().debug(method.getName() + " is not static.");
                    continue;
                }

                if (method.getParameterTypes().length != 2) {
                    Enjin.getPlugin().debug(method.getName() + " does not have 2 parameters.");
                    continue;
                }

                Class<?>[] types = method.getParameterTypes();
                if (!CommandSender.class.isAssignableFrom(types[0])) {
                    Enjin.getPlugin().debug(method.getName() + "'s first argument is not assignable from CommandSender.");
                    continue;
                }

                if (!String[].class.isAssignableFrom(types[1])) {
                    Enjin.getPlugin().debug(method.getName() + "'s second argument is not assignable from String[].");
                    continue;
                }

                methods.add(method);
            }

            List<CommandNode> root = Lists.newArrayList();
            List<DirectiveNode> sub = Lists.newArrayList();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    root.add(method.isAnnotationPresent(Permission.class)
                            ? new CommandNode(method.getAnnotation(Command.class), method, method.getAnnotation(Permission.class))
                            : new CommandNode(method.getAnnotation(Command.class), method));
                }

                if (method.isAnnotationPresent(Directive.class)) {
                    sub.add(method.isAnnotationPresent(Permission.class)
                            ? new DirectiveNode(method.getAnnotation(Directive.class), method, method.getAnnotation(Permission.class))
                            : new DirectiveNode(method.getAnnotation(Directive.class), method));
                }
            }

            registerCommandNodes(root.toArray(new CommandNode[root.size()]));
            registerDirectiveNodes(sub.toArray(new DirectiveNode[sub.size()]));
        }
    }

    /**
     * Registers value nodes.
     * @param nodes The command nodes to be registered.
     */
    private static void registerCommandNodes(CommandNode ... nodes) {
        for (CommandNode node : nodes) {
            if (CommandBank.nodes.containsKey(node.getData().value())) {
                continue;
            }

            Enjin.getPlugin().debug("Registering command: " + node.getData().value());
            CommandBank.nodes.put(node.getData().value(), node);
            registerCommandAlias(node.getData().value(), node.getData().aliases());
            ProxyServer.getInstance().getPluginManager().registerCommand(EnjinMinecraftPlugin.getInstance(), node);
        }
    }

    /**
     * Registers directives.
     * @param nodes The directive nodes to be registered.
     */
    private static void registerDirectiveNodes(DirectiveNode ... nodes) {
        for (DirectiveNode node : nodes) {
            CommandNode command = CommandBank.getNodes().get(node.getData().parent());

            if (command != null) {
                if (command.getDirectives().containsKey(node.getData().value())) {
                    continue;
                }

                Enjin.getPlugin().debug("Registering directive: " + node.getData().value() + " for command: " + node.getData().parent());
                command.getDirectives().put(node.getData().value(), node);
                registerDirectiveAlias(node.getData().parent(), node.getData().value(), node.getData().aliases());
            }
        }
    }

    public static void registerCommandAlias(String command, String ... alias) {
        if (nodes.containsKey(alias)) {
            Enjin.getPlugin().debug("That alias has already been registered by another command.");
            return;
        }

        CommandNode node = nodes.get(command);
        if (node != null) {
            for (String a : alias) {
                nodes.put(a, node);
            }
        }
    }

    public static void registerDirectiveAlias(String command, String directive, String ... alias) {
        CommandNode node = nodes.get(command);
        if (node != null) {
            if (node.getDirectives().containsKey(alias)) {
                Enjin.getPlugin().debug("That alias has already been registered by another directive.");
                return;
            }

            DirectiveNode n = node.getDirectives().get(directive);
            if (n != null) {
                for (String a : alias) {
                    node.getDirectives().put(a, n);
                }
            }
        }
    }

    private boolean handle(CommandSender sender, String c) {
        if (nodes.size() == 0) {
            return false;
        }

        String[] parts = c.startsWith("/") ? c.replaceFirst("/", "").split(" ") : c.split(" ");
        String command = parts[0];

        Optional<CommandNode> w = Optional.fromNullable(nodes.get(command));
        if (w.isPresent()) {
            CommandNode wrapper = w.get();
            String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
            wrapper.invoke(sender, args);
            return true;
        }

        return false;
    }
}
