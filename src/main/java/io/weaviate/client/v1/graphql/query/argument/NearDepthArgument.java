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
public class NearDepthArgument implements Argument {

  String depth;
  File depthFile;
  Float certainty;
  Float distance;
  String[] targetVectors;
  Targets targets;

  @Override
  public String build() {
    return NearMediaArgumentHelper.builder()
      .certainty(certainty)
      .distance(distance)
      .targetVectors(targetVectors)
      .data(depth)
      .dataFile(depthFile)
      .targets(targets)
      .mediaField("depth")
      .mediaName("nearDepth")
      .build().build();
  }
}
