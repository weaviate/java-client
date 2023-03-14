package io.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HybridArgument implements Argument {
  String query;
  Float alpha;
  Float[] vector;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    arg.add(String.format("query: \"%s\"", query));
    if (vector != null) {
      arg.add(String.format("vector: %s", Arrays.toString(vector)));
    }
    if (alpha != null) {
      arg.add(String.format("alpha: %s", alpha));
    }
    return String.format("hybrid: {%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
