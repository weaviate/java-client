package io.weaviate.client.v1.grpc.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.grpc.GrpcClient;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class Raw extends BaseClient<List<Map<String, Object>>> implements ClientResult<List<Map<String, Object>>> {
  private final AccessTokenProvider tokenProvider;
  private SearchRequest search;

  public Raw(HttpClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config);
    this.tokenProvider = tokenProvider;
  }

  public Raw withSearch(SearchRequest search) {
    this.search = search;
    return this;
  }

  @Override
  public Result<List<Map<String, Object>>> run() {
    GrpcClient grpcClient = GrpcClient.create(this.config, this.tokenProvider);
    try {
      SearchReply reply = grpcClient.search(this.search);
      List<Map<String, Object>> result = reply.getResultsList().stream()
          .map(list -> list.getAllFields().entrySet().stream()
              .collect(Collectors.toMap(
                  e -> e.getKey().getJsonName(),
                  e -> e.getValue())))
          .toList();
      return new Result<>(HttpStatus.SC_SUCCESS, result, WeaviateErrorResponse.builder().build());
    } finally {
      grpcClient.shutdown();
    }
  }
}
