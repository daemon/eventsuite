package net.rocketeer.eventsuite;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessagePrompt {
  public static void error(Player player, String message) {
    player.sendMessage(ChatColor.RED + message);
  }

  public static void success(Player player, String message) {
    player.sendMessage(ChatColor.AQUA + message);
  }

  public static void agnostic(Player player, String message) {
    player.sendMessage(ChatColor.YELLOW + message);
  }

  private static Object[] parameterize(ChatColor base, ChatColor color, Object ...params) {
    String[] strings = new String[params.length];
    for (int i = 0; i < params.length; ++i)
      strings[i] = color + params[i].toString() + base;
    return strings;
  }

  public static void error(Player player, String message, Object ...parameters) {
    player.sendMessage(ChatColor.RED + String.format(message, parameterize(ChatColor.RED, ChatColor.GOLD, parameters)));
  }

  public static void success(Player player, String message, Object ...parameters) {
    player.sendMessage(ChatColor.AQUA + String.format(message, parameterize(ChatColor.AQUA, ChatColor.GREEN, parameters)));
  }

  public static void agnostic(Player player, String message, Object ...parameters) {
    player.sendMessage(ChatColor.YELLOW + String.format(message, parameterize(ChatColor.YELLOW, ChatColor.WHITE, parameters)));
  }
}
