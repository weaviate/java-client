package io.weaviate.client6.v1.internal.grpc;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface GrpcTransport extends Closeable {
  <RequestT, RequestM, ReplyM, ResponseT> ResponseT performRequest(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc);

  <RequestT, RequestM, ReplyM, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc);
}
