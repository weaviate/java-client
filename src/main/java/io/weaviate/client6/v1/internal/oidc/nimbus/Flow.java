package io.weaviate.client6.v1.internal.oidc.nimbus;

import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

@FunctionalInterface
interface Flow {
  AuthorizationGrant getAuthorizationGrant();

  default ClientAuthentication getClientAuthentication() {
    return null;
  }

  static Flow refreshToken(Token t) {
    return new RefreshTokenFlow(t);
  }

  static Flow resourceOwnerPassword(String username, String password) {
    final var grant = new ResourceOwnerPasswordCredentialsGrant(username, new Secret(password));
    return () -> grant; // Reuse cached authorization grant
  }

  static Flow clientCredentials(String clientId, String clientSecret) {
    return new Flow() {
      private static final AuthorizationGrant GRANT = new ClientCredentialsGrant();

      @Override
      public AuthorizationGrant getAuthorizationGrant() {
        return GRANT;
      }

      @Override
      public ClientAuthentication getClientAuthentication() {
        return new ClientSecretPost(new ClientID(clientId), new Secret(clientSecret));
      }
    };
  }
}
