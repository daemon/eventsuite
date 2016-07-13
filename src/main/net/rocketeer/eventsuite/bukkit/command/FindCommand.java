package net.rocketeer.eventsuite.bukkit.command;

import net.rocketeer.eventsuite.Endpoint;
import net.rocketeer.eventsuite.Endpoints;
import net.rocketeer.eventsuite.bukkit.EventSuitePlugin;
import net.rocketeer.eventsuite.bukkit.FindPlayerRequest;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class FindCommand implements SubCommandExecutor<Player> {
  @Override
  public String commandName() {
    return "find";
  }

  @Override
  public String permissionName() {
    return "eventsuite.cmd.find";
  }

  @Override
  public String usage() {
    return "/es find <player>";
  }

  @Override
  public boolean onCommand(Player sender, Command command, String label, String[] args) {
    if (args.length <= 1)
      return false;
    String name = args[1];
    FindPlayerRequest request = new FindPlayerRequest(sender.getName(), name);
    EventSuitePlugin.instance.eventBus().publishAll(new Endpoint(Endpoints.FIND_REQUEST), request);
    return true;
  }
}
