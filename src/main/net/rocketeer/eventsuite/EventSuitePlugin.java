package net.rocketeer.eventsuite;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.rocketeer.eventsuite.api.EventModule;
import net.rocketeer.eventsuite.arena.ArenaCreationWizard;
import net.rocketeer.eventsuite.arena.ArenaManager;
import net.rocketeer.eventsuite.command.*;
import net.rocketeer.eventsuite.config.ConfigManager;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import net.rocketeer.eventsuite.eventbus.Subscribers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import java.util.List;

public class EventSuitePlugin extends JavaPlugin {
  public static EventSuitePlugin instance;
  private EventBus eventBus;
  private String serverName;
  private ConfigManager configManager;
  private BungeeServerManager manager;
  private ArenaManager arenaManager;
  private EventSuiteBaseCommand baseCmd;

  // TODO ModuleManager?
  public ModuleBaseCommand registerModule(EventModule module) {
    String name = module.getConfig().getString("eventsuite.name");
    List<String> commands = module.getConfig().getStringList("eventsuite.commands");
    ModuleBaseCommand mbc = new ModuleBaseCommand(module);
    this.baseCmd.registerCommand(mbc);
    return mbc;
  }

  public BaseCommandExecutor baseCommand() {
    return this.baseCmd;
  }

  private void setupManagers() {
    this.configManager = new ConfigManager(this.getConfig());
    this.arenaManager = new ArenaManager(this.configManager.arenas());
    this.eventBus = new EventBus();
    BungeeServerManager manager = new BungeeServerManager();
    this.manager = manager;
    Bukkit.getPluginManager().registerEvents(manager, this);
    Messenger messenger = this.getServer().getMessenger();
    messenger.registerOutgoingPluginChannel(this, "BungeeCord");
    messenger.registerIncomingPluginChannel(this, "BungeeCord", eventBus);
    messenger.registerIncomingPluginChannel(this, "BungeeCord", manager);
    this.eventBus.subscribe(Subscribers.makeDefault());
  }

  private void setupCommands() {
    this.baseCmd = new EventSuiteBaseCommand();
    baseCmd.registerCommand(new AnnounceCommand());
    baseCmd.registerPlayerCommand(new FindCommand());
    baseCmd.registerCommand(new TeleportCommand());
    baseCmd.registerPlayerCommand(new ArenaCreateCommand());
    this.getCommand("es").setExecutor(baseCmd);
  }

  @Override
  public void onEnable() {
    instance = this;
    this.saveDefaultConfig();
    this.setupManagers();
    this.setupCommands();
    ArenaCreationWizard.init();
  }

  public ArenaManager arenaManager() {
    return this.arenaManager;
  }

  public BungeeServerManager bungeeManager() {
    return this.manager;
  }

  public ConfigManager configManager() {
    return this.configManager;
  }

  public static void announce(String str) {
    announce(str, "Events");
  }

  public static void announce(String str, String tag) {
    str = "&b[&6" + tag + "&b]&f " + str.trim();
    EventSuitePlugin.instance.eventBus().publishAll(new Endpoint(Endpoints.ANNOUNCE_MESSAGE), str);
  }

  public EventBus eventBus() {
    return this.eventBus;
  }

  public WorldGuardPlugin worldGuard() {
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    if (plugin == null)
      return null;
    return (WorldGuardPlugin) plugin;
  }
}
