package io.weaviate.client6.v1.internal.grpc;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.util.concurrent.ListenableFuture;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;

public class SimpleRpc<RequestT, RequestM, ResponseT, ReplyM> implements Rpc<RequestT, RequestM, ResponseT, ReplyM> {

  private final boolean isInsert;

  private final Function<RequestT, RequestM> marshal;
  private final Function<ReplyM, ResponseT> unmarshal;
  private final Supplier<BiFunction<WeaviateBlockingStub, RequestM, ReplyM>> method;
  private final Supplier<BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>>> methodAsync;

  SimpleRpc(Function<RequestT, RequestM> marshal, Function<ReplyM, ResponseT> unmarshal,
      Supplier<BiFunction<WeaviateBlockingStub, RequestM, ReplyM>> method,
      Supplier<BiFunction<WeaviateFutureStub, RequestM, ListenableFuture<ReplyM>>> methodAsync,
      boolean isInsert) {
    this.marshal = marshal;
    this.unmarshal = unmarshal;
    this.method = method;
    this.methodAsync = methodAsync;
    this.isInsert = isInsert;
  }

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

  @Override
  public boolean isInsert() {
    return isInsert;
  }
}
