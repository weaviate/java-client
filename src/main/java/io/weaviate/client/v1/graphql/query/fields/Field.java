package io.weaviate.client.v1.graphql.query.fields;

import io.weaviate.client.v1.graphql.query.argument.Argument;
import java.util.Arrays;
import java.util.stream.Collectors;

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
public class Field implements Argument {
  String name;
  Field[] fields;

  @Override
  public String build() {
    StringBuilder s = new StringBuilder();
    if (StringUtils.isNotBlank(name)) {
      s.append(name);
    }
    if (ArrayUtils.isNotEmpty(fields)) {
      s.append(String.format("{%s}", Arrays.stream(fields).map(Field::build).collect(Collectors.joining(" "))));
    }
    return s.toString();
  }


  // created to accept a variable number of fields
  public static class FieldBuilder {
    private Field[] fields;

    public FieldBuilder fields(Field... fields) {
      this.fields = fields;
      return this;
    }
  }
}
