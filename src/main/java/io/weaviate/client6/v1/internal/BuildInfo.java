package io.weaviate.client6.v1.internal;

import java.io.IOException;
import java.util.Properties;

public final class BuildInfo {
  /** Prevent public initialization. */
  private BuildInfo() {
  }

  public static final String BRANCH;
  public static final String COMMIT_ID;
  public static final String COMMIT_ID_ABBREV;
  public static final String VERSION;

  static {
    var properties = new Properties();

    try (var is = BuildInfo.class.getClassLoader().getResourceAsStream("client6-git.properties")) {
      if (is != null) {
        properties.load(is);
      }
    } catch (IOException e) {
      System.out.println("failed to load client6-git.properties, no build information will be available");
    }

    BRANCH = String.valueOf(properties.getOrDefault("git.branch", "unknown"));
    COMMIT_ID = String.valueOf(properties.getOrDefault("git.commit.id.full", "unknown"));
    COMMIT_ID_ABBREV = String.valueOf(properties.getOrDefault("git.commit.id.abbrev", "unknown"));

    String tags = (String) properties.get("git.tags");
    if (tags != null && !tags.isBlank() && !tags.equals("null")) {
      VERSION = tags;
    } else {
      VERSION = COMMIT_ID_ABBREV;
    }
  }
}
