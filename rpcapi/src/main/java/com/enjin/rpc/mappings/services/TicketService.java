package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.mappings.adapters.BooleanAdapter;
import com.enjin.rpc.mappings.adapters.ByteAdapter;
import com.enjin.rpc.mappings.adapters.DoubleAdapter;
import com.enjin.rpc.mappings.adapters.FloatAdapter;
import com.enjin.rpc.mappings.adapters.IntegerAdapter;
import com.enjin.rpc.mappings.adapters.LongAdapter;
import com.enjin.rpc.mappings.adapters.ShortAdapter;
import com.enjin.rpc.mappings.deserializers.*;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.enjin.rpc.EnjinRPC;

import java.util.*;

public class TicketService implements Service {
    public static final Gson GSON_TICKET = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, new BooleanAdapter())
            .registerTypeAdapter(Byte.class, new ByteAdapter())
            .registerTypeAdapter(Short.class, new ShortAdapter())
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new LongAdapter())
            .registerTypeAdapter(Float.class, new FloatAdapter())
            .registerTypeAdapter(Double.class, new DoubleAdapter())
            .registerTypeAdapter(Ticket.class, new TicketDeserializer())
            .create();
    public static final Gson GSON_EXTRA_QUESTION = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, new BooleanAdapter())
            .registerTypeAdapter(Byte.class, new ByteAdapter())
            .registerTypeAdapter(Short.class, new ShortAdapter())
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new LongAdapter())
            .registerTypeAdapter(Float.class, new FloatAdapter())
            .registerTypeAdapter(Double.class, new DoubleAdapter())
            .registerTypeAdapter(ExtraQuestion.class, new ExtraQuestionDeserializer())
            .create();
    public static final Gson GSON_QUESTION = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, new BooleanAdapter())
            .registerTypeAdapter(Byte.class, new ByteAdapter())
            .registerTypeAdapter(Short.class, new ShortAdapter())
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new LongAdapter())
            .registerTypeAdapter(Float.class, new FloatAdapter())
            .registerTypeAdapter(Double.class, new DoubleAdapter())
            .registerTypeAdapter(Question.class, new QuestionDeserializer())
            .create();

    public RPCData<TicketResults> getPlayerTickets(final Integer preset, final String player) {
        String method = "Tickets.getPlayerTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("minecraft_player", player);
        }};

        if (!(preset == -1)) {
            parameters.put("preset_id", preset);
        }

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<TicketResults> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<TicketResults>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<TicketResults> getTickets(final Integer preset, final TicketStatus status) {
        String method = "Tickets.getTickets";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("status", status.name());
        }};

        if (!(preset == -1)) {
            parameters.put("preset_id", preset);
        }

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<TicketResults> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<TicketResults>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Map<Integer, TicketModule>> getModules() {
        String method = "Tickets.getModules";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
        }};
        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Map<Integer, TicketModule>> data = GSON_QUESTION.fromJson(response.toJSONString(), new TypeToken<RPCData<HashMap<Integer, TicketModule>>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Boolean> setStatus(final Integer preset, final String code, final TicketStatus status) {
        String method = "Tickets.setStatus";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("preset_id", preset);
            put("ticket_code", code);
            put("status", status.name());
        }};
        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Boolean> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<PlayerAccess> getPlayerAccess(final Integer preset, final String player) {
        String method = "Tickets.getPlayerAccess";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("preset_id", preset);
            put("minecraft_player", player);
        }};
        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<PlayerAccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<PlayerAccess>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<ReplyResults> getReplies(final Integer preset, final String code, final String player) {
        String method = "Tickets.getReplies";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("ticket_code", code);
            put("minecraft_player", player);
        }};

        if (preset != -1) {
            parameters.put("preset_id", preset);
        }

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<ReplyResults> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<ReplyResults>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<RPCSuccess> createTicket(final Integer preset, final String subject, final String description, final String player, final List<ExtraQuestion> extraQuestions) {
        String method = "Tickets.createTicket";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("preset_id", preset);
            put("subject", subject);
            put("description", description);
            put("minecraft_player", player);
            put("extra_questions", extraQuestions.toArray());
        }};
        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<RPCSuccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<RPCSuccess>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (Exception e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<RPCSuccess> sendReply(final Integer preset, final String code, final String text, final String mode, final TicketStatus status, final String player) {
        String method = "Tickets.sendReply";
        final Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("preset_id", preset);
            put("ticket_code", code);
            put("text", text);
            put("mode", mode);
            put("status", status.name());
            put("minecraft_player", player);
        }};
        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<RPCSuccess> data = GSON_TICKET.fromJson(response.toJSONString(), new TypeToken<RPCData<RPCSuccess>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().log(e);
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
