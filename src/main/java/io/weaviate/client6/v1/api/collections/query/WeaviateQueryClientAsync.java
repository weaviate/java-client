package io.weaviate.client6.v1.api.collections.query;

import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;

public class WeaviateQueryClientAsync<T>
    extends AbstractQueryClient<CompletableFuture<QueryResponse<T>>, CompletableFuture<QueryResponseGrouped<T>>> {

  public WeaviateQueryClientAsync(String collectionName, GrpcChannelOptions options) {
    super(collectionName, options);
  }

  protected final CompletableFuture<QueryResponse<T>> performRequest(SearchOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequestAsync(request, QueryRequest.rpc(collectionName));
  }

  protected final CompletableFuture<QueryResponseGrouped<T>> performRequest(SearchOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequestAsync(request, QueryRequest.grouped(collectionName));
  }
}
