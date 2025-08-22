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

  static {
    var properties = new Properties();

    try {
      properties.load(BuildInfo.class.getClassLoader().getResourceAsStream("client6-git.properties"));
    } catch (IOException | NullPointerException e) {
      System.out.println("failed to load client6-git.properties, no build information will be available");
    }

    BRANCH = String.valueOf(properties.get("git.branch"));
    COMMIT_ID = String.valueOf(properties.get("git.commit.id.full"));
    COMMIT_ID_ABBREV = String.valueOf(properties.get("git.commit.id.abbrev"));
  }
}
