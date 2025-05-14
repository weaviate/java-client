package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClient<T> extends AbstractQueryClient<T, QueryResponse<T>, QueryResponseGrouped<T>> {

  public WeaviateQueryClient(CollectionDescriptor<T> collection, GrpcTransport transport) {
    super(collection, transport);
  }

  protected final QueryResponse<T> performRequest(SearchOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.transport.performRequest(request, QueryRequest.rpc(collection));
  }

  protected final QueryResponseGrouped<T> performRequest(SearchOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.transport.performRequest(request, QueryRequest.grouped(collection));
  }
}
