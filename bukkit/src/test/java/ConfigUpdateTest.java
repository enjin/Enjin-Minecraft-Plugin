import com.enjin.bukkit.config.EMPConfig;
import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.EnjinRPC;
import data.ConfigUpdateData;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ConfigUpdateTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";

    @Test
    public void test1ConfigUpdate() {
        File file = new File("./target/", "config.json");
        if (file.exists()) {
            file.delete();
        }

        try {
            EMPConfig        config = JsonConfig.load(file, EMPConfig.class);
            ConfigUpdateData data   = new ConfigUpdateData(false);

            config.update(file, EnjinRPC.gson.fromJson(EnjinRPC.gson.toJson(data), Object.class));
            config = JsonConfig.load(file, EMPConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void prepare() {

    }
}
