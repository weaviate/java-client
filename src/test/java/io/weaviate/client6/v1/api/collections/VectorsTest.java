package io.weaviate.client6.v1.api.collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class VectorsTest {
  @Test
  public void testToString_1d() {
    var vector = Vectors.of(new float[] { 1, 2, 3 });
    var got = vector.toString();
    Assertions.assertThat(got).isEqualTo("Vectors(default=[1.0, 2.0, 3.0])");
  }

  @Test
  public void testToString_2d() {
    var vector = Vectors.of(new float[][] { { 1, 2, 3 }, { 1, 2, 3 } });
    var got = vector.toString();
    Assertions.assertThat(got).isEqualTo("Vectors(default=[[1.0, 2.0, 3.0], [1.0, 2.0, 3.0]])");
  }

  @Test
  public void testToString_multiple() {
    var title = Vectors.of("title", new float[] { 1, 2, 3 });
    var body = Vectors.of("body", new float[][] { { 1, 2, 3 }, { 1, 2, 3 } });
    var vectors = new Vectors(title, body);
    var got = vectors.toString();
    Assertions.assertThat(got).isEqualTo("Vectors(title=[1.0, 2.0, 3.0], body=[[1.0, 2.0, 3.0], [1.0, 2.0, 3.0]])");
  }
}
