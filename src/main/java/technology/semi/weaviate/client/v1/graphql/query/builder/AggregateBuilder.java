package technology.semi.weaviate.client.v1.graphql.query.builder;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  Fields fields;
  String groupByClausePropertyName;

  @Override
  public String buildQuery() {
    String filterClause = "";
    if (StringUtils.isNotBlank(groupByClausePropertyName)) {
      filterClause = String.format("(groupBy: \"%s\")", groupByClausePropertyName);
    }
    String fieldsClause = fields != null ? fields.build() : "";
    return String.format("{Aggregate{%s%s{%s}}}", className, filterClause, fieldsClause);
  }
}
