package io.weaviate.client6.v1.internal.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.core5.http.message.BasicHeader;

import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TransportOptions;

public final class RestTransportOptions extends TransportOptions<Collection<BasicHeader>> {
  private static final String API_VERSION = "v1";

  public RestTransportOptions(String scheme, String host, int port, Map<String, String> headers,
      TokenProvider tokenProvider, TrustManagerFactory trust, Timeout timeout) {
    super(scheme, host, port, buildHeaders(headers), tokenProvider, trust, timeout);
  }

  private RestTransportOptions(String scheme, String host, int port, Collection<BasicHeader> headers,
      TokenProvider tokenProvider, TrustManagerFactory trust, Timeout timeout) {
    super(scheme, host, port, headers, tokenProvider, trust, timeout);
  }

  public final RestTransportOptions withTimeout(Timeout timeout) {
    return new RestTransportOptions(scheme, host, port, headers, tokenProvider, trustManagerFactory, timeout);
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
