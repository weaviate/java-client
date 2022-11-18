package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AskArgument implements Argument {
  String question;
  String[] properties;
  Float certainty;
  Float distance;
  Boolean autocorrect;
  Boolean rerank;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    if (StringUtils.isNotBlank(question)) {
      arg.add(String.format("question: \"%s\"", question));
    }
    if (properties != null && properties.length > 0) {
      String props = Stream.of(properties)
              .map(f -> String.format("\"%s\"", f))
              .collect(Collectors.joining(", "));
      arg.add(String.format("properties: [%s]", props));
    }
    if (certainty != null) {
      arg.add(String.format("certainty: %s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance: %s", distance));
    }
    if (autocorrect != null) {
      arg.add(String.format("autocorrect: %s", autocorrect));
    }
    if (rerank != null) {
      arg.add(String.format("rerank: %s", rerank));
    }
    return String.format("ask: {%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
