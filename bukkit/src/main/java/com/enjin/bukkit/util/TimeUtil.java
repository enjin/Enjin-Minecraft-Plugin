package com.enjin.bukkit.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimeUtil {

    public static long utcNowSeconds() {
        return OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond();
    }

}
