package io.weaviate.client.base.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.grpc.base.BaseGrpcClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateGrpc;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GrpcClient extends BaseGrpcClient {
  WeaviateGrpc.WeaviateBlockingStub client;
  ManagedChannel channel;

  private GrpcClient(WeaviateGrpc.WeaviateBlockingStub client, ManagedChannel channel) {
    this.client = client;
    this.channel = channel;
  }

  public WeaviateProtoBatch.BatchObjectsReply batchObjects(WeaviateProtoBatch.BatchObjectsRequest request) {
    return this.client.batchObjects(request);
  }

  public WeaviateProtoSearchGet.SearchReply search(WeaviateProtoSearchGet.SearchRequest request) {
    return this.client.search(request);
  }

  public void shutdown() {
    this.channel.shutdown();
  }

  public static GrpcClient create(Config config, AccessTokenProvider tokenProvider) {
    Metadata headers = getHeaders(config, tokenProvider);
    ManagedChannel channel = buildChannel(config);
    WeaviateGrpc.WeaviateBlockingStub blockingStub = WeaviateGrpc.newBlockingStub(channel);
    WeaviateGrpc.WeaviateBlockingStub client = blockingStub
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    return new GrpcClient(client, channel);
  }
}
