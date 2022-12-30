package technology.semi.weaviate.client.v1.auth;

import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.provider.AuthException;
import technology.semi.weaviate.client.v1.auth.provider.AuthType;
import technology.semi.weaviate.client.v1.auth.provider.NimbusAuth;

public class ResourceOwnerPasswordFlow extends NimbusAuth implements Authentication {
  private final String username;
  private final String password;

  public ResourceOwnerPasswordFlow(String username, String password) {
    super();
    this.username = username;
    this.password = password;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    WeaviateClient authClient = getAuthClient(config, "", username, password, scopes, AuthType.USER_PASSWORD);
    return authClient;
  }
}
