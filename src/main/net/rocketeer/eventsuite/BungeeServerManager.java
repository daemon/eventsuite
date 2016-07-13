package net.rocketeer.eventsuite;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeServerManager implements Listener, PluginMessageListener {
  private boolean foundName = false;
  private String serverName;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (this.foundName)
      return;
    Player player = event.getPlayer();
    Bukkit.getScheduler().runTaskLater(EventSuitePlugin.instance, () -> {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("GetServer");
      send(player, out.toByteArray());
    }, 5);
  }

  public String serverName() {
    return this.serverName;
  }

  private static Player firstPlayer() {
    return Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
  }

  private static void send(Player player, byte[] message) {
    player.sendPluginMessage(EventSuitePlugin.instance, "BungeeCord", message);
  }

  public void tell(String playerName, String message) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Message");
    out.writeUTF(playerName);
    out.writeUTF(message);
    Player p = firstPlayer();
    if (p == null)
      return;
    send(p, out.toByteArray());
  }

  public void connect(Player player, String serverName) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("Connect");
    out.writeUTF(serverName);
    send(player, out.toByteArray());
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
    if (!channel.equals("BungeeCord"))
      return;
    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
    String command = in.readUTF();
    if (command.equals("GetServer")) {
      String name = in.readUTF();
      System.out.println(name);
      this.serverName = name;
      this.foundName = true;
    }
  }
}
