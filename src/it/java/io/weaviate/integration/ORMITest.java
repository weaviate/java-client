package io.weaviate.integration;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.containers.Container;

public class ORMITest extends ConcurrentTest {
  private static WeaviateClient client = Container.WEAVIATE.getClient();

  @Test
  public void test_createCollection() throws Exception {
    // Arrange
    var things = client.collections.use(Things.class);

    // Act
    client.collections.create(Things.class);

    // Assert
    var config = things.config.get();
    Assertions.assertThat(config).get()
        .returns("Things", CollectionConfig::collectionName)
        .extracting(CollectionConfig::properties, InstanceOfAssertFactories.list(Property.class))
        .extracting(p -> Map.entry(
            p.propertyName(),
            p.dataTypes().get(0)))
        .contains(
            Map.entry("text", "text"),
            Map.entry("textArray", "text[]"),
            Map.entry("textList", "text[]"),

            Map.entry("short_", "int"),
            Map.entry("shortBoxed", "int"),
            Map.entry("shortArray", "int[]"),
            Map.entry("shortBoxedArray", "int[]"),
            Map.entry("shortBoxedList", "int[]"),

            Map.entry("int_", "int"),
            Map.entry("intBoxed", "int"),
            Map.entry("intArray", "int[]"),
            Map.entry("intBoxedArray", "int[]"),
            Map.entry("intBoxedList", "int[]"),

            Map.entry("long_", "int"),
            Map.entry("longBoxed", "int"),
            Map.entry("longArray", "int[]"),
            Map.entry("longBoxedArray", "int[]"),
            Map.entry("longBoxedList", "int[]"),

            Map.entry("float_", "number"),
            Map.entry("floatBoxed", "number"),
            Map.entry("floatArray", "number[]"),
            Map.entry("floatBoxedArray", "number[]"),
            Map.entry("floatBoxedList", "number[]"),

            Map.entry("double_", "number"),
            Map.entry("doubleBoxed", "number"),
            Map.entry("doubleArray", "number[]"),
            Map.entry("doubleBoxedArray", "number[]"),
            Map.entry("doubleBoxedList", "number[]"));
  }
}

class Things {
  // text / text[]
  private String text;
  private String[] textArray;
  private List<String> textList;

  // int / int[]
  private short short_;
  private Short shortBoxed;
  private short[] shortArray;
  private Short[] shortBoxedArray;
  private List<Short> shortBoxedList;

  private int int_;
  private Integer intBoxed;
  private int[] intArray;
  private Integer[] intBoxedArray;
  private List<Integer> intBoxedList;

  private long long_;
  private Long longBoxed;
  private long[] longArray;
  private Long[] longBoxedArray;
  private List<Long> longBoxedList;

  // number / number[]
  private float float_;
  private Float floatBoxed;
  private float[] floatArray;
  private Float[] floatBoxedArray;
  private List<Float> floatBoxedList;

  private double double_;
  private Double doubleBoxed;
  private double[] doubleArray;
  private Double[] doubleBoxedArray;
  private List<Double> doubleBoxedList;
}
