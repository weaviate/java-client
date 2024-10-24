package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearVectorArgument implements Argument {
  Float[] vector;
  Float certainty;
  Float distance;
  String[] targetVectors;
  Map<String, Float[][]> vectorsPerTarget;
  Targets targets;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (vector != null) {
      arg.add(String.format("vector:%s", Serializer.array(vector)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty:%s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance:%s", distance));
    }
    if (ArrayUtils.isNotEmpty(targetVectors)) {
      arg.add(String.format("targetVectors:%s", Serializer.arrayWithQuotes(targetVectors)));
    }
    if (vectorsPerTarget != null && !vectorsPerTarget.isEmpty()) {
      Set<String> vectorPerTargetArg = new LinkedHashSet<>();
      for (Map.Entry<String, Float[][]> e : vectorsPerTarget.entrySet()) {
        Float[][] vectors = e.getValue();
        vectorPerTargetArg.add(String.format("%s:%s", e.getKey(), vectors.length == 1 ? Serializer.array(vectors[0]) : Serializer.array(vectors)));
      }
      arg.add(String.format("vectorPerTarget:{%s}", String.join(" ", vectorPerTargetArg)));
    }
    if (targets != null) {
      arg.add(String.format("%s", targets.build()));
    }

    return String.format("nearVector:{%s}", String.join(" ", arg));
  }

  // Extend lombok's builder to overload some methods.
  public static class NearVectorArgumentBuilder {
    Map<String, Float[][]> vectorsPerTarget = new LinkedHashMap<>();

    public NearVectorArgumentBuilder vectorPerTarget(Map<String, Float[]> vectors) {
      this.vectorsPerTarget.clear(); // Overwrite the existing entries each time this is called.
      for (Map.Entry<String, Float[]> e : vectors.entrySet()) {
        this.vectorsPerTarget.put(e.getKey(), new Float[][]{e.getValue()});
      }
      return this;
    }

    public NearVectorArgumentBuilder vectorsPerTarget(Map<String, Float[][]> vectors) {
      this.vectorsPerTarget = vectors;
      return this;
    }
  }
}
