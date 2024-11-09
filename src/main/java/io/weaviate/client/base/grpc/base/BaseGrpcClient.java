package io.weaviate.client.base.grpc.base;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.weaviate.client.Config;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import java.util.Map;

public class BaseGrpcClient {

  protected static Metadata getHeaders(Config config, AccessTokenProvider tokenProvider) {
    Metadata headers = new Metadata();
    if (config.getHeaders() != null) {
      for (Map.Entry<String, String> e : config.getHeaders().entrySet()) {
        headers.put(Metadata.Key.of(e.getKey(), Metadata.ASCII_STRING_MARSHALLER), e.getValue());
      }
    }
    if (tokenProvider != null) {
      headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), String.format("Bearer %s", tokenProvider.getAccessToken()));
    }
    return headers;
  }

  protected static ManagedChannel buildChannel(Config config) {
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(getAddress(config));
    if (config.isGRPCSecured()) {
      channelBuilder = channelBuilder.useTransportSecurity();
    } else {
      channelBuilder.usePlaintext();
    }
    return channelBuilder.build();
  }

  private static String getAddress(Config config) {
    if (config.getGRPCHost() != null) {
      String host = config.getGRPCHost();
      if (host.contains(":")) {
        return host;
      }
      if (config.isGRPCSecured()) {
        return String.format("%s:443", host);
      }
      return String.format("%s:80", host);
    }
    return "";
  }
}
