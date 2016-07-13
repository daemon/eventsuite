package net.rocketeer.eventsuite.eventbus.message;

import net.rocketeer.eventsuite.BungeeServerManager;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.EventBus;
import org.bukkit.entity.Player;

public class TeleportPlayerRequest {
  public String playerName;
  private String serverName;
  public String worldName;
  public transient static final String DEFAULT_WORLD = "";
  public double x, y, z;
  public TeleportPlayerRequest() {}
  public TeleportPlayerRequest(String playerName, String serverName, double[] loc) {
    this(playerName, serverName, loc, DEFAULT_WORLD);
  }

  public TeleportPlayerRequest(String playerName, String serverName, double[] loc, String worldName) {
    this.playerName = playerName;
    this.serverName = serverName;
    this.worldName = worldName;
    this.x = loc[0];
    this.y = loc[1];
    this.z = loc[2];
  }

  public void doRequest(Player player) {
    EventBus eb = EventSuitePlugin.instance.eventBus();
    BungeeServerManager manager = EventSuitePlugin.instance.bungeeManager();
    manager.connect(player, this.serverName);
    eb.publishAll(new Endpoint(Endpoints.TELEPORT_REQUEST), this);
  }
}
