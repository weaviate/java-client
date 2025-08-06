package io.weaviate.client6.v1.internal.oidc.nimbus;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

@FunctionalInterface
interface Flow {
  AuthorizationGrant getAuthorizationGrant();

  static Flow bearerToken(Token t) {
    return new BearerTokenFlow(t);
  }
}
