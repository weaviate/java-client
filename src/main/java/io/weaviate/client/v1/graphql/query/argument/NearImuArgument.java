package io.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.File;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearImuArgument implements Argument {

  String imu;
  File imuFile;
  Float certainty;
  Float distance;

  @Override
  public String build() {
    return NearMediaArgumentHelper.builder()
      .certainty(certainty)
      .distance(distance)
      .data(imu)
      .dataFile(imuFile)
      .mediaField("imu")
      .mediaName("nearIMU")
      .build().build();
  }
}
