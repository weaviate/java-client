package technology.semi.weaviate.client.v1.classifications.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassificationFilters {
  WhereFilter sourceWhere;
  WhereFilter targetWhere;
  WhereFilter trainingSetWhere;
}
