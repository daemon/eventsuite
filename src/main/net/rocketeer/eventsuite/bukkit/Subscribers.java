package net.rocketeer.eventsuite.bukkit;

import net.rocketeer.eventsuite.Endpoints;
import net.rocketeer.eventsuite.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Subscribers {
  public static DefaultSubscribers makeDefault() {
    return new DefaultSubscribers();
  }

  public static class DefaultSubscribers {
    @Subscribe(Endpoints.ANNOUNCE_MESSAGE)
    public void onAnnounce(String message) {
      Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
  }
}
