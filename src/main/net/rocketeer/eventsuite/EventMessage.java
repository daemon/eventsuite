package net.rocketeer.eventsuite;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class EventMessage {
  public enum Type {
    SUBSCRIBE((byte) 1), PUBLISH((byte) 2);
    public byte type;
    Type(byte type) {
      this.type = type;
    }
  }

  public static EventMessage.Type type(byte[] message) {
    if (message.length == 0)
      return null;
    byte b = message[0];
    for (Type type : EventMessage.Type.values())
      if (b == type.type)
        return type;
    return null;
  }

  public static PublishMessage fromPublishMessage(byte[] message) {
    Type type = type(message);
    if (type != Type.PUBLISH)
      return null;
    String str = new String(Arrays.copyOfRange(message, 1, message.length));
    Gson gson = new Gson();
    try {
      return gson.fromJson(str, PublishMessage.class);
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

    public byte[] toRawMessage() {
      Gson gson = new Gson();
      try {
        byte[] msgArr = gson.toJson(this, this.getClass()).getBytes("UTF8");
        ByteBuffer buffer = ByteBuffer.allocate(msgArr.length + 1);
        buffer.put(Type.PUBLISH.type);
        buffer.put(msgArr);
        return buffer.array();
      } catch(JsonSyntaxException e) {
        return null;
      } catch (UnsupportedEncodingException e) {
        return null;
      }
    }
  }
}
