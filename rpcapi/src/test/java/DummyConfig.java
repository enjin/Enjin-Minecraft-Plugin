import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;

import java.io.File;

public class DummyConfig implements EnjinConfig {

    private String key;
    private String apiUrl;

    private DummyConfig(String key, String apiUrl) {
        this.key = key;
        this.apiUrl = apiUrl;
    }

    @Override
    public boolean isDebug() {
        return true;
    }

    @Override
    public void setDebug(boolean debug) {
    }

    @Override
    public String getAuthKey() {
        return key;
    }

    @Override
    public void setAuthKey(String key) {
        this.key = key;
    }

    @Override
    public boolean isHttps() {
        return false;
    }

    @Override
    public void setHttps(boolean https) {
    }

    @Override
    public int getSyncDelay() {
        return 0;
    }

    @Override
    public void setSyncDelay(int delay) {

    }

    @Override
    public boolean isLoggingEnabled() {
        return false;
    }

    @Override
    public void setLoggingEnabled(boolean loggingEnabled) {
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public boolean save(File file) {
        return true;
    }

    @Override
    public boolean update(File file, Object data) {
        return true;
    }

    public static void set(String key, String apiUrl) {
        Enjin.setConfiguration(new DummyConfig(key, apiUrl));
    }

}
