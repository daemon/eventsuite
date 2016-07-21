package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.MessagePrompt;
import net.rocketeer.eventsuite.arena.ArenaCreationWizard;
import net.rocketeer.eventsuite.arena.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class ArenaCreateCommand implements SubCommandExecutor<Player> {
  @Override
  public String commandName() {
    return "createarena"; // TODO change, testing for now
  }

  @Override
  public String permissionName() {
    return "eventsuite.cmd.createarena";
  }

  @Override
  public String usage() {
    return "/es createarena";
  }

  @Override
  public boolean onCommand(Player sender, Command command, String label, String[] args) {
    new ArenaCreationWizard(sender).thenSetupPoint("test").thenSetupRegion("hello").onComplete(arena -> {
      EventSuitePlugin.instance.arenaManager().registerNew(arena);
    }).run();
    return true;
  }
}
