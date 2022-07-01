package technology.semi.weaviate.client.v1.graphql.query.argument;

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
public class NearVectorArgument implements Argument {
  Float[] vector;
  Float certainty;
  Float distance;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    if (vector != null) {
      arg.add(String.format("vector: %s", Arrays.toString(vector)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty: %s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance: %s", distance));
    }
    return String.format("nearVector: {%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
