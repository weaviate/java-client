package io.weaviate.client6.v1.collections.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.codec.grpc.v1.AggregateMarshaler;
import io.weaviate.client6.v1.query.NearVector;

public class WeaviateAggregate {
  private final String collectionName;
  private final GrpcClient grpcClient;

  public WeaviateAggregate(String collectionName, GrpcClient grpc) {
    this.collectionName = collectionName;
    this.grpcClient = grpc;
  }

  public AggregateResponse overAll(Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var req = new AggregateMarshaler().marshal(aggregation);
    var reply = grpcClient.grpc.aggregate(req);

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
    var result = new AggregateResponse(properties, totalCount);
    return result;
  }

  public AggregateGroupByResponse overAll(Consumer<AggregateGroupByRequest.GroupBy.Builder> groupByOptions,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var groupBy = AggregateGroupByRequest.GroupBy.with(groupByOptions);

    var req = new AggregateMarshaler().marshal(new AggregateGroupByRequest(aggregation, groupBy));
    var reply = grpcClient.grpc.aggregate(req);

    List<Group<?>> groups = new ArrayList<>();
    if (reply.hasGroupedResults()) {
      for (var result : reply.getGroupedResults().getGroupsList()) {
        final Long totalCount = result.hasObjectsCount() ? result.getObjectsCount() : null;

        GroupedBy<?> groupedBy = null;
        var gb = result.getGroupedBy();
        if (gb.hasInt()) {
          groupedBy = new GroupedBy<Long>(gb.getPathList().get(0), gb.getInt());
        } else if (gb.hasText()) {
          groupedBy = new GroupedBy<String>(gb.getPathList().get(0), gb.getText());
        } else {
          assert false : "branch not covered";
        }

        Map<String, Metric.Values> properties = new HashMap<>();
        for (var agg : result.getAggregations().getAggregationsList()) {
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
        Group<?> group = new Group<>(groupedBy, properties, totalCount);
        groups.add(group);

      }
    }
    return new AggregateGroupByResponse(groups);
  }

  public AggregateResponse nearVector(Float[] vector, Consumer<NearVector.Builder> nearVectorOptions,
      Consumer<AggregateRequest.Builder> options) {
    var aggregation = AggregateRequest.with(collectionName, options);
    var nearVector = NearVector.with(vector, nearVectorOptions);

    var req = new AggregateMarshaler().marshal(nearVector, aggregation);
    var reply = grpcClient.grpc.aggregate(req);

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
    var result = new AggregateResponse(properties, totalCount);
    return result;
  }
}
