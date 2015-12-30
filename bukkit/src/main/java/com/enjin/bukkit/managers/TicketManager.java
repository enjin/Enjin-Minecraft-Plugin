package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.bukkit.tickets.TicketListener;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.services.TicketService;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class TicketManager {
    @Getter
    private static Map<Integer, Module> modules = new HashMap<>();
    @Getter
    private static long modulesLastPolled = 0;
    @Getter
    private static TicketListener ticketListener;

    public static void init(EnjinMinecraftPlugin plugin) {
        clean();

        ticketListener = new TicketListener();
        Bukkit.getPluginManager().registerEvents(ticketListener, plugin);

        pollModules();
    }

    private static void clean() {
        TicketManager.getModules().clear();
        for (TicketCreationSession session : TicketCreationSession.getSessions().values()) {
            session.getConversation().abandon();
        }
    }

    public static void pollModules() {
        if (System.currentTimeMillis() - modulesLastPolled > 10 * 60 * 1000) {
            modulesLastPolled = System.currentTimeMillis();
            RPCData<Map<Integer, Module>> data = EnjinServices.getService(TicketService.class).getModules();

            if (data == null || data.getError() != null) {
                Enjin.getPlugin().debug(data == null ? "Could not retrieve support modules." : data.getError().getMessage());
                modules.clear();
                return;
            }

            if (data.getResult().size() == 0) {
                modules.clear();
            }

            for (Map.Entry<Integer, Module> entry : data.getResult().entrySet()) {
                modules.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
