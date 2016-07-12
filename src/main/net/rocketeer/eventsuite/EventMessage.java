package net.rocketeer.eventsuite;

import com.google.gson.Gson;

public class EventMessage {
  public enum Type {
    SUBSCRIBE('\1'), PUBLISH('\2');
    public char type;
    Type(char type) {
      this.type = type;
    }
  }

  public static EventMessage.Type type(String message) {
    if (message.length() == 0)
      return null;
    char c = message.charAt(0);
    for (Type type : EventMessage.Type.values())
      if (c == type.type)
        return type;
    return null;
  }

  public static Endpoint toSubscribeMessage(String message) {
    Type type = type(message);
    if (type != Type.SUBSCRIBE)
      return null;
    String[] tokens = message.substring(1).split("\\.");
    boolean inclusive = false;
    if (tokens.length > 0) {
      String lastStr = tokens[tokens.length - 1];
      inclusive = lastStr.length() == 1 && lastStr.charAt(0) == '*';
    }

    return new Endpoint(tokens, inclusive);
  }

  public static PublishMessage toPublishMessage(String message) {
    Type type = type(message);
    if (type != Type.PUBLISH)
      return null;
    Gson gson = new Gson();
    return gson.fromJson(message.substring(1), PublishMessage.class);
  }

  private static class PublishMessage {
    public Endpoint endpoint;
    String data;
    public PublishMessage() {}
  }
}
