package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;

public class WeaviateQueryClient<T> extends AbstractQueryClient<QueryResponse<T>, QueryResponseGrouped<T>> {

  public WeaviateQueryClient(String collectionName, GrpcChannelOptions options) {
    super(collectionName, options);
  }

  protected final QueryResponse<T> performRequest(SearchOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequest(request, QueryRequest.rpc(collectionName));
  }

  protected final QueryResponseGrouped<T> performRequest(SearchOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequest(request, QueryRequest.grouped(collectionName));
  }
}
