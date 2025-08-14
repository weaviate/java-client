package io.weaviate.client6.v1.internal.oidc.nimbus;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.auth.Secret;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

@FunctionalInterface
interface Flow {
  AuthorizationGrant getAuthorizationGrant();

  static Flow bearerToken(Token t) {
    return new BearerTokenFlow(t);
  }

  static Flow resourceOwnerPassword(String username, String password) {
    return () -> new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password));
  }
}
