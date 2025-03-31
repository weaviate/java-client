package io.weaviate.client6.v1.query;

import java.util.List;
import java.util.Map;

import io.weaviate.client6.v1.query.QueryResult.SearchObject;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GroupedQueryResult<T> {
  public final List<WithGroupSearchObject<T>> objects;
  public final Map<String, Group<T>> groups;

  public static class WithGroupSearchObject<T> extends SearchObject<T> {
    public final String belongsToGroup;

    public WithGroupSearchObject(String group, T properties, QueryMetadata metadata) {
      super(properties, metadata);
      this.belongsToGroup = group;
    }
  }

  public record Group<T>(String name, Float minDistance, Float maxDistance, long numberOfObjects,
      List<WithGroupSearchObject<T>> objects) {
  }
}
