package io.weaviate.client.v1.grpc.query;

import java.util.Map;

import io.weaviate.client.Config;
import io.weaviate.client.base.grpc.GrpcClient;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.experimental.SearchClient;
import io.weaviate.client.v1.experimental.SearchResult;

public class Raw {
  private final AccessTokenProvider tokenProvider;
  private final Config config;
  private SearchRequest search;

  public Raw(HttpClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    this.config = config;
    this.tokenProvider = tokenProvider;
  }

  public Raw withSearch(SearchRequest search) {
    this.search = search;
    return this;
  }

  public SearchResult<Map<String, Object>> run() {
    GrpcClient grpcClient = GrpcClient.create(this.config, this.tokenProvider);
    try {
      SearchReply reply = grpcClient.search(this.search);
      return SearchClient.deserializeUntyped(reply);
    } finally {
      grpcClient.shutdown();
    }
  }
}
