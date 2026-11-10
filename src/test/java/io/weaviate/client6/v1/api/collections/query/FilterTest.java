package io.weaviate.client6.v1.api.collections.query;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FilterTest {
  @Test
  public void test_withinGeoRange_toString() {
    var filter = Filter.property("location").withinGeoRange(52.52f, 13.4f, 2000f);

    Assertions.assertThat(filter.toString())
        .contains("lat=52.52", "lon=13.4", "distance=2000.0");
  }
}
