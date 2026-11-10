package io.weaviate.client6.v1.api.collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.gson.JsonParser;

import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.api.collections.vectorindex.Dynamic;
import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.client6.v1.internal.json.JSON;

public class DynamicQuantizationTest {

  private static String dynamic(String hnswExtra, String flatExtra) {
    return """
        {
          "vectorIndexType": "dynamic",
          "vectorizer": {"none": {}},
          "vectorIndexConfig": {
            "threshold": 10000,
            "hnsw": {"ef": -1%s},
            "flat": {"vectorCacheMaxObjects": 1000000%s}
          }
        }
        """.formatted(hnswExtra, flatExtra);
  }

  @Test
  public void test_readsQuantizerFromHnsw() {
    var config = JSON.deserialize(
        dynamic(", \"rq\": {\"enabled\": true, \"bits\": 8, \"rescoreLimit\": 20}", ""),
        VectorConfig.class);

    Assertions.assertThat(config.quantization())
        .isNotNull()
        .returns(Quantization.Kind.RQ, Quantization::_kind);
    Assertions.assertThat(config.quantization().asRQ().rescoreLimit()).isEqualTo(20);
    Assertions.assertThat(config.quantization().asRQ().bits()).isEqualTo(8);
  }

  @Test
  public void test_readsQuantizerFromFlatWhenHnswHasNone() {
    var config = JSON.deserialize(
        dynamic("", ", \"bq\": {\"enabled\": true, \"rescoreLimit\": 5}"),
        VectorConfig.class);

    Assertions.assertThat(config.quantization())
        .isNotNull()
        .returns(Quantization.Kind.BQ, Quantization::_kind);
    Assertions.assertThat(config.quantization().asBQ().rescoreLimit()).isEqualTo(5);
  }

  @Test
  public void test_prefersHnswWhenBothCarryOne() {
    var config = JSON.deserialize(
        dynamic(", \"rq\": {\"enabled\": true, \"bits\": 8}", ", \"bq\": {\"enabled\": true}"),
        VectorConfig.class);

    Assertions.assertThat(config.quantization())
        .returns(Quantization.Kind.RQ, Quantization::_kind);
  }

  @Test
  public void test_noQuantizerIsNull() {
    var config = JSON.deserialize(dynamic("", ""), VectorConfig.class);

    Assertions.assertThat(config.quantization()).isNull();
  }

  @Test
  public void test_disabledQuantizerIsNull() {
    var config = JSON.deserialize(
        dynamic(", \"rq\": {\"enabled\": false, \"bits\": 8}", ""),
        VectorConfig.class);

    Assertions.assertThat(config.quantization()).isNull();
  }

  @Test
  public void test_readsDistanceAtDynamicLevel() {
    var config = JSON.deserialize("""
        {
          "vectorIndexType": "dynamic",
          "vectorizer": {"none": {}},
          "vectorIndexConfig": {
            "threshold": 10000,
            "distance": "l2-squared",
            "hnsw": {"ef": -1},
            "flat": {"vectorCacheMaxObjects": 1000000}
          }
        }
        """, VectorConfig.class);

    Assertions.assertThat(config.vectorIndex().asDynamic())
        .returns(Distance.L2_SQUARED, Dynamic::distance)
        .returns(10000L, Dynamic::threshold);
  }

  @Test
  public void test_ignoresDisabledQuantizersOnHnsw() {
    var config = JSON.deserialize(
        dynamic("""
            , "bq": {"enabled": false, "rescoreLimit": -1, "cache": false},
              "pq": {"enabled": false, "bitCompression": false, "segments": 0, "centroids": 256,
                     "trainingLimit": 100000, "encoder": {"type": "kmeans", "distribution": "log-normal"}},
              "sq": {"enabled": false, "rescoreLimit": 20, "trainingLimit": 100000, "cache": false},
              "rq": {"enabled": false, "bits": 8, "rescoreLimit": 20, "cache": false},
              "skipDefaultQuantization": false
            """,
            ", \"bq\": {\"enabled\": true, \"rescoreLimit\": 5}"),
        VectorConfig.class);

    Assertions.assertThat(config.quantization())
        .isNotNull()
        .returns(Quantization.Kind.BQ, Quantization::_kind);
    Assertions.assertThat(config.quantization().asBQ().rescoreLimit()).isEqualTo(5);
  }

  @Test
  public void test_writesQuantizerToHnswWhenOnlyFlatIsSet() {
    var config = SelfProvidedVectorizer.of(none -> none
        .vectorIndex(Dynamic.of(idx -> idx.flat(Flat.of(flat -> flat.vectorCacheMaxObjects(100)))))
        .quantization(Quantization.rq(rq -> rq.bits(8))));

    var indexConfig = JsonParser.parseString(JSON.serialize(config)).getAsJsonObject()
        .getAsJsonObject("vectorIndexConfig");

    Assertions.assertThat(indexConfig.getAsJsonObject("hnsw").has("rq")).as("rq under hnsw").isTrue();
    Assertions.assertThat(indexConfig.getAsJsonObject("flat").has("rq")).as("rq under flat").isFalse();
  }
}
