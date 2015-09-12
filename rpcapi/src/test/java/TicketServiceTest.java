import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.enjin.rpc.mappings.services.TicketsService;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class TicketServiceTest {
    private static final String API_URL = "https://api.enjin.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER = "Favorlock";
    private static final int PRESET_ID = 31915436;
    private static final String TICKET_CODE = "ef452f91";

    @Test
    public void test1GetModules() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<Map<Integer, Module>> data = service.getModules(KEY);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());

        Map<Integer, Module> modules = data.getResult();

        Assert.assertNotNull(modules);

        System.out.println("# of modules: " + modules.size());
        for (Module shop : modules.values()) {
            System.out.println(shop.toString());
        }
    }

    @Test
    public void test2GetTickets() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<List<Ticket>> data = service.getTickets(KEY, -1, TicketStatus.closed);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());

        List<Ticket> tickets = data.getResult();

        Assert.assertNotNull(tickets);

        System.out.println("# of tickets: " + tickets.size());
    }

    @Test
    public void test3GetPlayerTickets() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<List<Ticket>> data = service.getPlayerTickets(KEY, -1, PLAYER);

        Assert.assertNotNull(data);
        Assert.assertNotNull(data.getResult());

        List<Ticket> tickets = data.getResult();

        Assert.assertNotNull(tickets);

        System.out.println("# of tickets: " + tickets.size());
    }

    @Test
    public void test4GetReplies() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<List<Reply>> data = service.getReplies(KEY, PRESET_ID, TICKET_CODE, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Reply> replies = data.getResult();

        Assert.assertNotNull("replies is null", replies);

        System.out.println("# of replies: " + replies.size());
    }

    @Test
    public void test5CreateTicket() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<Boolean> data = service.createTicket(KEY, PRESET_ID, "This is my subject", "This is my description", "Favorlock", new ArrayList<ExtraQuestion>());

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
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<RPCSuccess> data = service.sendReply(KEY, PRESET_ID, TICKET_CODE, "This is a reply", "public", TicketStatus.pending, "Favorlock");

        Assert.assertNotNull("data is null", data);

        if (data.getError() == null) {
            Assert.assertNotNull("data result is null", data.getResult());
        } else {
            System.out.println("Error: " + data.getError().getMessage());
            return;
        }

        RPCSuccess success = data.getResult();

        Assert.assertNotNull("success is null", success);

        System.out.println("Replied to ticket: " + success.isSuccess());
    }

    @Test
    public void test7SetStatus() {
        TicketsService service = EnjinServices.getService(TicketsService.class);
        RPCData<Boolean> data = service.setStatus(KEY, PRESET_ID, TICKET_CODE, TicketStatus.closed);

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
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl(API_URL);
    }
}
