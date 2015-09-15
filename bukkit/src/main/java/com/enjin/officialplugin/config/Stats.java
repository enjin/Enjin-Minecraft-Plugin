package com.enjin.officialplugin.config;

import lombok.Getter;

public class Stats {
    @Getter
    private PlayerStats player = new PlayerStats();
    @Getter
    private ServerStats server = new ServerStats();
}

