package io.weaviate.client6.v1.api.collections.query;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClientAsync<T>
    extends
    AbstractQueryClient<T, CompletableFuture<Optional<WeaviateObject<T, Object, QueryMetadata>>>, CompletableFuture<QueryResponse<T>>, CompletableFuture<QueryResponseGrouped<T>>> {

  public WeaviateQueryClientAsync(
      CollectionDescriptor<T> collection,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    super(collection, grpcTransport, defaults);
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateQueryClientAsync(WeaviateQueryClientAsync<T> qc, CollectionHandleDefaults defaults) {
    super(qc, defaults);
  }

  @Override
  protected CompletableFuture<Optional<WeaviateObject<T, Object, QueryMetadata>>> byId(
      ById byId) {
    var request = new QueryRequest(byId, null);
    var result = this.grpcTransport.performRequestAsync(request, QueryRequest.rpc(collection, defaults));
    return result.thenApply(r -> optionalFirst(r.objects()));
  }

  @Override
  protected final CompletableFuture<QueryResponse<T>> performRequest(QueryOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.grpcTransport.performRequestAsync(request, QueryRequest.rpc(collection, defaults));
  }

  @Override
  protected final CompletableFuture<QueryResponseGrouped<T>> performRequest(QueryOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.grpcTransport.performRequestAsync(request, QueryRequest.grouped(collection, defaults));
  }

}
