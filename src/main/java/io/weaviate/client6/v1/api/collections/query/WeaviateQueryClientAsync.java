package io.weaviate.client6.v1.api.collections.query;

import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClientAsync<T>
    extends AbstractQueryClient<T, CompletableFuture<QueryResponse<T>>, CompletableFuture<QueryResponseGrouped<T>>> {

  public WeaviateQueryClientAsync(CollectionDescriptor<T> collection, GrpcTransport transport) {
    super(collection, transport);
  }

  protected final CompletableFuture<QueryResponse<T>> performRequest(SearchOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequestAsync(request, QueryRequest.rpc(collection));
  }

  protected final CompletableFuture<QueryResponseGrouped<T>> performRequest(SearchOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequestAsync(request, QueryRequest.grouped(collection));
  }
}
