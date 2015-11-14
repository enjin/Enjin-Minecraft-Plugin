import com.enjin.bukkit.statsigns.SignType;
import com.enjin.rpc.EnjinRPC;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Optional;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class SignTypeMatchingTest {
    @Test
    public void test1MatchPositive() {
        String line = "[" + SignType.DONATION.name() + "1]";
        Optional<Integer> index = SignType.DONATION.matches(line);

        System.out.println("Matched Index: " + (index.isPresent() ? index.get() : "null"));
    }

    @Test
    public void test2MatchNegative() {
        String line = "[" + SignType.DONATION.name() + "11]";
        Optional<Integer> index = SignType.DONATION.matches(line);

        System.out.println("Matched Index: " + (index.isPresent() ? index.get() : "null"));
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setDebug(true);
    }
}