package net.rocketeer.eventsuite.eventbus;

import java.util.Arrays;

public class Endpoint {
  private final String[] name;
  private final boolean inclusive;

  public Endpoint(String name) {
    String[] tokens = name.split("\\.");
    boolean inclusive = false;
    if (tokens.length > 0) {
      String lastStr = tokens[tokens.length - 1];
      inclusive = lastStr.length() == 1 && lastStr.charAt(0) == '*';
    }

    if (inclusive && tokens[tokens.length - 1].charAt(0) == '*')
      tokens = Arrays.copyOf(tokens, tokens.length - 1);
    this.inclusive = inclusive;
    this.name = tokens;
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

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.tokens());
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Endpoint))
      return false;
    Endpoint otherEndpoint = (Endpoint) other;
    return Arrays.equals(otherEndpoint.tokens(), this.tokens()) && otherEndpoint.inclusive == this.inclusive;
  }
}
