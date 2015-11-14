package com.enjin.bukkit.listeners;

import com.enjin.bukkit.statsigns.SignData;
import com.enjin.bukkit.statsigns.SignType;
import com.enjin.bukkit.managers.StatSignManager;
import com.enjin.bukkit.util.serialization.SerializableLocation;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Optional;

public class SignListener implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String line = event.getLine(0);
        for (SignType type : SignType.values()) {
            Optional<Integer> index = type.matches(line);
            SignType.SubType subType = null;
            Optional<Integer> itemId = Optional.empty();

            if (index.isPresent()) {
                if (!type.getSupportedSubTypes().isEmpty()) {
                    String line2 = event.getLine(1);

                    if (line2 != null && !line2.isEmpty()) {
                        switch (line2.toLowerCase()) {
                            case "day":
                                subType = SignType.SubType.DAY;
                                break;
                            case "week":
                                subType = SignType.SubType.WEEK;
                                break;
                            case "month":
                                subType = SignType.SubType.MONTH;
                                break;
                            case "total":
                                subType = SignType.SubType.TOTAL;
                                break;
                            default:
                                subType = SignType.SubType.ITEMID;
                                break;
                        }

                        if (!type.getSupportedSubTypes().contains(subType)) {
                            subType = type.getDefaultSubType();
                        }

                        if (subType == SignType.SubType.ITEMID) {
                            try {
                                itemId = Optional.ofNullable(Integer.parseInt(line2));
                            } catch (NumberFormatException e) {
                                itemId = Optional.empty();
                            }
                        }
                    }
                }

                if (itemId.isPresent()) {
                    StatSignManager.add(new SignData(event.getBlock(), type, subType, itemId.get(), index.get()));
                } else {
                    StatSignManager.add(new SignData(event.getBlock(), type, subType, index.get()));
                }

                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            SerializableLocation location = new SerializableLocation(event.getBlock().getLocation());
            StatSignManager.remove(location);
        }
    }
}
