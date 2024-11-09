package io.weaviate.client.base.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.grpc.base.BaseGrpcClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateGrpc;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBatch;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AsyncGrpcClient extends BaseGrpcClient {
  WeaviateGrpc.WeaviateFutureStub client;
  ManagedChannel channel;

  private AsyncGrpcClient(WeaviateGrpc.WeaviateFutureStub client, ManagedChannel channel) {
    this.client = client;
    this.channel = channel;
  }

  public ListenableFuture<WeaviateProtoBatch.BatchObjectsReply> batchObjects(WeaviateProtoBatch.BatchObjectsRequest request) {
    return this.client.batchObjects(request);
  }

  public void shutdown() {
    this.channel.shutdown();
  }

  public static AsyncGrpcClient create(Config config, AccessTokenProvider tokenProvider) {
    Metadata headers = getHeaders(config, tokenProvider);
    ManagedChannel channel = buildChannel(config);
    WeaviateGrpc.WeaviateFutureStub stub = WeaviateGrpc.newFutureStub(channel);
    WeaviateGrpc.WeaviateFutureStub client = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    return new AsyncGrpcClient(client, channel);
  }
}
