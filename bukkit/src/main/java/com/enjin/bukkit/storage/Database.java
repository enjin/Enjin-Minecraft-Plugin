package com.enjin.bukkit.storage;

import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database {

    private static final String DB_NAME = "enjin.db";
    private static final String MEMORY_URL = "jdbc:sqlite::memory:";
    private static final String RESOURCE_FORMAT = "db/%s.sql";
    private static final Gson GSON = new GsonBuilder()
            .create();

    private static final String TEMPLATE_CREATE_COMMANDS_TABLE = "setup/CreateCommandsTable";
    private static final String TEMPLATE_CREATE_PLAYER_GROUPS_TABLE = "setup/CreatePlayerGroupsTable";
    private static final String TEMPLATE_INSERT_COMMAND = "commands/InsertCommand";
    private static final String TEMPLATE_GET_ALL_COMMANDS = "commands/GetAllCommands";
    private static final String TEMPLATE_GET_EXECUTED_COMMANDS = "commands/GetExecutedCommands";
    private static final String TEMPLATE_GET_PENDING_COMMANDS = "commands/GetPendingCommands";
    private static final String TEMPLATE_GET_COMMAND_FOR_ID = "commands/GetCommandForId";
    private static final String TEMPLATE_SET_COMMAND_AS_EXECUTED = "commands/SetCommandAsExecuted";
    private static final String TEMPLATE_DELETE_COMMAND = "commands/DeleteCommand";
    private static final String TEMPLATE_ADD_PLAYER_GROUPS_FOR_WORLD = "groups/AddPlayerGroupsForWorld";
    private static final String TEMPLATE_GET_PLAYER_GROUPS = "groups/GetPlayerGroups";
    private static final String TEMPLATE_DELETE_PLAYER_GROUPS = "groups/DeletePlayerGroups";
    private static final String TEMPLATE_BACKUP = "backup to %s";
    private static final String TEMPLATE_RESTORE = "restore from %s";

    private Plugin plugin;
    private Connection conn;

    private PreparedStatement createCommandsTable;
    private PreparedStatement createPlayerGroupsTable;
    private PreparedStatement insertCommand;
    private PreparedStatement getCommands;
    private PreparedStatement getExecutedCommands;
    private PreparedStatement getPendingCommands;
    private PreparedStatement getCommandForId;
    private PreparedStatement setCommandAsExecuted;
    private PreparedStatement deleteCommand;
    private PreparedStatement addGroups;
    private PreparedStatement getGroups;
    private PreparedStatement deleteGroups;

    public Database(Plugin plugin) throws SQLException, IOException {
        this.plugin = plugin;
        this.conn = DriverManager.getConnection(MEMORY_URL);
        this.createCommandsTable = createPreparedStatement(TEMPLATE_CREATE_COMMANDS_TABLE);
        this.createPlayerGroupsTable = createPreparedStatement(TEMPLATE_CREATE_PLAYER_GROUPS_TABLE);

        configure();

        this.insertCommand = createPreparedStatement(TEMPLATE_INSERT_COMMAND);
        this.getCommands = createPreparedStatement(TEMPLATE_GET_ALL_COMMANDS);
        this.getExecutedCommands = createPreparedStatement(TEMPLATE_GET_EXECUTED_COMMANDS);
        this.getPendingCommands = createPreparedStatement(TEMPLATE_GET_PENDING_COMMANDS);
        this.getCommandForId = createPreparedStatement(TEMPLATE_GET_COMMAND_FOR_ID);
        this.setCommandAsExecuted = createPreparedStatement(TEMPLATE_SET_COMMAND_AS_EXECUTED);
        this.deleteCommand = createPreparedStatement(TEMPLATE_DELETE_COMMAND);
        this.addGroups = createPreparedStatement(TEMPLATE_ADD_PLAYER_GROUPS_FOR_WORLD);
        this.getGroups = createPreparedStatement(TEMPLATE_GET_PLAYER_GROUPS);
        this.deleteGroups = createPreparedStatement(TEMPLATE_DELETE_PLAYER_GROUPS);
    }

    public void backup() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format(TEMPLATE_BACKUP, getDatabasePath()));
        }
    }

    public void restore() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format(TEMPLATE_RESTORE, getDatabasePath()));
        }
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void insertCommand(StoredCommand command) throws SQLException {
        synchronized (insertCommand) {
            insertCommand.clearParameters();
            insertCommand.setLong(1, command.getId());
            insertCommand.setString(2, command.getCommand());

            if (command.getDelay().isPresent())
                insertCommand.setLong(3, command.getDelay().get());
            else
                insertCommand.setNull(3, Types.INTEGER);

            if (command.getRequireOnline().isPresent())
                insertCommand.setBoolean(4, command.getRequireOnline().get());
            else
                insertCommand.setNull(4, Types.INTEGER);


            insertCommand.setString(5, command.getPlayerName().orNull());
            insertCommand.setString(6, command.getPlayerUuid().isPresent()
                    ? command.getPlayerUuid().get().toString()
                    : null);
            insertCommand.setString(7, null);
            insertCommand.setString(8, null);
            insertCommand.setLong(9, command.getCreatedAt());
            insertCommand.executeUpdate();
        }
    }

    public List<StoredCommand> getAllCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        synchronized (getCommands) {
            try (ResultSet rs = getCommands.executeQuery()) {
                while (rs.next()) {
                    result.add(new StoredCommand(rs));
                }
            }
        }

        return result;
    }

    public List<StoredCommand> getExecutedCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        synchronized (getExecutedCommands) {
            try (ResultSet rs = getExecutedCommands.executeQuery()) {
                while (rs.next()) {
                    result.add(new StoredCommand(rs));
                }
            }
        }

        return result;
    }

    public List<StoredCommand> getPendingCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        synchronized (getPendingCommands) {
            try (ResultSet rs = getPendingCommands.executeQuery()) {
                while (rs.next()) {
                    result.add(new StoredCommand(rs));
                }
            }
        }

        return result;
    }

    public StoredCommand getCommand(long id) throws SQLException {
        StoredCommand result = null;

        synchronized (getCommandForId) {
            getCommandForId.clearParameters();
            getCommandForId.setLong(1, id);

            try (ResultSet rs = getCommandForId.executeQuery()) {
                if (rs.next()) {
                    result = new StoredCommand(rs);
                }
            }
        }

        return result;
    }

    public void setCommandAsExecuted(long id, String hash, String response) throws SQLException {
        setCommandAsExecuted.clearParameters();
        setCommandAsExecuted.setString(1, hash);
        setCommandAsExecuted.setString(2, response);
        setCommandAsExecuted.setLong(3, id);
        setCommandAsExecuted.executeUpdate();
    }

    public void deleteCommand(long id) throws SQLException {
        deleteCommand.clearParameters();
        deleteCommand.setLong(1, id);
        deleteCommand.executeUpdate();
    }

    public void addGroups(UUID playerUuid, String playerName, String worldId, List<String> groups) throws SQLException {
        String serializedGroups = GSON.toJson(groups);

        synchronized (addGroups) {
            addGroups.clearParameters();
            addGroups.setString(1, playerUuid.toString());
            addGroups.setString(2, playerName);
            addGroups.setString(3, worldId);
            addGroups.setString(4, serializedGroups);
            addGroups.executeUpdate();
        }
    }

    public Map<String, PlayerGroupInfo> getGroups() throws SQLException {
        Map<String, PlayerGroupInfo> result = new HashMap<>();

        synchronized (getGroups) {
            try (ResultSet rs = getGroups.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String uuid = rs.getString("uuid");
                    String world = rs.getString("world");
                    List<String> groups = GSON.fromJson(rs.getString("groups"),
                            TypeToken.getParameterized(List.class, String.class).getType());

                    if (!result.containsKey(name))
                        result.put(name, new PlayerGroupInfo(uuid));

                    PlayerGroupInfo info = result.get(name);
                    info.getWorlds().put(world,groups);
                }
            }
        }

        return result;
    }

    public void deleteGroups(String playerName) throws SQLException {
        synchronized (deleteGroups) {
            deleteGroups.clearParameters();
            deleteGroups.setString(1, playerName);
            deleteGroups.executeUpdate();
        }
    }

    public File getDatabasePath() {
        return new File(plugin.getDataFolder(), DB_NAME);
    }

    private void configure() throws SQLException {
        restore();
        createCommandsTable.execute();
        createPlayerGroupsTable.execute();
        conn.setAutoCommit(false);
    }

    private String loadSqlFile(String template) throws IOException {
        try (InputStream is = plugin.getResource(String.format(RESOURCE_FORMAT, template))) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
                return br.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private PreparedStatement createPreparedStatement(String template) throws SQLException, IOException {
        return conn.prepareStatement(loadSqlFile(template));
    }

}
