package com.enjin.rpc.mappings.mappings.plugin.data;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class NotificationData {
    @Getter
    private String       message;
    @Getter
    private List<String> players;
    @Getter
    private Long         time;
}
