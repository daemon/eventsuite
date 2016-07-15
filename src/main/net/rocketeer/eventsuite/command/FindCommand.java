package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.eventbus.message.FindPlayerRequest;
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
    if (args.length == 0)
      return false;
    String name = args[0];
    FindPlayerRequest request = new FindPlayerRequest(sender.getName(), name);
    EventSuitePlugin.instance.eventBus().publishAll(new Endpoint(Endpoints.FIND_REQUEST), request);
    return true;
  }
}
