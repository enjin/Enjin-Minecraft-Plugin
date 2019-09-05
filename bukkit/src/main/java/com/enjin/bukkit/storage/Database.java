package com.enjin.bukkit.storage;

import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Database {

    private static final String DB_NAME = "enjin.db";
    private static final String MEMORY_URL = "jdbc:sqlite::memory:";
    private static final String RESOURCE_FORMAT = "db/%s.sql";

    private static final String TEMPLATE_SETUP = "Setup";
    private static final String TEMPLATE_INSERT_COMMAND = "InsertCommand";
    private static final String TEMPLATE_GET_ALL_COMMANDS = "GetAllCommands";
    private static final String TEMPLATE_GET_EXECUTED_COMMANDS = "GetExecutedCommands";
    private static final String TEMPLATE_GET_PENDING_COMMANDS = "GetPendingCommands";
    private static final String TEMPLATE_GET_COMMAND_FOR_ID = "GetCommandForId";
    public static final String TEMPLATE_SET_COMMAND_AS_EXECUTED = "SetCommandAsExecuted";
    private static final String TEMPLATE_DELETE_COMMAND = "DeleteCommand";
    private static final String TEMPLATE_BACKUP = "backup to %s";
    private static final String TEMPLATE_RESTORE = "restore from %s";

    private Plugin plugin;
    private Connection conn;

    private PreparedStatement setup;
    private PreparedStatement insertCommand;
    private PreparedStatement getCommands;
    private PreparedStatement getExecutedCommands;
    private PreparedStatement getPendingCommands;
    private PreparedStatement getCommandForId;
    private PreparedStatement setCommandAsExecuted;
    private PreparedStatement deleteCommand;

    public Database(Plugin plugin) throws SQLException, IOException {
        this.plugin = plugin;
        this.conn = DriverManager.getConnection(MEMORY_URL);
        this.setup = createPreparedStatement(TEMPLATE_SETUP);

        configure();

        this.insertCommand = createPreparedStatement(TEMPLATE_INSERT_COMMAND);
        this.getCommands = createPreparedStatement(TEMPLATE_GET_ALL_COMMANDS);
        this.getExecutedCommands = createPreparedStatement(TEMPLATE_GET_EXECUTED_COMMANDS);
        this.getPendingCommands = createPreparedStatement(TEMPLATE_GET_PENDING_COMMANDS);
        this.getCommandForId = createPreparedStatement(TEMPLATE_GET_COMMAND_FOR_ID);
        this.setCommandAsExecuted = createPreparedStatement(TEMPLATE_SET_COMMAND_AS_EXECUTED);
        this.deleteCommand = createPreparedStatement(TEMPLATE_DELETE_COMMAND);
    }

    public void backup() throws SQLException {
        commit();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format(TEMPLATE_BACKUP, getDatabasePath()));
        }
    }

    public void restore() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format(TEMPLATE_RESTORE, getDatabasePath()));
        }
        commit();
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void insertCommand(StoredCommand command) throws SQLException {
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

    public List<StoredCommand> getAllCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        try (ResultSet rs = getCommands.executeQuery()) {
            while (rs.next()) {
                result.add(new StoredCommand(rs));
            }
        }

        return result;
    }

    public List<StoredCommand> getExecutedCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        try (ResultSet rs = getExecutedCommands.executeQuery()) {
            while (rs.next()) {
                result.add(new StoredCommand(rs));
            }
        }

        return result;
    }

    public List<StoredCommand> getPendingCommands() throws SQLException {
        List<StoredCommand> result = new ArrayList<>();

        try (ResultSet rs = getPendingCommands.executeQuery()) {
            while (rs.next()) {
                result.add(new StoredCommand(rs));
            }
        }

        return result;
    }

    public StoredCommand getCommand(long id) throws SQLException {
        StoredCommand result = null;

        getCommandForId.clearParameters();
        getCommandForId.setLong(1, id);

        try (ResultSet rs = getCommandForId.executeQuery()) {
            if (rs.next()) {
                result = new StoredCommand(rs);
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

    public File getDatabasePath() {
        return new File(plugin.getDataFolder(), DB_NAME);
    }

    private void configure() throws SQLException {
        conn.setAutoCommit(false);
        restore();
        setup.execute();
        commit();
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
