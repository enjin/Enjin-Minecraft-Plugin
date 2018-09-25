package com.enjin.bukkit.modules.impl;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.Module;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.bukkit.tickets.TicketListener;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.TicketService;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

@Module(name = "Support")
public class SupportModule {
    private EnjinMinecraftPlugin                                               plugin;
    @Getter
    private Map<Integer, com.enjin.rpc.mappings.mappings.tickets.TicketModule> modules           = new HashMap<>();
    @Getter
    private long                                                               modulesLastPolled = 0;
    @Getter
    private TicketListener                                                     ticketListener;

    public SupportModule() {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        init();
    }

    public void init() {
        clean();

        ticketListener = new TicketListener();
        Bukkit.getPluginManager().registerEvents(ticketListener, plugin);

        pollModules();
    }

    private void clean() {
        getModules().clear();
        for (TicketCreationSession session : TicketCreationSession.getSessions().values()) {
            session.getConversation().abandon();
        }
    }

    public void pollModules() {
        if (System.currentTimeMillis() - modulesLastPolled > 10 * 60 * 1000) {
            modulesLastPolled = System.currentTimeMillis();
            RPCData<Map<Integer, com.enjin.rpc.mappings.mappings.tickets.TicketModule>> data = EnjinServices.getService(
                    TicketService.class).getModules();

            if (data == null || data.getError() != null) {
                Enjin.getLogger()
                     .debug(data == null ? "Could not retrieve support modules." : data.getError().getMessage());
                modules.clear();
                return;
            }

            if (data.getResult().size() == 0) {
                modules.clear();
            }

            for (Map.Entry<Integer, com.enjin.rpc.mappings.mappings.tickets.TicketModule> entry : data.getResult()
                                                                                                      .entrySet()) {
                modules.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
