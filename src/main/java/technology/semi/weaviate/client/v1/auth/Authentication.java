package technology.semi.weaviate.client.v1.auth;

import java.util.List;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.auth.exception.AuthException;

public interface Authentication {
  WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException;
  WeaviateClient getAuthClient(Config config) throws AuthException;
}
