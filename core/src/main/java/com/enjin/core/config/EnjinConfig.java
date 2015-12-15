package com.enjin.core.config;

import java.io.File;

public interface EnjinConfig {
    public boolean isDebug();

    public void setDebug(boolean debug);

    public String getAuthKey();

    public void setAuthKey(String key);

    public boolean isHttps();

    public void setHttps(boolean https);

    public boolean isAutoUpdate();

    public void setAutoUpdate(boolean autoUpdate);

    public boolean isLoggingEnabled();

    public void setLoggingEnabled(boolean loggingEnabled);

    public String getApiUrl();

    public void setApiUrl(String apiUrl);

    public boolean save(File file);

    public boolean update(File file, Object data);
}
