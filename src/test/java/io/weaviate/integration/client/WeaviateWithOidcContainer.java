package io.weaviate.integration.client;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateWithOidcContainer extends WeaviateContainer {

  public WeaviateWithOidcContainer(String dockerImageName) {
    super(dockerImageName);

    waitingFor(Wait.forHttp("/v1/.well-known/openid-configuration").forPort(8080).forStatusCode(200));
    withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
    withEnv("AUTHENTICATION_OIDC_ENABLED", "true");
    withEnv("AUTHENTICATION_OIDC_CLIENT_ID", "wcs");
    withEnv("AUTHENTICATION_OIDC_ISSUER", "https://auth.wcs.api.weaviate.io/auth/realms/SeMI");
    withEnv("AUTHENTICATION_OIDC_USERNAME_CLAIM", "email");
    withEnv("AUTHENTICATION_OIDC_GROUPS_CLAIM", "groups");
    withEnv("AUTHORIZATION_ADMINLIST_ENABLED", "true");
    withEnv("AUTHORIZATION_ADMINLIST_USERS", "oidc-test-user@weaviate.io");
    withEnv("AUTHENTICATION_OIDC_SCOPES", "openid,email");
    withEnv("AUTHENTICATION_APIKEY_ENABLED", "true");
    withEnv("AUTHENTICATION_APIKEY_ALLOWED_KEYS", "my-secret-key");
    withEnv("AUTHENTICATION_APIKEY_USERS", "oidc-test-user@weaviate.io");
    withEnv("DISABLE_TELEMETRY", "true");
  }
}
