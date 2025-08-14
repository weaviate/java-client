package io.weaviate.client6.v1.internal.oidc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.WeaviateOAuthException;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.ExternalEndpoint;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public final class OidcUtils {
  /** Prevents public initialization. */
  private OidcUtils() {
  }

  private static final String OPENID_CONFIGURATION_URL = "/.well-known/openid-configuration";

  private static final Endpoint<Void, OpenIdConfiguration> GET_OPENID_ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> OPENID_CONFIGURATION_URL,
      request -> Collections.emptyMap(),
      OpenIdConfiguration.class);

  private static final Endpoint<String, String> GET_PROVIDER_METADATA_ENDPOINT = new ExternalEndpoint<>(
      request -> "GET",
      request -> request, // URL is the request body.
      requesf -> Collections.emptyMap(),
      request -> null,
      (__, response) -> response);

  private static record OpenIdConfiguration(
      @SerializedName("clientId") String clientId,
      @SerializedName("scopes") List<String> scopes,
      @SerializedName("href") String endpoint) {
  }

  /** Fetch cluster's OIDC config. */
  public static final OidcConfig getConfig(RestTransport transport) {
    OpenIdConfiguration openid;
    try {
      openid = transport.performRequest(null, GET_OPENID_ENDPOINT);
    } catch (IOException e) {
      throw new WeaviateOAuthException("fetch OpenID configuration", e);
    }

    String providerMetadata;
    try {
      providerMetadata = transport.performRequest(openid.endpoint(), GET_PROVIDER_METADATA_ENDPOINT);
    } catch (IOException e) {
      throw new WeaviateOAuthException("fetch provider metadata", e);
    }
    return new OidcConfig(openid.clientId(), providerMetadata, openid.scopes());
  }
}
