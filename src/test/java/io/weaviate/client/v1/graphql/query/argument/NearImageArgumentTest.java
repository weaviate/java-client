package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NearImageArgumentTest {

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearImage = NearImageArgument.builder()
      .imageFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\"}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearImage = NearImageArgument.builder()
      .imageFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .certainty(certainty)
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\" certainty:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearImage = NearImageArgument.builder()
      .imageFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .distance(distance)
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\" distance:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String imageBase64 = "iVBORw0KGgoAAAANS";

    String nearImage = NearImageArgument.builder()
      .image(imageBase64)
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\"}", imageBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String imageBase64 = "data:image/mp4;base64,iVBORw0KGgoAAAANS";

    String nearImage = NearImageArgument.builder()
      .image(imageBase64)
      .build().build();

    assertThat(nearImage).isEqualTo("nearImage:{image:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String imageBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearImage = NearImageArgument.builder()
      .image(imageBase64)
      .certainty(certainty)
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\" certainty:%s}", imageBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String imageBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearImage = NearImageArgument.builder()
      .image(imageBase64)
      .distance(distance)
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\" distance:%s}", imageBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String imageBase64 = "iVBORw0KGgoAAAANS";

    String nearImage = NearImageArgument.builder()
      .image(imageBase64)
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearImage).isEqualTo(String.format("nearImage:{image:\"%s\" targetVectors:[\"vector1\"]}", imageBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearImage = NearImageArgument.builder()
      .imageFile(badFile)
      .build().build();

    assertThat(nearImage).isEqualTo("nearImage:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearImage = NearImageArgument.builder()
      .build().build();

    assertThat(nearImage).isEqualTo("nearImage:{}");
  }
}
