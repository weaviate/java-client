package io.weaviate.client6.internal;

import java.io.Closeable;
import java.io.IOException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client6.Config;
import io.weaviate.client6.grpc.protocol.v1.WeaviateGrpc;
import io.weaviate.client6.grpc.protocol.v1.WeaviateGrpc.WeaviateBlockingStub;

public class GrpcClient implements Closeable {
  private final ManagedChannel channel;
  public final WeaviateBlockingStub grpc;

  public GrpcClient(Config config) {
    this.channel = buildChannel(config);
    this.grpc = buildStub(channel);
  }

  @Override
  public void close() throws IOException {
    channel.shutdown();
  }

  private static ManagedChannel buildChannel(Config config) {
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(config.grpcAddress());
    channelBuilder.usePlaintext();
    return channelBuilder.build();
  }

  private static WeaviateBlockingStub buildStub(ManagedChannel channel) {
    return WeaviateGrpc.newBlockingStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(new io.grpc.Metadata()));
  }
}
