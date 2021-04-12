package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearTextArgument implements Argument {
  String[] concepts;
  Float certainty;
  NearTextMoveParameters moveTo;
  NearTextMoveParameters moveAwayFrom;

  private String getConcepts(String[] concepts) {
    return Stream.of(concepts)
            .map(f -> String.format("\"%s\"", f))
            .collect(Collectors.joining(", "));
  }

  private String buildMoveParam(String name, NearTextMoveParameters moveParam) {
    if (moveParam.getConcepts() != null && moveParam.getConcepts().length > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("concepts: [%s] ", getConcepts(moveParam.getConcepts())));
      if (moveParam.getForce() != null) {
        sb.append(String.format("force: %s", moveParam.getForce()));
      }
      return String.format("%s: {%s} ", name, sb);
    }
    return "";
  }

  @Override
  public String build() {
    if (concepts != null && concepts.length > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("concepts: [%s] ", getConcepts(concepts)));
      if (certainty != null) {
        sb.append(String.format("certainty: %s ", certainty));
      }
      if (moveTo != null) {
        sb.append(buildMoveParam("moveTo", moveTo));
      }
      if (moveAwayFrom != null) {
        sb.append(buildMoveParam("moveAwayFrom", moveAwayFrom));
      }
      return String.format("nearText: {%s}", sb);
    }
    return "";
  }
}
