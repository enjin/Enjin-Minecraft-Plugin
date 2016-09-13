import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.minecraft.MinecraftPlayerInfo;
import com.enjin.rpc.mappings.mappings.minecraft.ServerInfo;
import com.enjin.rpc.mappings.services.MinecraftService;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class MinecraftServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final int SERVER = 439024;

    @Test
    public void test1GetServers() {
        MinecraftService service = EnjinServices.getService(MinecraftService.class);
        RPCData<List<ServerInfo>> data = service.getServers();

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());
        Assert.assertTrue(data.getResult().size() > 0);

        List<ServerInfo> servers = data.getResult();
    }

    @Test
    public void test2GetPlayers() {
        MinecraftService service = EnjinServices.getService(MinecraftService.class);

        RPCData<List<MinecraftPlayerInfo>> data = service.getPlayers(SERVER,
                Optional.<List<String>>of(new ArrayList<String>() {{
                    add("Favorlock");
                    add("AlmightyToaster");
                }}),
                Optional.<List<String>>absent());

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());
        Assert.assertTrue(data.getResult().size() > 0);

        List<MinecraftPlayerInfo> players = data.getResult();
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
