package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GroupByArgument implements Argument {
  String[] path;
  Integer groups;
  Integer objectsPerGroup;

  @Override
  public String build() {
    Set<String> args = new LinkedHashSet<>();

    if (ArrayUtils.isNotEmpty(path)) {
      args.add(String.format("path:%s", Serializer.arrayWithQuotes(path)));
    }
    if (groups != null) {
      args.add(String.format("groups:%s", groups));
    }
    if (objectsPerGroup != null) {
      args.add(String.format("objectsPerGroup:%s", objectsPerGroup));
    }

    return String.format("groupBy:{%s}", String.join(" ", args));
  }
}
