package io.weaviate.client6.v1.collections.data;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import com.google.gson.Gson;

import io.weaviate.client6.Config;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.FilterTarget;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters.Operator;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Vectors.VectorType;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoProperties.Value;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataResult;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesResult;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.RefPropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.RefPropertiesResult;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.internal.GrpcClient;
import io.weaviate.client6.internal.HttpClient;
import io.weaviate.client6.v1.collections.object.ObjectMetadata;
import io.weaviate.client6.v1.collections.object.ObjectReference;
import io.weaviate.client6.v1.collections.object.Vectors;
import io.weaviate.client6.v1.collections.object.WeaviateObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DataClient<T> {
  // TODO: inject singleton as dependency
  private static final Gson gson = new Gson();

  // TODO: this should be wrapped around in some TypeInspector etc.
  private final String collectionName;

  // TODO: hide befind an internal HttpClient
  private final Config config;
  private final HttpClient httpClient;
  private final GrpcClient grpcClient;

  public WeaviateObject<T> insert(T properties) throws IOException {
    return insert(properties, opt -> {
    });
  }

  public WeaviateObject<T> insert(T properties, Consumer<InsertObjectRequest.Builder<T>> fn) throws IOException {
    return insert(InsertObjectRequest.of(collectionName, properties, fn));
  }

  public WeaviateObject<T> insert(InsertObjectRequest<T> request) throws IOException {
    ClassicHttpRequest httpPost = ClassicRequestBuilder
        .post(config.baseUrl() + "/objects")
        .setEntity(request.serialize(gson), ContentType.APPLICATION_JSON)
        .build();

    return httpClient.http.execute(httpPost, response -> {
      var entity = response.getEntity();
      if (response.getCode() != HttpStatus.SC_SUCCESS) { // Does not return 201
        var message = EntityUtils.toString(entity);
        throw new RuntimeException("HTTP " + response.getCode() + ": " + message);
      }

      return WeaviateObject.fromJson(gson, entity.getContent());
    });
  }

  public Optional<WeaviateObject<T>> get(String id) throws IOException {
    return get(id, q -> {
    });
  }

  public Optional<WeaviateObject<T>> get(String id, Consumer<GetParameters> query) throws IOException {
    return findById(id);
    // try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
    // ClassicHttpRequest httpGet = ClassicRequestBuilder
    // .get(config.baseUrl() + "/objects/" + collectionName + "/" + id +
    // QueryParameters.encodeGet(query))
    // .build();
    //
    // return httpClient.http.execute(httpGet, response -> {
    // if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
    // return Optional.empty();
    // }
    //
    // WeaviateObject<T> object = WeaviateObject.fromJson(
    // gson, response.getEntity().getContent());
    // return Optional.ofNullable(object);
    // });
    // }
  }

  private Optional<WeaviateObject<T>> findById(String id) {
    var req = SearchRequest.newBuilder();
    req.setCollection(collectionName);

    var filter = Filters.newBuilder();
    var target = FilterTarget.newBuilder();
    target.setProperty("_id");
    filter.setTarget(target);
    filter.setValueText(id);
    filter.setOperator(Operator.OPERATOR_EQUAL);
    req.setFilters(filter);

    var properties = PropertiesRequest.newBuilder();
    var references = RefPropertiesRequest.newBuilder();
    references.setReferenceProperty("hasAwards");
    references.setTargetCollection("ReferencesITest_testReferences_Oscar");
    references.setMetadata(MetadataRequest.newBuilder().setUuid(true));
    // TODO: pass references and properties to fetch
    properties.addRefProperties(references);
    req.setProperties(properties);

    var result = grpcClient.grpc.search(req.build());
    var objects = result.getResultsList().stream().map(r -> readPropertiesResult(r.getProperties())).toList();
    return Optional.ofNullable((WeaviateObject<T>) objects.get(0));
  }

  private static WeaviateObject<?> readPropertiesResult(PropertiesResult res) {
    var collection = res.getTargetCollection();

    var objectProperties = convertProtoMap(res.getNonRefProps().getFieldsMap());
    var referenceProperties = res.getRefPropsList().stream()
        .collect(Collectors.<RefPropertiesResult, String, ObjectReference>toMap(
            RefPropertiesResult::getPropName,
            ref -> {
              var refObjects = ref.getPropertiesList().stream()
                  .map(DataClient::readPropertiesResult)
                  .toList();
              return new ObjectReference(refObjects);
            }));

    MetadataResult meta = res.getMetadata();
    Vectors vectors;
    if (meta.getVectorBytes() != null) {
      vectors = Vectors.of(GRPC.fromByteString(meta.getVectorBytes()));
    } else {
      vectors = Vectors.of(meta.getVectorsList().stream().collect(
          Collectors.<io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Vectors, String, Object>toMap(
              io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Vectors::getName,
              v -> {
                if (v.getType().equals(VectorType.VECTOR_TYPE_MULTI_FP32)) {
                  return GRPC.fromByteString(v.getVectorBytes());
                } else {
                  return GRPC.fromByteStringMulti(v.getVectorBytes());
                }
              })));
    }
    var metadata = new ObjectMetadata(meta.getId(), vectors);
    return new WeaviateObject<>(collection, objectProperties, referenceProperties, metadata);
  }

  /*
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

  public void delete(String id) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder
          .delete(config.baseUrl() + "/objects/" + collectionName + "/" + id)
          .build();

      httpClient.http.execute(httpGet, response -> {
        if (response.getCode() != HttpStatus.SC_NO_CONTENT) {
          throw new RuntimeException(EntityUtils.toString(response.getEntity()));
        }
        return null;
      });
    }
  }
}
