package io.weaviate.client6.v1.api.collections.query;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClientAsync<T>
    extends
    AbstractQueryClient<T, CompletableFuture<Optional<WeaviateObject<T, Object, QueryMetadata>>>, CompletableFuture<QueryResponse<T>>, CompletableFuture<QueryResponseGrouped<T>>> {

  public WeaviateQueryClientAsync(CollectionDescriptor<T> collection, GrpcTransport transport) {
    super(collection, transport);
  }

  @Override
  protected CompletableFuture<Optional<WeaviateObject<T, Object, QueryMetadata>>> byId(
      ById byId) {
    var request = new QueryRequest(byId, null);
    var result = this.transport.performRequestAsync(request, QueryRequest.rpc(collection));
    return result.thenApply(r -> optionalFirst(r.objects()));
  }

  @Override
  protected final CompletableFuture<QueryResponse<T>> performRequest(QueryOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequestAsync(request, QueryRequest.rpc(collection));
  }

  @Override
  protected final CompletableFuture<QueryResponseGrouped<T>> performRequest(QueryOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequestAsync(request, QueryRequest.grouped(collection));
  }

}
