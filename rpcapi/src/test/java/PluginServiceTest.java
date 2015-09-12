import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class PluginServiceTest {
    private static final String API_URL = "https://api.enjin.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final int PORT = 25565;

    @Test
    public void test1Auth() {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Boolean> data = service.auth(KEY, PORT, true);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());
        Assert.assertTrue(data.getResult());

        System.out.println("Successfully authenticated: " + data.getResult().booleanValue());
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl(API_URL);
    }
}
