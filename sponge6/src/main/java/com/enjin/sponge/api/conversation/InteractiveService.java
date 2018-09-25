package com.enjin.sponge.api.conversation;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent.Chat;
import org.spongepowered.api.event.network.ClientConnectionEvent.Disconnect;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tristate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InteractiveService {
    private static InteractiveService            instance;
    private        Object                        plugin;
    private        List<InteractiveConversation> conversations;

    private InteractiveService(Object plugin) {
        this.plugin = plugin;
        this.conversations = new ArrayList<>();
        init();
    }

    private void init() {
        instance = this;
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    protected static void setup(Object plugin) {
        if (instance == null) {
            instance = new InteractiveService(plugin);
        }
    }

    protected static void registerConversation(InteractiveConversation conversation) {
        Optional<PluginContainer> container = conversation.getContext().getPlugin();
        if (container.isPresent()) {
            setup(container.get());

            Optional<InteractiveConversation> optionalConversation = instance.conversations.stream()
                                                                                           .filter(c -> c.getContext()
                                                                                                         .getReceiver()
                                                                                                         .equals(conversation
                                                                                                                         .getContext()
                                                                                                                         .getReceiver()))
                                                                                           .findFirst();

            if (!optionalConversation.isPresent()) {
                instance.conversations.add(conversation);
            }
        }
    }

    protected static void removeConversation(InteractiveConversation conversation) {
        Optional<PluginContainer> container = conversation.getContext().getPlugin();
        if (container.isPresent()) {
            setup(container.get());
            instance.conversations.remove(conversation);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(value = Tristate.FALSE)
    public void onChat(final Chat event, @Root Player player) {
        Optional<InteractiveConversation> optionalConversation = conversations.stream()
                                                                              .filter(c -> c.getContext()
                                                                                            .getReceiver()
                                                                                            .equals(player))
                                                                              .findFirst();
        if (optionalConversation.isPresent()) {
            event.setMessageCancelled(true);
            InteractiveConversation conversation = optionalConversation.get();
            conversation.processInput(event.getRawMessage(), event.getOriginalChannel());
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(value = Tristate.FALSE)
    public void onCommand(final SendCommandEvent event, @Root Player player) {
        Optional<InteractiveConversation> optionalConversation = conversations.stream()
                                                                              .filter(c -> c.getContext()
                                                                                            .getReceiver()
                                                                                            .equals(player))
                                                                              .findFirst();
        if (optionalConversation.isPresent()) {
            InteractiveConversation conversation = optionalConversation.get();
            if (!conversation.isAllowCommands()) {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onDisconnect(final Disconnect event) {
        Optional<InteractiveConversation> optionalConversation = conversations.stream()
                                                                              .filter(c -> c.getContext()
                                                                                            .getReceiver()
                                                                                            .equals(event.getTargetEntity()))
                                                                              .findFirst();
        if (optionalConversation.isPresent()) {
            conversations.remove(optionalConversation.get());
        }
    }
}
