package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashSet;
import java.util.Set;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Bm25Argument implements Argument {
  String query;
  String[] properties;
  SearchOperator searchOperator;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    arg.add(String.format("query:%s", Serializer.quote(query)));
    if (properties != null) {
      arg.add(String.format("properties:%s", Serializer.arrayWithQuotes(properties)));
    }
    if (searchOperator != null) {
      arg.add(String.format("searchOperator:%s", searchOperator.build()));
    }

    return String.format("bm25:{%s}", String.join(" ", arg));
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class SearchOperator implements Argument {
    private static final String OR = "Or";
    private static final String AND = "And";

    private String operator;
    private int minimumMatch;

    public static SearchOperator and() {
      return new SearchOperator(AND, 0); // minimumMatch ignored for And
    }

    public static SearchOperator or(int minimumMatch) {
      return new SearchOperator(OR, minimumMatch);
    }

    @Override
    public String build() {
      // While minimumOrTokensMatch is ignored, it should nevertheless be included
      // in the query, otherwise the server refuses to execute it.
      return String.format("{operator:%s minimumOrTokensMatch:%s}", operator, minimumMatch);
    }
  }
}
