package net.rocketeer.eventsuite.arena;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import net.rocketeer.eventsuite.EventSuitePlugin;
import net.rocketeer.eventsuite.MessagePrompt;
import net.rocketeer.eventsuite.command.SubCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArenaCreationWizard implements Listener {
  public enum Step { ARENA_NAME_INPUT, ARENA_REGION_SELECTION, REGION_SELECTION, POINT_SELECTION };
  public static final Map<Player, ArenaCreationWizard> arenaWizards = new ConcurrentHashMap<>();
  private final Lock lock = new ReentrantLock();
  private final List<Step> steps = new LinkedList<>();
  private final Queue<String> regionNames = new LinkedList<>();
  private final Queue<String> pointNames = new LinkedList<>();
  private final List<Arena.Region> regions = new LinkedList<>();
  private final List<Arena.NamedPoint> points = new LinkedList<>();
  private volatile ListIterator<Step> currentStepIterator;
  private volatile Step currentStep;
  private volatile Arena.Region baseRegion;
  private final Player player;
  private String arenaName;
  private Callback callback;
  private volatile Arena storedArena;
  public ArenaCreationWizard(Player player) {
    this.player = player;
    this.currentStep = Step.ARENA_NAME_INPUT;
    this.steps.add(Step.ARENA_REGION_SELECTION);
  }

  public ArenaCreationWizard thenSetupRegion(String name) {
    this.steps.add(Step.REGION_SELECTION);
    this.regionNames.add(name);
    return this;
  }

  public ArenaCreationWizard thenSetupPoint(String name) {
    this.steps.add(Step.POINT_SELECTION);
    this.pointNames.add(name);
    return this;
  }

  public ArenaCreationWizard onComplete(Callback callback) {
    this.callback = callback;
    return this;
  }

  public Arena arena() {
    return this.storedArena;
  }

  public void confirm() {
    if (this.currentStep == Step.ARENA_REGION_SELECTION || this.currentStep == Step.REGION_SELECTION) {
      WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
      Selection selection = we.getSelection(player);
      if (selection == null) {
        MessagePrompt.error(this.player, "Make a selection first!");
        return;
      } else if (!(selection instanceof CuboidSelection)) {
        MessagePrompt.error(this.player, "Region must be cuboid!");
        return;
      }
      CuboidSelection cube = (CuboidSelection) selection;
      World world = new BukkitWorld(this.player.getWorld());
      CuboidRegion cuboidRegion = new CuboidRegion(world, cube.getNativeMinimumPoint(), cube.getNativeMaximumPoint());
      String name = this.currentStep == Step.ARENA_REGION_SELECTION ? "base" : this.regionNames.poll();
      Arena.Region region = new Arena.Region(name, cuboidRegion);
      if (this.baseRegion == null)
        this.baseRegion = region;
      else
        this.regions.add(region);
      MessagePrompt.success(this.player, "Successfully created region %s for %s", name, this.arenaName);
    } else if (this.currentStep == Step.POINT_SELECTION) {
      String name = this.pointNames.poll();
      Arena.NamedPoint namedPoint = new Arena.NamedPoint(name, this.player.getLocation().toVector());
      this.points.add(namedPoint);
      MessagePrompt.success(this.player, "Successfully created point %s for %s", name, this.arenaName);
    }

    if (this.currentStepIterator.hasNext()) {
      this.currentStep = this.currentStepIterator.next();
      this.prompt(this.currentStep);
      return;
    }

    arenaWizards.remove(this.player);
    String serverName = EventSuitePlugin.instance.bungeeManager().serverName();
    this.storedArena = new Arena(this.arenaName, serverName, this.player.getWorld(), this.baseRegion);
    this.storedArena.addPoints(this.points);
    this.storedArena.addRegions(this.regions);
    MessagePrompt.success(this.player, "Successfully created %s", this.arenaName);
    if (this.callback != null)
      this.callback.onFinish(this.storedArena);
  }

  private void prompt(Step step) {
    switch (step) {
    case ARENA_NAME_INPUT:
      MessagePrompt.agnostic(player, "You may do %s to leave at any time.", "/es cancel");
      MessagePrompt.agnostic(player, "Type in chat the desired arena name:");
      break;
    case ARENA_REGION_SELECTION:
      MessagePrompt.agnostic(player, "Select a cube region for %s using WorldEdit:", this.arenaName);
      MessagePrompt.agnostic(player, "Do %s after you're done", "/es confirm");
      break;
    case REGION_SELECTION:
      MessagePrompt.agnostic(player, "Select a cube region for %s (%s):", this.arenaName, this.regionNames.peek());
      MessagePrompt.agnostic(player, "Do %s after you're done", "/es confirm");
      break;
    case POINT_SELECTION:
      MessagePrompt.agnostic(player, "Select a point for %s (%s):", this.arenaName, this.pointNames.peek());
      MessagePrompt.agnostic(player, "Do %s to set your current location as the point", "/es confirm");
      break;
    }
  }

  private void handle(AsyncPlayerChatEvent event) {
    String message = event.getMessage();
    Player player = event.getPlayer();
    if (this.currentStep != Step.ARENA_NAME_INPUT)
      return;
    event.setCancelled(true);
    if (message.contains(" ")) {
      MessagePrompt.error(player, "Arena cannot contains spaces!");
      return;
    } else if (message.length() > 32) {
      MessagePrompt.error(player, "Arena cannot be longer than 32 letters!");
      return;
    }
    this.arenaName = message;
    MessagePrompt.success(player, "You have named your arena %s", arenaName);
    this.currentStep = this.currentStepIterator.next();
    this.prompt(this.currentStep);
  }

  public void run() {
    this.prompt(this.currentStep);
  }

  public void init() {
    EventSuitePlugin plugin = EventSuitePlugin.instance;
    Bukkit.getPluginManager().registerEvents(new Listener(), plugin);
    plugin.baseCommand().registerPlayerCommand(new ConfirmCommand());
    plugin.baseCommand().registerPlayerCommand(new CancelCommand());
  }

  private static class ConfirmCommand implements SubCommandExecutor<Player> {
    @Override
    public String commandName() {
      return "confirm";
    }

    @Override
    public String permissionName() {
      return "eventsuite.cmd";
    }

    @Override
    public String usage() {
      return "/es confirm";
    }

    @Override
    public boolean onCommand(Player sender, Command command, String label, String[] args) {
      ArenaCreationWizard wizard = arenaWizards.get(sender);
      if (wizard == null) {
        MessagePrompt.error(sender, "You have nothing to confirm!");
        return true;
      }
      wizard.lock.lock();
      try {
        wizard.confirm();
      } finally {
        wizard.lock.unlock();
      }
      return true;
    }
  }

  private static class CancelCommand implements SubCommandExecutor<Player> {
    @Override
    public String commandName() {
      return "cancel";
    }

    @Override
    public String permissionName() {
      return "eventsuite.cmd";
    }

    @Override
    public String usage() {
      return "/es cancel";
    }

    @Override
    public boolean onCommand(Player sender, Command command, String label, String[] args) {
      ArenaCreationWizard wizard = arenaWizards.get(sender);
      if (wizard == null) {
        MessagePrompt.error(sender, "You have nothing to cancel!");
        return true;
      }
      arenaWizards.remove(sender);
      MessagePrompt.success(sender, "Successfully cancelled creating arena.");
      return true;
    }
  }

  private static class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void onChatMessage(AsyncPlayerChatEvent event) {
      if (event.isCancelled())
        return;
      Player player = event.getPlayer();
      ArenaCreationWizard wizard = arenaWizards.get(player);
      if (wizard == null)
        return;
      wizard.lock.lock();
      try {
        wizard.handle(event);
      } finally {
        wizard.lock.unlock();
      }
    }
  }

  public static interface Callback {
    void onFinish(Arena arena);
  }
}
