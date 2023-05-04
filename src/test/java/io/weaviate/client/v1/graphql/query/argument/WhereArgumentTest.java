package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class WhereArgumentTest {

  @Test
  public void testValueText() {
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .valueText("txt")
        .operator(Operator.And)
        .path(new String[]{ "add" })
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
        .path(new String[]{ "add" })
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
        .path(new String[]{ "add" })
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
        .path(new String[]{ "add" })
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
        .path(new String[]{ "add" })
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
            .build()
          )
          .distance(WhereFilter.GeoDistance.builder()
            .max(3000f)
            .build()
          )
          .build()
        )
        .operator(Operator.WithinGeoRange).path(new String[]{ "add" })
        .build())
      .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueGeoRange:{geoCoordinates:{latitude:50.51,longitude:0.11},distance:{max:3000.0}} operator:WithinGeoRange}");
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
        .path(new String[]{ "add" })
        .build())
      .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"add\"] valueDate:\"2023-03-15T17:01:02+00:00\" operator:Like}");
  }

  @Test
  public void testOperands() {
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .operands(new WhereFilter[]{
          WhereFilter.builder()
            .valueInt(10)
            .path(new String[]{ "wordCount" })
            .operator(Operator.LessThanEqual)
            .build(),
          WhereFilter.builder()
            .valueText("word")
            .path(new String[]{ "word" })
            .operator(Operator.LessThan)
            .build(),
        })
        .operator(Operator.And)
        .build())
      .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{operator:And operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"word\"] valueText:\"word\" operator:LessThan}]}");
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
        .path(new String[]{ "p1", "p2", "p3" })
        .build())
      .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{path:[\"p1\",\"p2\",\"p3\"] valueDate:\"2023-03-15T17:01:02+00:00\" operator:Not}");
  }

  @Test
  public void testOperandsWithMultiplePathParams() {
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .operands(new WhereFilter[]{
          WhereFilter.builder()
            .valueInt(10)
            .operator(Operator.LessThanEqual)
            .path(new String[]{ "wordCount" })
            .build(),
          WhereFilter.builder()
            .valueText("word")
            .operator(Operator.LessThan)
            .path(new String[]{ "w1", "w2", "w3" })
            .build(),
        })
        .operator(Operator.NotEqual)
        .build())
      .build();

    String str = where.build();

    assertThat(str).isEqualTo("where:{operator:NotEqual operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"w1\",\"w2\",\"w3\"] valueText:\"word\" operator:LessThan}]}");
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
}
