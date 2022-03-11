package technology.semi.weaviate.client.v1.graphql.query.builder;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereArgument;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AggregateBuilder implements Query {
  String className;
  Fields fields;
  String groupByClausePropertyName;
  WhereArgument withWhereArgument;

  private boolean includesFilterClause() {
    return withWhereArgument != null || StringUtils.isNotBlank(groupByClausePropertyName);
  }

  private String createFilterClause() {
    if (includesFilterClause()) {
      Set<String> filters = new LinkedHashSet<>();
      if (StringUtils.isNotBlank(groupByClausePropertyName)) {
        filters.add(String.format("groupBy: \"%s\"", groupByClausePropertyName));
      }
      if (withWhereArgument != null) {
        filters.add(withWhereArgument.build());
      }
      return String.format("(%s)", StringUtils.joinWith(", ", filters.toArray()));
    }
    return "";
  }

  @Override
  public String buildQuery() {
    String fieldsClause = fields != null ? fields.build() : "";
    return String.format("{Aggregate{%s%s{%s}}}", className, createFilterClause(), fieldsClause);
  }
}
