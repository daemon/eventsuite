package net.rocketeer.eventsuite;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

  public static Endpoint fromSubscribeMessage(String message)  {
    Type type = type(message);
    if (type != Type.SUBSCRIBE)
      return null;
    return new Endpoint(message.substring(1));
  }

  public static PublishMessage fromPublishMessage(String message) {
    Type type = type(message);
    if (type != Type.PUBLISH)
      return null;
    Gson gson = new Gson();
    try {
      return gson.fromJson(message.substring(1), PublishMessage.class);
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static <T> PublishMessage toPublishMessage(Endpoint endpoint, T data) {
    Gson gson = new Gson();
    try {
      String json = gson.toJson(data, data.getClass());
      return new PublishMessage(endpoint, json);
    } catch(JsonSyntaxException e) {
      return null;
    }
  }

  public static class PublishMessage {
    private Endpoint endpoint;
    private String data;
    public PublishMessage() {}
    public PublishMessage(Endpoint endpoint, String data) {
      this.data = data;
      this.endpoint = endpoint;
    }
    public Endpoint endpoint() {
      return this.endpoint;
    }

    public String data() {
      return this.data;
    }

    public String toRawMessage() {
      Gson gson = new Gson();
      try {
        return Type.PUBLISH.type + gson.toJson(this, this.getClass());
      } catch(JsonSyntaxException e) {
        return null;
      }
    }
  }
}
