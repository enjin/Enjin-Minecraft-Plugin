package com.enjin.bukkit.storage;

import com.enjin.bukkit.util.TimeUtil;
import com.enjin.core.Enjin;
import com.google.common.base.Optional;
import lombok.Data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class StoredCommand {

    private long id;
    private String command;
    Optional<Long> delay;
    Optional<Boolean> requireOnline;
    Optional<String> playerName;
    Optional<UUID> playerUuid;
    Optional<String> hash;
    Optional<String> response;
    private long createdAt;

    public StoredCommand(ResultSet rs) throws SQLException {
        id = rs.getLong("id");
        command = rs.getString("command");

        delay = Optional.of(rs.getLong("delay"));
        if (rs.wasNull())
            delay = Optional.absent();

        requireOnline = Optional.of(rs.getBoolean("requireOnline"));
        if (rs.wasNull())
            requireOnline = Optional.absent();

        playerName = Optional.fromNullable(rs.getString("playerName"));

        String uuid = rs.getString("playerUuid");
        if (uuid == null || uuid.isEmpty())
            playerUuid = Optional.absent();
        else
            playerUuid = Optional.of(UUID.fromString(uuid));

        hash = Optional.fromNullable(rs.getString("hash"));
        response = Optional.fromNullable(rs.getString("response"));
        createdAt = rs.getLong("createdAt");
    }

    public StoredCommand(Long id,
                         String command,
                         Optional<Long> delay,
                         Optional<Boolean> requireOnline,
                         Optional<String> playerName,
                         Optional<UUID> playerUuid) {
        this(id, command, delay, requireOnline, playerName, playerUuid,
                Optional.absent(), Optional.absent());
    }

    public StoredCommand(Long id,
                         String command,
                         Optional<Long> delay,
                         Optional<Boolean> requireOnline,
                         Optional<String> playerName,
                         Optional<UUID> playerUuid,
                         Optional<String> hash,
                         Optional<String> response) {
        this.id = id;
        this.command = command;
        this.delay = delay;
        this.requireOnline = requireOnline;
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.hash = hash;
        this.response = response;
        this.createdAt = TimeUtil.utcNowSeconds();
    }

    public boolean hasExecuted() {
        return hash.isPresent();
    }

    public String generateHash() {
        try {
            MessageDigest md     = MessageDigest.getInstance("MD5");
            byte[]        digest = md.digest(command.getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String     hash   = bigInt.toString(16);

            while (hash.length() < 32) {
                hash = "0" + hash;
            }

            return hash;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("command_id", id);
        map.put("hash", hash.orNull());
        map.put("response", response.orNull());
        map.put("command", command);
        return map;
    }
}
