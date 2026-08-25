package io.weaviate.client6.v1.internal;

import java.time.OffsetDateTime;
import java.util.List;
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

/**
 * Timestamps must reach the server as RFC 3339, which requires the seconds.
 *
 * <p>
 * {@link OffsetDateTime#toString()} omits them when the second and the
 * nanosecond are both zero, so every timestamp on an exact minute boundary used
 * to be rejected by the server. The existing date tests all seed from
 * {@code OffsetDateTime.now()}, which practically never lands on one -- hence
 * the literals here.
 */
@RunWith(JParamsTestRunner.class)
public class Rfc3339DateTest {
  /** The value that used to serialize as "2024-03-01T00:00Z". */
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

  /** Whatever we write has to be readable again. */
  @Name("{0}")
  @DataMethod(source = Rfc3339DateTest.class, method = "timestamps")
  @Test
  public void test_roundTrip(String __, OffsetDateTime value, String ___) {
    Assertions.assertThat(DateUtil.fromISO8601(DateUtil.toRFC3339(value))).isEqualTo(value);
  }

  /** The reader stays lenient, so dates written by older clients still parse. */
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
        // The metadata filters take OffsetDateTime only -- no String overload to fall
        // back on, so these had no workaround at all.
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

  private static WeaviateProtoBase.Filters marshal(Filter filter) {
    var builder = WeaviateProtoBase.Filters.newBuilder();
    filter.appendTo(builder);
    return builder.build();
  }
}
