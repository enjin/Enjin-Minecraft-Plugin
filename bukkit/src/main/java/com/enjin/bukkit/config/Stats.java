package com.enjin.bukkit.config;

import lombok.Getter;

public class Stats {
    @Getter
    private PlayerStats player = new PlayerStats();
    @Getter
    private ServerStats server = new ServerStats();
}

