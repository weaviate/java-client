package io.weaviate.client6.v1.api.collections.config;

public enum PropertyIndexType {
  FILTERABLE("filterable"),
  SEARCHABLE("searchable"),
  RANGE_FILTERS("rangeFilters");

  private final String value;

  private PropertyIndexType(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }
}
