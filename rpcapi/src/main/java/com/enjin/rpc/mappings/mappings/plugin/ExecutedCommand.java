package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ExecutedCommand {
    @Getter
    @SerializedName(value = "command_id")
    private String id;
    @Getter
    private String hash;
    @Getter
    private String response;
    @Getter
    private transient String command;

    public ExecutedCommand(String id, String command, String response) {
        this.id = id;
        this.command = command;
        this.response = response;
        this.hash = generateHash(command);
    }

    private String generateHash(String command) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(command.getBytes("UTF-8"));

            BigInteger bigInt = new BigInteger(1, digest);
            String hash = bigInt.toString(16);

            while (hash.length() < 32) {
                hash = "0" + hash;
            }

            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
