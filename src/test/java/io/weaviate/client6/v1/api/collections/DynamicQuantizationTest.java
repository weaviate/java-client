package io.weaviate.client6.v1.api.collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.api.collections.vectorindex.Dynamic;
import io.weaviate.client6.v1.internal.json.JSON;

/**
 * Reading a quantizer off a dynamic index.
 *
 * <p>
 * The round-trip cases live in {@code JSONTest}; these cover the shapes the
 * server can return but this client never writes, since a single
 * {@code quantization()} slot always writes to {@code hnsw}.
 */
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

  /** The reported case: a dynamic index with RQ enabled on hnsw. */
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

  /** flat carries its own quantizer, and only bq is valid there. */
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

  /**
   * hnsw and flat can carry different quantizers, which one slot cannot
   * represent. hnsw wins; see QuantizerJson.
   */
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

  /** A disabled quantizer reads as absent, same as for hnsw and flat indexes. */
  @Test
  public void test_disabledQuantizerIsNull() {
    var config = JSON.deserialize(
        dynamic(", \"rq\": {\"enabled\": false, \"bits\": 8}", ""),
        VectorConfig.class);

    Assertions.assertThat(config.quantization()).isNull();
  }

  /** The dynamic level carries its own distance, which used to be dropped. */
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
}
