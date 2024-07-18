package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashMap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class NearMediaArgumentHelperTest {

  public static File exampleMediaFile() {
    return new File("src/test/resources/image/pixel.png");
  }

  public static String exampleMediaFileAsBase64() throws IOException {
    Path path = Paths.get("src/test/resources/image/base64.txt");
    return String.join("\n", Files.readAllLines(path));
  }

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearMedia = NearMediaArgumentHelper.builder()
      .dataFile(exampleMediaFile())
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\"}", exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearMedia = NearMediaArgumentHelper.builder()
      .dataFile(exampleMediaFile())
      .certainty(certainty)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" certainty:%s}", exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearMedia = NearMediaArgumentHelper.builder()
      .dataFile(exampleMediaFile())
      .distance(distance)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" distance:%s}", exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String mediaBase64 = "iVBORw0KGgoAAAANS";

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\"}", mediaBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String mediaBase64 = "data:image/png;base64,iVBORw0KGgoAAAANS";

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo("nearMedia:{media:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String mediaBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .certainty(certainty)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" certainty:%s}", mediaBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String mediaBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .distance(distance)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" distance:%s}", mediaBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String mediaBase64 = "iVBORw0KGgoAAAANS";

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .targetVectors(new String[]{"vector1"})
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" targetVectors:[\"vector1\"]}", mediaBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearMedia = NearMediaArgumentHelper.builder()
      .dataFile(badFile)
      .mediaField("media")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo("nearMedia:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearMedia = NearMediaArgumentHelper.builder()
      .mediaField("whatever")
      .mediaName("nearMedia")
      .build().build();

    assertThat(nearMedia).isEqualTo("nearMedia:{}");
  }

  @Test
  public void shouldBuildFromBase64WithTargets() {
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.manualWeights)
      .weights(weights)
      .build();

    String mediaBase64 = "iVBORw0KGgoAAAANS";

    String nearMedia = NearMediaArgumentHelper.builder()
      .data(mediaBase64)
      .mediaField("media")
      .mediaName("nearMedia")
      .targets(targets)
      .build().build();

    assertThat(nearMedia).isEqualTo(String.format("nearMedia:{media:\"%s\" targets:{combinationMethod:manualWeights targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}", mediaBase64));
  }
}
