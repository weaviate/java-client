package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class GeoCoordinatesParameterTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    Float latitude = 51.51f;
    Float longitude = -0.09f;
    Float maxDistance = 2000f;
    GeoCoordinatesParameter param = GeoCoordinatesParameter.builder().latitude(latitude).longitude(longitude).maxDistance(maxDistance).build();
    String expected = "{geoCoordinates:{latitude:51.51,longitude:-0.09},distance:{max:2000.0}}";
    // when
    String geoFilter = param.build();
    // then
    Assert.assertEquals(expected, geoFilter);
  }
}