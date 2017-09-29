package com.enjin.sponge.api.conversation;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.List;

public class InteractiveConversation {
    @Getter
    private InteractiveContext context;
    private InteractivePrompt startPrompt;
    private InteractivePrompt currentPrompt;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean passthroughToChat;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean echoInput;
    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    private boolean allowCommands;
    @Setter(value = AccessLevel.PROTECTED)
    private Text prefix;
    private List<InteractiveAbandonedListener> abandonedListeners;
    private List<InteractiveCompletedListener> completedListeners;
    private List<InteractiveCanceller> cancellers;

    public InteractiveConversation(InteractiveContext context, InteractivePrompt startPrompt) {
        this.context = context;
        this.startPrompt = startPrompt;
        this.currentPrompt = startPrompt;
        this.passthroughToChat = false;
        this.echoInput = true;
        this.allowCommands = false;
        this.prefix = Text.EMPTY;
        this.abandonedListeners = Lists.newArrayList();
        this.completedListeners = Lists.newArrayList();
        this.cancellers = Lists.newArrayList();
    }

    /**
     * Sets the next prompt.
     *
     * @param prompt the prompt
     */
    private void nextPrompt(InteractivePrompt prompt) {
        this.currentPrompt = prompt;
    }

    /**
     * Outputs the current (in this case the next) prompt or signals that the conversation is completed
     * if the current prompt is null.
     */
    private void outputNextPrompt() {
        if (currentPrompt == InteractivePrompt.END_OF_CONVERSATION) {
            complete();
        } else {
            sendText(currentPrompt.getPromptText(context));

            if (!currentPrompt.waitForInput(context)) {
                processInput(Text.EMPTY, null);
            }
        }
    }

    /**
     * Ends the conversation, removing it from the register list of conversations and fires
     * an InteractiveCompletedEvent to all InteractiveCompletedListener.
     */
    private void complete() {
        InteractiveService.removeConversation(this);
        final InteractiveCompletedEvent event = new InteractiveCompletedEvent(context, this);
        completedListeners.forEach(listener -> listener.onComplete(event));
    }

    /**
     * Processes the Text input for the provided MessageChannel.
     *
     * @param input   the input
     * @param channel the channel
     */
    protected void processInput(Text input, MessageChannel channel) {
        if (input != null && !input.isEmpty()) {
            if (passthroughToChat && channel != null) {
                // This message should passthrough to all players of the original channel.
                channel.send(context.getReceiver(), input);
            } else if (echoInput) {
                // This message should only passthrough to the sender.
                context.getReceiver().sendMessage(ChatTypes.CHAT, input);
            }
        }

        boolean cancelled = false;
        for (InteractiveCanceller canceller : cancellers) {
            if (canceller.cancelBasedOnInput(context, input)) {
                cancelled = true;
            }
        }

        if (!cancelled) {
            nextPrompt(this.currentPrompt.acceptInput(context, input));
            outputNextPrompt();
        } else {
            abandon();
        }
    }

    /**
     * Adds an InteractiveAbandonedListener to the conversation.
     *
     * @param listener the listener
     */
    protected void addAbandonedListener(InteractiveAbandonedListener listener) {
        abandonedListeners.add(listener);
    }

    /**
     * Adds an InteractiveCompletedListener to the conversation.
     *
     * @param listener the listener
     */
    protected void addCompletedListener(InteractiveCompletedListener listener) {
        completedListeners.add(listener);
    }

    protected void addCanceller(InteractiveCanceller canceller) {
        cancellers.add(canceller);
    }

    /**
     * Starts the conversation, adding it to the registered list of conversations in progress.
     */
    public void begin() {
        InteractiveService.registerConversation(this);
        outputNextPrompt();
    }

    /**
     * Ends the conversation, removing it from the register list of conversations and fires
     * an InteractiveAbandonedEvent to all InteractiveAbandonedListeners.
     */
    public void abandon() {
        InteractiveService.removeConversation(this);

        final InteractiveAbandonedEvent event = new InteractiveAbandonedEvent(context, this);
        abandonedListeners.forEach(listener -> listener.onAbandon(event));
    }

    /**
     * Sends the provided text to the receiver.
     *
     * @param text the text
     */
    public void sendText(Text text) {
        if (text != null && !text.isEmpty()) {
            context.getReceiver().sendMessage(ChatTypes.CHAT, Text.of(prefix, text));
        }
    }
}
