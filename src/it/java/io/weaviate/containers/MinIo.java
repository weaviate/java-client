package io.weaviate.containers;

import org.testcontainers.containers.MinIOContainer;

public class MinIo extends MinIOContainer {
  private static final String DOCKER_IMAGE = "minio/minio";
  public static final String ACCESS_KEY = "minioadmin";
  public static final String SECRET_KEY = "minioadmin";

  public static final String HOST_NAME = "minio";
  public static final String URL = HOST_NAME + ":9000";

  static MinIo createDefault() {
    return new MinIo();
  }

  private MinIo() {
    super(DOCKER_IMAGE);
    withUserName(ACCESS_KEY);
    withPassword(SECRET_KEY);
    withCreateContainerCmdModifier(cmd -> cmd.withHostName("minio"));
  }
}
