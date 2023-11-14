package io.weaviate.client.base.util;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CrossReferenceTest {

  @Test
  public void testParseBeaconWithClass() {
    // given
    String beacon = "weaviate://localhost/RefClass/f81bfe5e-16ba-4615-a516-46c2ae2e5a80";
    // when
    CrossReference crossRef = CrossReference.fromBeacon(beacon);
    // then
    assertThat(crossRef).isNotNull().satisfies(cf -> {
      assertThat(cf.isLocal()).isTrue();
      assertThat(cf.getPeerName()).isEqualTo("localhost");
      assertThat(cf.getClassName()).isEqualTo("RefClass");
      assertThat(cf.getTargetID()).isEqualTo("f81bfe5e-16ba-4615-a516-46c2ae2e5a80");
    });
  }

  @Test
  public void testParseBeaconWithoutClass() {
    // given
    String beacon = "weaviate://localhost/f81bfe5e-16ba-4615-a516-46c2ae2e5a80";
    // when
    CrossReference crossRef = CrossReference.fromBeacon(beacon);
    // then
    assertThat(crossRef).isNotNull().satisfies(cf -> {
      assertThat(cf.isLocal()).isTrue();
      assertThat(cf.getPeerName()).isEqualTo("localhost");
      assertThat(cf.getClassName()).isEqualTo("");
      assertThat(cf.getTargetID()).isEqualTo("f81bfe5e-16ba-4615-a516-46c2ae2e5a80");
    });
  }

  @Test
  public void testParseBeaconEmpty() {
    // given
    String beacon = "";
    // when
    CrossReference crossRef = CrossReference.fromBeacon(beacon);
    // then
    assertThat(crossRef).isNull();
  }
}
