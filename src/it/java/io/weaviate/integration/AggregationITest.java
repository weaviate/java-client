package io.weaviate.integration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Vectorizers;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.aggregate.Aggregate;
import io.weaviate.client6.v1.api.collections.aggregate.AggregateResponseGroup;
import io.weaviate.client6.v1.api.collections.aggregate.AggregateResponseGrouped;
import io.weaviate.client6.v1.api.collections.aggregate.GroupBy;
import io.weaviate.client6.v1.api.collections.aggregate.GroupedBy;
import io.weaviate.client6.v1.api.collections.aggregate.IntegerAggregation;
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
                Property.integer("price"))
            .vectorConfig(Vectorizers.selfProvided()));

    var things = client.collections.use(COLLECTION);
    for (var category : List.of("Shoes", "Hat", "Jacket")) {
      for (var i = 0; i < 5; i++) {
        var vector = randomVector(10, -.1f, .1f);
        // For simplicity, the "price" for each items equals to the
        // number of characters in the name of the category.
        things.data.insert(Map.of(
            "category", category,
            "price", category.length()),
            meta -> meta.vectors(Vectors.of(vector)));
      }
    }
  }

  @Test
  public void testOverAll() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.overAll(
        with -> with
            .metrics(
                Aggregate.integer("price",
                    calculate -> calculate.median().max().count()))
            .includeTotalCount(true));

    Assertions.assertThat(result)
        .as("includes all objects").hasFieldOrPropertyWithValue("totalCount", 15L)
        .as("'price' is IntegerAggregation").returns(true, p -> p.isInteger("price"))
        .as("aggregated prices").extracting(p -> p.integer("price"))
        .as("min").returns(null, IntegerAggregation.Values::min)
        .as("max").returns(6L, IntegerAggregation.Values::max)
        .as("median").returns(5D, IntegerAggregation.Values::median)
        .as("count").returns(15L, IntegerAggregation.Values::count);
  }

  @Test
  public void testOverAll_groupBy_category() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.overAll(
        with -> with
            .metrics(
                Aggregate.integer("price",
                    calculate -> calculate.min().max().count()))
            .includeTotalCount(true),
        GroupBy.property("category"));

    Assertions.assertThat(result)
        .extracting(AggregateResponseGrouped::groups)
        .asInstanceOf(InstanceOfAssertFactories.list(AggregateResponseGroup.class))
        .as("group per category").hasSize(3)
        .allSatisfy(group -> {
          Assertions.assertThat(group)
              .extracting(AggregateResponseGroup::groupedBy)
              .as(group.groupedBy().property() + " is Text property").returns(true, GroupedBy::isText);

          String category = group.groupedBy().text();
          var expectedPrice = (long) category.length();

          Function<String, Supplier<String>> desc = (String metric) -> {
            return () -> "%s ('%s'.length)".formatted(metric, category);
          };

          Assertions.assertThat(group)
              .as("'price' is IntegerAggregation").returns(true, g -> g.isInteger("price"))
              .as("aggregated prices").extracting(g -> g.integer("price"))
              .as(desc.apply("max")).returns(expectedPrice, IntegerAggregation.Values::max)
              .as(desc.apply("min")).returns(expectedPrice, IntegerAggregation.Values::min)
              .as(desc.apply("count")).returns(5L, IntegerAggregation.Values::count);
        });
  }

  @Test
  public void testNearVector() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.nearVector(
        randomVector(10, -1f, 1f),
        near -> near.limit(5),
        with -> with
            .metrics(
                Aggregate.integer("price",
                    calculate -> calculate.min().max().count()))
            .objectLimit(4)
            .includeTotalCount(true));

    Assertions.assertThat(result)
        .as("includes all objects").hasFieldOrPropertyWithValue("totalCount", 4L)
        .as("'price' is IntegerAggregation").returns(true, p -> p.isInteger("price"))
        .as("aggregated prices").extracting(p -> p.integer("price"))
        .as("count").returns(4L, IntegerAggregation.Values::count);
  }

  @Test
  public void testNearVector_groupBy_category() {
    var things = client.collections.use(COLLECTION);
    var result = things.aggregate.nearVector(
        randomVector(10, -1f, 1f),
        near -> near.distance(2f),
        with -> with
            .metrics(
                Aggregate.integer("price",
                    calculate -> calculate.min().max().median()))
            .objectLimit(9)
            .includeTotalCount(true),
        GroupBy.property("category"));

    Assertions.assertThat(result)
        .extracting(AggregateResponseGrouped::groups)
        .asInstanceOf(InstanceOfAssertFactories.list(AggregateResponseGroup.class))
        .as("group per category").hasSize(3)
        .allSatisfy(group -> {
          Assertions.assertThat(group)
              .extracting(AggregateResponseGroup::groupedBy)
              .as(group.groupedBy().property() + " is Text property").returns(true, GroupedBy::isText);

          String category = group.groupedBy().text();
          var expectedPrice = (long) category.length();

          Function<String, Supplier<String>> desc = (String metric) -> {
            return () -> "%s ('%s'.length)".formatted(metric, category);
          };

          Assertions.assertThat(group)
              .as("'price' is IntegerAggregation").returns(true, g -> g.isInteger("price"))
              .as("aggregated prices").extracting(g -> g.integer("price"))
              .as(desc.apply("max")).returns(expectedPrice, IntegerAggregation.Values::max)
              .as(desc.apply("min")).returns(expectedPrice, IntegerAggregation.Values::min)
              .as(desc.apply("median")).returns((double) expectedPrice, IntegerAggregation.Values::median);
        });
  }

  @Test
  public void testCollestionSizeShortcut() {
    var things = client.collections.use(COLLECTION);
    var countAggregate = things.aggregate
        .overAll(x -> x.includeTotalCount(true)).totalCount();
    var size = things.size();
    Assertions.assertThat(size).isEqualTo(countAggregate);
  }
}
