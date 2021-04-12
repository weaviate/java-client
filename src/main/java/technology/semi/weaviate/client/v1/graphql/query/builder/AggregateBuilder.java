package technology.semi.weaviate.client.v1.graphql.query.builder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  String fields;
  String groupByClausePropertyName;

  @Override
  public String buildQuery() {
    String filterClause = "";
    if (StringUtils.isNotBlank(groupByClausePropertyName)) {
      filterClause = String.format("(groupBy: \"%s\")", groupByClausePropertyName);
    }
    return String.format("{Aggregate{%s%s{%s}}}", className, filterClause, fields);
  }
}
