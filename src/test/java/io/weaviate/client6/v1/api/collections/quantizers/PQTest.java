package io.weaviate.client6.v1.api.collections.quantizers;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PQTest {
  @Test
  @SuppressWarnings("deprecation")
  public void test_legacyConstructor() {
    var legacy = new PQ(true, 256, 8, PQ.EncoderType.TILE, PQ.EncoderDistribution.NORMAL, 1000, false);
    var built = PQ.of(pq -> pq
        .centroids(256)
        .segments(8)
        .encoderType(PQ.EncoderType.TILE)
        .encoderDistribution(PQ.EncoderDistribution.NORMAL)
        .trainingLimit(1000)
        .bitCompression(false));

    Assertions.assertThat(legacy).isEqualTo(built);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void test_legacyConstructor_noEncoder() {
    var legacy = new PQ(true, null, null, null, null, null, null);

    Assertions.assertThat(legacy.encoder()).isNull();
  }
}
