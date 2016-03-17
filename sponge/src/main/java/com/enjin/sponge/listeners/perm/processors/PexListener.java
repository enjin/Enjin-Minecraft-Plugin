package com.enjin.sponge.listeners.perm.processors;

import com.enjin.sponge.listeners.perm.PermissionListener;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.command.SendCommandEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PexListener extends PermissionListener {
	private static final Pattern PEX_USER_PARENT_ADD_GROUP = Pattern.compile("^(?:pex|permissionsex|permissions) user "
			+ "([a-zA-Z0-9_]{2,16}) (?:parents|parent|par|p) (?:add|a|\\+|remove|rem|delete|del) "
			+ "group (?:[a-zA-Z0-9]{1,32})$");
	private static final Pattern PROMOTE_DEMOTE_USER = Pattern.compile("^(?:promote|prom|demote|dem) user ([a-zA-Z0-9_]{2,16})$");

    public void processCommand(CommandSource sender, String command, SendCommandEvent event) {
		if (command.startsWith("pex")) {
			Matcher matcher = PEX_USER_PARENT_ADD_GROUP.matcher(command);
			if (matcher.find()) {
				final String player = matcher.group(1);
				update(player);
			}
		} else if (command.startsWith("promote") || command.startsWith("prom")
				|| command.startsWith("demote") || command.startsWith("dem")) {
			Matcher matcher = PROMOTE_DEMOTE_USER.matcher(command);
			if (matcher.find()) {
				final String player = matcher.group(1);
				update(player);
			}
		}
    }
}
