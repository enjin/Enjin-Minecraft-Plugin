import com.enjin.core.EnjinServices;
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
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";

    @Test
    public void test1Get() {
        VoteService service = EnjinServices.getService(VoteService.class);
        RPCData<String> data = service.get(new HashMap<String, List<Object[]>>() {{
            put("planetminecraft.com", new ArrayList<Object[]>() {{
                add(new Object[] {"Notch", System.currentTimeMillis() / 1000});
                add(new Object[] {"Jeb_", System.currentTimeMillis() / 1000});
                add(new Object[] {"Favorlock|8b7a881c-6ccb-4ada-8f6a-60cc99e6aa20", System.currentTimeMillis() / 1000});
            }});
        }});

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());
        Assert.assertTrue("result is not equal to ok", data.getResult().equalsIgnoreCase("ok"));
    }

    @BeforeClass
    public static void prepare() {
        DummyConfig.set(KEY, API_URL);
    }
}
