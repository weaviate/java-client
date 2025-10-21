package io.weaviate.client6.v1.api.collections.generate;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateGenerateClient<PropertiesT>
    extends
    AbstractGenerateClient<PropertiesT, GenerativeResponse<PropertiesT>, GenerativeResponseGrouped<PropertiesT>> {

  public WeaviateGenerateClient(
      CollectionDescriptor<PropertiesT> collection,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    super(collection, grpcTransport, defaults);
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateGenerateClient(WeaviateGenerateClient<PropertiesT> c, CollectionHandleDefaults defaults) {
    super(c, defaults);
  }

  @Override
  protected final GenerativeResponse<PropertiesT> performRequest(QueryOperator operator, GenerativeTask generate) {
    var request = new GenerativeRequest(operator, generate, null);
    return this.grpcTransport.performRequest(request, GenerativeRequest.rpc(collection, defaults));
  }

  @Override
  protected final GenerativeResponseGrouped<PropertiesT> performRequest(QueryOperator operator, GenerativeTask generate,
      GroupBy groupBy) {
    var request = new GenerativeRequest(operator, generate, groupBy);
    return this.grpcTransport.performRequest(request, GenerativeRequest.grouped(collection, defaults));
  }
}
