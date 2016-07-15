package net.rocketeer.eventsuite.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EventSuiteBaseCommand extends BaseCommandExecutor {
  @Override
  public boolean defaultOnCommand(CommandSender sender, Command command, String s, String[] strings) {
    return true;
  }

  @Override
  public String commandName() {
    return "es";
  }

  @Override
  public String permissionName() {
    return "eventsuite.cmd";
  }

  @Override
  public String usage() {
    return "/es";
  }
}
