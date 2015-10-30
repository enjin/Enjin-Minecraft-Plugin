package com.enjin.bukkit.command;

import com.enjin.core.Enjin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@NoArgsConstructor
public class CommandBank implements Listener {
    @Getter
    private static Map<String, CommandNode> nodes = Maps.newHashMap();
    private static CommandBank instance;

    /**
     * Prepares the parent bank for operation.
     * @param plugin
     */
    public static void setup(Plugin plugin) {
        if (instance != null) {
            Enjin.getPlugin().debug("Command bank has already been initialized.");
            return;
        }

        Bukkit.getPluginManager().registerEvents((instance = new CommandBank()), plugin);
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

                if (method.getParameterCount() != 2) {
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
                    root.add(new CommandNode(method.getAnnotation(Command.class), method));
                }

                if (method.isAnnotationPresent(Directive.class)) {
                    sub.add(new DirectiveNode(method.getAnnotation(Directive.class), method));
                }
            }

            registerCommandNodes(root.toArray(new CommandNode[]{}));
            registerDirectiveNodes(sub.toArray(new DirectiveNode[]{}));
        }
    }

    /**
     * Registers command nodes.
     * @param nodes
     */
    private static void registerCommandNodes(CommandNode ... nodes) {
        for (CommandNode node : nodes) {
            Enjin.getPlugin().debug("Registering command: " + node.getData().command());
            CommandBank.nodes.put(node.getData().command(), node);
        }
    }

    /**
     * Registers directives.
     * @param nodes
     */
    private static void registerDirectiveNodes(DirectiveNode ... nodes) {
        for (DirectiveNode node : nodes) {
            CommandNode command = CommandBank.getNodes().get(node.getData().parent());

            if (command != null) {
                Enjin.getPlugin().debug("Registering directive: " + node.getData().directive() + " for command: " + node.getData().parent());
                command.getDirectives().put(node.getData().directive(), node);
            }
        }
    }

    public static void registerCommandAlias(String command, String alias) {
        if (nodes.containsKey(alias)) {
            Enjin.getPlugin().debug("That alias has already been registered by another command.");
            return;
        }

        CommandNode node = nodes.get(command);
        if (node != null) {
            nodes.put(alias, node);
        }
    }

    public static void registerDirectiveAlias(String command, String directive, String alias) {
        CommandNode node = nodes.get(command);
        if (node != null) {
            if (node.getDirectives().containsKey(alias)) {
                Enjin.getPlugin().debug("That alias has already been registered by another directive.");
                return;
            }

            DirectiveNode n = node.getDirectives().get(directive);
            if (n != null) {
                node.getDirectives().put(alias, n);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (nodes.size() == 0) {
            return;
        }

        String[] parts = event.getMessage().replaceFirst("/", "").split(" ");
        String command = parts[0];

        Optional<CommandNode> w = Optional.ofNullable(nodes.get(command));
        if (w.isPresent()) {
            event.setCancelled(true);

            CommandNode wrapper = w.get();
            String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
            wrapper.invoke(event.getPlayer(), args);
        }
    }
}
