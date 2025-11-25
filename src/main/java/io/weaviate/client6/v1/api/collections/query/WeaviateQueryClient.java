package io.weaviate.client6.v1.api.collections.query;

import java.util.Optional;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateQueryClient<PropertiesT>
    extends
    AbstractQueryClient<PropertiesT, Optional<WeaviateObject<PropertiesT>>, QueryResponse<PropertiesT>, QueryResponseGrouped<PropertiesT>> {

  public WeaviateQueryClient(
      CollectionDescriptor<PropertiesT> collection,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    super(collection, grpcTransport, defaults);
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateQueryClient(WeaviateQueryClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    super(c, defaults);
  }

  @Override
  protected Optional<WeaviateObject<PropertiesT>> fetchObjectById(FetchObjectById byId) {
    var request = new QueryRequest(byId, null);
    var result = this.grpcTransport.performRequest(request, QueryRequest.rpc(collection, defaults));
    return optionalFirst(result);

  }

  @Override
  protected final QueryResponse<PropertiesT> performRequest(QueryOperator operator) {
    var request = new QueryRequest(operator, null);
    return this.grpcTransport.performRequest(request, QueryRequest.rpc(collection, defaults));
  }

  @Override
  protected final QueryResponseGrouped<PropertiesT> performRequest(QueryOperator operator, GroupBy groupBy) {
    var request = new QueryRequest(operator, groupBy);
    return this.grpcTransport.performRequest(request, QueryRequest.grouped(collection, defaults));
  }
}
