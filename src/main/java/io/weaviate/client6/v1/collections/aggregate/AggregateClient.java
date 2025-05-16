package io.weaviate.client6.v1.collections.aggregate;

import java.util.function.Consumer;
import java.util.function.Function;

import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.codec.grpc.v1.AggregateMarshaler;
import io.weaviate.client6.internal.codec.grpc.v1.AggregateUnmarshaler;
import io.weaviate.client6.v1.api.collections.query.NearVector;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class AggregateClient {
  private final String collectionName;
  private final GrpcClient grpcClient;

  public AggregateClient(String collectionName, GrpcClient grpc) {
    this.collectionName = collectionName;
    this.grpcClient = grpc;
  }

  public AggregateResponse overAll(Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var req = new AggregateMarshaler(aggregation.collectionName())
        .addAggregation(aggregation)
        .marshal();
    var reply = grpcClient.grpc.aggregate(req);
    return new AggregateUnmarshaler(reply).single();
  }

  public AggregateGroupByResponse overAll(
      AggregateGroupByRequest.GroupBy groupBy,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);

    var req = new AggregateMarshaler(aggregation.collectionName())
        .addAggregation(aggregation)
        .addGroupBy(groupBy)
        .marshal();
    var reply = grpcClient.grpc.aggregate(req);
    return new AggregateUnmarshaler(reply).grouped();
  }

  public AggregateResponse nearVector(
      Float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nearVectorOptions,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var nearVector = NearVector.of(vector, nearVectorOptions);

    var req = new AggregateMarshaler(aggregation.collectionName())
        .addAggregation(aggregation)
        .addNearVector(nearVector)
        .marshal();

    var reply = grpcClient.grpc.aggregate(req);
    return new AggregateUnmarshaler(reply).single();
  }

  public AggregateGroupByResponse nearVector(
      Float[] vector,
      Function<NearVector.Builder, ObjectBuilder<NearVector>> nearVectorOptions,
      AggregateGroupByRequest.GroupBy groupBy,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var nearVector = NearVector.of(vector, nearVectorOptions);

    var req = new AggregateMarshaler(aggregation.collectionName())
        .addAggregation(aggregation)
        .addGroupBy(groupBy)
        .addNearVector(nearVector)
        .marshal();
    var reply = grpcClient.grpc.aggregate(req);
    return new AggregateUnmarshaler(reply).grouped();
  }

  public AggregateGroupByResponse nearVector(
      Float[] vector,
      AggregateGroupByRequest.GroupBy groupBy,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var nearVector = NearVector.of(vector);

    var req = new AggregateMarshaler(aggregation.collectionName())
        .addAggregation(aggregation)
        .addGroupBy(groupBy)
        .addNearVector(nearVector)
        .marshal();
    var reply = grpcClient.grpc.aggregate(req);
    return new AggregateUnmarshaler(reply).grouped();
  }
}
