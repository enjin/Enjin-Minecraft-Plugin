import com.enjin.core.EnjinServices;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class BungeeCordServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "5739f6891198cdd40898a5e7e222c77592a8e0dcb62f2d97e8";
    private static final int PORT = 25565;
    private static final String PLAYER = "Favorlock";

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
                null);
        Map<String, NodeState> servers = new HashMap<String, NodeState>(){{
            put("lobby", new NodeState(new ArrayList<String>(){{add("Favorlock");}}, 50));
            put("game", new NodeState(new ArrayList<String>(){{add("AlmightyToaster");}}, 25));
        }};
        BungeeCordService service = EnjinServices.getService(BungeeCordService.class);
        RPCData<SyncResponse> data = service.get(KEY, status, servers);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("result is null", data.getResult());

        System.out.println(data.getResult().toString());
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setDebug(true);
        EnjinRPC.setHttps(false);
        EnjinRPC.setApiUrl(API_URL);
    }
}
