package com.enjin.common;

import com.enjin.common.compatibility.CurrentLog4j2Handler;
import com.enjin.common.compatibility.LegacyLog4j2Handler;
import com.enjin.common.compatibility.Log4j2Handler;

public class Log4j2Handlers {

    public static Log4j2Handler findHandler() {
        Log4j2Handler handler = null;
        if (LegacyLog4j2Handler.detected)
            handler = new LegacyLog4j2Handler();
        else if (CurrentLog4j2Handler.detected)
            handler = new CurrentLog4j2Handler();
        return handler;
    }

}
