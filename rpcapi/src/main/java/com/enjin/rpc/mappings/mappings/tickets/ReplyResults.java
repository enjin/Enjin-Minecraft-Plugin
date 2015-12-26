package com.enjin.rpc.mappings.mappings.tickets;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ReplyResults {
    @Getter
    private List<Reply> results = new ArrayList<>();
}
