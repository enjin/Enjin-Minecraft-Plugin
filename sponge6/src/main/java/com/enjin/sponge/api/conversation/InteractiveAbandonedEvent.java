package com.enjin.sponge.api.conversation;

import lombok.Getter;

public class InteractiveAbandonedEvent {
    @Getter
    private InteractiveContext      context;
    @Getter
    private InteractiveConversation conversation;

    public InteractiveAbandonedEvent(InteractiveContext context, InteractiveConversation conversation) {
        this.context = context;
        this.conversation = conversation;
    }
}
