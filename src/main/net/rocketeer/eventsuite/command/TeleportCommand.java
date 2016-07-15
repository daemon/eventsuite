package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.eventbus.message.TeleportPlayerRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements SubCommandExecutor<CommandSender> {
  @Override
  public String commandName() {
    return "teleport";
  }

  @Override
  public String permissionName() {
    return "eventsuite.cmd.teleport";
  }

  @Override
  public String usage() {
    return "/es teleport <name> <x> <y> <z> <server> [world]";
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length < 5)
      return false;
    String worldName = TeleportPlayerRequest.DEFAULT_WORLD;
    if (args.length >= 6)
      worldName = args[5];
    String playerName = args[0];
    double x, y, z;
    try {
      x = Double.parseDouble(args[1]);
      y = Double.parseDouble(args[2]);
      z = Double.parseDouble(args[3]);
    } catch (Exception e) {
      return false;
    }

    String serverName = args[4];
    Player p = Bukkit.getPlayer(playerName);
    if (p == null) {
      sender.sendMessage(ChatColor.RED + "Couldn't find player on this server");
      return true;
    }
    new TeleportPlayerRequest(playerName, serverName, new double[]{x, y, z}, worldName).doRequest(p);
    return true;
  }
}
