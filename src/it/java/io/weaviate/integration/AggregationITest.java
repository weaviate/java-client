package io.weaviate.integration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.WeaviateClient;
import io.weaviate.client6.v1.collections.Property;
import io.weaviate.client6.v1.collections.aggregate.IntegerMetric;
import io.weaviate.client6.v1.collections.aggregate.Metric;
import io.weaviate.containers.Container;

public class AggregationITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();
  private static final String COLLECTION = unique("Things");

  @BeforeClass
  public static void beforeAll() throws IOException {
    client.collections.create(COLLECTION,
        collection -> collection
            .properties(
                Property.text("category"),
                Property.integer("price")));

    var things = client.collections.use(COLLECTION);
    for (var category : List.of("Shoes", "Hat", "Jacket")) {
      for (var i = 0; i < 5; i++) {
        // For simplicity, the "price" for each items equals to the
        // number of characters in the name of the category.
        things.data.insert(Map.of(
            "category", category,
            "price", category.length()));
      }
    }
  }

  @Test
  public void testOverAll() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.overAll(
        with -> with.metrics(
            Metric.integer("price", calculate -> calculate
                .median().max().count()))
            .includeTotalCount());

    Assertions.assertThat(result)
        .as("includes all objects").hasFieldOrPropertyWithValue("totalCount", 15L)
        .as("'price' is IntegerMetric").returns(true, p -> p.isIntegerProperty("price"))
        .as("aggregated prices").extracting(p -> p.getInteger("price"))
        .as("min").returns(null, IntegerMetric.Values::min)
        .as("max").returns(6L, IntegerMetric.Values::max)
        .as("median").returns(5D, IntegerMetric.Values::median)
        .as("count").returns(15L, IntegerMetric.Values::count);
  }

  // @Test
  public void testOverAll_groupBy_category() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.overAll(
        with -> with.metrics(
            Metric.integer("price", calculate -> calculate
                .median().max().count()))
            .includeTotalCount());

    Assertions.assertThat(result)
        .as("includes all objects").hasFieldOrPropertyWithValue("totalCount", 15L)
        .as("'price' is IntegerMetric").returns(true, p -> p.isIntegerProperty("price"))
        .as("aggregated prices").extracting(p -> p.getInteger("price"))
        .as("min").returns(null, IntegerMetric.Values::min)
        .as("max").returns(6L, IntegerMetric.Values::max)
        .as("median").returns(5f, IntegerMetric.Values::median)
        .as("count").returns(15L, IntegerMetric.Values::count);
  }
}
