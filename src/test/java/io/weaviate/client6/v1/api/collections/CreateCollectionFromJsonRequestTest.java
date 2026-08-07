package io.weaviate.client6.v1.api.collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CreateCollectionFromJsonRequestTest {

  @Test
  public void test_endpoint_passesBodyThroughVerbatim() {
    // Includes a key the client does not model to prove nothing is stripped.
    var json = """
        { "class": "Things", "someFutureOption": { "enabled": true } }
        """;
    var request = new CreateCollectionFromJsonRequest(json);

    Assertions.assertThat(CreateCollectionFromJsonRequest._ENDPOINT.method(request)).isEqualTo("POST");
    Assertions.assertThat(CreateCollectionFromJsonRequest._ENDPOINT.requestUrl(request)).isEqualTo("/schema/");
    Assertions.assertThat(CreateCollectionFromJsonRequest._ENDPOINT.queryParameters(request)).isEmpty();
    Assertions.assertThat(CreateCollectionFromJsonRequest._ENDPOINT.body(request))
        .as("body is forwarded byte-for-byte").isEqualTo(json);
  }

  @Test
  public void test_collectionName() {
    var request = new CreateCollectionFromJsonRequest("""
        { "class": "Things" }
        """);
    Assertions.assertThat(request.collectionName()).isEqualTo("Things");
  }

  @Test
  public void test_rejects_blankInput() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or blank");
  }

  @Test
  public void test_rejects_nullInput() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or blank");
  }

  @Test
  public void test_rejects_malformedJson() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("{ \"class\": "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not valid JSON");
  }

  @Test
  public void test_rejects_nonObjectJson() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("[ { \"class\": \"Things\" } ]"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be a JSON object");
  }

  @Test
  public void test_rejects_missingClass() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("{ \"description\": \"no name\" }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }

  @Test
  public void test_rejects_blankClass() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("{ \"class\": \"  \" }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }

  @Test
  public void test_rejects_nonStringClass() {
    Assertions.assertThatThrownBy(() -> new CreateCollectionFromJsonRequest("{ \"class\": 42 }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }
}
