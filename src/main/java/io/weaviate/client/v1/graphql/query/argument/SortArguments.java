package io.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SortArguments implements Argument {
  SortArgument[] sort;

  @Override
  public String build() {
    if (ArrayUtils.isNotEmpty(sort)) {
      return String.format("sort:[%s]", Arrays.stream(sort).map(SortArgument::build).collect(Collectors.joining(", ")));
    }
    return "sort:[]";
  }
}
