package io.weaviate.client6.v1.collections.aggregate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.weaviate.client6.internal.GrpcClient;

public class WeaviateAggregate {
  private final String collectionName;
  private final GrpcClient grpcClient;

  public WeaviateAggregate(String collectionName, GrpcClient grpc) {
    this.collectionName = collectionName;
    this.grpcClient = grpc;
  }

  public AggregateResult overAll(Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var req = io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.newBuilder();
    req.setCollection(collectionName);
    aggregation.appendTo(req);
    var reply = grpcClient.grpc.aggregate(req.build());

    Long totalCount = null;
    Map<String, Metric.Values> properties = new HashMap<>();

    if (reply.hasSingleResult()) {
      var single = reply.getSingleResult();
      totalCount = single.hasObjectsCount() ? single.getObjectsCount() : null;
      var aggregations = single.getAggregations().getAggregationsList();
      for (var agg : aggregations) {
        var property = agg.getProperty();
        Metric.Values value = null;

        if (agg.hasInt()) {
          var metrics = agg.getInt();
          value = new IntegerMetric.Values(
              metrics.hasCount() ? metrics.getCount() : null,
              metrics.hasMinimum() ? metrics.getMinimum() : null,
              metrics.hasMaximum() ? metrics.getMaximum() : null,
              metrics.hasMean() ? metrics.getMean() : null,
              metrics.hasMedian() ? metrics.getMedian() : null,
              metrics.hasMode() ? metrics.getMode() : null,
              metrics.hasSum() ? metrics.getSum() : null);
        } else {
          assert false : "branch not covered";
        }

        if (value != null) {
          properties.put(property, value);
        }
      }
    }
    var result = new AggregateResult(properties, totalCount);
    return result;
  }

  public AggregateGroupByResult overAll(Consumer<AggregateRequest.GroupBy.Builder> groupByOptions,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options, groupByOptions);
    var req = io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.newBuilder();
    req.setCollection(collectionName);
    aggregation.appendTo(req);
    var reply = grpcClient.grpc.aggregate(req.build());
    return null;
  }
}
