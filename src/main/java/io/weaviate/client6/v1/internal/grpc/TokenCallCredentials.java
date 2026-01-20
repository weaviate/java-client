package io.weaviate.client6.v1.internal.grpc;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.weaviate.client6.v1.internal.AsyncTokenProvider;
import io.weaviate.client6.v1.internal.TokenProvider;

class TokenCallCredentials extends CallCredentials implements AutoCloseable {
  private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("Authorization",
      Metadata.ASCII_STRING_MARSHALLER);

  /**
   * Since {@link #applyRequestMetadata} accepts an {@link Executor} anyways,
   * we can always just use an async provider, instead of creating 2 separate
   * instances for it.
   */
  private final AsyncTokenProvider tokenProviderAsync;

  TokenCallCredentials(TokenProvider tokenProvider) {
    this.tokenProviderAsync = AsyncTokenProvider.wrap(tokenProvider);
  }

  @Override
  public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
    tokenProviderAsync.getToken(executor)
        .whenComplete((tok, ex) -> {
          if (ex != null) {
            metadataApplier.fail(Status.UNAUTHENTICATED.withCause(ex));
            return;
          }
          var headers = new Metadata();
          headers.put(AUTHORIZATION, "Bearer " + tok.accessToken());
          metadataApplier.apply(headers);
        });
  }

  @Override
  public void close() throws Exception {
    tokenProviderAsync.close();
  }
}
