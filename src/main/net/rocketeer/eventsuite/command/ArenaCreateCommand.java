package net.rocketeer.eventsuite.command;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ArenaCreateCommand implements SubCommandExecutor<Player> {
  @Override
  public String commandName() {
    return null;
  }

  @Override
  public String permissionName() {
    return null;
  }

  @Override
  public String usage() {
    return null;
  }

  @Override
  public boolean onCommand(Player sender, Command command, String label, String[] args) {
    return false;
  }
}
