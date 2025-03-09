package io.weaviate.client6.v1.query;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.weaviate.client6.Config;
import io.weaviate.client6.grpc.protocol.v1.WeaviateGrpc;
import io.weaviate.client6.grpc.protocol.v1.WeaviateGrpc.WeaviateBlockingStub;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoProperties.Value;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataResult;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;

public class Query<T> {
  // TODO: inject singleton as dependency
  private static final Gson gson = new Gson();

  // TODO: this should be wrapped around in some TypeInspector etc.
  private final String collectionName;

  // TODO: hide befind an internal HttpClient
  private final Config config;

  // TODO: implement Closeable and call grpc.shutdown() on exit
  // (probably on a "higher" level);
  private WeaviateBlockingStub grpc;

  public Query(String collectionName, Config config) {
    this.config = config;
    this.collectionName = collectionName;

    // TODO: add request headers (config.headers + authorization)
    this.grpc = WeaviateGrpc.newBlockingStub(buildChannel(config))
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(new io.grpc.Metadata()));
  }

  public QueryResult<T> nearVector(Float[] vector, Consumer<NearVector.Options> options) {
    var query = new NearVector(vector, options);

    // TODO: Since we always need to set these values, we migth want to move the
    // next block to some factory method.
    var req = SearchRequest.newBuilder();
    req.setCollection(collectionName);
    req.setUses123Api(true);
    req.setUses125Api(true);
    req.setUses127Api(true);

    query.appendTo(req);
    return search(req.build());
  }

  private QueryResult<T> search(SearchRequest req) {
    var reply = grpc.search(req);
    return deserializeUntyped(reply);
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

  private static ManagedChannel buildChannel(Config config) {
    ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forTarget(config.grpcAddress());
    channelBuilder.usePlaintext();
    return channelBuilder.build();
  }
}
