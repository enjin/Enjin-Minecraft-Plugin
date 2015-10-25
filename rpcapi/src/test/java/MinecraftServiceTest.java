import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.minecraft.PlayerInfo;
import com.enjin.rpc.mappings.mappings.minecraft.ServerInfo;
import com.enjin.rpc.mappings.services.MinecraftService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class MinecraftServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final int SERVER = 439024;

    @Test
    public void test1GetServers() {
        MinecraftService service = EnjinServices.getService(MinecraftService.class);
        RPCData<List<ServerInfo>> data = service.getServers(KEY);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());
        Assert.assertTrue(data.getResult().size() > 0);

        List<ServerInfo> servers = data.getResult();

        System.out.println(servers.toString());
    }

    @Test
    public void test2GetPlayers() {
        MinecraftService service = EnjinServices.getService(MinecraftService.class);

        RPCData<List<PlayerInfo>> data = service.getPlayers(KEY, SERVER,
                Optional.of(new ArrayList<String>(){{
                    add("Favorlock");
                    add("AlmightyToaster");
                }}),
                Optional.empty());

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());
        Assert.assertTrue(data.getResult().size() > 0);

        List<PlayerInfo> players = data.getResult();

        System.out.println(players.toString());
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setDebug(true);
        EnjinRPC.setHttps(false);
        EnjinRPC.setApiUrl(API_URL);
    }
}
