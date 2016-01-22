package com.enjin.core.config;

import java.io.File;

public interface EnjinConfig {
    boolean isDebug();

    void setDebug(boolean debug);

    String getAuthKey();

    void setAuthKey(String key);

    boolean isHttps();

    void setHttps(boolean https);

    boolean isAutoUpdate();

    void setAutoUpdate(boolean autoUpdate);

    boolean isLoggingEnabled();

    void setLoggingEnabled(boolean loggingEnabled);

    String getApiUrl();

    void setApiUrl(String apiUrl);

    boolean save(File file);

    boolean update(File file, Object data);
}
