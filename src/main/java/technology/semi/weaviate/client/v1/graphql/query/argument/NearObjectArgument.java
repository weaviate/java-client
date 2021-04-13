package technology.semi.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NearObjectArgument implements Argument {
  String id;
  String beacon;
  Float certainty;

  @Override
  public String build() {
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotBlank(id)) {
      sb.append(String.format("id: \"%s\" ", id));
    }
    if (StringUtils.isNotBlank(beacon)) {
      sb.append(String.format("beacon: \"%s\" ", beacon));
    }
    if (certainty != null) {
      sb.append(String.format("certainty: %s ", certainty));
    }
    if (StringUtils.isNotBlank(sb)) {
      return String.format("nearObject: {%s}", sb);
    }
    return "";
  }
}
