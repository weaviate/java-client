package io.weaviate.client.v1.classifications.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import io.weaviate.client.v1.filters.WhereFilter;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationFilters {
  WhereFilter sourceWhere;
  WhereFilter targetWhere;
  WhereFilter trainingSetWhere;
}
