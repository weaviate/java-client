package io.weaviate.client6.v1.api.collections;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.internal.rest.JsonEndpoint;

public class GetConfigJsonRequestTest {
  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<GetConfigJsonRequest, Optional<String>> GET = //
      (JsonEndpoint<GetConfigJsonRequest, Optional<String>>) GetConfigJsonRequest._ENDPOINT;

  @SuppressWarnings("unchecked")
  private static final JsonEndpoint<ListCollectionJsonRequest, String> LIST = //
      (JsonEndpoint<ListCollectionJsonRequest, String>) ListCollectionJsonRequest._ENDPOINT;

  @Test
  public void test_endpoint() {
    var request = new GetConfigJsonRequest("Things");

    Assertions.assertThat(GET.method(request)).isEqualTo("GET");
    Assertions.assertThat(GET.requestUrl(request)).isEqualTo("/schema/Things");
    Assertions.assertThat(GET.queryParameters(request)).isEmpty();
    Assertions.assertThat(GET.body(request)).isNull();
  }

  @Test
  public void test_responseIsNotParsed() {
    // Includes a key the client does not model, and a quantizer nested inside a
    // "dynamic" index, neither of which survives CollectionConfig deserialization.
    var raw = """
        {
          "class": "Things",
          "someFutureOption": { "enabled": true },
          "vectorConfig": { "default": {
            "vectorIndexType": "dynamic",
            "vectorIndexConfig": { "hnsw": { "rq": { "enabled": true, "bits": 8 } } }
          }}
        }
        """;

    var got = GET.deserializeResponse(200, raw);

    Assertions.assertThat(got).as("body is returned verbatim").contains(raw);
  }

  @Test
  public void test_missingCollectionIsEmpty() {
    Assertions.assertThat(GET.deserializeResponse(404, "{\"error\":[]}"))
        .isEqualTo(Optional.empty());
    Assertions.assertThat(GET.isError(404))
        .as("404 is not an error, it means 'no such collection'").isFalse();
  }

  @Test
  public void test_listEndpoint() {
    var request = new ListCollectionJsonRequest();

    Assertions.assertThat(LIST.method(request)).isEqualTo("GET");
    Assertions.assertThat(LIST.requestUrl(request)).isEqualTo("/schema");
    Assertions.assertThat(LIST.queryParameters(request)).isEmpty();
    Assertions.assertThat(LIST.body(request)).isNull();

    var raw = "{ \"classes\": [ { \"class\": \"Things\" } ] }";
    Assertions.assertThat(LIST.deserializeResponse(200, raw))
        .as("body is returned verbatim").isEqualTo(raw);
  }
}
