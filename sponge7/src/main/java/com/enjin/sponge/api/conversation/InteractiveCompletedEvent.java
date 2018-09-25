package com.enjin.sponge.api.conversation;

import lombok.Getter;

public class InteractiveCompletedEvent {
    @Getter
    private InteractiveContext      context;
    @Getter
    private InteractiveConversation conversation;

    public InteractiveCompletedEvent(InteractiveContext context, InteractiveConversation conversation) {
        this.context = context;
        this.conversation = conversation;
    }
}
