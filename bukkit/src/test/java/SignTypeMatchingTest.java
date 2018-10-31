import com.enjin.bukkit.statsigns.SignType;
import com.google.common.base.Optional;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class SignTypeMatchingTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY     = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";

    @Test
    public void test1MatchPositive() {
        String            line  = "[" + SignType.DONATION.name() + "1]";
        Optional<Integer> index = SignType.DONATION.matches(line);
    }

    @Test
    public void test2MatchNegative() {
        String            line  = "[" + SignType.DONATION.name() + "11]";
        Optional<Integer> index = SignType.DONATION.matches(line);
    }

    @BeforeClass
    public static void prepare() {
        DummyConfig.set(KEY, API_URL);
    }
}