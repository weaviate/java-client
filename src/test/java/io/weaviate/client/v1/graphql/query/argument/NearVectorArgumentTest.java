package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class NearVectorArgumentTest {

  @Test
  public void testBuildWithCertainty() {
    // given
    NearVectorArgument nearVector = NearVectorArgument.builder()
      .vector(new Float[]{1f, 2f, 3f}).certainty(0.8f).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0] certainty:0.8}", arg);
  }

  @Test
  public void testBuildWithDistance() {
    // given
    NearVectorArgument nearVector = NearVectorArgument.builder()
      .vector(new Float[]{1f, 2f, 3f}).distance(0.8f).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0] distance:0.8}", arg);
  }

  @Test
  public void testBuildWithNoCertainty() {
    NearVectorArgument nearVector = NearVectorArgument.builder()
      .vector(new Float[]{1f, 2f, 3f}).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0]}", arg);
  }

  @Test
  public void shouldBuildWithTargetVectors() {
    String nearVector = NearVectorArgument.builder()
      .vector(new Float[]{1f, 2f, 3f})
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearVector).isEqualTo("nearVector:{vector:[1.0,2.0,3.0] targetVectors:[\"vector1\"]}");
  }
}
