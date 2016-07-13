package net.rocketeer.eventsuite.bukkit.command;

import net.rocketeer.eventsuite.bukkit.EventSuitePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AnnounceCommand implements SubCommandExecutor<CommandSender> {
  @Override
  public String commandName() {
    return "announce";
  }

  @Override
  public String permissionName() {
    return "eventsuite.cmd.announce";
  }

  @Override
  public String usage() {
    return "/es announce <message>";
  }

  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
    if (strings.length <= 1)
      return false;
    strings[0] = "";
    EventSuitePlugin.announce(String.join(" ", strings).trim());
    return true;
  }
}
