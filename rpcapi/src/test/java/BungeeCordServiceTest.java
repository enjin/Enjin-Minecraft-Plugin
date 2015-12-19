import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.bungeecord.NodeState;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.services.BungeeCordService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class BungeeCordServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "5739f6891198cdd40898a5e7e222c77592a8e0dcb62f2d97e8";

    @Test
    public void test1Get() {
        Status status = new Status(true,
                "2.7.0-bungee",
                null,
                null,
                75,
                2,
                new ArrayList<PlayerInfo>(){{
                    add(new PlayerInfo("Favorlock", UUID.fromString("8b7a881c-6ccb-4ada-8f6a-60cc99e6aa20")));
                    add(new PlayerInfo("AlmightyToaster", UUID.fromString("5b6cf5cd-d1c8-4f54-a06e-9c4462095706")));
                }},
                null,
                null,
                null,
                null);
        Map<String, NodeState> servers = new HashMap<String, NodeState>(){{
            put("lobby", new NodeState(new ArrayList<String>(){{add("Favorlock");}}, 50));
            put("game", new NodeState(new ArrayList<String>(){{add("AlmightyToaster");}}, 25));
        }};
        BungeeCordService service = EnjinServices.getService(BungeeCordService.class);
        RPCData<SyncResponse> data = service.get(status, servers);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());

        System.out.println(data.getResult().toString());
    }

    @BeforeClass
    public static void prepare() {
        Enjin.setConfiguration(new EnjinConfig() {
            @Override
            public boolean isDebug() {
                return true;
            }

            @Override
            public void setDebug(boolean debug) {}

            @Override
            public String getAuthKey() {
                return KEY;
            }

            @Override
            public void setAuthKey(String key) {}

            @Override
            public boolean isHttps() {
                return false;
            }

            @Override
            public void setHttps(boolean https) {}

            @Override
            public boolean isAutoUpdate() {
                return false;
            }

            @Override
            public void setAutoUpdate(boolean autoUpdate) {}

            @Override
            public boolean isLoggingEnabled() {
                return false;
            }

            @Override
            public void setLoggingEnabled(boolean loggingEnabled) {}

            @Override
            public String getApiUrl() {
                return API_URL;
            }

            @Override
            public void setApiUrl(String apiUrl) {}

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
