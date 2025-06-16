package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch.SearchOperatorOptions.Operator;

public class SearchOperator {
  private final String operator;
  private final Integer minimumOrTokensMatch;

  public static final SearchOperator or(int minimumOrTokensMatch) {
    return new SearchOperator("Or", minimumOrTokensMatch);
  }

  public static final SearchOperator and() {
    return new SearchOperator("And", 0);
  }

  private SearchOperator(String operator, Integer minimumOrTokensMatch) {
    this.operator = operator;
    this.minimumOrTokensMatch = minimumOrTokensMatch;
  }

  void appendTo(WeaviateProtoBaseSearch.SearchOperatorOptions.Builder options) {
    options.setOperator(operator == "And" ? Operator.OPERATOR_AND : Operator.OPERATOR_OR);
    if (minimumOrTokensMatch != null) {
      options.setMinimumOrTokensMatch(minimumOrTokensMatch);
    }
  }
}
