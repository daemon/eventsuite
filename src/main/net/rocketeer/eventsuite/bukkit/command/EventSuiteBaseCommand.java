package net.rocketeer.eventsuite.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EventSuiteBaseCommand extends BaseCommandExecutor {
  @Override
  public boolean defaultOnCommand(CommandSender sender, Command command, String s, String[] strings) {
    return true;
  }
}
