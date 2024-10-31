package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
      arg.add(String.format("%s", withValidTargetVectors(this.targets).build()));
    }

    return String.format("nearVector:{%s}", String.join(" ", arg));
  }

  /**
   * withValidTargetVectors makes sure the target names are repeated for each target vector,
   * which is required by server, but may be easily overlooked by the user.
   *
   * <p>
   * Note, too, that in case the user fails to pass a value in targetVectors altogether, it will not be added to the query.
   *
   * @return A copy of the Targets with validated target vectors.
   */
  private Targets withValidTargetVectors(Targets targets) {
    return Targets.builder().
      combinationMethod(targets.getCombinationMethod()).
      weightsMulti(targets.getWeights()).
      targetVectors(prepareTargetVectors(targets.getTargetVectors())).
      build();
  }

  /**
   * prepareTargetVectors adds appends the target name for each target vector associated with it.
   */
  private String[] prepareTargetVectors(String[] targets) {
    List<String> out = new ArrayList<>();
    for (String target : targets) {
      if (this.vectorsPerTarget.containsKey(target)) {
        int l = this.vectorsPerTarget.get(target).length;
        for (int i = 0; i < l; i++) {
          out.add(target);
        }
      } else {
        out.add(target);
      }
    }
    return out.toArray(new String[0]);
  }

  // Extend Lombok's builder to overload some methods.
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
