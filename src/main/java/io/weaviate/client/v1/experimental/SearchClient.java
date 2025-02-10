package io.weaviate.client.v1.experimental;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.grpc.GrpcClient;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoProperties.Value;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataResult;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchReply;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.experimental.NearVector.Options;
import io.weaviate.client.v1.grpc.GRPC;

public class SearchClient<T> {
  private final AccessTokenProvider tokenProvider;
  private final Config config;
  private final String collection;
  private final Gson gson;

  // We won't be able to get away with doing reflection with the type variable,
  // because it is erased at compilation. Gson works around that by introducing
  // their own TypeToken, from which annonymous subclasses can be created at
  // runtime.
  // Those retain information about generic type:
  // https://github.com/google/gson/blob/528fd3195bad9c6c816e77c96750b3188a514365/gson/src/main/java/com/google/gson/reflect/TypeToken.java#L40-L44
  // Most likely we won't need any such machinery, because users' models will
  // probably be POJOs rathen than List<MyClass<Something>>.
  private final Class<T> cls;

  public Result<List<Map<String, Object>>> nearVectorUntyped(float[] vector) {
    return nearVectorUntyped(vector, nop -> {
    });
  }

  public Result<List<Map<String, Object>>> nearVectorUntyped(float[] vector, Consumer<Options> options) {
    NearVector operator = new NearVector(vector, options);
    SearchRequest.Builder req = SearchRequest.newBuilder();
    req.setCollection(collection);
    req.setUses123Api(true);
    req.setUses125Api(true);
    req.setUses127Api(true);
    operator.append(req);
    return searchUntyped(req.build());
  }

  private Result<List<Map<String, Object>>> searchUntyped(SearchRequest req) {
    GrpcClient grpc = GrpcClient.create(config, tokenProvider);
    try {
      SearchReply reply = grpc.search(req);
      return new Result<>(HttpStatus.SC_SUCCESS, deserializeUntyped(reply), WeaviateErrorResponse.builder().build());
    } finally {
      grpc.shutdown();
    }
  }

  private List<Map<String, Object>> deserializeUntyped(SearchReply reply) {
    return reply.getResultsList().stream()
        .map(list -> list.getAllFields().entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().getJsonName(),
                e -> e.getValue())))
        .toList();
  }

  public SearchResult<T> nearVector(float[] vector) {
    return nearVector(vector, nop -> {
    });
  }

  public SearchResult<T> nearVector(float[] vector, Consumer<Options> options) {
    NearVector operator = new NearVector(vector, options);
    SearchRequest.Builder req = SearchRequest.newBuilder();
    req.setCollection(collection);
    req.setUses123Api(true);
    req.setUses125Api(true);
    req.setUses127Api(true);
    operator.append(req);
    return search(req.build());
  }

  private SearchResult<T> search(SearchRequest req) {
    GrpcClient grpc = GrpcClient.create(config, tokenProvider);
    try {
      SearchReply reply = grpc.search(req);
      return deserialize(reply);
    } finally {
      grpc.shutdown();
    }
  }

  /**
   * deserialize offers a naive ORM implementation. It extracts properties map for
   * each result object and creates an instance of type T from it using
   * {@code Gson} as a reflection-based mapper.
   *
   * <p>
   * This incurrs an overhead of creating an intermediate JSON representation of
   * the property map, which is necessary to use {@link Gson}'s reflection. This
   * will suffice for a POC, but will be replaced by our own reflection module
   * before a productive release.
   */
  private SearchResult<T> deserialize(SearchReply reply) {
    List<SearchResult.SearchObject<T>> objects = reply.getResultsList().stream()
        .map(res -> {
          Map<String, Object> propertiesMap = convertProtoMap(res.getProperties().getNonRefProps().getFieldsMap());
          JsonElement el = gson.toJsonTree(propertiesMap);
          T properties = gson.fromJson(el, cls);

          MetadataResult meta = res.getMetadata();
          SearchResult.SearchObject.SearchMetadata metadata = new SearchResult.SearchObject.SearchMetadata(
              meta.getId(),
              meta.getDistancePresent() ? meta.getDistance() : null,
              GRPC.fromByteString(meta.getVectorBytes()));

          return new SearchResult.SearchObject<T>(properties, metadata);
        }).toList();

    return new SearchResult<T>(objects);
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
    } else {
      assert false : "branch not covered";
    }
    return null;
  }

  SearchClient(Config config, AccessTokenProvider tokenProvider, String collection, Class<T> cls) {
    this.config = config;
    this.tokenProvider = tokenProvider;
    this.collection = collection;
    this.gson = new Gson();
    this.cls = cls;
  }
}
