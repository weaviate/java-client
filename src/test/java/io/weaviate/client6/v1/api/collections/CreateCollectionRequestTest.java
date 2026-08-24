package io.weaviate.client6.v1.api.collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.gson.JsonParser;

import io.weaviate.client6.v1.internal.rest.Endpoint;

public class CreateCollectionRequestTest {
  private static final Endpoint<CreateCollectionRequest<String>, Void> RAW = //
      CreateCollectionRequest.endpoint();

  private static final Endpoint<CreateCollectionRequest<CollectionConfig>, Void> TYPED = //
      CreateCollectionRequest.endpoint();

  @Test
  public void test_endpoint_passesRawBodyThroughVerbatim() {
    // Includes a key the client does not model to prove nothing is stripped.
    var json = """
        { "class": "Things", "someFutureOption": { "enabled": true } }
        """;
    var request = new CreateCollectionRequest<>(json);

    Assertions.assertThat(RAW.method(request)).isEqualTo("POST");
    Assertions.assertThat(RAW.requestUrl(request)).isEqualTo("/schema/");
    Assertions.assertThat(RAW.queryParameters(request)).isEmpty();
    Assertions.assertThat(RAW.body(request)).as("body is forwarded byte-for-byte").isEqualTo(json);
  }

  @Test
  public void test_endpoint_serializesTypedPayload() {
    var request = new CreateCollectionRequest<>(CollectionConfig.of("Things"));

    Assertions.assertThat(TYPED.method(request)).isEqualTo("POST");
    Assertions.assertThat(TYPED.requestUrl(request)).isEqualTo("/schema/");

    var body = JsonParser.parseString(TYPED.body(request)).getAsJsonObject();
    Assertions.assertThat(body.get("class").getAsString()).isEqualTo("Things");
  }

  @Test
  public void test_collectionNameFromJson() {
    Assertions.assertThat(CreateCollectionRequest.collectionNameFromJson("""
        { "class": "Things" }
        """)).isEqualTo("Things");
  }

  @Test
  public void test_rejects_blankInput() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or blank");
  }

  @Test
  public void test_rejects_nullInput() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must not be null or blank");
  }

  @Test
  public void test_rejects_malformedJson() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("{ \"class\": "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not valid JSON");
  }

  @Test
  public void test_rejects_nonObjectJson() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("[ { \"class\": \"Things\" } ]"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must be a JSON object");
  }

  @Test
  public void test_rejects_missingClass() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("{ \"description\": \"x\" }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }

  @Test
  public void test_rejects_blankClass() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("{ \"class\": \"  \" }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }

  @Test
  public void test_rejects_nonStringClass() {
    Assertions.assertThatThrownBy(() -> CreateCollectionRequest.collectionNameFromJson("{ \"class\": 42 }"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("\"class\"");
  }
}
