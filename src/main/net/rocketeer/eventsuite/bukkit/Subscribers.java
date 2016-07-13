package net.rocketeer.eventsuite.bukkit;

import net.rocketeer.eventsuite.Endpoint;
import net.rocketeer.eventsuite.Endpoints;
import net.rocketeer.eventsuite.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
  }
}
