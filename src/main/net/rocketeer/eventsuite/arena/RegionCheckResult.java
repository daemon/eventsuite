package net.rocketeer.eventsuite.arena;

import java.util.List;
import java.util.Optional;

public class RegionCheckResult {
  public enum Type { SUCCESS, SQL_FAILURE, UNKNOWN_FAILURE, NO_ID };
  private final Type type;
  private Optional<List<Arena.Region>> regions = Optional.empty();
  public RegionCheckResult(Type type) {
    this.type = type;
  }

  public Type type() {
    return this.type;
  }

  public RegionCheckResult(List<Arena.Region> regions) {
    this.regions = Optional.of(regions);
    this.type = Type.SUCCESS;
  }
}
