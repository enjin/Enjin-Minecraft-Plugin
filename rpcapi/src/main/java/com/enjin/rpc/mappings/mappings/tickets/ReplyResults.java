package com.enjin.rpc.mappings.mappings.tickets;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ReplyResults {
    @Getter
    private Map<Integer, Reply> results = new HashMap<>();
}
