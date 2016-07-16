package net.rocketeer.eventsuite.command;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.MessagePrompt;
import net.rocketeer.eventsuite.arena.Arena;
import net.rocketeer.eventsuite.arena.ArenaCreationWizard;
import net.rocketeer.eventsuite.arena.ArenaDatabase;
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
      System.out.println(arena.toString());
      ArenaDatabase arenaDatabase = new ArenaDatabase();
      MessagePrompt.agnostic(sender, "Saving %s into database...", arena.name());
      Bukkit.getScheduler().runTaskAsynchronously(EventSuitePlugin.instance, () -> {
        ArenaDatabase.ArenaInsertResult result = arenaDatabase.insertArena(arena);
        Bukkit.getScheduler().runTask(EventSuitePlugin.instance, () -> {
          switch (result) {
          case SUCCESS:
            MessagePrompt.success(sender, "Successfully saved %s.", arena.name());
            break;
          case EXISTS:
            MessagePrompt.error(sender, "An arena called %s already exists!", arena.name());
            break;
          case SQL_FAILURE:
            MessagePrompt.error(sender, "Database failure. Is the database configured properly?");
            break;
          }
        });
      });
    }).run();
    return true;
  }
}
