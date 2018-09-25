import com.enjin.bukkit.config.EMPConfig;
import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.EnjinRPC;
import data.ConfigUpdateData;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ConfigUpdateTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";

    @Test
    public void test1ConfigUpdate() {
        File file = new File("./target/", "config.json");
        if (file.exists()) {
            file.delete();
        }

        EMPConfig        config = JsonConfig.load(file, EMPConfig.class);
        ConfigUpdateData data   = new ConfigUpdateData(false);

        config.update(file, EnjinRPC.gson.fromJson(EnjinRPC.gson.toJson(data), Object.class));
        config = JsonConfig.load(file, EMPConfig.class);
    }

    @BeforeClass
    public static void prepare() {
        Enjin.setConfiguration(new EnjinConfig() {
            @Override
            public boolean isDebug() {
                return true;
            }

            @Override
            public void setDebug(boolean debug) {
            }

            @Override
            public String getAuthKey() {
                return KEY;
            }

            @Override
            public void setAuthKey(String key) {
            }

            @Override
            public boolean isHttps() {
                return false;
            }

            @Override
            public void setHttps(boolean https) {
            }

            @Override
            public boolean isAutoUpdate() {
                return false;
            }

            @Override
            public void setAutoUpdate(boolean autoUpdate) {
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
                return API_URL;
            }

            @Override
            public void setApiUrl(String apiUrl) {
            }

            @Override
            public boolean save(File file) {
                return true;
            }

            @Override
            public boolean update(File file, Object data) {
                return true;
            }
        });
    }
}
