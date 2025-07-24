package io.weaviate.client6.v1.internal.grpc;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.handler.ssl.SslContext;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;

public final class DefaultGrpcTransport implements GrpcTransport {
  private final ManagedChannel channel;

  private final WeaviateBlockingStub blockingStub;
  private final WeaviateFutureStub futureStub;

  public DefaultGrpcTransport(GrpcChannelOptions transportOptions) {
    this.channel = buildChannel(transportOptions);

    var blockingStub = WeaviateGrpc.newBlockingStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(transportOptions.headers()));

    var futureStub = WeaviateGrpc.newFutureStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(transportOptions.headers()));

    if (transportOptions.tokenProvider() != null) {
      var credentials = new TokenCallCredentials(transportOptions.tokenProvider());
      blockingStub = blockingStub.withCallCredentials(credentials);
      futureStub = futureStub.withCallCredentials(credentials);
    }

    this.blockingStub = blockingStub;
    this.futureStub = futureStub;
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

  private static ManagedChannel buildChannel(GrpcChannelOptions transportOptions) {
    var channel = NettyChannelBuilder.forAddress(transportOptions.host(), transportOptions.port());

    if (transportOptions.isSecure()) {
      channel.useTransportSecurity();
    } else {
      channel.usePlaintext();
    }

    if (transportOptions.trustManagerFactory() != null) {
      SslContext sslCtx;
      try {
        sslCtx = GrpcSslContexts.forClient()
            .trustManager(transportOptions.trustManagerFactory())
            .build();
      } catch (SSLException e) {
        // todo: rethrow as WeaviateConnectionException
        throw new RuntimeException("create grpc transport", e);
      }
      channel.sslContext(sslCtx);
    }

    channel.intercept(MetadataUtils.newAttachHeadersInterceptor(transportOptions.headers()));

    return channel.build();
  }

  @Override
  public void close() throws IOException {
    this.channel.shutdown();
  }
}
