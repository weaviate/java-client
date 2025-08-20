package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.concurrent.CompletableFuture;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.GrpcTransport;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public class WeaviateAggregateClientAsync
    extends AbstractAggregateClient<CompletableFuture<AggregateResponse>, CompletableFuture<AggregateResponseGrouped>> {

  public WeaviateAggregateClientAsync(
      CollectionDescriptor<?> collection,
      GrpcTransport transport,
      CollectionHandleDefaults defaults) {
    super(collection, transport, defaults);
  }

  protected final CompletableFuture<AggregateResponse> performRequest(Aggregation aggregation) {
    var request = new AggregateRequest(aggregation, null);
    return this.transport.performRequestAsync(request, AggregateRequest.rpc(collection));
  }

  protected final CompletableFuture<AggregateResponseGrouped> performRequest(Aggregation aggregation, GroupBy groupBy) {
    var request = new AggregateRequest(aggregation, groupBy);
    return this.transport.performRequestAsync(request, AggregateRequest.grouped(collection));
  }
}
