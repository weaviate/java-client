package io.weaviate.client6.v1.internal.oidc;

import java.util.Collections;
import java.util.List;

public record OidcConfig(
    String clientId,
    String providerMetadata,
    List<String> scopes) {

  public OidcConfig(String clientId, String providerMetadata, List<String> scopes) {
    this.clientId = clientId;
    this.providerMetadata = providerMetadata;
    this.scopes = scopes != null ? scopes : Collections.emptyList();
  }
}
