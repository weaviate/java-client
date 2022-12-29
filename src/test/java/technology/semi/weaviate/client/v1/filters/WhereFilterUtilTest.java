package technology.semi.weaviate.client.v1.filters;

import java.util.Date;
import junit.framework.TestCase;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Assert;
import org.junit.Test;

public class WhereFilterUtilTest extends TestCase {

  @Test
  public void testValueText() {
    // given
    String expected = "where:{path:[\"add\"] valueText:\"txt\" operator:And}";
    WhereFilter where = WhereFilter.builder()
      .valueText("txt").operator(Operator.And).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueString() {
    // given
    String expected = "where:{path:[\"add\"] valueString:\"txt\" operator:Equal}";
    WhereFilter where = WhereFilter.builder()
      .valueString("txt").operator(Operator.Equal).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueInt() {
    // given
    String expected = "where:{path:[\"add\"] valueInt:11 operator:Or}";
    WhereFilter where = WhereFilter.builder()
      .valueInt(11).operator(Operator.Or).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueBoolean() {
    // given
    String expected = "where:{path:[\"add\"] valueBoolean:true operator:GreaterThan}";
    WhereFilter where = WhereFilter.builder()
      .valueBoolean(true).operator(Operator.GreaterThan).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueNumber() {
    // given
    String expected = "where:{path:[\"add\"] valueNumber:22.1 operator:GreaterThanEqual}";
    WhereFilter where = WhereFilter.builder()
      .valueNumber(22.1).operator(Operator.GreaterThanEqual).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueGeoCoordinates() {
    // given
    String expected = "where:{path:[\"add\"] valueGeoRange:{geoCoordinates:{latitude:50.51,longitude:0.11},distance:{max:3000.0}} operator:WithinGeoRange}";
    WhereFilter where = WhereFilter.builder()
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
      .operator(Operator.WithinGeoRange).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueDate() {
    // given
    Date date = new Date();
    String formatted = DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    String expected = String.format("where:{path:[\"add\"] valueDate:\"%s\" operator:Like}", formatted);
    WhereFilter where = WhereFilter.builder()
      .valueDate(date).operator(Operator.Like).path(new String[]{"add"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testOperands() {
    // given
    String expected = "where:{operator:And operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"word\"] valueString:\"word\" operator:LessThan}]}";
    WhereFilter where = WhereFilter.builder()
      .operands(new WhereFilter[]{
        WhereFilter.builder()
          .valueInt(10).path(new String[]{"wordCount"}).operator(Operator.LessThanEqual)
          .build(),
        WhereFilter.builder()
          .valueString("word").path(new String[]{"word"}).operator(Operator.LessThan)
          .build(),
      })
      .operator(Operator.And)
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testMultiplePathParams() {
    // given
    Date date = new Date();
    String formatted = DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    String expected = String.format("where:{path:[\"p1\",\"p2\",\"p3\"] valueDate:\"%s\" operator:Not}", formatted);
    WhereFilter where = WhereFilter.builder()
      .valueDate(date).operator(Operator.Not).path(new String[]{"p1", "p2", "p3"})
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testOperandsWithMultiplePathParams() {
    // given
    String expected = "where:{operator:NotEqual operands:[{path:[\"wordCount\"] valueInt:10 operator:LessThanEqual},{path:[\"w1\",\"w2\",\"w3\"] valueString:\"word\" operator:LessThan}]}";
    WhereFilter where = WhereFilter.builder()
      .operands(new WhereFilter[]{
        WhereFilter.builder()
          .valueInt(10).operator(Operator.LessThanEqual).path(new String[]{"wordCount"})
          .build(),
        WhereFilter.builder()
          .valueString("word").operator(Operator.LessThan).path(new String[]{"w1", "w2", "w3"})
          .build(),
      })
      .operator(Operator.NotEqual)
      .build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testWithoutAll() {
    // given
    WhereFilter where = WhereFilter.builder().build();
    // when
    String whereFilter = WhereFilterUtil.toGraphQLString(where);
    // then
    // builder will return a faulty where arg in order for Weaviate to error
    // so that user will know that something was wrong
    Assert.assertEquals("where:{}", whereFilter);
  }
}
