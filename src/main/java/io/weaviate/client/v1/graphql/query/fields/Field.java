package io.weaviate.client.v1.graphql.query.fields;

import io.weaviate.client.v1.graphql.query.argument.Argument;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
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
    if (fields != null && fields.length > 0) {
      s.append(String.format("{%s}", StringUtils.joinWith(" ", Arrays.stream(this.fields).map(Field::build).toArray())));
    }
    return s.toString();
  }
}
