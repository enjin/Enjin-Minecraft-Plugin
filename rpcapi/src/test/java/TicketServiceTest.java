import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.rpc.mappings.mappings.tickets.TicketStatus;
import com.enjin.rpc.mappings.services.TicketsService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
        RPCData<List<Reply>> data = service.getReplies(API_URL, PRESET_ID, TICKET_CODE, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Reply> replies = data.getResult();

        Assert.assertNotNull("replies is null", replies);

        System.out.println("# of replies: " + replies.size());
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl(API_URL);
    }
}
