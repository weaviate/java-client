package technology.semi.weaviate.client.v1.graphql.query.fields;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.Argument;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Fields implements Argument {
  Field[] fields;

  @Override
  public String build() {
    if (this.fields != null && this.fields.length > 0) {
      return StringUtils.joinWith(" ", Arrays.stream(this.fields).map(Field::build).toArray());
    }
    return "";
  }
}
