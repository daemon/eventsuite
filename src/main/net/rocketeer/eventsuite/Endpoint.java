package net.rocketeer.eventsuite;

import java.util.Arrays;

public class Endpoint {
  private final String[] name;
  private final boolean inclusive;

  public Endpoint(String[] name, boolean inclusive) {
    if (inclusive && name[name.length - 1].charAt(0) == '*')
      name = Arrays.copyOf(name, name.length - 1);
    this.inclusive = inclusive;
    this.name = name;
  }

  public String[] tokens() {
    return this.name;
  }

  public boolean inclusive() {
    return this.inclusive;
  }

  public boolean in(Endpoint other) {
    // implement trie later to reduce string checks
    // e.g. worldedit.axe.* is in worldedit.*, but worldedit.axe.* is not in worldedit.axe.swing.* or
    // worldedit.axe
    String[] otherToks = other.name;
    String[] toks = this.name;
    if (toks.length != otherToks.length && !this.inclusive)
      return false;
    else if (toks.length < otherToks.length)
      return false;
    for (int i = 0; i < otherToks.length; ++i)
      if (!otherToks[i].equals(toks[i]))
        return false;
    if (toks.length == otherToks.length && !this.inclusive)
      return true;
    else if (toks.length > otherToks.length && this.inclusive)
      return true;
    return false;
  }
}
