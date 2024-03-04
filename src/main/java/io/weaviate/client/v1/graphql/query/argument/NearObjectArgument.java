package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashSet;
import java.util.Set;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearObjectArgument implements Argument {
  String id;
  String beacon;
  Float certainty;
  Float distance;
  String[] targetVectors;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (StringUtils.isNotBlank(id)) {
      arg.add(String.format("id:%s", Serializer.quote(id)));
    }
    if (StringUtils.isNotBlank(beacon)) {
      arg.add(String.format("beacon:%s", Serializer.quote(beacon)));
    }
    if (certainty != null) {
      arg.add(String.format("certainty:%s", certainty));
    }
    if (distance != null) {
      arg.add(String.format("distance:%s", distance));
    }
    if (ArrayUtils.isNotEmpty(targetVectors)) {
      arg.add(String.format("targetVectors:%s",  Serializer.arrayWithQuotes(targetVectors)));
    }

    return String.format("nearObject:{%s}", String.join(" ", arg));
  }
}
