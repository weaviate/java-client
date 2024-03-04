package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class NearObjectArgumentTest {

  @Test
  public void testBuildWithCertainty() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon").certainty(0.8f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{id:\"id\" beacon:\"beacon\" certainty:0.8}", arg);
  }

  @Test
  public void testBuildWithoutCertainity() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon")
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{id:\"id\" beacon:\"beacon\"}", arg);
  }

  @Test
  public void testBuildWithDistance() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").beacon("beacon").distance(0.8f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{id:\"id\" beacon:\"beacon\" distance:0.8}", arg);
  }

  @Test
  public void testBuildWithCertaintyAndWithoutId() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .beacon("beacon").certainty(0.4f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{beacon:\"beacon\" certainty:0.4}", arg);
  }

  @Test
  public void testBuildWithDistanceAndWithoutId() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .beacon("beacon").distance(0.4f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{beacon:\"beacon\" distance:0.4}", arg);
  }

  @Test
  public void testBuildWithCertaintyWithoutBeacon() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").certainty(0.1f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{id:\"id\" certainty:0.1}", arg);
  }

  @Test
  public void testBuildWithDistanceWithoutBeacon() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder()
            .id("id").distance(0.1f)
            .build();
    // when
    String arg = nearObject.build();
    // then
    assertEquals("nearObject:{id:\"id\" distance:0.1}", arg);
  }

  @Test
  public void testBuildWithoutAll() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder().build();
    // when
    String arg = nearObject.build();
    // then
    // builder will return a faulty nearObject arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearObject:{}", arg);
  }

  @Test
  public void shouldBuildWithTargetVectors() {
    String nearObject = NearObjectArgument.builder()
      .id("id")
      .beacon("beacon")
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearObject).isEqualTo("nearObject:{id:\"id\" beacon:\"beacon\" targetVectors:[\"vector1\"]}");
  }
}
