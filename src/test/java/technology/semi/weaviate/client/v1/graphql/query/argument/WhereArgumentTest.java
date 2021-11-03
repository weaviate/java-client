package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Date;
import junit.framework.TestCase;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Assert;
import org.junit.Test;

public class WhereArgumentTest extends TestCase {

  @Test
  public void testValueText() {
    // given
    String expected = "where:{path:[\"add\"] valueText:\"txt\" operator:And}";
    WhereArgument arg = WhereArgument.builder()
            .valueText("txt").operator(WhereOperator.And).path(new String[]{"add"}).
            build();
    // when
    String valueText = arg.build();
    // then
    Assert.assertEquals(expected, valueText);
  }

  @Test
  public void testValueString() {
    // given
    String expected = "where:{path:[\"add\"] valueString:\"txt\" operator:Equal}";
    WhereArgument arg = WhereArgument.builder()
            .valueString("txt").operator(WhereOperator.Equal).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueInt() {
    // given
    String expected = "where:{path:[\"add\"] valueInt:11 operator:Or}";
    WhereArgument arg = WhereArgument.builder()
            .valueInt(11).operator(WhereOperator.Or).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueBoolean() {
    // given
    String expected = "where:{path:[\"add\"] valueBoolean:true operator:GreaterThan}";
    WhereArgument arg = WhereArgument.builder()
            .valueBoolean(true).operator(WhereOperator.GreaterThan).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueNumber() {
    // given
    String expected = "where:{path:[\"add\"] valueNumber:22.1 operator:GreaterThanEqual}";
    WhereArgument arg = WhereArgument.builder()
            .valueNumber(22.1).operator(WhereOperator.GreaterThanEqual).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueGeoCoordinates() {
    // given
    GeoCoordinatesParameter geo = GeoCoordinatesParameter.builder().latitude(50.51f).longitude(0.11f).maxDistance(3000f).build();
    String expected = "where:{path:[\"add\"] valueGeoRange:{geoCoordinates:{latitude:50.51,longitude:0.11},distance:{max:3000.0}} operator:WithinGeoRange}";
    WhereArgument arg = WhereArgument.builder()
            .valueGeoRange(geo).operator(WhereOperator.WithinGeoRange).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testValueDate() {
    // given
    Date d = new Date();
    String formatted = DateFormatUtils.format(d, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    String expected = String.format("where:{path:[\"add\"] valueDate:%s operator:Like}", formatted);
    WhereArgument arg = WhereArgument.builder()
            .valueDate(d).operator(WhereOperator.Like).path(new String[]{"add"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testOperands() {
    // given
    WhereFilter operand1 = WhereFilter.builder()
            .valueInt(10).path(new String[]{ "wordCount" }).operator(WhereOperator.LessThanEqual).build();
    WhereFilter operand2 = WhereFilter.builder()
            .valueString("word").path(new String[]{ "word" }).operator(WhereOperator.LessThan).build();
    String expected = "where:{operator:And operands:[{operator:LessThanEqual path:[\"wordCount\"] valueInt:10},{operator:LessThan path:[\"word\"] valueString:\"word\"}]}";
    WhereArgument arg = WhereArgument.builder()
            .operands(new WhereFilter[]{operand1, operand2}).operator(WhereOperator.And).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testMultiplePathParams() {
    // given
    Date d = new Date();
    String formatted = DateFormatUtils.format(d, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
    String expected = String.format("where:{path:[\"p1\",\"p2\",\"p3\"] valueDate:%s operator:Not}", formatted);
    WhereArgument arg = WhereArgument.builder()
            .valueDate(d).operator(WhereOperator.Not).path(new String[]{"p1","p2","p3"}).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }

  @Test
  public void testOperandsWithMultiplePathParams() {
    // given
    WhereFilter operand1 = WhereFilter.builder()
            .valueInt(10).path(new String[]{ "wordCount" }).operator(WhereOperator.LessThanEqual).build();
    WhereFilter operand2 = WhereFilter.builder()
            .valueString("word").path(new String[]{ "w1", "w2", "w3" }).operator(WhereOperator.LessThan).build();
    String expected = "where:{operator:NotEqual operands:[{operator:LessThanEqual path:[\"wordCount\"] valueInt:10},{operator:LessThan path:[\"w1\",\"w2\",\"w3\"] valueString:\"word\"}]}";
    WhereArgument arg = WhereArgument.builder()
            .operands(new WhereFilter[]{operand1, operand2}).operator(WhereOperator.NotEqual).
            build();
    // when
    String whereFilter = arg.build();
    // then
    Assert.assertEquals(expected, whereFilter);
  }
}