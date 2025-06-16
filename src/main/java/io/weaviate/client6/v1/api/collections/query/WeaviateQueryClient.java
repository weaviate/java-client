package io.weaviate.client6.v1.api.collections.query;

import java.util.Optional;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClient<T>
    extends
    AbstractQueryClient<T, Optional<WeaviateObject<T, Object, QueryMetadata>>, QueryResponse<T>, QueryResponseGrouped<T>> {

  public WeaviateQueryClient(CollectionDescriptor<T> collection, GrpcTransport transport) {
    super(collection, transport);
  }

  @Override
  protected Optional<WeaviateObject<T, Object, QueryMetadata>> byId(ById byId) {
    var request = new QueryRequest(byId, null);
    var result = this.transport.performRequest(request, QueryRequest.rpc(collection));
    return optionalFirst(result.objects());

  }

  @Override
  protected final QueryResponse<T> performRequest(QueryOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequest(request, QueryRequest.rpc(collection));
  }

  @Override
  protected final QueryResponseGrouped<T> performRequest(QueryOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequest(request, QueryRequest.grouped(collection));
  }

}
