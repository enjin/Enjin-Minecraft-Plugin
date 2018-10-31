import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.FilteredItem;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ShopServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER  = "Favorlock";

    @Test
    public void test1Get() {
        ShopService         service = EnjinServices.getService(ShopService.class);
        RPCData<List<Shop>> data    = service.get(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Shop> shops = data.getResult();

        Assert.assertNotNull("shops is null", shops);
    }

    @Test
    public void test2GetPurchases() {
        ShopService             service = EnjinServices.getService(ShopService.class);
        RPCData<List<Purchase>> data    = service.getPurchases(PLAYER, true);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Purchase> purchases = data.getResult();

        Assert.assertNotNull("purchases is null", purchases);
    }

    @Test
    public void test3GetItems() {
        ShopService                 service = EnjinServices.getService(ShopService.class);
        RPCData<List<FilteredItem>> data    = service.getItems(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<FilteredItem> items = data.getResult();

        Assert.assertNotNull("items is null", items);
    }

    @BeforeClass
    public static void prepare() {
        DummyConfig.set(KEY, API_URL);
    }
}
