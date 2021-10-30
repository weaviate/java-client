package technology.semi.weaviate.client.v1.graphql.query.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.Argument;

@Getter
@Builder
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
