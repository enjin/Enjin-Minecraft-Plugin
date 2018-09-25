package com.enjin.core;

import com.google.common.base.Optional;

import java.util.List;

public interface InstructionHandler {
    void addToWhitelist(String player);

    void removeFromWhitelist(String player);

    void ban(String player);

    void pardon(String player);

    void addToGroup(String player, String group, String world);

    void removeFromGroup(String player, String group, String world);

    void execute(Long id,
                 String command,
                 Optional<Long> delay,
                 Optional<Boolean> requireOnline,
                 Optional<String> name,
                 Optional<String> uuid);

    void commandConfirmed(List<Long> executed);

    void configUpdated(Object update);

    void statusReceived(String status);

    void clearInGameCache(String player, int id, String price);

    void notify(List<String> players, String message, long time);

    void version(String version);
}
