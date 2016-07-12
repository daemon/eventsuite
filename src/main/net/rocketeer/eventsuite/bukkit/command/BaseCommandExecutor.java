package net.rocketeer.eventsuite.bukkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseCommandExecutor implements CommandExecutor {
  private Map<String, SubCommandExecutor<Player>> playerCmdMap = new HashMap<>();
  private Map<String, SubCommandExecutor<CommandSender>> generalCmdMap = new HashMap<>();

  public abstract boolean defaultOnCommand(CommandSender sender, Command command, String s, String[] strings);

  private boolean checkPerms(CommandSender sender, String permissionName) {
    if (!sender.hasPermission(permissionName)) {
      sender.sendMessage(ChatColor.RED + "You don't have permission for that!");
      return false;
    }
    return true;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
    if (strings.length < 1)
      return this.defaultOnCommand(sender, command, s, strings);
    SubCommandExecutor<CommandSender> executor = this.generalCmdMap.get(strings[0].toLowerCase());
    SubCommandExecutor<Player> playerExecutor = this.playerCmdMap.get(strings[0].toLowerCase());
    if (executor == null && playerExecutor == null)
      return this.defaultOnCommand(sender, command, s, strings);
    else if (executor != null) {
      if (!this.checkPerms(sender, executor.permissionName()))
        return true;
      if (!executor.onCommand(sender, command, s, strings))
        sender.sendMessage(ChatColor.RED + executor.usage());
    } else {
      if (!this.checkPerms(sender, playerExecutor.permissionName()))
        return true;
      if (!playerExecutor.onCommand((Player) sender, command, s, strings))
        sender.sendMessage(ChatColor.RED + playerExecutor.usage());
    }

    return true;
  }

  public void registerPlayerCommand(SubCommandExecutor<Player> executor) {
    this.playerCmdMap.put(executor.commandName(), executor);
  }

  public void registerCommand(SubCommandExecutor<CommandSender> executor) {
    this.generalCmdMap.put(executor.commandName(), executor);
  }

}
