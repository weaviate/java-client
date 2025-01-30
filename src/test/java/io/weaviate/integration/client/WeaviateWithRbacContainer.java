package io.weaviate.integration.client;

import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateWithRbacContainer extends WeaviateContainer {

  public WeaviateWithRbacContainer(String dockerImageName, String admin, String... viewers) {
    super(dockerImageName);

    withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
    withEnv("AUTHENTICATION_APIKEY_ENABLED", "true");
    withEnv("AUTHORIZATION_RBAC_ENABLED", "true");
    withEnv("AUTHENTICATION_APIKEY_ALLOWED_KEYS", makeSecret(admin));
    withEnv("AUTHENTICATION_APIKEY_USERS", admin);
    withEnv("AUTHORIZATION_ADMIN_USERS", admin);
    withEnv("PERSISTENCE_DATA_PATH", "./data");
    withEnv("BACKUP_FILESYSTEM_PATH", "/tmp/backups");
    withEnv("ENABLE_MODULES", "backup-filesystem");
    withEnv("CLUSTER_GOSSIP_BIND_PORT", "7100");
    withEnv("CLUSTER_DATA_BIND_PORT", "7101");

    if (viewers.length > 0) {
      withEnv("AUTHORIZATION_VIEWER_USERS", String.join(",", viewers));
    }
  }

  /**
   * Generate API secret for a username. When running an instance with
   * authentication enabled, {@link WeaviateWithRbacContainer} will use this
   * method to generate secrets for all users.
   * Use this method to get a valid API key for a test client.
   */
  public static String makeSecret(String user) {
    return user + "-secret";
  }
}
