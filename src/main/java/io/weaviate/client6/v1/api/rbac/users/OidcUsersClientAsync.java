package io.weaviate.client6.v1.api.rbac.users;

import io.weaviate.client6.v1.internal.rest.RestTransport;

public class OidcUsersClientAsync extends NamespacedUsersClientAsync {

  public OidcUsersClientAsync(RestTransport restTransport) {
    super(restTransport, UserType.OIDC);
  }
}
