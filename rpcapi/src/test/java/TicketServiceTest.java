import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.enjin.rpc.mappings.services.TicketService;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class TicketServiceTest {
    private static final String API_URL = "http://api.enjinpink.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER = "Favorlock";
    private static final int PRESET_ID = 31915436;
    private static final String TICKET_CODE = "ef452f91";

    @Test
    public void test1GetModules() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<Map<Integer, Module>> data = service.getModules();

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        Map<Integer, Module> modules = data.getResult();

        Assert.assertNotNull("modules is null", modules);

        System.out.println("# of modules: " + modules.size());
        for (Module shop : modules.values()) {
            System.out.println(shop.toString());
        }
    }

    @Test
    public void test2GetTickets() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<TicketResults> data = service.getTickets(-1, TicketStatus.closed);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Ticket> tickets = new ArrayList<>(data.getResult().getResults().values());

        Assert.assertNotNull("tickets is null", tickets);

        System.out.println("# of tickets: " + tickets.size());
    }

    @Test
    public void test3GetPlayerTickets() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<TicketResults> data = service.getPlayerTickets(-1, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Ticket> tickets = new ArrayList<>(data.getResult().getResults().values());

        Assert.assertNotNull("tickets is null", tickets);

        System.out.println("# of tickets: " + tickets.size());
    }

    @Test
    public void test4GetReplies() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<ReplyResults> data = service.getReplies(PRESET_ID, TICKET_CODE, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Reply> replies = new ArrayList<>(data.getResult().getResults().values());

        Assert.assertNotNull("replies is null", replies);

        System.out.println("# of replies: " + replies.size());
    }

    @Test
    public void test5CreateTicket() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<Boolean> data = service.createTicket(PRESET_ID, "This is my subject", "This is my description", "Favorlock", new ArrayList<ExtraQuestion>());

        Assert.assertNotNull("data is null", data);

        if (data.getError() == null) {
            Assert.assertNotNull("data result is null", data.getResult());
        } else {
            System.out.println("Error: " + data.getError().getMessage());
            return;
        }

        Boolean success = data.getResult();

        Assert.assertNotNull("success is null", success);

        System.out.println("Created ticket: " + success.booleanValue());
    }

    @Test
    public void test6SendReply() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<RPCSuccess> data = service.sendReply(PRESET_ID, TICKET_CODE, "This is a reply", "public", TicketStatus.pending, "Favorlock");

        Assert.assertNotNull("data is null", data);

        if (data.getError() == null) {
            Assert.assertNotNull("data result is null", data.getResult());
        } else {
            System.out.println("Error: " + data.getError().getMessage());
            return;
        }

        RPCSuccess success = data.getResult();

        Assert.assertNotNull("success is null", success);

        System.out.println("Replied to ticket: " + success.getSuccess().booleanValue());
    }

    @Test
    public void test7SetStatus() {
        TicketService service = EnjinServices.getService(TicketService.class);
        RPCData<Boolean> data = service.setStatus(PRESET_ID, TICKET_CODE, TicketStatus.closed);

        Assert.assertNotNull("data is null", data);

        if (data.getError() == null) {
            Assert.assertNotNull("data result is null", data.getResult());
        } else {
            System.out.println("Error: " + data.getError().getMessage());
            return;
        }

        Boolean success = data.getResult();

        Assert.assertNotNull("success is null", success);

        System.out.println("Set ticket status: " + success.booleanValue());
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
