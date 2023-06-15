package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SortArgument implements Argument {
  String[] path;
  SortOrder order;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (ArrayUtils.isNotEmpty(path)) {
      arg.add(String.format("path:%s", Serializer.arrayWithQuotes(path)));
    }
    if (order != null) {
      arg.add(String.format("order:%s", order));
    }

    return String.format("{%s}", String.join(" ", arg));
  }
}
