package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AskArgument implements Argument {
  String question;
  String[] properties;
  Float certainty;

  @Override
  public String build() {
    if (StringUtils.isNotBlank(question)) {
      Set<String> arg = new LinkedHashSet<>();
      arg.add(String.format("question: \"%s\"", question));
      if (properties != null && properties.length > 0) {
        String props = Stream.of(properties)
                .map(f -> String.format("\"%s\"", f))
                .collect(Collectors.joining(", "));
        arg.add(String.format("properties: [%s]", props));
      }
      if (certainty != null) {
        arg.add(String.format("certainty: %s", certainty));
      }
      return String.format("ask: {%s}", StringUtils.joinWith(" ", arg.toArray()));
    }
    return "";
  }
}
