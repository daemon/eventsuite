package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.api.EventModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ModuleBaseCommand extends BaseCommandExecutor {
  private final EventModule module;

  public ModuleBaseCommand(EventModule module) {
    this.module = module;
  }

  @Override
  public boolean defaultOnCommand(CommandSender sender, Command command, String s, String[] strings) {
    // TODO return module commands
    return true;
  }

  @Override
  public String commandName() {
    return module.getDescription().getName().toLowerCase();
  }

  @Override
  public String permissionName() {
    return "eventsuite.module.cmd." + this.commandName();
  }

  @Override
  public String usage() {
    return "/es " + this.commandName();
  }
}
