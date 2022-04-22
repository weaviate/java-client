package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SortArgument implements Argument {
  String[] path;
  SortOrder order;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    if (ArrayUtils.isNotEmpty(path)) {
      arg.add(String.format("path:[%s]", Arrays.stream(path).map(s -> String.format("\"%s\"", s)).collect(Collectors.joining(","))));
    }
    if (order != null) {
      arg.add(String.format("order:%s", order));
    }
    return String.format("{%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
