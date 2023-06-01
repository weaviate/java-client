package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashSet;
import java.util.Set;

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
  Float[] vector;

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

    return String.format("hybrid:{%s}", String.join(" ", arg));
  }
}
