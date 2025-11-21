package io.weaviate.client6.v1.internal;

import java.util.Arrays;

public final class VersionSupport {
  public static final SemanticVersion MINIMAL_SUPPORTED_VERSION = new SemanticVersion(1, 32);

  /**
   * Returns true if the {@code version} is the same as or older than the
   * {@link VersionSupport#MINIMAL_SUPPORTED_VERSION}.
   */
  public static boolean isSupported(String version) {
    var semver = SemanticVersion.of(version);
    return semver.compareTo(MINIMAL_SUPPORTED_VERSION) >= 0;
  }

  public record SemanticVersion(int major, int minor, String patch) implements Comparable<SemanticVersion> {

    public SemanticVersion(int major, int minor) {
      this(major, minor, null);
    }

    public SemanticVersion(int major, int minor, int patch) {
      this(major, minor, String.valueOf(patch));
    }

    /**
     * Parse semantic version from a formatted string,
     * e.g. {@code "(v)1.23.6-rc.1"}.
     */
    public static SemanticVersion of(String version) {
      var parts = version.replaceFirst("v", "").split("\\.");
      var major = Integer.valueOf(parts[0].replaceAll("[^0-9]", ""));
      var minor = Integer.valueOf(parts[1].replaceAll("[^0-9]", ""));
      var patch = parts.length > 2
          ? String.join(".", Arrays.stream(parts, 2, parts.length).toList())
          : null;
      return new SemanticVersion(major, minor, patch);
    }

    @Override
    public int compareTo(SemanticVersion that) {
      var this_v = Integer.valueOf("%d%d".formatted(this.major, this.minor));
      var that_v = Integer.valueOf("%d%d".formatted(that.major, that.minor));
      return this_v.compareTo(that_v);
    }

    public String toString() {
      return String.join(".", String.valueOf(major), String.valueOf(minor), patch != null ? patch : "0");
    }
  }
}
