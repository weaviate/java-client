package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch.SearchOperatorOptions.Operator;

public class SearchOperator {
  private final Operator operator;
  private final Integer minimumOrTokensMatch;

  public static final SearchOperator or(int minimumOrTokensMatch) {
    return new SearchOperator(Operator.OPERATOR_OR, minimumOrTokensMatch);
  }

  public static final SearchOperator and() {
    return new SearchOperator(Operator.OPERATOR_AND, 0);
  }

  public static final SearchOperator andCross() {
    return new SearchOperator(Operator.OPERATOR_AND_CROSS, 0);
  }

  private SearchOperator(Operator operator, Integer minimumOrTokensMatch) {
    this.operator = operator;
    this.minimumOrTokensMatch = minimumOrTokensMatch;
  }

  void appendTo(WeaviateProtoBaseSearch.BM25.Builder req) {
    var options = WeaviateProtoBaseSearch.SearchOperatorOptions.newBuilder()
        .setOperator(operator);
    if (minimumOrTokensMatch != null) {
      options.setMinimumOrTokensMatch(minimumOrTokensMatch);
    }
    req.setSearchOperator(options);
  }
}
