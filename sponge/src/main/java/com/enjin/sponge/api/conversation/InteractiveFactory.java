package com.enjin.sponge.api.conversation;

import com.google.common.collect.Lists;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;

import java.util.Arrays;
import java.util.List;

public class InteractiveFactory {
	private Object plugin;
	private InteractivePrompt startPrompt;
	private boolean passthroughToChat;
	private boolean echoInput;
	private boolean allowCommands;
	private Text prefix;
	private List<InteractiveAbandonedListener> abandonedListeners;
	private List<InteractiveCompletedListener> completedListeners;
	private List<InteractiveCanceller> cancellers;

	public InteractiveFactory(Object plugin) {
		this.plugin = plugin;
		this.startPrompt = InteractivePrompt.END_OF_CONVERSATION;
		this.passthroughToChat = false;
		this.echoInput = true;
		this.allowCommands = false;
		this.prefix = Text.EMPTY;
		this.abandonedListeners = Lists.newArrayList();
		this.completedListeners = Lists.newArrayList();
		this.cancellers = Lists.newArrayList();
	}

	private InteractiveContext getContextFor(ChatTypeMessageReceiver receiver) {
		return new InteractiveContext(plugin, receiver);
	}

	public InteractiveFactory withFirstPrompt(InteractivePrompt startPrompt) {
		this.startPrompt = startPrompt;
		return this;
	}

	public InteractiveFactory withPassthroughToChat(boolean passthroughToChat) {
		this.passthroughToChat = passthroughToChat;
		return this;
	}

	public InteractiveFactory withEchoInput(boolean echoInput) {
		this.echoInput = echoInput;
		return this;
	}

	public InteractiveFactory withPrefix(Text prefix) {
		this.prefix = prefix;
		return this;
	}

	public InteractiveFactory withAbandonListeners(InteractiveAbandonedListener ... listeners) {
		abandonedListeners.addAll(Arrays.asList(listeners));
		return this;
	}

	public InteractiveFactory withCompletedListeners(InteractiveCompletedListener ... listeners) {
		completedListeners.addAll(Arrays.asList(listeners));
		return this;
	}

	public InteractiveFactory withCancellers(InteractiveCanceller ... cancellers) {
		this.cancellers.addAll(Arrays.asList(cancellers));
		return this;
	}

	public InteractiveConversation buildConversation(ChatTypeMessageReceiver receiver) {
		InteractiveConversation conversation = new InteractiveConversation(getContextFor(receiver), startPrompt);
		conversation.setPassthroughToChat(passthroughToChat);
		conversation.setEchoInput(echoInput);
		conversation.setAllowCommands(allowCommands);
		conversation.setPrefix(prefix);

		abandonedListeners.forEach(listener -> conversation.addAbandonedListener(listener));
		completedListeners.forEach(listener -> conversation.addCompletedListener(listener));
		cancellers.forEach(canceller -> conversation.addCanceller(canceller));

		return conversation;
	}
}
