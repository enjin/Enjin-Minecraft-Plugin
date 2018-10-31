import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Stats;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class PluginServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final int    PORT    = 25565;
    private static final String PLAYER  = "Favorlock";

    @Test
    public void test1Auth() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Auth> data    = service.auth(Optional.<String>absent(), PORT, true, true);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());
        Assert.assertTrue("result is not true", data.getResult().isAuthed());
    }

    @Test
    public void test2Sync() {
        HashMap<String, Object> status = new HashMap<>();
        status.put("java_version", System.getProperty("java.version"));
        status.put("mc_version", "UNKNOWN");
        status.put("plugins", null);
        status.put("pluginversion", "3.0.0-bukkit");
        status.put("worlds", new ArrayList<String>() {{
            add("world");
            add("end");
            add("nether");
        }});
        status.put("groups", new ArrayList<String>() {{
            add("default");
            add("creeper");
        }});
        status.put("maxplayers", 50);
        status.put("players", 2);
        status.put("playerlist", new ArrayList<PlayerInfo>() {{
            add(new PlayerInfo("Favorlock",
                               UUID.fromString("8b7a881c-6ccb-4ada-8f6a-60cc99e6aa20")));
            add(new PlayerInfo("AlmightyToaster",
                               UUID.fromString("5b6cf5cd-d1c8-4f54-a06e-9c4462095706")));
        }});
        status.put("playergroups", null);
        status.put("tps", null);
        status.put("executed_commands", null);
        status.put("votifier", null);
        status.put("stats", null);
        PluginService         service = EnjinServices.getService(PluginService.class);
        RPCData<SyncResponse> data    = service.sync(status);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());
    }

    @Test
    public void test3GetTags() {
        PluginService          service = EnjinServices.getService(PluginService.class);
        RPCData<List<TagData>> data    = service.getTags(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());
    }

    @Test
    public void test4GetStats() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Stats> data = service.getStats(Optional.<List<Integer>>fromNullable(new ArrayList<Integer>() {{
            add(1584937);
            add(1604379);
        }}));

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());
    }

    @BeforeClass
    public static void prepare() {
        DummyConfig.set(KEY, API_URL);
    }
}
