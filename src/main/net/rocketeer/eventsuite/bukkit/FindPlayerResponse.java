package net.rocketeer.eventsuite.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FindPlayerResponse {
  private String playerName;
  private boolean found;
  private String requesterName;
  private String ipAddr = "";
  public FindPlayerResponse() {}
  public FindPlayerResponse(String requesterName, Player foundPlayer) {
    this.requesterName = requesterName;
    this.found = false;
    if (foundPlayer == null)
      return;
    this.found = true;
    this.ipAddr = foundPlayer.getAddress().toString();
    this.playerName = foundPlayer.getName();
  }

  public boolean found() {
    return this.found;
  }
  
  public String requesterName() {
    return this.requesterName;
  }

  public String foundAddress() {
    return this.ipAddr;
  }

  public String playerName() {
    return this.playerName;
  }
}
