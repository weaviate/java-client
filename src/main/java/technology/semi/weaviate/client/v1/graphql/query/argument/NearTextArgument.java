package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearTextArgument implements Argument {
  String[] concepts;
  Float certainty;
  Float distance;
  NearTextMoveParameters moveTo;
  NearTextMoveParameters moveAwayFrom;
  Boolean autocorrect;

  private String getConcepts(String[] concepts) {
    return Stream.of(concepts)
      .map(f -> String.format("\"%s\"", f))
      .collect(Collectors.joining(", "));
  }

  private String buildMoveParam(String name, NearTextMoveParameters moveParam) {
    Set<String> arg = new LinkedHashSet<>();
    if (ArrayUtils.isNotEmpty(moveParam.getConcepts())) {
      arg.add(String.format("concepts: [%s]", getConcepts(moveParam.getConcepts())));
    }
    if (moveParam.getForce() != null) {
      arg.add(String.format("force: %s", moveParam.getForce()));
    }
    if (ArrayUtils.isNotEmpty(moveParam.getObjects())) {
      String objects = Arrays.stream(moveParam.getObjects())
        .map(this::mapObjectMoveToStringClause).collect(Collectors.joining(","));
      arg.add(String.format("objects: [%s]", objects));
    }
    return String.format("%s: {%s}", name, StringUtils.joinWith(" ", arg.toArray()));
  }

  private String mapObjectMoveToStringClause(NearTextMoveParameters.ObjectMove obj) {
    Set<String> objectsArg = new LinkedHashSet<>();
    if (StringUtils.isNotBlank(obj.getId())) {
      objectsArg.add(String.format("id: \"%s\"", obj.getId()));
    }
    if (StringUtils.isNotBlank(obj.getBeacon())) {
      objectsArg.add(String.format("beacon: \"%s\"", obj.getBeacon()));
    }
    return String.format("{%s}", StringUtils.joinWith(" ", objectsArg.toArray()));
  }

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    if (concepts != null && concepts.length > 0) {
      arg.add(String.format("concepts: [%s]", getConcepts(concepts)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty: %s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance: %s", distance));
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
}
