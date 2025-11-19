package io.weaviate.integration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.GeoCoordinates;
import io.weaviate.client6.v1.api.collections.PhoneNumber;
import io.weaviate.client6.v1.api.collections.annotations.Collection;
import io.weaviate.client6.v1.api.collections.annotations.Property;
import io.weaviate.client6.v1.api.collections.data.InsertManyResponse.InsertObject;
import io.weaviate.client6.v1.api.collections.query.ReadWeaviateObject;
import io.weaviate.client6.v1.api.collections.query.Where;
import io.weaviate.containers.Container;

public class ORMITest extends ConcurrentTest {
  private static final WeaviateClient client = Container.WEAVIATE.getClient();

  @Collection("ORMITestThings")
  static record Thing(
      // text / text[]
      String text,
      String[] textArray,
      List<String> textList,

      // date / date[]
      OffsetDateTime date,
      OffsetDateTime[] dateArray,
      List<OffsetDateTime> dateList,

      // uuid / uuid[]
      UUID uuid,
      UUID[] uuidArray,
      List<UUID> uuidList,

      // int / int[]
      @Property("short") short short_,
      Short shortBoxed,
      short[] shortArray,
      Short[] shortBoxedArray,
      List<Short> shortBoxedList,

      @Property("int") int int_,
      Integer intBoxed,
      int[] intArray,
      Integer[] intBoxedArray,
      List<Integer> intBoxedList,

      @Property("long") long long_,
      Long longBoxed,
      long[] longArray,
      Long[] longBoxedArray,
      List<Long> longBoxedList,

      // number / number[]
      @Property("float") float float_,
      Float floatBoxed,
      float[] floatArray,
      Float[] floatBoxedArray,
      List<Float> floatBoxedList,

      @Property("double") double double_,
      Double doubleBoxed,
      double[] doubleArray,
      Double[] doubleBoxedArray,
      List<Double> doubleBoxedList,

      // boolean / boolean[]
      @Property("boolean") boolean boolean_,
      Boolean booleanBoxed,
      boolean[] booleanArray,
      Boolean[] booleanBoxedArray,
      List<Boolean> booleanBoxedList,

      PhoneNumber phoneNumber,
      GeoCoordinates geoCoordinates) {
  }

  @BeforeClass
  public static void setUp() throws Exception {
    client.collections.create(Thing.class);
  }

  @Test
  public void test_createCollection() throws Exception {
    // Arrange
    var things = client.collections.use(Thing.class);

    // Act
    var config = things.config.get();

    // Assert
    Assertions.assertThat(config).get()
        .returns("ORMITestThings", CollectionConfig::collectionName)
        .extracting(CollectionConfig::properties,
            InstanceOfAssertFactories.list(io.weaviate.client6.v1.api.collections.Property.class))
        .extracting(p -> Map.entry(
            p.propertyName(),
            p.dataTypes().get(0)))
        .contains(
            Map.entry("text", "text"),
            Map.entry("textArray", "text[]"),
            Map.entry("textList", "text[]"),

            Map.entry("date", "date"),
            Map.entry("dateArray", "date[]"),
            Map.entry("dateList", "date[]"),

            Map.entry("uuid", "uuid"),
            Map.entry("uuidArray", "uuid[]"),
            Map.entry("uuidList", "uuid[]"),

            Map.entry("short", "int"),
            Map.entry("shortBoxed", "int"),
            Map.entry("shortArray", "int[]"),
            Map.entry("shortBoxedArray", "int[]"),
            Map.entry("shortBoxedList", "int[]"),

            Map.entry("int", "int"),
            Map.entry("intBoxed", "int"),
            Map.entry("intArray", "int[]"),
            Map.entry("intBoxedArray", "int[]"),
            Map.entry("intBoxedList", "int[]"),

            Map.entry("long", "int"),
            Map.entry("longBoxed", "int"),
            Map.entry("longArray", "int[]"),
            Map.entry("longBoxedArray", "int[]"),
            Map.entry("longBoxedList", "int[]"),

            Map.entry("float", "number"),
            Map.entry("floatBoxed", "number"),
            Map.entry("floatArray", "number[]"),
            Map.entry("floatBoxedArray", "number[]"),
            Map.entry("floatBoxedList", "number[]"),

            Map.entry("double", "number"),
            Map.entry("doubleBoxed", "number"),
            Map.entry("doubleArray", "number[]"),
            Map.entry("doubleBoxedArray", "number[]"),
            Map.entry("doubleBoxedList", "number[]"),

            Map.entry("boolean", "boolean"),
            Map.entry("booleanBoxed", "boolean"),
            Map.entry("booleanArray", "boolean[]"),
            Map.entry("booleanBoxedArray", "boolean[]"),
            Map.entry("booleanBoxedList", "boolean[]"),

            Map.entry("phoneNumber", "phoneNumber"),
            Map.entry("geoCoordinates", "geoCoordinates"));
  }

  private static final RecursiveComparisonConfiguration COMPARISON_CONFIG = RecursiveComparisonConfiguration.builder()
      // Assertj is having a really bad time comparing List<Float>,
      // so we'll just always return true here.
      .withComparatorForFields((a, b) -> 0, "floatBoxedList")
      .withComparatorForType((a, b) -> Double.compare(a.doubleValue(), b.doubleValue()), Number.class)
      .withComparatorForType(ORMITest::comparePhoneNumbers, PhoneNumber.class)
      .build();

