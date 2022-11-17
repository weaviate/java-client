package technology.semi.weaviate.client.v1.classifications.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import technology.semi.weaviate.client.v1.filters.WhereFilter;

@Getter
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationFilters {
  WhereFilter sourceWhere;
  WhereFilter targetWhere;
  WhereFilter trainingSetWhere;
}
