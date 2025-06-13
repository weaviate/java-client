package io.weaviate.client6.v1.api;

import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TokenProvider.Token;

public class Authorization {
  public static TokenProvider apiKey(String apiKey) {
    return TokenProvider.staticToken(new Token(apiKey));
  }
}
