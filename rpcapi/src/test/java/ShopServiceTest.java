import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ShopServiceTest {
    @Test
    public void test1Get() {
        prepare();

        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Shop>> data = service.get("cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e", "Favorlock");

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());

        List<Shop> shops = data.getResult();

        Assert.assertNotNull(shops);

        System.out.println("# of shops: " + shops.size());
        for (Shop shop : shops) {
            System.out.println(shop.toString());
        }
    }

    @Test
    public void test2GetPurchases() {
        prepare();

        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Purchase>> data = service.getPurchases("cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e", "Favorlock", true);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());

        List<Purchase> purchases = data.getResult();

        Assert.assertNotNull(purchases);

        System.out.println("# of purchases: " + purchases.size());
        for (Purchase purchase : purchases) {
            System.out.println(purchase.toString());
        }
    }

    private static void prepare() {
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl("https://api.enjin.com/api/v1/");
    }
}
