package io.weaviate.client6.v1.collections.query;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoProperties.Value;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataResult;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.codec.grpc.v1.SearchMarshaler;

public class QueryClient<T> {
  // TODO: this should be wrapped around in some TypeInspector etc.
  private final String collectionName;

  // TODO: implement Closeable and call grpc.shutdown() on exit
  // (probably on a "higher" level);
  private final GrpcClient grpcClient;

  public QueryClient(String collectionName, GrpcClient grpc) {
    this.grpcClient = grpc;
    this.collectionName = collectionName;
  }

  public QueryResult<T> nearVector(Float[] vector) {
    var query = NearVector.with(vector, opt -> {
    });
    var req = new SearchMarshaler(collectionName).addNearVector(query);
    return search(req.marshal());
  }

  public QueryResult<T> nearVector(Float[] vector, Consumer<NearVector.Builder> options) {
    var query = NearVector.with(vector, options);
    var req = new SearchMarshaler(collectionName).addNearVector(query);
    return search(req.marshal());
  }

  public GroupedQueryResult<T> nearVector(Float[] vector, NearVector.GroupBy groupBy,
      Consumer<NearVector.Builder> options) {
    var query = NearVector.with(vector, options);
    var req = new SearchMarshaler(collectionName).addNearVector(query)
        .addGroupBy(groupBy);
    return searchGrouped(req.marshal());
  }

  public GroupedQueryResult<T> nearVector(Float[] vector, NearVector.GroupBy groupBy) {
    var query = NearVector.with(vector, opt -> {
    });
    var req = new SearchMarshaler(collectionName).addNearVector(query)
        .addGroupBy(groupBy);
    return searchGrouped(req.marshal());
  }

  public QueryResult<T> nearText(String text, Consumer<NearText.Builder> fn) {
    var query = NearText.with(text, fn);
    var req = new SearchMarshaler(collectionName).addNearText(query);
    return search(req.marshal());
  }

  public QueryResult<T> nearText(String text) {
    var query = NearText.with(text, opt -> {
    });
    var req = new SearchMarshaler(collectionName).addNearText(query);
    return search(req.marshal());
  }

  private QueryResult<T> search(SearchRequest req) {
    var reply = grpcClient.grpc.search(req);
    return deserializeUntyped(reply);
  }

  private GroupedQueryResult<T> searchGrouped(SearchRequest req) {
    var reply = grpcClient.grpc.search(req);
    return deserializeUntypedGrouped(reply);
  }

  public QueryResult<T> deserializeUntyped(SearchReply reply) {
    List<QueryResult.SearchObject<T>> objects = reply.getResultsList().stream()
        .map(res -> {
          Map<String, Object> properties = convertProtoMap(res.getProperties().getNonRefProps().getFieldsMap());

          MetadataResult meta = res.getMetadata();
          var metadata = new QueryResult.SearchObject.QueryMetadata(
              meta.getId(),
              meta.getDistancePresent() ? meta.getDistance() : null,
              GRPC.fromByteString(meta.getVectorBytes()));
          // FIXME: rather than doing this unchecked cast, we should deal
          // with the ORM and "untyped map" cases explicitly.
          return new QueryResult.SearchObject<T>((T) properties, metadata);
        }).toList();

    return new QueryResult<T>(objects);
  }

  public GroupedQueryResult<T> deserializeUntypedGrouped(SearchReply reply) {
    var allObjects = new ArrayList<GroupedQueryResult.WithGroupSearchObject<T>>();
    Map<String, GroupedQueryResult.Group<T>> allGroups = reply.getGroupByResultsList()
        .stream().map(g -> {
          var groupName = g.getName();
          var groupObjects = g.getObjectsList().stream().map(res -> {
            Map<String, Object> properties = convertProtoMap(res.getProperties().getNonRefProps().getFieldsMap());

            MetadataResult meta = res.getMetadata();
            var metadata = new QueryResult.SearchObject.QueryMetadata(
                meta.getId(),
                meta.getDistancePresent() ? meta.getDistance() : null,
                GRPC.fromByteString(meta.getVectorBytes()));
            var obj = new GroupedQueryResult.WithGroupSearchObject<T>(groupName, (T) properties, metadata);

            allObjects.add(obj);

            return obj;
          }).toList();

          return new GroupedQueryResult.Group<>(
              groupName,
              g.getMinDistance(),
              g.getMaxDistance(),
              g.getNumberOfObjects(),
              groupObjects);
        }).collect(Collectors.toMap(GroupedQueryResult.Group::name, g -> g));
    return new GroupedQueryResult<>(allObjects, allGroups);
  }

  /**
   * Convert Map<String, Value> to Map<String,Object> such that can be
   * (de-)serialized by {@link Gson}.
   */
  private static Map<String, Object> convertProtoMap(Map<String, Value> map) {
    return map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey, e -> convertProtoValue(e.getValue())));
  }

  /**
   * Convert protobuf's Value stub to an Object by extracting the first available
   * field. The checks are non-exhaustive and only cover text, boolean, and
   * integer values.
   */
  private static Object convertProtoValue(Value value) {
    if (value.hasTextValue()) {
      return value.getTextValue();
    } else if (value.hasBoolValue()) {
      return value.getBoolValue();
    } else if (value.hasIntValue()) {
      return value.getIntValue();
    } else if (value.hasNumberValue()) {
      return value.getNumberValue();
    } else if (value.hasDateValue()) {
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(value.getDateValue());
      return Date.from(offsetDateTime.toInstant());
    } else {
      assert false : "branch not covered";
    }
    return null;
  }
}
