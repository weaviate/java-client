package io.weaviate.client6.v1.internal.oidc;

import io.weaviate.client6.v1.internal.Proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public record OidcConfig(
    String clientId,
    String providerMetadata,
    Set<String> scopes,
    OidcProxy proxy) {

  public record OidcProxy(
      String scheme,
      String host,
      int port) {
    
    public OidcProxy(Proxy proxy) {
      this(requireNonNull(proxy, "proxy is null").scheme(), proxy.host(), proxy.port());
    }

  }

  public OidcConfig(String clientId, String providerMetadata, Set<String> scopes, OidcProxy proxy) {
    this.clientId = clientId;
    this.providerMetadata = providerMetadata;
    this.scopes = scopes != null ? Set.copyOf(scopes) : Collections.emptySet();
    this.proxy = proxy;
  }

  public OidcConfig(String clientId, String providerMetadata, Set<String> scopes) {
    this(clientId, providerMetadata, scopes, null);
  }

  public OidcConfig(String clientId, String providerMetadata, List<String> scopes) {
    this(clientId, providerMetadata, scopes == null ? null : new HashSet<>(scopes), null);
  }

  public OidcConfig(String clientId, String providerMetadata, List<String> scopes, OidcProxy proxy) {
    this(clientId, providerMetadata, scopes == null ? null : new HashSet<>(scopes), proxy);
  }

  /** Create a new OIDC config with extended scopes. */
  public OidcConfig withScopes(String... scopes) {
    return withScopes(Arrays.asList(scopes));
  }

  /** Create a new OIDC config with extended scopes. */
  public OidcConfig withScopes(List<String> scopes) {
    var newScopes = Stream.concat(this.scopes.stream(), scopes.stream()).collect(Collectors.toSet());
    return new OidcConfig(clientId, providerMetadata, newScopes, proxy);
  }
}
