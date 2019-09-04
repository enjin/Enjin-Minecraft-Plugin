package com.enjin.bukkit.storage;

import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Database {

    public static final String DB_NAME = "enjin.db";
    public static final String MEMORY_URL = "jdbc:sqlite::memory:";
    public static final String RESOURCE_FORMAT = "db/%s.sql";

    public static final String TEMPLATE_SETUP = "Setup";
    public static final String TEMPLATE_INSERT_EXECUTED_COMMAND = "InsertExecutedCommand";
    public static final String TEMPLATE_GET_EXECUTED_COMMANDS = "GetExecutedCommands";
    public static final String TEMPLATE_GET_EXECUTED_COMMAND_FOR_ID = "GetExecutedCommandForId";
    public static final String TEMPLATE_DELETE_EXECUTED_COMMAND = "DeleteExecutedCommand";
    public static final String TEMPLATE_BACKUP = "backup to %s";
    public static final String TEMPLATE_RESTORE = "restore from %s";

    private Plugin plugin;
    private Connection conn;

    private PreparedStatement setup;
    private PreparedStatement insertExecutedCommand;
    private PreparedStatement getExecutedCommands;
    private PreparedStatement getExecutedCommandForId;
    private PreparedStatement deleteExecutedCommand;

    public Database(Plugin plugin) throws SQLException, IOException {
        this.plugin = plugin;
        this.conn = DriverManager.getConnection(MEMORY_URL);
        this.setup = createPreparedStatement(TEMPLATE_SETUP);

        configure();

        this.insertExecutedCommand = createPreparedStatement(TEMPLATE_INSERT_EXECUTED_COMMAND);
        this.getExecutedCommands = createPreparedStatement(TEMPLATE_GET_EXECUTED_COMMANDS);
        this.getExecutedCommandForId = createPreparedStatement(TEMPLATE_GET_EXECUTED_COMMAND_FOR_ID);
        this.deleteExecutedCommand = createPreparedStatement(TEMPLATE_DELETE_EXECUTED_COMMAND);
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

    @SuppressWarnings("unchecked") // Suppressing unchecked because the optional values are empty
    public void insertCommand(Long id,
                              String command) throws SQLException {
        Optional empty = Optional.empty();
        insertCommand(id, command, empty, empty, empty, empty);
    }

    public void insertCommand(Long id,
                              String command,
                              Optional<Long> delay,
                              Optional<Boolean> requireOnline,
                              Optional<String> playerName,
                              Optional<String> playerUuid) throws SQLException {
        insertExecutedCommand.clearParameters();
        insertExecutedCommand.setLong(1, id);
        insertExecutedCommand.setString(2, command);

        if (delay.isPresent())
            insertExecutedCommand.setLong(3, delay.get());
        else
            insertExecutedCommand.setNull(3, Types.INTEGER);

        if (requireOnline.isPresent())
            insertExecutedCommand.setBoolean(4, requireOnline.get());
        else
            insertExecutedCommand.setNull(4, Types.INTEGER);

        insertExecutedCommand.setString(5, playerName.orElse(null));
        insertExecutedCommand.setString(6,playerUuid.orElse(null));
        insertExecutedCommand.setString(7, null);
        insertExecutedCommand.setString(8, null);
        insertExecutedCommand.executeUpdate();
    }

    public List<ExecutedCommand> getExecutedCommands() throws SQLException {
        List<ExecutedCommand> result = new ArrayList<>();

        try (ResultSet rs = getExecutedCommands.executeQuery()) {
            while (rs.next()) {
                result.add(new ExecutedCommand(rs));
            }
        }

        return result;
    }

    public ExecutedCommand getExecutedCommand(long id) throws SQLException {
        ExecutedCommand result = null;

        try (ResultSet rs = getExecutedCommandForId.executeQuery()) {
            if (rs.next()) {
                result = new ExecutedCommand(rs);
            }
        }

        return result;
    }

    public void deleteExecutedCommand(long id) throws SQLException {
        deleteExecutedCommand.clearParameters();
        deleteExecutedCommand.setLong(1, id);
        deleteExecutedCommand.executeUpdate();
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
