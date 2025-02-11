package io.weaviate.client.v1.experimental;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.data.Data;
import lombok.RequiredArgsConstructor;

/** DataClient handles insertions, updates, and deletes, as well as batching. */
@RequiredArgsConstructor
public class DataClient {
  private final Config config;
  private final HttpClient httpClient;
  private final AccessTokenProvider tokenProvider;
  private final DbVersionSupport dbVersion;
  private final GrpcVersionSupport grpcVersion;
  private final Data data;

  public <T> Batcher<T> batch(Class<T> cls) {
    return new Batcher<>(config, httpClient, tokenProvider, dbVersion, grpcVersion, data, cls);
  }
}
