package io.weaviate.client.base.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client.Config;
import io.weaviate.client.grpc.protocol.v1.WeaviateGrpc;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import java.util.Map;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GrpcClient {
  WeaviateGrpc.WeaviateBlockingStub client;
  ManagedChannel channel;

  private GrpcClient(WeaviateGrpc.WeaviateBlockingStub client, ManagedChannel channel) {
    this.client = client;
    this.channel = channel;
  }

  public WeaviateProtoBatch.BatchObjectsReply batchObjects(WeaviateProtoBatch.BatchObjectsRequest request) {
    return this.client.batchObjects(request);
  }

  public void shutdown() {
    this.channel.shutdown();
  }

  public static GrpcClient create(Config config, AccessTokenProvider tokenProvider) {
    Metadata headers = new Metadata();
    if (config.getHeaders() != null) {
      for (Map.Entry<String, String> e : config.getHeaders().entrySet()) {
        headers.put(Metadata.Key.of(e.getKey(), Metadata.ASCII_STRING_MARSHALLER), e.getValue());
      }
    }
    if (tokenProvider != null) {
      headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), String.format("Bearer %s", tokenProvider.getAccessToken()));
    }
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(getAddress(config));
    if (config.isGRPCSecured()) {
      channelBuilder = channelBuilder.useTransportSecurity();
    } else {
      channelBuilder.usePlaintext();
    }
    ManagedChannel channel = channelBuilder.build();
    WeaviateGrpc.WeaviateBlockingStub blockingStub = WeaviateGrpc.newBlockingStub(channel);
    WeaviateGrpc.WeaviateBlockingStub client = blockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    return new GrpcClient(client, channel);
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
