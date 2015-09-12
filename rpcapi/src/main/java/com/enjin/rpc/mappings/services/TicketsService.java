package com.enjin.rpc.mappings.services;

import com.enjin.core.services.Service;
import com.enjin.rpc.mappings.deserializers.QuestionDeserializer;
import com.enjin.rpc.mappings.deserializers.TicketDeserializer;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCResult;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.general.ResultType;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.enjin.rpc.mappings.deserializers.ExtraQuestionDeserializer;
import com.enjin.rpc.EnjinRPC;

import java.util.*;

public class TicketsService implements Service {
    public static final Gson GSON_TICKET = new GsonBuilder()
            .registerTypeAdapter(Ticket.class, new TicketDeserializer())
            .create();
    public static final Gson GSON_EXTRA_QUESTION = new GsonBuilder()
            .registerTypeAdapter(ExtraQuestion.class, new ExtraQuestionDeserializer())
            .create();
    public static final Gson GSON_QUESTION = new GsonBuilder()
            .registerTypeAdapter(Question.class, new QuestionDeserializer())
            .create();

    public RPCData<List<Ticket>> getPlayerTickets(final String authkey, final int preset, final String player) {
        String method = "Tickets.getPlayerTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("minecraft_player", player);
        }};

        if (!(preset == -1)) {
            parameters.put("preset_id", preset);
        }

        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<List<Ticket>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Ticket>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<Ticket>> getTickets(final String authkey, final int preset, final TicketStatus status) {
        String method = "Tickets.getTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("status", status.name());
        }};

        if (!(preset == -1)) {
            parameters.put("preset_id", preset);
        }

        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<List<Ticket>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Ticket>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Map<Integer, Module>> getModules(final String authkey) {
        String method = "Tickets.getModules";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
        }};
        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<Map<Integer, Module>> data = GSON_QUESTION.fromJson(response.toJSONString(), new TypeToken<RPCData<HashMap<Integer, Module>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Boolean> setStatus(final String authkey, final int preset, final String code, final TicketStatus status) {
        String method = "Tickets.setStatus";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("ticket_code", code);
            put("status", status.name());
        }};
        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<Boolean> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<PlayerAccess> getPlayerAccess(final String authkey, final int preset, final String player) {
        String method = "Tickets.getPlayerAccess";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("minecraft_player", player);
        }};
        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<PlayerAccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<PlayerAccess>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<Reply>> getReplies(final String authkey, final int preset, final String code, final String player) {
        String method = "Tickets.getReplies";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("ticket_code", code);
            put("minecraft_player", player);
        }};

        if (preset != -1) {
            parameters.put("preset_id", preset);
        }

        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<List<Reply>> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<List<Reply>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Boolean> createTicket(final String authkey, final int preset, final String subject, final String description, final String player, final List<ExtraQuestion> extraQuestions) {
        String method = "Tickets.createTicket";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("subject", subject);
            put("description", description);
            put("minecraft_player", player);
            put("extra_questions", extraQuestions.toArray());
        }};
        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<Boolean> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (Exception e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<RPCSuccess> sendReply(final String authkey, final int preset, final String code, final String text, final String mode, final TicketStatus status, final String player) {
        String method = "Tickets.sendReply";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("preset_id", preset);
            put("ticket_code", code);
            put("text", text);
            put("mode", mode);
            put("status", status.name());
            put("minecraft_player", player);
        }};
        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());

            RPCData<RPCSuccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<RPCSuccess>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
