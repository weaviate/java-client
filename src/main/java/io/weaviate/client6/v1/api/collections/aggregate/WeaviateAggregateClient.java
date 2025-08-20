package io.weaviate.client6.v1.api.collections.aggregate;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateAggregateClient extends AbstractAggregateClient<AggregateResponse, AggregateResponseGrouped> {

  public WeaviateAggregateClient(
      CollectionDescriptor<?> collection,
      GrpcTransport transport,
      CollectionHandleDefaults defaults) {
    super(collection, transport, defaults);
  }

  protected final AggregateResponse performRequest(Aggregation aggregation) {
    var request = new AggregateRequest(aggregation, null);
    return this.transport.performRequest(request, AggregateRequest.rpc(collection));
  }

  protected final AggregateResponseGrouped performRequest(Aggregation aggregation, GroupBy groupBy) {
    var request = new AggregateRequest(aggregation, groupBy);
    return this.transport.performRequest(request, AggregateRequest.grouped(collection));
  }
}
