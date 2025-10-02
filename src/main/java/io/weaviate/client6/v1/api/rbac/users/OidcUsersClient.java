package io.weaviate.client6.v1.api.rbac.users;

import io.weaviate.client6.v1.internal.rest.RestTransport;

public class OidcUsersClient extends NamespacedUsersClient {

  public OidcUsersClient(RestTransport restTransport) {
    super(restTransport, UserType.OIDC);
  }
}
