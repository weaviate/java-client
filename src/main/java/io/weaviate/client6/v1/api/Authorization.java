package io.weaviate.client6.v1.api;

import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.oidc.OidcConfig;
import io.weaviate.client6.v1.internal.oidc.OidcUtils;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public interface Authorization {
  TokenProvider getTokenProvider(RestTransport transport);

  public static Authorization apiKey(String apiKey) {
    return __ -> TokenProvider.staticToken(apiKey);
  }

  public static Authorization bearerToken(String accessToken, String refreshToken, long expiresIn) {
    return transport -> {
      OidcConfig oidc = OidcUtils.getConfig(transport);
      return TokenProvider.bearerToken(oidc, accessToken, refreshToken, expiresIn);
    };
  }
}
