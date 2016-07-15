package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.EventSuitePlugin;
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
    if (strings.length == 0)
      return false;
    EventSuitePlugin.announce(String.join(" ", strings).trim());
    return true;
  }
}
