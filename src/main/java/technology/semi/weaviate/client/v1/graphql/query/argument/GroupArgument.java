package technology.semi.weaviate.client.v1.graphql.query.argument;

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
public class GroupArgument implements Argument {
  GroupType type;
  Float force;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    if (type != null) {
      arg.add(String.format("type: %s", type));
    }
    if (force != null) {
      arg.add(String.format("force: %s", force));
    }
    if (arg.size() > 0) {
      return String.format("group:{%s}", StringUtils.joinWith(" ", arg.toArray()));
    }
    return "";
  }
}
