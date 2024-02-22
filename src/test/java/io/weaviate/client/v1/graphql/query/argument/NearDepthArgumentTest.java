package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NearDepthArgumentTest {

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearDepth = NearDepthArgument.builder()
      .depthFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\"}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearDepth = NearDepthArgument.builder()
      .depthFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .certainty(certainty)
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\" certainty:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearDepth = NearDepthArgument.builder()
      .depthFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .distance(distance)
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\" distance:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String depthBase64 = "iVBORw0KGgoAAAANS";

    String nearDepth = NearDepthArgument.builder()
      .depth(depthBase64)
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\"}", depthBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String depthBase64 = "data:image/png;base64,iVBORw0KGgoAAAANS";

    String nearDepth = NearDepthArgument.builder()
      .depth(depthBase64)
      .build().build();

    assertThat(nearDepth).isEqualTo("nearDepth:{depth:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String depthBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearDepth = NearDepthArgument.builder()
      .depth(depthBase64)
      .certainty(certainty)
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\" certainty:%s}", depthBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String depthBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearDepth = NearDepthArgument.builder()
      .depth(depthBase64)
      .distance(distance)
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\" distance:%s}", depthBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String depthBase64 = "iVBORw0KGgoAAAANS";

    String nearDepth = NearDepthArgument.builder()
      .depth(depthBase64)
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearDepth).isEqualTo(String.format("nearDepth:{depth:\"%s\" targetVectors:[\"vector1\"]}", depthBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearDepth = NearDepthArgument.builder()
      .depthFile(badFile)
      .build().build();

    assertThat(nearDepth).isEqualTo("nearDepth:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearDepth = NearDepthArgument.builder()
      .build().build();

    assertThat(nearDepth).isEqualTo("nearDepth:{}");
  }
}
