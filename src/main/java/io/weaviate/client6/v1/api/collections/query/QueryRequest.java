package io.weaviate.client6.v1.api.collections.query;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.internal.grpc.Rpc;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateGrpc.WeaviateFutureStub;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoProperties;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.orm.PropertiesBuilder;

public record QueryRequest(SearchOperator operator, GroupBy groupBy) {

  static <T> Rpc<QueryRequest, WeaviateProtoSearchGet.SearchRequest, QueryResponse<T>, WeaviateProtoSearchGet.SearchReply> rpc(
      CollectionDescriptor<T> collection) {
    return Rpc.of(
        request -> {
          var message = WeaviateProtoSearchGet.SearchRequest.newBuilder();
          message.setUses127Api(true);
          message.setUses125Api(true);
          message.setUses123Api(true);
          message.setCollection(collection.name());
          request.operator.appendTo(message);
          if (request.groupBy != null) {
            request.groupBy.appendTo(message);
          }
          return message.build();
        },
        reply -> {
          List<QueryObject<T>> objects = reply.getResultsList().stream()
              .map(obj -> QueryRequest.unmarshalResultObject(obj, collection))
              .toList();
          return new QueryResponse<>(objects);
        },
        () -> WeaviateBlockingStub::search,
        () -> WeaviateFutureStub::search);
  }

  static <T> Rpc<QueryRequest, WeaviateProtoSearchGet.SearchRequest, QueryResponseGrouped<T>, WeaviateProtoSearchGet.SearchReply> grouped(
      CollectionDescriptor<T> collection) {
    var rpc = rpc(collection);
    return Rpc.of(request -> rpc.marshal(request), reply -> {
      var allObjects = new ArrayList<QueryObjectGrouped<T>>();
      var groups = reply.getGroupByResultsList()
          .stream().map(group -> {
            var name = group.getName();
            List<QueryObjectGrouped<T>> objects = group.getObjectsList().stream()
                .map(obj -> QueryRequest.unmarshalResultObject(obj, collection))
                .map(obj -> new QueryObjectGrouped<>(obj, name))
                .toList();

            allObjects.addAll(objects);
            return new QueryResponseGroup<>(
                name,
                group.getMinDistance(),
                group.getMaxDistance(),
                group.getNumberOfObjects(),
                objects);
          }).collect(Collectors.toMap(QueryResponseGroup::name, Function.identity()));

      return new QueryResponseGrouped<T>(allObjects, groups);
    }, () -> rpc.method(), () -> rpc.methodAsync());
  }

  private static <T> QueryObject<T> unmarshalResultObject(WeaviateProtoSearchGet.SearchResult object,
      CollectionDescriptor<T> descriptor) {
    var properties = descriptor.propertiesBuilder();
    object.getProperties().getNonRefProps().getFieldsMap()
        .entrySet().stream().forEach(entry -> setProperty(entry.getKey(), entry.getValue(), properties));

    var queryMetadata = object.getMetadata();
    var metadata = new QueryObject.Metadata.Builder()
        .id(queryMetadata.getId())
        .distance(queryMetadata.getDistance())
        .certainty(queryMetadata.getCertainty());

    var vectors = new Vectors.Builder();
    for (final var vector : queryMetadata.getVectorsList()) {
      var vectorName = vector.getName();
      switch (vector.getType()) {
        case VECTOR_TYPE_SINGLE_FP32:
          vectors.vector(vectorName, GRPC.fromByteString(vector.getVectorBytes()));
          break;
        case VECTOR_TYPE_MULTI_FP32:
          vectors.vector(vectorName, GRPC.fromByteStringMulti(vector.getVectorBytes()));
          break;
        default:
          continue;
      }
    }
    metadata.vectors(vectors.build());

    return new QueryObject<>(properties.build(), metadata.build());
  }

  private static <T> void setProperty(String property, WeaviateProtoProperties.Value value,
      PropertiesBuilder<T> builder) {
    if (value.hasTextValue()) {
      builder.setText(property, value.getTextValue());
    } else if (value.hasBoolValue()) {
      builder.setBoolean(property, value.getBoolValue());
    } else if (value.hasIntValue()) {
      builder.setInteger(property, value.getIntValue());
    } else if (value.hasNumberValue()) {
      builder.setNumber(property, value.getNumberValue());
    } else if (value.hasDateValue()) {
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(value.getDateValue());
      builder.setDate(property, Date.from(offsetDateTime.toInstant()));
    } else {
      assert false : "branch not covered";
    }
  }
}
