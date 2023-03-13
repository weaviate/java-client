package io.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Assert;

public class NearObjectArgumentTest extends TestCase {

  public void testBuildWithCertainty() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon").certainty(0.8f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {id: \"id\" beacon: \"beacon\" certainty: 0.8}", arg);
  }

  public void testBuildWithoutCertainity() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon")
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {id: \"id\" beacon: \"beacon\"}", arg);
  }

  public void testBuildWithDistance() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon").distance(0.8f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {id: \"id\" beacon: \"beacon\" distance: 0.8}", arg);
  }

  public void testBuildWithCertaintyAndWithoutId() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .beacon("beacon").certainty(0.4f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {beacon: \"beacon\" certainty: 0.4}", arg);
  }

  public void testBuildWithDistanceAndWithoutId() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .beacon("beacon").distance(0.4f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {beacon: \"beacon\" distance: 0.4}", arg);
  }

  public void testBuildWithCertaintyWithoutBeacon() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").certainty(0.1f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {id: \"id\" certainty: 0.1}", arg);
  }

  public void testBuildWithDistanceWithoutBeacon() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").distance(0.1f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    Assert.assertEquals("nearObject: {id: \"id\" distance: 0.1}", arg);
  }

  public void testBuildWithoutAll() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder().build();
    // when
    String arg = nearObject.build();
    // then
    // builder will return a faulty nearObject arg in order for Weaviate to error
    // so that user will know that something was wrong
    Assert.assertEquals("nearObject: {}", arg);
  }
}
