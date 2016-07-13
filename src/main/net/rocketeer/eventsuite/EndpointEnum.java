package net.rocketeer.eventsuite;

public enum EndpointEnum {
  ANNOUNCE_MESSAGE("eventsuite.announce");
  private final Endpoint endpoint;

  EndpointEnum(String endpointName) {
    this.endpoint = new Endpoint(endpointName);
  }

  public Endpoint get() {
    return this.endpoint;
  }
}
