package com.crownSMP.commands;

import com.crownSMP.CrownSMPPlugin;
import com.crownSMP.models.CrownType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CrownCommand implements CommandExecutor, TabCompleter {

    private final CrownSMPPlugin plugin;

    public CrownCommand(CrownSMPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("crownsmp.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give"   -> cmdGive(sender, args);
            case "remove" -> cmdRemove(sender, args);
            case "lives"  -> cmdLives(sender, args);
            case "revive" -> cmdRevive(sender, args);
            case "scatter"-> cmdScatter(sender, args);
            case "reload" -> cmdReload(sender);
            default       -> sendHelp(sender);
        }
        return true;
    }

    // /crown give <player> <crown>
    private void cmdGive(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage("§cUsage: /crown give <player> <crown>"); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return; }

        CrownType type = parseCrown(args[2]);
        if (type == null) { sender.sendMessage("§cUnknown crown: §e" + args[2]); return; }

        plugin.getCrownManager().giveCrown(target, type);
        sender.sendMessage("§aGave " + type.getDisplayName() + " §ato §e" + target.getName());
    }

    // /crown remove <player>
    private void cmdRemove(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("§cUsage: /crown remove <player>"); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return; }

        plugin.getCrownManager().removeCrown(target);
        sender.sendMessage("§aRemoved Crown from §e" + target.getName());
    }

    // /crown lives <player> [set|add|remove] [amount]
    private void cmdLives(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("§cUsage: /crown lives <player> [set|add|remove] [amount]"); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found."); return; }

        if (args.length == 2) {
            int lives = plugin.getLivesManager().getLives(target.getUniqueId());
            sender.sendMessage("§e" + target.getName() + " §7has §c" + lives + " §7lives.");
            return;
        }

        if (args.length < 4) { sender.sendMessage("§cUsage: /crown lives <player> <set|add|remove> <amount>"); return; }
        int amount;
        try { amount = Integer.parseInt(args[3]); }
        catch (NumberFormatException ex) { sender.sendMessage("§cInvalid number."); return; }

        switch (args[2].toLowerCase()) {
            case "set"    -> plugin.getLivesManager().setLives(target.getUniqueId(), amount);
            case "add"    -> plugin.getLivesManager().addLives(target.getUniqueId(), amount);
            case "remove" -> plugin.getLivesManager().addLives(target.getUniqueId(), -amount);
            default       -> { sender.sendMessage("§cUnknown operation. Use set/add/remove."); return; }
        }
        sender.sendMessage("§aUpdated §e" + target.getName() + "§a's lives. Now: §c"
                + plugin.getLivesManager().getLives(target.getUniqueId()));
    }

    // /crown revive <player>
    private void cmdRevive(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage("§cUsage: /crown revive <player>"); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cPlayer not found or not online."); return; }

        if (sender instanceof Player player) {
            // Living player joining a ritual
            plugin.getRevivalManager().joinRitual(player, target);
        } else {
            // Console force-revive
            plugin.getRevivalManager().forceRevive(target);
            sender.sendMessage("§aForce-revived §e" + target.getName());
        }
    }

    // /crown scatter [crown]
    private void cmdScatter(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            CrownType type = parseCrown(args[1]);
            if (type == null) { sender.sendMessage("§cUnknown crown: §e" + args[1]); return; }
            plugin.getCrownManager().scatterCrowns(List.of(type));
            sender.sendMessage("§aScattered the " + type.getDisplayName());
        } else {
            plugin.getCrownManager().scatterAll();
            sender.sendMessage("§aAll Crowns have been scattered!");
        }
    }

    // /crown reload
    private void cmdReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getCrownManager().loadCrowns();
        sender.sendMessage("§aCrownSMP config reloaded.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private CrownType parseCrown(String input) {
        for (CrownType t : CrownType.values()) {
            if (t.name().equalsIgnoreCase(input) || t.getRawName().equalsIgnoreCase(input)) return t;
        }
        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lCrownSMP Commands:");
        sender.sendMessage("§e/crown give <player> <crown>");
        sender.sendMessage("§e/crown remove <player>");
        sender.sendMessage("§e/crown lives <player> [set|add|remove] [amount]");
        sender.sendMessage("§e/crown revive <player>");
        sender.sendMessage("§e/crown scatter [crown]");
        sender.sendMessage("§e/crown reload");
    }

    // ── Tab completion ─────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("crownsmp.admin")) return List.of();

        List<String> completions = new ArrayList<>();
        List<String> crowns = Arrays.stream(CrownType.values())
                .map(t -> t.name().toLowerCase()).collect(Collectors.toList());
        List<String> players = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName).collect(Collectors.toList());

        if (args.length == 1) {
            completions.addAll(List.of("give","remove","lives","revive","scatter","reload"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give","remove","lives","revive" -> completions.addAll(players);
                case "scatter" -> completions.addAll(crowns);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) completions.addAll(crowns);
            if (args[0].equalsIgnoreCase("lives")) completions.addAll(List.of("set","add","remove"));
        }

        String filter = args[args.length - 1].toLowerCase();
        return completions.stream().filter(s -> s.startsWith(filter)).collect(Collectors.toList());
    }
}
