package net.rocketeer.eventsuite.bukkit;

import net.rocketeer.eventsuite.AnnounceClient;
import net.rocketeer.eventsuite.bukkit.command.AnnounceCommand;
import net.rocketeer.eventsuite.bukkit.command.EventSuiteBaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private AnnounceClient announceClient;

  @Override
  public void onEnable() {
    instance = this;
    this.announceClient = new AnnounceClient();
    Bukkit.getScheduler().runTaskAsynchronously(this, this.announceClient::connect);
    EventSuiteBaseCommand baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    this.getCommand("es").setExecutor(baseCmd);
  }

  public AnnounceClient getAnnounceClient() {
    return this.announceClient;
  }
}
