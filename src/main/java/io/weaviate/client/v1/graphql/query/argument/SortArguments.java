package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
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
public class SortArguments implements Argument {
  SortArgument[] sort;

  @Override
  public String build() {
    if (ArrayUtils.isNotEmpty(sort)) {
      return String.format("sort:%s", Serializer.array(sort, SortArgument::build));
    }
    return "sort:[]";
  }
}
