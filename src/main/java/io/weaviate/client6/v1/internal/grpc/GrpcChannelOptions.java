package io.weaviate.client6.v1.internal.grpc;

import java.util.Map;

import javax.net.ssl.TrustManagerFactory;

import io.grpc.Metadata;
import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TransportOptions;

public class GrpcChannelOptions extends TransportOptions<Metadata> {
  private final Integer maxMessageSize;

  public GrpcChannelOptions(String scheme, String host, int port, Map<String, String> headers,
      TokenProvider tokenProvider, TrustManagerFactory tmf, Timeout timeout) {
    this(scheme, host, port, buildMetadata(headers), tokenProvider, tmf, null, timeout);
  }

  private GrpcChannelOptions(String scheme, String host, int port, Metadata headers,
      TokenProvider tokenProvider, TrustManagerFactory tmf, Integer maxMessageSize, Timeout timeout) {
    super(scheme, host, port, headers, tokenProvider, tmf, timeout);
    this.maxMessageSize = maxMessageSize;
  }

  public GrpcChannelOptions withMaxMessageSize(int maxMessageSize) {
    return new GrpcChannelOptions(scheme, host, port, headers, tokenProvider, trustManagerFactory, maxMessageSize,
        timeout);
  }

  public Integer maxMessageSize() {
    return maxMessageSize;
  }

  private static final Metadata buildMetadata(Map<String, String> headers) {
    var metadata = new Metadata();
    for (var header : headers.entrySet()) {
      metadata.put(
          Metadata.Key.of(header.getKey(), Metadata.ASCII_STRING_MARSHALLER),
          header.getValue());
    }
    return metadata;
  }
}
