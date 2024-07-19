package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NearAudioArgumentTest {

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearAudio = NearAudioArgument.builder()
      .audioFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\"}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearAudio = NearAudioArgument.builder()
      .audioFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .certainty(certainty)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" certainty:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearAudio = NearAudioArgument.builder()
      .audioFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .distance(distance)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" distance:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String audioBase64 = "iVBORw0KGgoAAAANS";

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\"}", audioBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String audioBase64 = "data:audio/mp4;base64,iVBORw0KGgoAAAANS";

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .build().build();

    assertThat(nearAudio).isEqualTo("nearAudio:{audio:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String audioBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .certainty(certainty)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" certainty:%s}", audioBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String audioBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .distance(distance)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" distance:%s}", audioBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String audioBase64 = "iVBORw0KGgoAAAANS";

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" targetVectors:[\"vector1\"]}", audioBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearAudio = NearAudioArgument.builder()
      .audioFile(badFile)
      .build().build();

    assertThat(nearAudio).isEqualTo("nearAudio:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearAudio = NearAudioArgument.builder()
      .build().build();

    assertThat(nearAudio).isEqualTo("nearAudio:{}");
  }

  @Test
  public void shouldBuildFromBase64WithTargets() {
    // given
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.minimum)
      .weights(weights)
      .build();
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{"concept"}).targets(targets).build();

    String audioBase64 = "iVBORw0KGgoAAAANS";

    String nearAudio = NearAudioArgument.builder()
      .audio(audioBase64)
      .targets(targets)
      .build().build();

    assertThat(nearAudio).isEqualTo(String.format("nearAudio:{audio:\"%s\" targets:{combinationMethod:minimum targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}", audioBase64));
  }
}
