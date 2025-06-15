package io.weaviate.client6.v1.internal.grpc;

import java.util.Map;

import io.grpc.Metadata;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TransportOptions;

public class GrpcChannelOptions extends TransportOptions<Metadata> {

  public GrpcChannelOptions(String host, Map<String, String> headers, TokenProvider tokenProvider) {
    super(host, buildMetadata(headers), tokenProvider);
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
