package io.weaviate.client6.v1.internal.grpc;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.weaviate.client6.v1.internal.TokenProvider;

class TokenCallCredentials extends CallCredentials {
  private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("Authorization",
      Metadata.ASCII_STRING_MARSHALLER);

  private final TokenProvider tokenProvider;

  TokenCallCredentials(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
    executor.execute(() -> {
      try {
        var headers = new Metadata();
        var token = tokenProvider.getToken().accessToken();
        headers.put(AUTHORIZATION, "Bearer " + token);
        metadataApplier.apply(headers);
      } catch (Exception e) {
        metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
      }
    });
  }
}
