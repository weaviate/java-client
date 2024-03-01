package io.weaviate.integration.client;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateWithOktaCcContainer extends WeaviateContainer {

  public WeaviateWithOktaCcContainer(String dockerImageName) {
    super(dockerImageName);

    waitingFor(Wait.forHttp("/v1/.well-known/openid-configuration").forPort(8080).forStatusCode(200));
    withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
    withEnv("AUTHENTICATION_OIDC_ENABLED", "true");
    withEnv("AUTHENTICATION_OIDC_CLIENT_ID", "0oa7e9ipdkVZRUcxo5d7");
    withEnv("AUTHENTICATION_OIDC_ISSUER", "https://dev-32300990.okta.com/oauth2/aus7e9kxbwYQB0eht5d7");
    withEnv("AUTHENTICATION_OIDC_USERNAME_CLAIM", "cid");
    withEnv("AUTHENTICATION_OIDC_GROUPS_CLAIM", "groups");
    withEnv("AUTHORIZATION_ADMINLIST_ENABLED", "true");
    withEnv("AUTHORIZATION_ADMINLIST_USERS", "0oa7e9ipdkVZRUcxo5d7");
    withEnv("AUTHENTICATION_OIDC_SCOPES", "");
    withEnv("DISABLE_TELEMETRY", "true");
  }
}
