package technology.semi.weaviate.client.v1.graphql.query.argument;

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
public class NearTextArgument implements Argument {
  String[] concepts;
  Float certainty;
  NearTextMoveParameters moveTo;
  NearTextMoveParameters moveAwayFrom;
  Boolean autocorrect;

  private String getConcepts(String[] concepts) {
    return Stream.of(concepts)
            .map(f -> String.format("\"%s\"", f))
            .collect(Collectors.joining(", "));
  }

  private String buildMoveParam(String name, NearTextMoveParameters moveParam) {
    if (moveParam.getConcepts() != null && moveParam.getConcepts().length > 0) {
      Set<String> arg = new LinkedHashSet<>();
      arg.add(String.format("concepts: [%s]", getConcepts(moveParam.getConcepts())));
      if (moveParam.getForce() != null) {
        arg.add(String.format("force: %s", moveParam.getForce()));
      }
      return String.format("%s: {%s}", name, StringUtils.joinWith(" ", arg.toArray()));
    }
    return "";
  }

  @Override
  public String build() {
    if (concepts != null && concepts.length > 0) {
      Set<String> arg = new LinkedHashSet<>();
      arg.add(String.format("concepts: [%s]", getConcepts(concepts)));
      if (certainty != null) {
        arg.add(String.format("certainty: %s", certainty));
      }
      if (moveTo != null) {
        arg.add(buildMoveParam("moveTo", moveTo));
      }
      if (moveAwayFrom != null) {
        arg.add(buildMoveParam("moveAwayFrom", moveAwayFrom));
      }
      if (autocorrect != null) {
        arg.add(String.format("autocorrect: %s", autocorrect));
      }
      return String.format("nearText: {%s}", StringUtils.joinWith(" ", arg.toArray()));
    }
    return "";
  }
}
