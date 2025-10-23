package io.weaviate.client6.v1.api.collections.generate;

import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.query.GroupBy;
import io.weaviate.client6.v1.api.collections.query.QueryOperator;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateGenerateClientAsync<PropertiesT>
    extends
    AbstractGenerateClient<PropertiesT, CompletableFuture<GenerativeResponse<PropertiesT>>, CompletableFuture<GenerativeResponseGrouped<PropertiesT>>> {

  public WeaviateGenerateClientAsync(
      CollectionDescriptor<PropertiesT> collection,
      GrpcTransport grpcTransport,
      CollectionHandleDefaults defaults) {
    super(collection, grpcTransport, defaults);
  }

  /** Copy constructor that sets new defaults. */
  public WeaviateGenerateClientAsync(WeaviateGenerateClientAsync<PropertiesT> c, CollectionHandleDefaults defaults) {
    super(c, defaults);
  }

  @Override
  protected final CompletableFuture<GenerativeResponse<PropertiesT>> performRequest(QueryOperator operator,
      GenerativeTask generate) {
    var request = new GenerativeRequest(operator, generate, null);
    return this.grpcTransport.performRequestAsync(request, GenerativeRequest.rpc(collection, defaults));
  }

  @Override
  protected final CompletableFuture<GenerativeResponseGrouped<PropertiesT>> performRequest(QueryOperator operator,
      GenerativeTask generate,
      GroupBy groupBy) {
    var request = new GenerativeRequest(operator, generate, groupBy);
    return this.grpcTransport.performRequestAsync(request, GenerativeRequest.grouped(collection, defaults));
  }
}
