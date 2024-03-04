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
public class NearThermalArgument implements Argument {

  String thermal;
  File thermalFile;
  Float certainty;
  Float distance;
  String[] targetVectors;

  @Override
  public String build() {
    return NearMediaArgumentHelper.builder()
      .certainty(certainty)
      .distance(distance)
      .targetVectors(targetVectors)
      .data(thermal)
      .dataFile(thermalFile)
      .mediaField("thermal")
      .mediaName("nearThermal")
      .build().build();
  }
}
