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

    public static final String DB_NAME = "enjin.db";
    public static final String MEMORY_URL = "jdbc:sqlite::memory:";
    public static final String RESOURCE_FORMAT = "db/%s.sql";

    public static final String TEMPLATE_SETUP = "Setup";
    public static final String TEMPLATE_INSERT_COMMAND = "InsertCommand";
    public static final String TEMPLATE_GET_ALL_COMMANDS = "GetAllCommands";
    public static final String TEMPLATE_GET_COMMAND_FOR_ID = "GetCommandForId";
    public static final String TEMPLATE_DELETE_COMMAND = "DeleteCommand";
    public static final String TEMPLATE_BACKUP = "backup to %s";
    public static final String TEMPLATE_RESTORE = "restore from %s";

    private Plugin plugin;
    private Connection conn;

    private PreparedStatement setup;
    private PreparedStatement insertCommand;
    private PreparedStatement getCommands;
    private PreparedStatement getCommandForId;
    private PreparedStatement deleteCommand;

    public Database(Plugin plugin) throws SQLException, IOException {
        this.plugin = plugin;
        this.conn = DriverManager.getConnection(MEMORY_URL);
        this.setup = createPreparedStatement(TEMPLATE_SETUP);

        configure();

        this.insertCommand = createPreparedStatement(TEMPLATE_INSERT_COMMAND);
        this.getCommands = createPreparedStatement(TEMPLATE_GET_ALL_COMMANDS);
        this.getCommandForId = createPreparedStatement(TEMPLATE_GET_COMMAND_FOR_ID);
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
        insertCommand.clearParameters();
        insertCommand.setLong(1, id);
        insertCommand.setString(2, command);

        if (delay.isPresent())
            insertCommand.setLong(3, delay.get());
        else
            insertCommand.setNull(3, Types.INTEGER);

        if (requireOnline.isPresent())
            insertCommand.setBoolean(4, requireOnline.get());
        else
            insertCommand.setNull(4, Types.INTEGER);

        insertCommand.setString(5, playerName.orElse(null));
        insertCommand.setString(6,playerUuid.orElse(null));
        insertCommand.setString(7, null);
        insertCommand.setString(8, null);
        insertCommand.setLong(9, OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond());
        insertCommand.executeUpdate();
    }

    public List<ExecutedCommand> getCommands() throws SQLException {
        List<ExecutedCommand> result = new ArrayList<>();

        try (ResultSet rs = getCommands.executeQuery()) {
            while (rs.next()) {
                result.add(new ExecutedCommand(rs));
            }
        }

        return result;
    }

    public ExecutedCommand getCommand(long id) throws SQLException {
        ExecutedCommand result = null;

        try (ResultSet rs = getCommandForId.executeQuery()) {
            if (rs.next()) {
                result = new ExecutedCommand(rs);
            }
        }

        return result;
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
