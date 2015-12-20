package com.enjin.rpc.mappings.mappings.tickets;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TicketResults {
    @Getter
    private List<Ticket> results = new ArrayList<>();
}
