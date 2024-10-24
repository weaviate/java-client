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
public class Targets {
  CombinationMethod combinationMethod;
  String[] targetVectors;
  Map<String, Float[]> weights;

  public enum CombinationMethod {
    minimum("minimum"),
    average("average"),
    sum("sum"),
    manualWeights("manualWeights"),
    relativeScore("relativeScore");

    private final String type;

    CombinationMethod(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type;
    }
  }

  String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (combinationMethod != null) {
      arg.add(String.format("combinationMethod:%s", combinationMethod.name()));
    }
    if (ArrayUtils.isNotEmpty(targetVectors)) {
      arg.add(String.format("targetVectors:%s", Serializer.arrayWithQuotes(targetVectors)));
    }
    if (weights != null && !weights.isEmpty()) {
      Set<String> weightsArg = new LinkedHashSet<>();
      for (Map.Entry<String, Float[]> e : weights.entrySet()) {
        Float[] weightsPerTarget = e.getValue();
        String target = e.getKey();

        String weight = Serializer.array(weightsPerTarget);
        if (weightsPerTarget.length == 1) {
          weight = weightsPerTarget[0].toString();
        }
        weightsArg.add(String.format("%s:%s", target, weight));
      }
      arg.add(String.format("weights:{%s}", String.join(" ", weightsArg)));
    }

    return String.format("targets:{%s}", String.join(" ", arg));
  }

  // Extend lombok's builder to overload some methods.
  public static class TargetsBuilder {
    Map<String, Float[]> weights = new LinkedHashMap<>();

    public TargetsBuilder weights(Map<String, Float> weights) {
      this.weights.clear(); // We want to overwrite the existing entries each time this is called.
      for (Map.Entry<String, Float> e : weights.entrySet()) {
        this.weights.put(e.getKey(), new Float[]{e.getValue()});
      }
      return this;
    }

    public TargetsBuilder weightsMulti(Map<String, Float[]> weights) {
      this.weights = weights;
      return this;
    }
  }
}

