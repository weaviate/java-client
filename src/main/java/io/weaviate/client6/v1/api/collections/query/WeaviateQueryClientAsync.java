package io.weaviate.client6.v1.api.collections.query;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.XWriteWeaviateObject;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClientAsync<PropertiesT>
    extends
    AbstractQueryClient<PropertiesT, CompletableFuture<Optional<XWriteWeaviateObject<PropertiesT>>>, CompletableFuture<QueryResponse<PropertiesT>>, CompletableFuture<QueryResponseGrouped<PropertiesT>>> {

  public WeaviateQueryClientAsync(
      CollectionDescriptor<PropertiesT> collection,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    super(collection, grpcTransport, defaults);
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateQueryClientAsync(WeaviateQueryClientAsync<PropertiesT> qc, CollectionHandleDefaults defaults) {
    super(qc, defaults);
  }

  @Override
  protected CompletableFuture<Optional<XWriteWeaviateObject<PropertiesT>>> fetchObjectById(
      FetchObjectById byId) {
    var request = new QueryRequest(byId, null);
    var result = this.grpcTransport.performRequestAsync(request, QueryRequest.rpc(collection, defaults));
    return result.thenApply(this::optionalFirst);
  }

  @Override
  protected final CompletableFuture<QueryResponse<PropertiesT>> performRequest(QueryOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.grpcTransport.performRequestAsync(request, QueryRequest.rpc(collection, defaults));
  }

  @Override
  protected final CompletableFuture<QueryResponseGrouped<PropertiesT>> performRequest(QueryOperator operator,
      GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.grpcTransport.performRequestAsync(request, QueryRequest.grouped(collection, defaults));
  }
}
