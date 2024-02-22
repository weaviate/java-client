package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NearImuArgumentTest {

  @Test
  public void shouldBuildFromFile() throws IOException {
    String nearImu = NearImuArgument.builder()
      .imuFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\"}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64()));
  }

  @Test
  public void shouldBuildFromFileWithCertainty() throws IOException {
    Float certainty = 0.5f;

    String nearImu = NearImuArgument.builder()
      .imuFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .certainty(certainty)
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\" certainty:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), certainty));
  }

  @Test
  public void shouldBuildFromFileWithDistance() throws IOException {
    Float distance = 0.5f;

    String nearImu = NearImuArgument.builder()
      .imuFile(NearMediaArgumentHelperTest.exampleMediaFile())
      .distance(distance)
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\" distance:%s}",
      NearMediaArgumentHelperTest.exampleMediaFileAsBase64(), distance));
  }

  @Test
  public void shouldBuildFromBase64() {
    String imuBase64 = "iVBORw0KGgoAAAANS";

    String nearImu = NearImuArgument.builder()
      .imu(imuBase64)
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\"}", imuBase64));
  }

  @Test
  public void shouldBuildFromBase64WithHeader() {
    String imuBase64 = "data:image/png;base64,iVBORw0KGgoAAAANS";

    String nearImu = NearImuArgument.builder()
      .imu(imuBase64)
      .build().build();

    assertThat(nearImu).isEqualTo("nearIMU:{imu:\"iVBORw0KGgoAAAANS\"}");
  }

  @Test
  public void shouldBuildFromBase64WithCertainty() {
    String imuBase64 = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;

    String nearImu = NearImuArgument.builder()
      .imu(imuBase64)
      .certainty(certainty)
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\" certainty:%s}", imuBase64, certainty));
  }

  @Test
  public void shouldBuildFromBase64WithDistance() {
    String imuBase64 = "iVBORw0KGgoAAAANS";
    Float distance = 0.5f;

    String nearImu = NearImuArgument.builder()
      .imu(imuBase64)
      .distance(distance)
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\" distance:%s}", imuBase64, distance));
  }

  @Test
  public void shouldBuildFromBase64WithTargetVectors() {
    String imuBase64 = "iVBORw0KGgoAAAANS";

    String nearImu = NearImuArgument.builder()
      .imu(imuBase64)
      .targetVectors(new String[]{"vector1"})
      .build().build();

    assertThat(nearImu).isEqualTo(String.format("nearIMU:{imu:\"%s\" targetVectors:[\"vector1\"]}", imuBase64));
  }

  @Test
  public void shouldBuildEmptyDueToBadFile() {
    File badFile = new File("");

    String nearImu = NearImuArgument.builder()
      .imuFile(badFile)
      .build().build();

    assertThat(nearImu).isEqualTo("nearIMU:{}");
  }

  @Test
  public void shouldBuildEmptyDueToNotSet() {
    String nearImu = NearImuArgument.builder()
      .build().build();

    assertThat(nearImu).isEqualTo("nearIMU:{}");
  }
}
