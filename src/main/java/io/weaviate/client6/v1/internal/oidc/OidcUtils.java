package io.weaviate.client6.v1.internal.oidc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.ExternalEndpoint;
import io.weaviate.client6.v1.internal.rest.RestTransport;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public final class OidcUtils {
  private OidcUtils() {
  }

  private static final String OPENID_URL = "/.well-known/openid-configuration";
  private static final Endpoint<Void, OpenIdConfiguration> GET_OPENID = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/.well-known/openid-configuration",
      request -> Collections.emptyMap(),
      OpenIdConfiguration.class);

  private static final Endpoint<String, String> GET_PROVIDER_METADATA = new ExternalEndpoint<>(
      request -> "GET",
      request -> request, // URL is the request body.
      requesf -> Collections.emptyMap(),
      request -> null,
      (statusCode, response) -> JSON.deserialize(response, String.class));

  private static record OpenIdConfiguration(
      @SerializedName("clientId") String clientId,
      @SerializedName("scopes") List<String> scopes,
      @SerializedName("href") String endpoint) {
  }

  public static final OidcConfig getConfig(RestTransport transport) {
    OpenIdConfiguration openid;
    try {
      openid = transport.performRequest(null, GET_OPENID);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String providerMetadata;
    try {
      providerMetadata = transport.performRequest(openid.endpoint(), GET_PROVIDER_METADATA);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var redirectUrl = transport.getTransportOptions().baseUrl() + OPENID_URL;
    return new OidcConfig(openid.clientId(), redirectUrl, providerMetadata, openid.scopes());
  }
}
