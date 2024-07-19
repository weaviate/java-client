package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NearVideoArgumentTest {

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearVideo = NearVideoArgument.builder()
      .videoFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\"}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearVideo = NearVideoArgument.builder()
      .videoFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .certainty(certainty)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" certainty:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearVideo = NearVideoArgument.builder()
      .videoFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .distance(distance)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" distance:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String videoBase64 = "iVBORw0KGgoAAAANS";

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\"}", videoBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String videoBase64 = "data:video/quicktime;base64,iVBORw0KGgoAAAANS";

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .build().build();

    assertThat(nearVideo).isEqualTo("nearVideo:{video:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String videoBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .certainty(certainty)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" certainty:%s}", videoBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String videoBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .distance(distance)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" distance:%s}", videoBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String videoBase64 = "iVBORw0KGgoAAAANS";

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" targetVectors:[\"vector1\"]}", videoBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearVideo = NearVideoArgument.builder()
      .videoFile(badFile)
      .build().build();

    assertThat(nearVideo).isEqualTo("nearVideo:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearVideo = NearVideoArgument.builder()
      .build().build();

    assertThat(nearVideo).isEqualTo("nearVideo:{}");
  }

  @Test
  public void shouldBuildFromBase64WithTargets() {
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.relativeScore)
      .weights(weights)
      .build();

    String videoBase64 = "iVBORw0KGgoAAAANS";

    String nearVideo = NearVideoArgument.builder()
      .video(videoBase64)
      .targets(targets)
      .build().build();

    assertThat(nearVideo).isEqualTo(String.format("nearVideo:{video:\"%s\" targets:{combinationMethod:relativeScore targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}", videoBase64));
  }
}
