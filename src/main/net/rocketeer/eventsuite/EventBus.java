package net.rocketeer.eventsuite;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.rocketeer.eventsuite.bukkit.EventSuitePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.lang.reflect.Method;
import java.util.List;

public class EventBus implements PluginMessageListener {
  private final EndpointTrie<Handler> callbackMap = new EndpointTrie<>();

  public void subscribe(Object callback) {
    for (Method m : callback.getClass().getDeclaredMethods())
      if (m.isAnnotationPresent(Subscribe.class) && m.getParameterCount() == 1) {
        Endpoint endpoint = new Endpoint(m.getDeclaredAnnotation(Subscribe.class).value());
        Handler handler = new Handler(callback, m);
        synchronized (callbackMap) {
          callbackMap.insert(endpoint, handler);
        }
      }
  }

  private void sendRawMessage(String message) {
    Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
    if (player == null)
      return;
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Forward");
    out.writeUTF("ALL");
    out.writeUTF("eventsuite");
    out.writeShort(message.length());
    out.write(message.getBytes());
    player.sendPluginMessage(EventSuitePlugin.instance, "BungeeCord", out.toByteArray());
  }

  public <T> void publish(Endpoint endpoint, T data) {
    EventMessage.PublishMessage message = EventMessage.toPublishMessage(endpoint, data);
    if (message == null)
      return;
    String rawMessage = message.toRawMessage();
    if (rawMessage == null)
      return;
    this.sendRawMessage(rawMessage);
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    ByteArrayDataInput input = ByteStreams.newDataInput(message);
    String chan = input.readUTF();
    if (!chan.equals("eventsuite"))
      return;
    short length = input.readShort();
    byte[] messageBytes = new byte[length];
    input.readFully(messageBytes);
    String msg = new String(messageBytes);
    if (EventMessage.type(msg) != EventMessage.Type.PUBLISH)
      return;
    EventMessage.PublishMessage pubMsg = EventMessage.fromPublishMessage(msg);
    if (pubMsg == null)
      return;
    List<Handler> handlers;
    synchronized (callbackMap) {
      handlers = callbackMap.lookup(pubMsg.endpoint());
    }

    for (Handler h : handlers)
      h.handle(pubMsg);
  }

  private static class Handler {
    private final Method method;
    private final Object object;
    private final Gson gson;

    public Handler(Object object, Method method) {
      this.method = method;
      this.object = object;
      this.gson = new Gson();
    }

    public void handle(EventMessage.PublishMessage pubMsg) {
      Object o;
      try {
        o = this.gson.fromJson(pubMsg.data(), this.method.getParameterTypes()[0]);
      } catch (JsonSyntaxException e) {
        e.printStackTrace();
        return;
      }
      try {
        System.out.println(o.toString());
        this.method.invoke(this.object, o);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
