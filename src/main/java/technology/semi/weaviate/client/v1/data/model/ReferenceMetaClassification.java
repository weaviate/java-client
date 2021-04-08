package technology.semi.weaviate.client.v1.data.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReferenceMetaClassification {
  Double closestLosingDistance;
  Double closestOverallDistance;
  Double closestWinningDistance;
  Long losingCount;
  Double losingDistance;
  Double meanLosingDistance;
  Double meanWinningDistance;
  Long overallCount;
  Long winningCount;
  Double winningDistance;
}
