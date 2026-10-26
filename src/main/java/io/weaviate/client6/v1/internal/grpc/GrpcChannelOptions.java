package io.weaviate.client6.v1.internal.grpc;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

import javax.net.ssl.TrustManagerFactory;

import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.weaviate.client6.v1.internal.Proxy;
import io.weaviate.client6.v1.internal.Timeout;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TransportOptions;

public class GrpcChannelOptions extends TransportOptions<Metadata> {
  private final OptionalInt maxMessageSize;
  private final Function<NettyChannelBuilder, NettyChannelBuilder> channelBuilder;

  public GrpcChannelOptions(
      String scheme,
      String host,
      int port,
      Map<String, String> headers,
      TokenProvider tokenProvider,
      TrustManagerFactory tmf,
      Timeout timeout,
      Proxy proxy,
      Function<NettyChannelBuilder, NettyChannelBuilder> channelBuilder) {
    this(
        scheme,
        host,
        port,
        buildMetadata(headers),
        tokenProvider,
        tmf,
        OptionalInt.empty(),
        timeout,
        proxy,
        channelBuilder);
  }

  private GrpcChannelOptions(String scheme,
      String host,
      int port,
      Metadata headers,
      TokenProvider tokenProvider,
      TrustManagerFactory tmf,
      OptionalInt maxMessageSize,
      Timeout timeout,
      Proxy proxy,
      Function<NettyChannelBuilder, NettyChannelBuilder> channelBuilder) {
    super(
        scheme,
        host,
        port,
        headers,
        tokenProvider,
        tmf,
        timeout,
        proxy);
    this.maxMessageSize = maxMessageSize;
    this.channelBuilder = channelBuilder;
  }

  public GrpcChannelOptions withMaxMessageSize(int maxMessageSize) {
    return new GrpcChannelOptions(scheme,
        host,
        port,
        headers,
        tokenProvider,
        trustManagerFactory,
        OptionalInt.of(maxMessageSize),
        timeout,
        proxy,
        channelBuilder);
  }

  public OptionalInt maxMessageSize() {
    return maxMessageSize;
  }

  public Function<NettyChannelBuilder, NettyChannelBuilder> channelBuilder() {
    return channelBuilder;
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
