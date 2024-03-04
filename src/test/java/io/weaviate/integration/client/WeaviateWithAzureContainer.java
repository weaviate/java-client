package io.weaviate.integration.client;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.weaviate.WeaviateContainer;

public class WeaviateWithAzureContainer extends WeaviateContainer {

  public WeaviateWithAzureContainer(String dockerImageName) {
    super(dockerImageName);

    waitingFor(Wait.forHttp("/v1/.well-known/openid-configuration").forPort(8080).forStatusCode(200));
    withEnv("AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED", "false");
    withEnv("AUTHENTICATION_OIDC_ENABLED", "true");
    withEnv("AUTHENTICATION_OIDC_CLIENT_ID", "4706508f-30c2-469b-8b12-ad272b3de864");
    withEnv("AUTHENTICATION_OIDC_ISSUER", "https://login.microsoftonline.com/36c47fb4-f57c-4e1c-8760-d42293932cc2/v2.0");
    withEnv("AUTHENTICATION_OIDC_USERNAME_CLAIM", "oid");
    withEnv("AUTHENTICATION_OIDC_GROUPS_CLAIM", "groups");
    withEnv("AUTHORIZATION_ADMINLIST_ENABLED", "true");
    withEnv("AUTHORIZATION_ADMINLIST_USERS", "b6bf8e1d-d398-4e5d-8f1b-50fda9146a64");
    withEnv("AUTHENTICATION_OIDC_SCOPES", "");
    withEnv("DISABLE_TELEMETRY", "true");
  }
}
