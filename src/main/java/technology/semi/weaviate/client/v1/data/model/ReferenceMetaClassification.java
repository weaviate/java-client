package technology.semi.weaviate.client.v1.data.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
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
