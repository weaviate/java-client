package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WhereArgument implements Argument {
  WhereFilter[] operands;
  WhereOperator operator;
  String[] path;
  Integer valueInt;
  Double valueNumber;
  Boolean valueBoolean;
  String valueString;
  String valueText;
  Date valueDate;
  GeoCoordinatesParameter valueGeoRange;

  private String getWhereFilter() {
    return WhereFilter.builder()
            .path(path).valueInt(valueInt).valueBoolean(valueBoolean).valueString(valueString)
            .valueText(valueText).valueDate(valueDate).valueGeoRange(valueGeoRange).valueNumber(valueNumber)
            .build().buildWhereFilter();
  }

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();
    String whereFilter = getWhereFilter();
    if (StringUtils.isNotBlank(whereFilter)) {
      arg.add(whereFilter);
    }
    if (operator != null) {
      arg.add(String.format("operator:%s", operator));
    }
    if (operands != null && operands.length > 0) {
      Set<String> operandsSet = new LinkedHashSet<>();
      for (WhereFilter f : operands) {
        operandsSet.add(String.format("{%s}", f.buildWhereFilter()));
      }
      arg.add(String.format("operands:[%s]", StringUtils.joinWith(",", operandsSet.toArray())));
    }
    return String.format("where:{%s}", StringUtils.joinWith(" ", arg.toArray()));
  }
}
