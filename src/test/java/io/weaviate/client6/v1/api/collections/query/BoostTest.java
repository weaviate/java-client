package io.weaviate.client6.v1.api.collections.query;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.client6.v1.api.collections.query.Boost.Condition;

public class BoostTest {
  @Test
  public void test_blend_useConditionWeight() {
    var boost = Boost.blend(null, 8,
        Boost.numericProperty("size", prop -> prop.weight(.45f)));

    Assertions.assertThat(boost)
        .returns(8, Boost::depth)
        .returns(null, Boost::weight);
    Assertions.assertThat(boost.conditions()).first()
        .returns(.45f, Condition::weight);
  }

  @Test
  public void test_blend_passWeightToCondition() {
    var boost = Boost.blend(.45f, 8,
        Boost.numericProperty("size"));

    Assertions.assertThat(boost)
        .returns(8, Boost::depth)
        .returns(.45f, Boost::weight);
    Assertions.assertThat(boost.conditions()).first()
        .returns(null, Condition::weight);
  }

  @Test
  public void test_blend_illegalDepth() {
    Assertions.assertThatCode(() -> {
      Boost.blend(1f, 2,
          Boost.numericProperty("size", prop -> prop.depth(3)));
    }).isInstanceOf(IllegalArgumentException.class);
  }
}
