package io.weaviate.client6.v1.api.collections;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.internal.rest.JsonEndpoint;

public class GetConfigRequestTest {
  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<GetConfigRequest, Optional<String>> GET_RAW = //
      (JsonEndpoint<GetConfigRequest, Optional<String>>) GetConfigRequest.endpoint(String.class);

  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<GetConfigRequest, Optional<CollectionConfig>> GET_TYPED = //
      (JsonEndpoint<GetConfigRequest, Optional<CollectionConfig>>) GetConfigRequest.endpoint(CollectionConfig.class);

  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<ListCollectionRequest, String> LIST_RAW = //
      (JsonEndpoint<ListCollectionRequest, String>) ListCollectionRequest.endpoint(String.class);

  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<ListCollectionRequest, ListCollectionResponse> LIST_TYPED = //
      (JsonEndpoint<ListCollectionRequest, ListCollectionResponse>) ListCollectionRequest
          .endpoint(ListCollectionResponse.class);

  // Includes a key the client does not model, and a quantizer nested inside a
  // "dynamic" index, neither of which survives CollectionConfig deserialization.
  private static final String RAW_SCHEMA = """
      {
        "class": "Things",
        "someFutureOption": { "enabled": true },
        "vectorConfig": { "default": {
          "vectorIndexType": "dynamic",
          "vectorIndexConfig": { "hnsw": { "rq": { "enabled": true, "bits": 8 } } }
        }}
      }
      """;

  @Test
  public void test_endpoint() {
    var request = new GetConfigRequest("Things");

    Assertions.assertThat(GET_RAW.method(request)).isEqualTo("GET");
    Assertions.assertThat(GET_RAW.requestUrl(request)).isEqualTo("/schema/Things");
    Assertions.assertThat(GET_RAW.queryParameters(request)).isEmpty();
    Assertions.assertThat(GET_RAW.body(request)).isNull();
  }

  @Test
  public void test_stringResponseIsNotParsed() {
    Assertions.assertThat(GET_RAW.deserializeResponse(200, RAW_SCHEMA))
        .as("body is returned verbatim").contains(RAW_SCHEMA);
  }

  @Test
  public void test_typedResponseIsDeserialized() {
    Assertions.assertThat(GET_TYPED.deserializeResponse(200, "{ \"class\": \"Things\" }")).get()
        .extracting(CollectionConfig::collectionName).isEqualTo("Things");
  }

  @Test
  public void test_missingCollectionIsEmpty() {
    Assertions.assertThat(GET_RAW.deserializeResponse(404, "{\"error\":[]}"))
        .isEqualTo(Optional.empty());
    Assertions.assertThat(GET_RAW.isError(404))
        .as("404 is not an error, it means 'no such collection'").isFalse();
  }

  @Test
  public void test_listEndpoint() {
    var request = new ListCollectionRequest();

    Assertions.assertThat(LIST_RAW.method(request)).isEqualTo("GET");
    Assertions.assertThat(LIST_RAW.requestUrl(request)).isEqualTo("/schema");
    Assertions.assertThat(LIST_RAW.queryParameters(request)).isEmpty();
    Assertions.assertThat(LIST_RAW.body(request)).isNull();

    var raw = "{ \"classes\": [ { \"class\": \"Things\" } ] }";
    Assertions.assertThat(LIST_RAW.deserializeResponse(200, raw))
        .as("body is returned verbatim").isEqualTo(raw);
    Assertions.assertThat(LIST_TYPED.deserializeResponse(200, raw).collections())
        .extracting(CollectionConfig::collectionName).containsExactly("Things");
  }
}
