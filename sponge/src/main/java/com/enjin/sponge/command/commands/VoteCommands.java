package com.enjin.sponge.command.commands;

import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import com.enjin.sponge.managers.VotifierManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Optional;

public class VoteCommands {
    @Permission(value = "enjin.test.vote")
    @Directive(parent = "enjin", value = "vote")
    public static void vote(CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Text.of(TextColors.RED, "Usage: /enjin vote <username> <list>"));
            return;
        }

        String username = args[0];
        String listname = args[1].replaceAll("[^0-9A-Za-z.\\-]", "");

        Optional<Player> player = Sponge.getServer().getPlayer(username);
		if (player.isPresent()) {
            username = username.concat("|" + player.get().getUniqueId().toString());
        }

        if (!VotifierManager.getPlayerVotes().containsKey(listname)) {
			VotifierManager.getPlayerVotes().put(listname, new ArrayList<>());
        }

		VotifierManager.getPlayerVotes().get(listname).add(new Object[]{username, System.currentTimeMillis() / 1000});
        sender.sendMessage(Text.of(TextColors.GREEN, "You just added a vote for player ", username, " on list ", listname));
    }
}
