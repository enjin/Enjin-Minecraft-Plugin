import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.VoteService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class VoteServiceTest {
    private static final String API_URL = "https://api.enjin.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER = "Favorlock";

    @Test
    public void test1Get() {
        VoteService service = EnjinServices.getService(VoteService.class);
        RPCData<String> data = service.get(KEY, new HashMap<String, List<String>>() {{
            put(PLAYER, new ArrayList<String>() {{
                add("Planet Minecraft");
            }});
        }});

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());
        Assert.assertTrue("result is not equal to ok", data.getResult().equalsIgnoreCase("ok"));

        System.out.println("Sent votes to enjin!");
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl(API_URL);
    }
}
