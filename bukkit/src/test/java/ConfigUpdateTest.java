import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.core.Enjin;
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
    @Test
    public void test1ConfigUpdate() {
        File file = new File("./target/", "config.json");
        if (file.exists()) {
            file.delete();
        }

        EnjinConfig config = JsonConfig.load(file, EnjinConfig.class);
        ConfigUpdateData data = new ConfigUpdateData(false);

        System.out.println(config.toString());
        config.update(file, EnjinRPC.gson.fromJson(EnjinRPC.gson.toJson(data), Object.class));
        config = config.load(file, EnjinConfig.class);
        System.out.println(config.toString());
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setDebug(true);
    }
}