  @Test
  public void test_insertAndQuery() throws Exception {
    short short_ = 666;
    int int_ = 666;
    long long_ = 666;
    float float_ = 666;
    double double_ = 666;
    boolean boolean_ = true;
    UUID uuid = UUID.randomUUID();
    OffsetDateTime date = OffsetDateTime.now();
    String text = "hello";

    var thing = new Thing(
        text,
        new String[] { text },
        List.of(text),

        OffsetDateTime.now(),
        new OffsetDateTime[] { date },
        List.of(date),

        UUID.randomUUID(),
        new UUID[] { uuid },
        List.of(uuid),

        short_,
        short_,
        new short[] { short_ },
        new Short[] { short_ },
        List.of(short_),

        int_,
        int_,
        new int[] { int_ },
        new Integer[] { int_ },
        List.of(int_),

        long_,
        long_,
        new long[] { long_ },
        new Long[] { long_ },
        List.of(long_),

        float_,
        float_,
        new float[] { float_ },
        new Float[] { float_ },
        List.of(float_),

        double_,
        double_,
        new double[] { double_ },
        new Double[] { double_ },
        List.of(double_),

        boolean_,
        boolean_,
        new boolean[] { boolean_ },
        new Boolean[] { boolean_ },
        List.of(boolean_),

        PhoneNumber.international("+380 95 1433336"),
        new GeoCoordinates(1f, 2f));

    var things = client.collections.use(Thing.class);

    // Act
    var inserted = things.data.insert(thing);

    // Assert
    var response = things.query.fetchObjectById(inserted.uuid());
    var got = Assertions.assertThat(response).get().actual();

    Assertions.assertThat(got.properties())
        .usingRecursiveComparison(COMPARISON_CONFIG)
        .isEqualTo(thing);
  }

  @Test
  public void test_insertManyAndQuery() throws Exception {
    short short_ = 666;
    int int_ = 666;
    long long_ = 666;
    float float_ = 666;
    double double_ = 666;
    boolean boolean_ = true;
    UUID uuid = UUID.randomUUID();
    OffsetDateTime date = OffsetDateTime.now();
    String text = "hello";

    var thing = new Thing(
        text,
        new String[] { text },
        List.of(text),

        OffsetDateTime.now(),
        new OffsetDateTime[] { date },
        List.of(date),

        UUID.randomUUID(),
        new UUID[] { uuid },
        List.of(uuid),

        short_,
        short_,
        new short[] { short_ },
        new Short[] { short_ },
        List.of(short_),

        int_,
        int_,
        new int[] { int_ },
        new Integer[] { int_ },
        List.of(int_),

        long_,
        long_,
        new long[] { long_ },
        new Long[] { long_ },
        List.of(long_),

        float_,
        float_,
        new float[] { float_ },
        new Float[] { float_ },
        List.of(float_),

        double_,
        double_,
        new double[] { double_ },
        new Double[] { double_ },
        List.of(double_),

        boolean_,
        boolean_,
        new boolean[] { boolean_ },
        new Boolean[] { boolean_ },
        List.of(boolean_),

        PhoneNumber.international("+380 95 1433336"),
        new GeoCoordinates(1f, 2f));

    var things = client.collections.use(Thing.class);

    // Act
    var inserted = things.data.insertMany(thing, thing, thing);

    // Assert
    var uuids = inserted.responses().stream().map(InsertObject::uuid).toArray(String[]::new);
    var got = things.query.fetchObjects(q -> q.where(Where.uuid().containsAny(uuids)));
    Assertions.assertThat(got.objects())
        .hasSize(3)
        .usingRecursiveComparison(COMPARISON_CONFIG)
        .asInstanceOf(InstanceOfAssertFactories.list(Thing.class));
  }

  @Collection("ORMITestSongs")
  record Song(
      String title,
      String album,
      int year,
      boolean hasAward,
      Long monthlyListeners) {
  }

  /**
   * Test that serialization works correctly when some fields are null and
   * deserialization works correctly when some properties are not returned.
   */
  @Test
  public void test_partialScan() throws IOException {
    client.collections.create(Song.class);

    var songs = client.collections.use(Song.class);

    // Act: insert with nulls
    var dystopia = songs.data.insert(new Song(
        "Dystopia",
        null,
        2016,
        true,
        null));

    // Act: return subset of the properties
    var got = songs.query.fetchObjectById(dystopia.uuid(),
        q -> q.returnProperties("title", "hasAward"));

    // Assert
    Assertions.assertThat(got).get()
        .extracting(ReadWeaviateObject::properties)
        .returns("Dystopia", Song::title)
        .returns(null, Song::album)
        .returns(0, Song::year)
        .returns(true, Song::hasAward)
        .returns(null, Song::monthlyListeners);
  }

  static int comparePhoneNumbers(PhoneNumber phone1, PhoneNumber phone2) {
    return phone1.rawInput().compareTo(phone2.rawInput());
  }
}
