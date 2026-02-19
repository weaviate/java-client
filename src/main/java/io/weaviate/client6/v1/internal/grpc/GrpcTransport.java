package io.weaviate.client6.v1.internal.grpc;

import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch;

public interface GrpcTransport extends AutoCloseable {
  <RequestT, RequestM, ReplyM, ResponseT> ResponseT performRequest(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc);

  <RequestT, RequestM, ReplyM, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc);

  /**
   * Create stream for batch insertion.
   *
   * @apiNote Batch insertion is presently the only operation performed over a
   *          StreamStream connection, which is why we do not parametrize this
   *          method.
   */
  StreamObserver<WeaviateProtoBatch.BatchStreamRequest> createStream(
      StreamObserver<WeaviateProtoBatch.BatchStreamReply> recv);

  String host();

  /**
   * Maximum inbound/outbound message size supported by the underlying channel.
   */
  OptionalInt maxMessageSizeBytes();
}
