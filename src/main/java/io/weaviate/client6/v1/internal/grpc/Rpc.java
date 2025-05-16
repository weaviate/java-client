package io.weaviate.client6.v1.internal.grpc;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.util.concurrent.ListenableFuture;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;

public interface Rpc<RequestT, RequestM, ResponseT, ReplyM> {
  RequestM marshal(RequestT request);

  ResponseT unmarshal(ReplyM reply);

  BiFunction<WeaviateBlockingStub, RequestM, ReplyM> method();

  BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>> methodAsync();

  public static <RequestT, RequestM, ResponseT, ReplyM> Rpc<RequestT, RequestM, ResponseT, ReplyM> of(
      Function<RequestT, RequestM> marshal,
      Function<ReplyM, ResponseT> unmarshal,
      Supplier<BiFunction<WeaviateBlockingStub, RequestM, ReplyM>> method,
      Supplier<BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>>> methodAsync) {
    return new Rpc<RequestT, RequestM, ResponseT, ReplyM>() {

      @Override
      public RequestM marshal(RequestT request) {
        return marshal.apply(request);
      }

      @Override
      public ResponseT unmarshal(ReplyM reply) {
        return unmarshal.apply(reply);
      }

      @Override
      public BiFunction<WeaviateBlockingStub, RequestM, ReplyM> method() {
        return method.get();
      }

      @Override
      public BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>> methodAsync() {
        return methodAsync.get();
      }
    };
  }
}
