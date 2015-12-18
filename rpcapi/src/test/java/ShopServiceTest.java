import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.FilteredItem;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ShopServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER = "Favorlock";

    @Test
    public void test1Get() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Shop>> data = service.get(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Shop> shops = data.getResult();

        Assert.assertNotNull("shops is null", shops);

        System.out.println("# of shops: " + shops.size());
        for (Shop shop : shops) {
            System.out.println(shop.toString());
        }
    }

    @Test
    public void test2GetPurchases() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Purchase>> data = service.getPurchases(PLAYER, true);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Purchase> purchases = data.getResult();

        Assert.assertNotNull("purchases is null", purchases);

        System.out.println("# of purchases: " + purchases.size());
        for (Purchase purchase : purchases) {
            System.out.println(purchase.toString());
        }
    }

    @Test
    public void test3GetItems() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<FilteredItem>> data = service.getItems(PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<FilteredItem> items = data.getResult();

        Assert.assertNotNull("items is null", items);

        System.out.println("# of items: " + items.size());
        for (FilteredItem item : items) {
            System.out.println(item.toString());
        }
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
