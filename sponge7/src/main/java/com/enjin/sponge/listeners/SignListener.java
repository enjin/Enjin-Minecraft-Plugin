package com.enjin.sponge.listeners;

import com.enjin.sponge.managers.StatSignManager;
import com.enjin.sponge.statsigns.EnjinSignData;
import com.enjin.sponge.statsigns.EnjinSignType;
import com.enjin.sponge.utils.serialization.SerializableLocation;
import com.google.common.base.Optional;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent.Break;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;

public class SignListener {
    @Listener
    public void onSignChange(ChangeSignEvent event) {
        if (!event.getCause().containsType(Player.class)) {
            return;
        }

        Player player = event.getCause().first(Player.class).get();
        ImmutableSignData data = event.getOriginalText();
        String line = data.get(0).get().toPlain();
        for (EnjinSignType type : EnjinSignType.values()) {
            Optional<Integer> index = type.matches(line);
            EnjinSignType.SubType subType = null;
            Optional<Integer> itemId = Optional.absent();

            if (index.isPresent()) {
                if (player.hasPermission("enjin.sign.set")) {
                    if (!type.getSupportedSubTypes().isEmpty()) {
                        String line2 = data.get(1).get().toPlain();

                        if (line2 != null && !line2.isEmpty()) {
                            switch (line2.toLowerCase()) {
                                case "day":
                                    subType = EnjinSignType.SubType.DAY;
                                    break;
                                case "week":
                                    subType = EnjinSignType.SubType.WEEK;
                                    break;
                                case "month":
                                    subType = EnjinSignType.SubType.MONTH;
                                    break;
                                case "total":
                                    subType = EnjinSignType.SubType.TOTAL;
                                    break;
                                default:
                                    subType = EnjinSignType.SubType.ITEMID;
                                    break;
                            }

                            if (!type.getSupportedSubTypes().contains(subType)) {
                                subType = type.getDefaultSubType();
                            }

                            if (subType == EnjinSignType.SubType.ITEMID) {
                                try {
                                    itemId = Optional.fromNullable(Integer.parseInt(line2));
                                } catch (NumberFormatException e) {
                                    itemId = Optional.absent();
                                }
                            }
                        }
                    }

                    if (itemId.isPresent()) {
                        StatSignManager.add(new EnjinSignData(event.getTargetTile().getLocation(), type, subType, itemId.get(), index.get()));
                    } else {
                        StatSignManager.add(new EnjinSignData(event.getTargetTile().getLocation(), type, subType, index.get()));
                    }

                    return;
                } else {
                    player.sendMessage(Text.of(TextColors.RED, "You do not have permission to set Enjin stat signs."));
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.POST)
    public void onBlockBreak(Break event) {
        if (event.isCancelled() || !event.getCause().containsType(Player.class)) {
            return;
        }

        Player player = event.getCause().first(Player.class).get();
        event.getTransactions().forEach(transaction -> {
            BlockSnapshot original = transaction.getOriginal();
            BlockState state = original.getState();
            if (state.getType().equals(BlockTypes.STANDING_SIGN) || state.getType().equals(BlockTypes.WALL_SIGN)) {
                SerializableLocation location = new SerializableLocation(original.getLocation().get());
                for (EnjinSignData data : new ArrayList<>(StatSignManager.getConfig().getSigns())) {
                    if (data.getLocation().equals(location)) {
                        if (!player.hasPermission("enjin.sign.remove")) {
                            player.sendMessage(Text.of(TextColors.RED, "You do not have permissions to remove Enjin stat signs."));
                            event.setCancelled(true);
                        } else {
                            StatSignManager.remove(location);
                        }
                    }

                    break;
                }
            }
        });
    }
}
