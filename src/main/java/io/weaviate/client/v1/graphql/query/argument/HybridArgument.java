package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HybridArgument implements Argument {
  String query;
  Float alpha;
  Float maxVectorDistance;
  Float[] vector;
  String fusionType;
  String[] properties;
  String[] targetVectors;
  Searches searches;
  Targets targets;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    arg.add(String.format("query:%s", Serializer.quote(query)));
    if (vector != null) {
      arg.add(String.format("vector:%s", Serializer.array(vector)));
    }
    if (alpha != null) {
      arg.add(String.format("alpha:%s", alpha));
    }
    if (maxVectorDistance != null) {
      arg.add(String.format("maxVectorDistance:%s", maxVectorDistance));
    }
    if (ArrayUtils.isNotEmpty(properties)) {
      arg.add(String.format("properties:%s", Serializer.arrayWithQuotes(properties)));
    }
    if (StringUtils.isNotBlank(fusionType)) {
      arg.add(String.format("fusionType:%s", fusionType));
    }
    if (ArrayUtils.isNotEmpty(targetVectors)) {
      arg.add(String.format("targetVectors:%s", Serializer.arrayWithQuotes(targetVectors)));
    }
    if (searches != null && (searches.nearVector != null || searches.nearText != null)) {
      Set<String> searchesArgs = new LinkedHashSet<>();
      if (searches.nearVector != null) {
        searchesArgs.add(searches.nearVector.build());
      }
      if (searches.nearText != null) {
        searchesArgs.add(searches.nearText.build());
      }
      arg.add(String.format("searches:{%s}", String.join(" ", searchesArgs)));
    }
    if (targets != null) {
      arg.add(String.format("%s", targets.build()));
    }

    return String.format("hybrid:{%s}", String.join(" ", arg));
  }

  @Getter
  @Builder
  @ToString
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class Searches {
    NearVectorArgument nearVector;
    NearTextArgument nearText;
  }
}
