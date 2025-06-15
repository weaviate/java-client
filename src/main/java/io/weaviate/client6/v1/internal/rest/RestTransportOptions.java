package io.weaviate.client6.v1.internal.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.hc.core5.http.message.BasicHeader;

import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TransportOptions;

public final class RestTransportOptions extends TransportOptions<Collection<BasicHeader>> {
  private static final String API_VERSION = "v1";

  public RestTransportOptions(String scheme, String host, int port, Map<String, String> headers,
      TokenProvider tokenProvider) {
    super(scheme, host, port, buildHeaders(headers), tokenProvider);
  }

  private static final Collection<BasicHeader> buildHeaders(Map<String, String> headers) {
    var basicHeaders = new HashSet<BasicHeader>();
    for (var header : headers.entrySet()) {
      basicHeaders.add(new BasicHeader(header.getKey(), header.getValue()));
    }
    return basicHeaders;
  }

  public String baseUrl() {
    return scheme() + "://" + host() + ":" + port() + "/" + API_VERSION;
  }
}
