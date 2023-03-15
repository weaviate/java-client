package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
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
      arg.add(String.format("question:%s", Serializer.quote(question)));
    }
    if (ArrayUtils.isNotEmpty(properties)) {
      arg.add(String.format("properties:%s",  Serializer.arrayWithQuotes(properties)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty:%s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance:%s", distance));
    }
    if (autocorrect != null) {
      arg.add(String.format("autocorrect:%s", autocorrect));
    }
    if (rerank != null) {
      arg.add(String.format("rerank:%s", rerank));
    }

    return String.format("ask:{%s}", String.join(" ", arg));
  }
}
