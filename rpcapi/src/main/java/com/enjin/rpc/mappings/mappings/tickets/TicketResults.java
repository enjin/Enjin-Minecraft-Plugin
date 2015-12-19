package com.enjin.rpc.mappings.mappings.tickets;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TicketResults {
    @Getter
    private Map<Integer, Ticket> results = new HashMap<>();
}
