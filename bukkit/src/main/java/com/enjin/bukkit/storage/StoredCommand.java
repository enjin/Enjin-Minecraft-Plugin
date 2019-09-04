package com.enjin.bukkit.storage;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import lombok.Data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
            delay = Optional.empty();

        requireOnline = Optional.of(rs.getBoolean("requireOnline"));
        if (rs.wasNull())
            requireOnline = Optional.empty();

        playerName = Optional.ofNullable(rs.getString("playerName"));

        String uuid = rs.getString("playerUuid");
        if (uuid == null || uuid.isEmpty())
            playerUuid = Optional.empty();
        else
            playerUuid = Optional.of(UUID.fromString(uuid));

        hash = Optional.ofNullable(rs.getString("hash"));
        response = Optional.ofNullable(rs.getString("response"));
        createdAt = rs.getLong("createdAt");
    }

    public boolean hasExecuted() {
        return hash.isPresent();
    }

    public void generateHash() {
        if (hash.isPresent())
            return;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(command.getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String hash = bigInt.toString(16);

            while (hash.length() < 32) {
                hash = "0" + hash;
            }

            this.hash = Optional.of(hash);
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("command_id", id);
        map.put("hash", hash.orElse(null));
        map.put("response", response.orElse(null));
        map.put("command", command);
        return map;
    }
}
