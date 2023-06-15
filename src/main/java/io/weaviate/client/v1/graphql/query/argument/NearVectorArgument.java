package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearVectorArgument implements Argument {
  Float[] vector;
  Float certainty;
  Float distance;

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

    return String.format("nearVector:{%s}", String.join(" ", arg));
  }
}
