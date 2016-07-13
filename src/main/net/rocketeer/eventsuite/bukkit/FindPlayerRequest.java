package net.rocketeer.eventsuite.bukkit;

public class FindPlayerRequest {
  private String requesterName;
  private String requestedName;
  public FindPlayerRequest() {}
  public FindPlayerRequest(String requesterName, String requestedName) {
    this.requesterName = requesterName;
    this.requestedName = requestedName;
  }
  public String requestedName() {
    return this.requestedName;
  }

  public String requesterName() {
    return this.requesterName;
  }
}
