package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearTextArgument implements Argument {
  String[] concepts;
  Float certainty;
  Float distance;
  NearTextMoveParameters moveTo;
  NearTextMoveParameters moveAwayFrom;
  Boolean autocorrect;

  private String buildMoveParam(String name, NearTextMoveParameters moveParam) {
    Set<String> arg = new LinkedHashSet<>();

    if (ArrayUtils.isNotEmpty(moveParam.getConcepts())) {
      arg.add(String.format("concepts:%s", Serializer.arrayWithQuotes(moveParam.getConcepts())));
    }
    if (moveParam.getForce() != null) {
      arg.add(String.format("force:%s", moveParam.getForce()));
    }
    if (ArrayUtils.isNotEmpty(moveParam.getObjects())) {
      arg.add(String.format("objects:%s", Serializer.array(moveParam.getObjects(), this::mapObjectMoveToStringClause)));
    }

    return String.format("%s:{%s}", name, String.join(" ", arg));
  }

  private String mapObjectMoveToStringClause(NearTextMoveParameters.ObjectMove obj) {
    Set<String> objectsArg = new LinkedHashSet<>();

    if (StringUtils.isNotBlank(obj.getId())) {
      objectsArg.add(String.format("id:%s", Serializer.quote(obj.getId())));
    }
    if (StringUtils.isNotBlank(obj.getBeacon())) {
      objectsArg.add(String.format("beacon:%s", Serializer.quote(obj.getBeacon())));
    }

    return String.format("{%s}", String.join(" ", objectsArg));
  }

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (ArrayUtils.isNotEmpty(concepts)) {
      arg.add(String.format("concepts:%s", Serializer.arrayWithQuotes(concepts)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty:%s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance:%s", distance));
    }
    if (moveTo != null) {
      arg.add(buildMoveParam("moveTo", moveTo));
    }
    if (moveAwayFrom != null) {
      arg.add(buildMoveParam("moveAwayFrom", moveAwayFrom));
    }
    if (autocorrect != null) {
      arg.add(String.format("autocorrect:%s", autocorrect));
    }

    return String.format("nearText:{%s}", String.join(" ", arg));
  }
}
