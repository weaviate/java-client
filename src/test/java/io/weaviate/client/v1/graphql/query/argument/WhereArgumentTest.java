package io.weaviate.client.v1.graphql.query.argument;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;

@RunWith(JParamsTestRunner.class)
public class WhereArgumentTest {

  @Test
  public void testValueText() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueText("txt")
            .operator(Operator.And)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueText:\"txt\" operator:And}");
  }

  @Test
  public void testDeprecatedValueString() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueString("txt")
            .operator(Operator.Equal)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueString:\"txt\" operator:Equal}");
  }

  @Test
  public void testValueInt() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueInt(11)
            .operator(Operator.Or)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueInt:11 operator:Or}");
  }

  @Test
  public void testValueBoolean() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueBoolean(true)
            .operator(Operator.GreaterThan)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueBoolean:true operator:GreaterThan}");
  }

  @Test
  public void testValueNumber() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueNumber(22.1)
            .operator(Operator.GreaterThanEqual)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueNumber:22.1 operator:GreaterThanEqual}");
  }

  @Test
  public void testValueGeoCoordinates() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueGeoRange(WhereFilter.GeoRange.builder()
                .geoCoordinates(WhereFilter.GeoCoordinates.builder()
                    .latitude(50.51f)
                    .longitude(0.11f)
                    .build())
                .distance(WhereFilter.GeoDistance.builder()
                    .max(3000f)
                    .build())
                .build())
            .operator(Operator.WithinGeoRange).path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo(
        "where:{path:[\"add\"] valueGeoRange:{geoCoordinates:{latitude:50.51,longitude:0.11},distance:{max:3000.0}} operator:WithinGeoRange}");
  }

  @Test
  public void testValueDate() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.MARCH, 15, 17, 1, 2);

    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueDate(cal.getTime())
            .operator(Operator.Like)
            .path(new String[] { "add" })
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueDate:\"2023-03-15T17:01:02+00:00\" operator:Like}");
  }

  @Test
  public void testOperands() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .operands(new WhereFilter[] {
                WhereFilter.builder()
                    .valueInt(10)
                    .path(new String[] { "wordCount" })
                    .operator(Operator.LessThanEqual)
                    .build(),
                WhereFilter.builder()
                    .valueText("word")
                    .path(new String[] { "word" })
                    .operator(Operator.LessThan)
                    .build(),
            })
            .operator(Operator.And)
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo(
        "where:{operator:And operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"word\"] valueText:\"word\" operator:LessThan}]}");
  }

  @Test
  public void testMultiplePathParams() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.MARCH, 15, 17, 1, 2);

    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .valueDate(cal.getTime())
            .operator(Operator.Not)
            .path(new String[] { "p1", "p2", "p3" })
            .build())
        .build();

    String str = where.build();

    assertThat(str)
        .isEqualTo("where:{path:[\"p1\",\"p2\",\"p3\"] valueDate:\"2023-03-15T17:01:02+00:00\" operator:Not}");
  }

  @Test
  public void testOperandsWithMultiplePathParams() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder()
            .operands(new WhereFilter[] {
                WhereFilter.builder()
                    .valueInt(10)
                    .operator(Operator.LessThanEqual)
                    .path(new String[] { "wordCount" })
                    .build(),
                WhereFilter.builder()
                    .valueText("word")
                    .operator(Operator.LessThan)
                    .path(new String[] { "w1", "w2", "w3" })
                    .build(),
            })
            .operator(Operator.NotEqual)
            .build())
        .build();

    String str = where.build();

    assertThat(str).isEqualTo(
        "where:{operator:NotEqual operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"w1\",\"w2\",\"w3\"] valueText:\"word\" operator:LessThan}]}");
  }

  @Test
  public void testWithoutAll() {
    WhereArgument where = WhereArgument.builder()
        .filter(WhereFilter.builder().build())
        .build();

    String str = where.build();

    // builder will return a faulty where arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertThat(str).isEqualTo("where:{}");
  }

  @Test
  @DataMethod(source = WhereArgumentTest.class, method = "provideForContains")
  public void shouldCreateWhereForContains(WhereFilter filter, String expectedWhere) {
    String where = WhereArgument.builder().filter(filter).build().build();
    assertThat(where).isEqualTo(expectedWhere);
  }

  public static Object[][] provideForContains() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2023, Calendar.JANUARY, 15, 17, 1, 2);
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2023, Calendar.FEBRUARY, 15, 17, 1, 2);
    Calendar cal3 = Calendar.getInstance();
    cal3.set(2023, Calendar.MARCH, 15, 17, 1, 2);

    return new Object[][] {
        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueBoolean(true)
                .build(),
            "where:{path:[\"some\",\"path\"] valueBoolean:[true] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueBoolean(true, false)
                .build(),
            "where:{path:[\"another_path\"] valueBoolean:[true,false] operator:ContainsAny}",
        },

        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueInt(1)
                .build(),
            "where:{path:[\"some\",\"path\"] valueInt:[1] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueInt(2, 3)
                .build(),
            "where:{path:[\"another_path\"] valueInt:[2,3] operator:ContainsAny}",
        },

        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueNumber(1.1)
                .build(),
            "where:{path:[\"some\",\"path\"] valueNumber:[1.1] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueNumber(2.2, 3.3)
                .build(),
            "where:{path:[\"another_path\"] valueNumber:[2.2,3.3] operator:ContainsAny}",
        },

        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueString("string")
                .build(),
            "where:{path:[\"some\",\"path\"] valueString:[\"string\"] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueString("string1", "string2")
                .build(),
            "where:{path:[\"another_path\"] valueString:[\"string1\",\"string2\"] operator:ContainsAny}",
        },

        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueText("text")
                .build(),
            "where:{path:[\"some\",\"path\"] valueText:[\"text\"] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueText("text1", "text2")
                .build(),
            "where:{path:[\"another_path\"] valueText:[\"text1\",\"text2\"] operator:ContainsAny}",
        },

        {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.ContainsAll)
                .valueDate(cal1.getTime())
                .build(),
            "where:{path:[\"some\",\"path\"] valueDate:[\"2023-01-15T17:01:02+00:00\"] operator:ContainsAll}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsAny)
                .valueDate(cal2.getTime(), cal3.getTime())
                .build(),
            "where:{path:[\"another_path\"] valueDate:[\"2023-02-15T17:01:02+00:00\",\"2023-03-15T17:01:02+00:00\"] operator:ContainsAny}",
        },
        {
            WhereFilter.builder().path("another_path")
                .operator(Operator.ContainsNone)
                .valueDate(cal2.getTime(), cal3.getTime())
                .build(),
            "where:{path:[\"another_path\"] valueDate:[\"2023-02-15T17:01:02+00:00\",\"2023-03-15T17:01:02+00:00\"] operator:ContainsNone}",
        },
    };
  }

  @Test
  @DataMethod(source = WhereArgumentTest.class, method = "provideForSingleValOrArray")
  public void shouldCreateWhereWithSingleValOrArray(WhereFilter filter, String expectedWhere) {
    String where = WhereArgument.builder().filter(filter).build().build();
    assertThat(where).isEqualTo(expectedWhere);
  }

  public static Object[][] provideForSingleValOrArray() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    Calendar cal1 = Calendar.getInstance();
    cal1.set(2023, Calendar.JANUARY, 15, 17, 1, 2);
    Calendar cal2 = Calendar.getInstance();
    cal2.set(2023, Calendar.FEBRUARY, 15, 17, 1, 2);
    Calendar cal3 = Calendar.getInstance();
    cal3.set(2023, Calendar.MARCH, 15, 17, 1, 2);

    return new Object[][] {
        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueBoolean(true)
                .build(),
            "where:{path:[\"some\",\"path\"] valueBoolean:true operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueBoolean(true, false)
                .build(),
            "where:{path:[\"another_path\"] valueBoolean:[true,false] operator:Equal}",
        },

        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueInt(1)
                .build(),
            "where:{path:[\"some\",\"path\"] valueInt:1 operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueInt(2, 3)
                .build(),
            "where:{path:[\"another_path\"] valueInt:[2,3] operator:Equal}",
        },

        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueNumber(1.1)
                .build(),
            "where:{path:[\"some\",\"path\"] valueNumber:1.1 operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueNumber(2.2, 3.3)
                .build(),
            "where:{path:[\"another_path\"] valueNumber:[2.2,3.3] operator:Equal}",
        },

        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueString("string")
                .build(),
            "where:{path:[\"some\",\"path\"] valueString:\"string\" operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueString("string1", "string2")
                .build(),
            "where:{path:[\"another_path\"] valueString:[\"string1\",\"string2\"] operator:Equal}",
        },

        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueText("text")
                .build(),
            "where:{path:[\"some\",\"path\"] valueText:\"text\" operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueText("text1", "text2")
                .build(),
            "where:{path:[\"another_path\"] valueText:[\"text1\",\"text2\"] operator:Equal}",
        },

        new Object[] {
            WhereFilter.builder().path("some", "path")
                .operator(Operator.Equal)
                .valueDate(cal1.getTime())
                .build(),
            "where:{path:[\"some\",\"path\"] valueDate:\"2023-01-15T17:01:02+00:00\" operator:Equal}",
        },
        new Object[] {
            WhereFilter.builder().path("another_path")
                .operator(Operator.Equal)
                .valueDate(cal2.getTime(), cal3.getTime())
                .build(),
            "where:{path:[\"another_path\"] valueDate:[\"2023-02-15T17:01:02+00:00\",\"2023-03-15T17:01:02+00:00\"] operator:Equal}",
        },
    };
  }
}
