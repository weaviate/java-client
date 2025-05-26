package io.weaviate.client6.v1.internal.grpc;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;

public final class DefaultGrpcTransport implements GrpcTransport {
  private final ManagedChannel channel;

  private final WeaviateBlockingStub blockingStub;
  private final WeaviateFutureStub futureStub;

  private static final int HTTP_PORT = 80;
  private static final int HTTPS_PORT = 443;

  public DefaultGrpcTransport(GrpcChannelOptions channelOptions) {
    this.channel = buildChannel(channelOptions);
    this.blockingStub = WeaviateGrpc.newBlockingStub(channel);
    this.futureStub = WeaviateGrpc.newFutureStub(channel);
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> ResponseT performRequest(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    var message = rpc.marshal(request);
    var method = rpc.method();
    var reply = method.apply(blockingStub, message);
    return rpc.unmarshal(reply);
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    var message = rpc.marshal(request);
    var method = rpc.methodAsync();
    var reply = method.apply(futureStub, message);
    return toCompletableFuture(reply).thenApply(r -> rpc.unmarshal(r));
  }

  /**
   * Convets {@link ListenableFuture} to {@link CompletableFuture}
   * reusing the thread in which the original future is completed.
   */
  private static final <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenable) {
    var completable = new CompletableFuture<T>();
    Futures.addCallback(listenable, new FutureCallback<T>() {

      @Override
      public void onSuccess(T result) {
        completable.complete(result);
      }

      @Override
      public void onFailure(Throwable t) {
        completable.completeExceptionally(t);
      }

    }, Runnable::run);
    return completable;
  }

  private static ManagedChannel buildChannel(GrpcChannelOptions options) {
    // var port = options.useTls() ? HTTPS_PORT : HTTP_PORT;
    // var channel = ManagedChannelBuilder.forAddress(options.host(), port);
    var channel = ManagedChannelBuilder.forTarget(options.host());

    if (options.useTls()) {
      channel.useTransportSecurity();
    } else {
      channel.usePlaintext();
    }

    var headers = new Metadata();
    for (final var header : options.headers().entrySet()) {
      var key = Metadata.Key.of(header.getKey(), Metadata.ASCII_STRING_MARSHALLER);
      headers.put(key, header.getValue());

    }
    channel.intercept(MetadataUtils.newAttachHeadersInterceptor(headers));
    return channel.build();
  }

  @Override
  public void close() throws IOException {
    this.channel.shutdown();
  }
}
