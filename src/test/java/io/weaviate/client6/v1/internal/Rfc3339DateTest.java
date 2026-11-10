package io.weaviate.client6.v1.internal;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;
import com.jparams.junit4.description.Name;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.InsertManyRequest;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

@RunWith(JParamsTestRunner.class)
public class Rfc3339DateTest {
  private static final OffsetDateTime ROUND = OffsetDateTime.parse("2024-03-01T00:00:00Z");
  private static final String ROUND_RFC3339 = "2024-03-01T00:00:00Z";

  public static Object[][] timestamps() {
    return new Object[][] {
        { "minute boundary", ROUND, ROUND_RFC3339 },
        { "zero nanos only", OffsetDateTime.parse("2024-03-01T00:00:30Z"), "2024-03-01T00:00:30Z" },
        { "millis", OffsetDateTime.parse("2024-03-01T12:34:56.789Z"), "2024-03-01T12:34:56.789Z" },
        { "nanos", OffsetDateTime.parse("2024-03-01T12:34:56.000000001Z"), "2024-03-01T12:34:56.000000001Z" },
        { "non-UTC offset", OffsetDateTime.parse("2024-03-01T00:00:00+02:00"), "2024-03-01T00:00:00+02:00" },
        { "negative offset", OffsetDateTime.parse("2024-03-01T00:00:00-05:30"), "2024-03-01T00:00:00-05:30" },
    };
  }

  @Name("{0}")
  @DataMethod(source = Rfc3339DateTest.class, method = "timestamps")
  @Test
  public void test_format(String __, OffsetDateTime value, String want) {
    Assertions.assertThat(DateUtil.toRFC3339(value)).isEqualTo(want);
  }

  @Name("{0}")
  @DataMethod(source = Rfc3339DateTest.class, method = "timestamps")
  @Test
  public void test_roundTrip(String __, OffsetDateTime value, String ___) {
    Assertions.assertThat(DateUtil.fromISO8601(DateUtil.toRFC3339(value))).isEqualTo(value);
  }

  @Test
  public void test_readsTheOldTruncatedForm() {
    Assertions.assertThat(DateUtil.fromISO8601("2024-03-01T00:00Z")).isEqualTo(ROUND);
  }

  public static Object[][] comparisons() {
    return new Object[][] {
        { "eq", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").eq(v) },
        { "ne", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").ne(v) },
        { "lt", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").lt(v) },
        { "lte", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").lte(v) },
        { "gt", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").gt(v) },
        { "gte", (Function<OffsetDateTime, Filter>) v -> Filter.property("when").gte(v) },
        { "createdAt", (Function<OffsetDateTime, Filter>) v -> Filter.createdAt().gt(v) },
        { "lastUpdatedAt", (Function<OffsetDateTime, Filter>) v -> Filter.lastUpdatedAt().lt(v) },
    };
  }

  @Name("{0}")
  @DataMethod(source = Rfc3339DateTest.class, method = "comparisons")
  @Test
  public void test_filterOperandKeepsSeconds(String __, Function<OffsetDateTime, Filter> build) {
    Assertions.assertThat(marshal(build.apply(ROUND)).getValueText()).isEqualTo(ROUND_RFC3339);
  }

  @Test
  public void test_filterArrayOperandKeepsSeconds() {
    var filter = Filter.property("when").containsAny(ROUND, OffsetDateTime.parse("2024-03-01T00:00:01Z"));

    Assertions.assertThat(marshal(filter).getValueTextArray().getValuesList())
        .containsExactly(ROUND_RFC3339, "2024-03-01T00:00:01Z");
  }

  @Test
  public void test_insertManyKeepsSeconds() {
    var properties = Map.<String, Object>of(
        "scalar", ROUND,
        "list", List.of(ROUND),
        "array", new OffsetDateTime[] { ROUND });

    var fields = InsertManyRequest.buildObject(
        WeaviateObject.<Map<String, Object>>of(o -> o.properties(properties)),
        CollectionDescriptor.ofMap("Things"),
        new CollectionHandleDefaults(Optional.empty(), Optional.empty()))
        .getProperties().getNonRefProperties().getFieldsMap();

    Assertions.assertThat(fields.get("scalar").getStringValue()).as("scalar").isEqualTo(ROUND_RFC3339);
    Assertions.assertThat(fields.get("list").getListValue().getValues(0).getStringValue())
        .as("list").isEqualTo(ROUND_RFC3339);
    Assertions.assertThat(fields.get("array").getListValue().getValues(0).getStringValue())
        .as("array").isEqualTo(ROUND_RFC3339);
  }

  @Test
  public void test_digitsAreAsciiInAnyLocale() {
    var previous = Locale.getDefault(Locale.Category.FORMAT);
    try {
      for (var tag : List.of("hi-IN-u-nu-deva", "ar-EG-u-nu-arab", "bn-BD-u-nu-beng")) {
        Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag(tag));
        Assertions.assertThat(DateUtil.toRFC3339(ROUND)).as(tag).isEqualTo(ROUND_RFC3339);
      }
    } finally {
      Locale.setDefault(Locale.Category.FORMAT, previous);
    }
  }

  private static WeaviateProtoBase.Filters marshal(Filter filter) {
    var builder = WeaviateProtoBase.Filters.newBuilder();
    filter.appendTo(builder);
    return builder.build();
  }
}
