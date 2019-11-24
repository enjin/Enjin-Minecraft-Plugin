package com.enjin.bukkit.cmd;

import com.enjin.bukkit.cmd.arg.PlayerArgumentProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandContext {

    protected CommandSender sender;
    protected SenderType senderType;
    protected Player player;
    protected List<String> args;
    protected String alias;
    protected Deque<EnjinCommand> commandStack;
    protected List<String> tabCompletionResult;

    public CommandContext(CommandSender sender, List<String> args, String alias) {
        this.sender = sender;
        this.senderType = SenderType.type(sender);
        this.args = args;
        this.alias = alias;
        this.commandStack = new ArrayDeque<>();
        this.tabCompletionResult = new ArrayList<>();

        if (sender instanceof Player)
            player = (Player) sender;
    }

    public Optional<Player> argToPlayer(int index) {
        if (args.isEmpty() || index >= args.size())
            return Optional.empty();
        return PlayerArgumentProcessor.INSTANCE.parse(sender, args.get(index));
    }

    public Optional<Integer> argToInt(int index) {
        if (index >= args.size())
            return Optional.empty();

        Optional<Integer> optional = Optional.empty();

        try {
            Integer value = Integer.parseInt(args.get(index));
            optional = Optional.of(value);
        } catch (NumberFormatException ignore) {
        }

        return optional;
    }

    public static List<EnjinCommand> createCommandStackAsList(EnjinCommand top) {
        List<EnjinCommand> list = new ArrayList<>();

        list.add(top);
        Optional<EnjinCommand> parent = top.parent;
        while (parent.isPresent()) {
            list.add(0, parent.get());
            parent = parent.get().parent;
        }

        return list;
    }

    public CommandSender getSender() {
        return sender;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public Player getPlayer() {
        return player;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getAlias() {
        return alias;
    }

    public Deque<EnjinCommand> getCommandStack() {
        return commandStack;
    }

    public List<String> getTabCompletionResult() {
        return tabCompletionResult;
    }
}
