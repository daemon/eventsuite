package net.rocketeer.eventsuite.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface SubCommandExecutor<ReceiverType extends CommandSender> {
  String commandName();
  String permissionName();
  String usage();
  boolean onCommand(ReceiverType sender, Command command, String label, String[] args);
}
