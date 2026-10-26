package io.weaviate.containers;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

public class MinIo extends MinIOContainer {
  private static final String DOCKER_IMAGE = "cgr.dev/chainguard/minio";
  public static final String ACCESS_KEY = "minioadmin";
  public static final String SECRET_KEY = "minioadmin";

  public static final String HOST_NAME = "minio";
  public static final String URL = HOST_NAME + ":9000";

  static MinIo createDefault() {
    return new MinIo();
  }

  private MinIo() {
    super(DockerImageName.parse(DOCKER_IMAGE)
        .asCompatibleSubstituteFor("minio/minio"));
    withUserName(ACCESS_KEY);
    withPassword(SECRET_KEY);
    withCreateContainerCmdModifier(cmd -> cmd.withHostName("minio"));
  }
}
