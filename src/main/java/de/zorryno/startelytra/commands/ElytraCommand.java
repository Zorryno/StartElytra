package de.zorryno.startelytra.commands;

import de.zorryno.startelytra.StartElytra;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElytraCommand implements CommandExecutor, TabCompleter {
    private StartElytra plugin;

    public ElytraCommand(StartElytra plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player) || args.length != 1)
            return true;

        Player player = (Player) sender;
        if(plugin.saveLocation(args[0], player.getLocation()))
            player.sendMessage(args[0] + " saved");
        else
            player.sendMessage("Error: Cant Save Location");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!(sender instanceof Player))
            return Collections.emptyList();

        List<String> commands = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        commands.add("pos1");
        commands.add("pos2");

        StringUtil.copyPartialMatches(args[0], commands, completions);
        Collections.sort(completions);
        return completions;
    }
}
