package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record AggregateRequest(Aggregation aggregation, GroupBy groupBy) {

  static <T> Rpc<AggregateRequest, WeaviateProtoAggregate.AggregateRequest, AggregateResponse, WeaviateProtoAggregate.AggregateReply> rpc(
      CollectionDescriptor<T> collection,
      CollectionHandleDefaults defaults) {
    return Rpc.of(
        request -> {
          var message = WeaviateProtoAggregate.AggregateRequest.newBuilder();
          message.setCollection(collection.name());
          request.aggregation.appendTo(message);
          if (request.groupBy != null) {
            request.groupBy.appendTo(message, collection.name());
          }
          if (defaults.tenant() != null) {
            message.setTenant(defaults.tenant());
          }
          return message.build();
        },
        reply -> {
          Long totalCount = null;
          Map<String, Object> properties = new HashMap<>();

          // FIXME: check if group by was requested!
          if (reply.hasSingleResult()) {
            var single = reply.getSingleResult();
            totalCount = single.hasObjectsCount() ? single.getObjectsCount() : null;
            properties = unmarshalAggregation(single.getAggregations());
          }

          var result = new AggregateResponse(properties, totalCount);
          return result;
        },
        () -> WeaviateBlockingStub::aggregate,
        () -> WeaviateFutureStub::aggregate);
  }

  static <T> Rpc<AggregateRequest, WeaviateProtoAggregate.AggregateRequest, AggregateResponseGrouped, WeaviateProtoAggregate.AggregateReply> grouped(
      CollectionDescriptor<T> collection,
      CollectionHandleDefaults defaults) {
    var rpc = rpc(collection, defaults);
    return Rpc.of(request -> rpc.marshal(request), reply -> {
      var groups = new ArrayList<AggregateResponseGroup<?>>();
      if (reply.hasGroupedResults()) {
        for (final var result : reply.getGroupedResults().getGroupsList()) {

          Long totalCount = result.hasObjectsCount() ? result.getObjectsCount() : null;
          GroupedBy<?> groupedBy = null;
          var groupBy = result.getGroupedBy();
          var property = groupBy.getPathList().get(0);

          if (groupBy.hasInt()) {
            groupedBy = new GroupedBy<Long>(property, groupBy.getInt());
          } else if (groupBy.hasText()) {
            groupedBy = new GroupedBy<String>(property, groupBy.getText());
          } else {
            assert false : "(aggregate) branch not covered";
          }

          var properties = unmarshalAggregation(result.getAggregations());
          var group = new AggregateResponseGroup<>(groupedBy, properties, totalCount);
          groups.add(group);

        }
      }
      return new AggregateResponseGrouped(groups);
    }, () -> rpc.method(), () -> rpc.methodAsync());
  }

  private static Map<String, Object> unmarshalAggregation(WeaviateProtoAggregate.AggregateReply.Aggregations result) {
    var properties = new HashMap<String, Object>();

    for (var aggregation : result.getAggregationsList()) {
      var property = aggregation.getProperty();
      Object value = null;

      if (aggregation.hasInt()) {
        var metric = aggregation.getInt();
        value = new IntegerAggregation.Values(
            metric.hasCount() ? metric.getCount() : null,
            metric.hasMinimum() ? metric.getMinimum() : null,
            metric.hasMaximum() ? metric.getMaximum() : null,
            metric.hasMean() ? metric.getMean() : null,
            metric.hasMedian() ? metric.getMedian() : null,
            metric.hasMode() ? metric.getMode() : null,
            metric.hasSum() ? metric.getSum() : null);
      } else if (aggregation.hasText()) {
        var metric = aggregation.getText();
        var topOccurrences = metric.hasTopOccurences()
            ? metric.getTopOccurences().getItemsList()
                .stream().map(
                    top -> new TextAggregation.TopOccurrence(top.getValue(), top.getOccurs()))
                .toList()
            : null;
        value = new TextAggregation.Values(
            metric.hasCount() ? metric.getCount() : null,
            topOccurrences);

      } else {
        assert false : "branch not covered";
      }

      if (value != null) {
        properties.put(property, value);
      }
    }
    return properties;
  }
}
