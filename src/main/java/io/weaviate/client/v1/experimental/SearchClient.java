package io.weaviate.client.v1.experimental;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.grpc.GrpcClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.experimental.NearVector.Options;

public class SearchClient {
  private final AccessTokenProvider tokenProvider;
  private final Config config;
  private final String collection;

  public Result<List<Map<String, Object>>> nearVector(float[] vector) {
    return nearVector(vector, nop -> {
    });
  }

  public Result<List<Map<String, Object>>> nearVector(float[] vector, Consumer<Options> options) {
    NearVector operator = new NearVector(vector, options);
    SearchRequest.Builder req = SearchRequest.newBuilder();
    req.setCollection(collection);
    req.setUses123Api(true);
    req.setUses125Api(true);
    req.setUses127Api(true);
    operator.append(req);
    return search(req.build());
  }

  private Result<List<Map<String, Object>>> search(SearchRequest req) {
    GrpcClient grpc = GrpcClient.create(config, tokenProvider);
    try {
      SearchReply reply = grpc.search(req);
      return new Result<>(HttpStatus.SC_SUCCESS, deserialize(reply), WeaviateErrorResponse.builder().build());
    } finally {
      grpc.shutdown();
    }
  }

  private List<Map<String, Object>> deserialize(SearchReply reply) {
    return reply.getResultsList().stream()
        .map(list -> list.getAllFields().entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().getJsonName(),
                e -> e.getValue())))
        .toList();

  }

  SearchClient(Config config, AccessTokenProvider tokenProvider, String collection) {
    this.config = config;
    this.tokenProvider = tokenProvider;
    this.collection = collection;
  }
}
