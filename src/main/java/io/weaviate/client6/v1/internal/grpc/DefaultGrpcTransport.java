package io.weaviate.client6.v1.internal.grpc;

import static java.util.Objects.requireNonNull;

import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamReply;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBatch.BatchStreamRequest;

public final class DefaultGrpcTransport implements GrpcTransport {
  /**
   * ListenableFuture callbacks are executed
   * in the same thread they are called from.
   */
  private static final Executor FUTURE_CALLBACK_EXECUTOR = Runnable::run;

  private final GrpcChannelOptions transportOptions;
  private final ManagedChannel channel;

  private final WeaviateBlockingStub blockingStub;
  private final WeaviateFutureStub futureStub;

  private TokenCallCredentials callCredentials;

  public DefaultGrpcTransport(GrpcChannelOptions transportOptions) {
    requireNonNull(transportOptions, "transportOptions is null");

    this.transportOptions = transportOptions;
    if (transportOptions.tokenProvider() != null) {
      this.callCredentials = new TokenCallCredentials(transportOptions.tokenProvider());
    }

    this.channel = buildChannel(transportOptions);
    this.blockingStub = configure(WeaviateGrpc.newBlockingStub(channel));
    this.futureStub = configure(WeaviateGrpc.newFutureStub(channel));
  }

  private <StubT extends AbstractStub<StubT>> StubT applyTimeout(StubT stub, Rpc<?, ?, ?, ?> rpc) {
    if (transportOptions.timeout() == null) {
      return stub;
    }
    int timeout = rpc.isInsert()
        ? transportOptions.timeout().insertSeconds()
        : transportOptions.timeout().querySeconds();
    return stub.withDeadlineAfter(timeout, TimeUnit.SECONDS);
  }

  @Override
  public OptionalInt maxMessageSizeBytes() {
    return transportOptions.maxMessageSize();
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> ResponseT performRequest(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    var message = rpc.marshal(request);
    var method = rpc.method();
    var stub = applyTimeout(blockingStub, rpc);
    try {
      var reply = method.apply(stub, message);
      return rpc.unmarshal(reply);
    } catch (io.grpc.StatusRuntimeException e) {
      throw WeaviateApiException.gRPC(e);
    }
  }

  @Override
  public <RequestT, RequestM, ReplyM, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Rpc<RequestT, RequestM, ResponseT, ReplyM> rpc) {
    var message = rpc.marshal(request);
    var method = rpc.methodAsync();
    var stub = applyTimeout(futureStub, rpc);
    var reply = method.apply(stub, message);
    return toCompletableFuture(reply).thenApply(r -> rpc.unmarshal(r));
  }

  /**
   * Convets {@link ListenableFuture} to {@link CompletableFuture}
   * reusing the thread in which the original future is completed.
   */
  private static final <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenable) {
    requireNonNull(listenable, "listenable is null");

    CompletableFuture<T> completable = new CompletableFuture<>();
    Futures.addCallback(listenable, new FutureCallback<T>() {

      @Override
      public void onSuccess(T result) {
        completable.complete(result);
      }

      @Override
      public void onFailure(Throwable t) {
        if (t instanceof StatusRuntimeException e) {
          completable.completeExceptionally(WeaviateApiException.gRPC(e));
          return;
        }
        completable.completeExceptionally(t);
      }

    }, FUTURE_CALLBACK_EXECUTOR);
    return completable;
  }

  private static ManagedChannel buildChannel(GrpcChannelOptions transportOptions) {
    requireNonNull(transportOptions, "transportOptions is null");

    NettyChannelBuilder channel = NettyChannelBuilder.forAddress(transportOptions.host(), transportOptions.port());
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
  public StreamObserver<BatchStreamRequest> createStream(StreamObserver<BatchStreamReply> recv) {
    return configure(WeaviateGrpc.newStub(channel)).batchStream(recv);
  }

  /** Apply common configuration to a stub. */
  private <S extends AbstractStub<S>> S configure(S stub) {
    requireNonNull(stub, "stub is null");

    stub = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(transportOptions.headers()));
    if (transportOptions.maxMessageSize().isPresent()) {
      int max = transportOptions.maxMessageSize().getAsInt();
      stub = stub.withMaxInboundMessageSize(max).withMaxOutboundMessageSize(max);
    }
    if (callCredentials != null) {
      stub = stub.withCallCredentials(callCredentials);
    }
    return stub;
  }

  @Override
  public void close() throws Exception {
    channel.shutdown();
    if (callCredentials != null) {
      callCredentials.close();
    }
  }
}
