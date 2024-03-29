package io.weaviate.client.v1.graphql.query.fields;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import io.weaviate.client.v1.graphql.query.argument.Argument;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Fields implements Argument {
  Field[] fields;

  @Override
  public String build() {
    if (ObjectUtils.isEmpty(fields)) {
      return "";
    }
    return Arrays.stream(fields)
      .map(Field::build)
      .collect(Collectors.joining(" "));
  }


  // created to accept a variable number of fields
  public static class FieldsBuilder {
    private Field[] fields;

    public FieldsBuilder fields(Field... fields) {
      this.fields = fields;
      return this;
    }
  }
}
