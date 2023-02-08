package technology.semi.weaviate.client.v1.auth;

import java.util.ArrayList;
import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.exception.AuthException;
import technology.semi.weaviate.client.v1.auth.nimbus.AuthType;
import technology.semi.weaviate.client.v1.auth.nimbus.NimbusAuth;
import technology.semi.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class ResourceOwnerPasswordFlow implements Authentication {

  private final NimbusAuth nimbusAuth;
  private final String username;
  private final String password;

  public ResourceOwnerPasswordFlow(String username, String password) {
    this.nimbusAuth = new NimbusAuth();
    this.username = username;
    this.password = password;
  }

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    AccessTokenProvider tokenProvider = nimbusAuth.getAccessTokenProvider(config, "", username, password, addDefaultScopes(scopes), AuthType.USER_PASSWORD);
    return new WeaviateClient(config, tokenProvider);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return getAuthClient(config, null);
  }

  private List<String> addDefaultScopes(List<String> scopes) {
    List<String> withDefaultScopes = new ArrayList<>();
    withDefaultScopes.add("offline_access");
    if (scopes != null) {
      scopes.forEach(withDefaultScopes::add);
    }
    return withDefaultScopes;
  }
}
