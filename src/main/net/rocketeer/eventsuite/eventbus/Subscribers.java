package net.rocketeer.eventsuite.eventbus;

import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.eventbus.Endpoint;
import net.rocketeer.eventsuite.eventbus.Endpoints;
import net.rocketeer.eventsuite.eventbus.Subscribe;
import net.rocketeer.eventsuite.eventbus.message.FindPlayerRequest;
import net.rocketeer.eventsuite.eventbus.message.FindPlayerResponse;
import net.rocketeer.eventsuite.eventbus.message.TeleportPlayerRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Subscribers {
  public static DefaultSubscribers makeDefault() {
    return new DefaultSubscribers();
  }

  public static class DefaultSubscribers {
    @Subscribe(Endpoints.ANNOUNCE_MESSAGE)
    public void onAnnounce(String message) {
      Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Subscribe(Endpoints.FIND_REQUEST)
    public void onFindRequest(FindPlayerRequest request) {
      Player player = Bukkit.getPlayer(request.requestedName());
      FindPlayerResponse response = new FindPlayerResponse(request.requesterName(), player);
      EventSuitePlugin.instance.eventBus().publishAll(new Endpoint(Endpoints.FIND_RESPONSE), response);
    }

    @Subscribe(Endpoints.FIND_RESPONSE)
    public void onFindResponse(FindPlayerResponse response) {
      Player player = Bukkit.getPlayer(response.requesterName());
      if (player == null)
        return;
      if (response.found())
        player.sendMessage(response.playerName() + "[" + response.foundAddress() + "]");
    }

    @Subscribe(Endpoints.TELEPORT_REQUEST)
    public void onTeleportRequest(TeleportPlayerRequest request) {
      Bukkit.getScheduler().runTaskLater(EventSuitePlugin.instance, () -> {
        Player player = Bukkit.getPlayer(request.playerName);
        if (player == null)
          return;
        World world = Bukkit.getWorld(request.worldName);
        if (request.worldName.equals(TeleportPlayerRequest.DEFAULT_WORLD))
          world = player.getWorld();
        if (world == null)
          return;
        Location l = new Location(world, request.x, request.y, request.z);
        player.teleport(l);
      }, 60);
    }
  }
}
