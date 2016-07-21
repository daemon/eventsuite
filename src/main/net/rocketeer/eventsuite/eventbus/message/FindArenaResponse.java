package net.rocketeer.eventsuite.eventbus.message;

public class FindArenaResponse {
  private String arenaName;
  private String serverName;
  public FindArenaResponse() {}
  public FindArenaResponse(String arenaName, String serverName) {
    this.arenaName = arenaName;
    this.serverName = serverName;
  }

  public String arenaName() {
    return this.arenaName;
  }

  public String serverName() {
    return this.serverName;
  }
}
