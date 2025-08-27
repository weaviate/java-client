package io.weaviate.client6.v1.internal.oidc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record OidcConfig(
    String clientId,
    String providerMetadata,
    Set<String> scopes) {

  public OidcConfig(String clientId, String providerMetadata, Set<String> scopes) {
    this.clientId = clientId;
    this.providerMetadata = providerMetadata;
    this.scopes = scopes != null ? Set.copyOf(scopes) : Collections.emptySet();
  }

  public OidcConfig(String clientId, String providerMetadata, List<String> scopes) {
    this(clientId, providerMetadata, scopes == null ? null : new HashSet<>(scopes));
  }

  /** Create a new OIDC config with extended scopes. */
  public OidcConfig withScopes(String... scopes) {
    return withScopes(Arrays.asList(scopes));
  }

  /** Create a new OIDC config with extended scopes. */
  public OidcConfig withScopes(List<String> scopes) {
    var newScopes = Stream.concat(this.scopes.stream(), scopes.stream()).collect(Collectors.toSet());
    return new OidcConfig(clientId, providerMetadata, newScopes);
  }
}
