package com.enjin.rpc.mappings.mappings.plugin;

import com.enjin.core.Enjin;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ExecutedCommand {
    @Getter
    @SerializedName(value = "command_id")
    private           Long id;
    @Getter
    private           String hash;
    @Getter
    private           String response;
    @Getter
    private transient String command;

    public ExecutedCommand(ResultSet rs) {
        try {
            id = rs.getLong(0);
            hash = rs.getString(1);
            response = rs.getString(2);
            command = rs.getString(3);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ExecutedCommand(Long id, String command, String response) {
        this.id = id;
        this.command = command;
        this.response = response;
        this.hash = generateHash(command);
    }

    private String generateHash(String command) {
        try {
            MessageDigest md     = MessageDigest.getInstance("MD5");
            byte[]        digest = md.digest(command.getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String     hash   = bigInt.toString(16);

            while (hash.length() < 32) {
                hash = "0" + hash;
            }

            return hash;
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }

        return null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap();
        map.put("command_id", id);
        map.put("hash", hash);
        map.put("response", response);
        map.put("command", command);
        return map;
    }
}
