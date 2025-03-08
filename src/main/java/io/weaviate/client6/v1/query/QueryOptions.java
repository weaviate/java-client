package io.weaviate.client6.v1.query;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
abstract class QueryOptions<SELF extends QueryOptions<SELF>> {
  private Integer limit;
  private Integer offset;
  private Integer autocut;
  private String after;
  private String consistencyLevel;
  private List<String> returnProperties = new ArrayList<>();

  public final SELF limit(Integer limit) {
    this.limit = limit;
    return (SELF) this;
  }

  public final SELF offset(Integer offset) {
    this.offset = offset;
    return (SELF) this;
  }

  public final SELF autocut(Integer autocut) {
    this.autocut = autocut;
    return (SELF) this;
  }

  public final SELF after(String after) {
    this.after = after;
    return (SELF) this;
  }

  public final SELF consistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return (SELF) this;
  }
}
