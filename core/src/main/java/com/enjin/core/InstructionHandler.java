package com.enjin.core;

import java.util.List;
import java.util.Map;

public interface InstructionHandler {
    public void addToWhitelist(String player);

    public void removeFromWhitelist(String player);

    public void ban(String player);

    public void pardon(String player);

    public void addToGroup(String player, String group, String world);

    public void removeFromGroup(String player, String group, String world);

    public void execute(long id, String command, long delay);

    public void commandConfirmed(List<Long> executed);

    public void configUpdated(Map<String, Object> updates);

    public void statusReceived(String status);

    public void clearInGameCache(String player, int id, String price);

    public void notify(List<String> players, String message, long time);

    public void version(String version);
}
