package io.weaviate.client.v1.auth;

import io.weaviate.client.v1.auth.exception.AuthException;
import java.util.List;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;

public interface Authentication {
  WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException;
  WeaviateClient getAuthClient(Config config) throws AuthException;
}
