package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public class WhereFilter implements Argument {
  WhereOperator operator;
  String[] path;
  Integer valueInt;
  Double valueNumber;
  Boolean valueBoolean;
  String valueString;
  String valueText;
  Date valueDate;
  GeoCoordinatesParameter valueGeoRange;

  private String getArg(String name, Object value) {
    return String.format("%s:%s", name, value);
  }

  public String buildWhereFilter() {
    Set<String> arg = new LinkedHashSet<>();
    if (operator != null) {
      arg.add(getArg("operator", operator));
    }
    if (path != null && path.length > 0) {
      arg.add(String.format("path:[%s]", StringUtils.joinWith(",", Arrays.stream(path).map(s -> String.format("\"%s\"", s)).toArray())));
    }
    if (valueInt != null) {
      arg.add(getArg("valueInt", valueInt));
    }
    if (valueNumber != null) {
      arg.add(getArg("valueNumber", valueNumber));
    }
    if (valueBoolean != null) {
      arg.add(getArg("valueBoolean", valueBoolean));
    }
    if (valueString != null) {
      arg.add(getArg("valueString", String.format("\"%s\"", valueString)));
    }
    if (valueText != null) {
      arg.add(getArg("valueText", String.format("\"%s\"", valueText)));
    }
    if (valueDate != null) {
      arg.add(getArg("valueDate", DateFormatUtils.format(valueDate, "yyyy-MM-dd'T'HH:mm:ssZZZZZ")));
    }
    if (valueGeoRange != null) {
      arg.add(getArg("valueGeoRange", valueGeoRange.build()));
    }
    if (arg.size() > 0) {
      return String.format("%s", StringUtils.joinWith(" ", arg.toArray()));
    }
    return "";
  }

  @Override
  public String build() {
    return buildWhereFilter();
  }
}
