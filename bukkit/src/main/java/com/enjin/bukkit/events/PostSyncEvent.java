package com.enjin.bukkit.events;

import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PostSyncEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private boolean successful;
    @Getter
    private RPCData<SyncResponse> response;

    public PostSyncEvent(boolean successful, RPCData<SyncResponse> response) {
        this.successful = successful;
        this.response = response;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
