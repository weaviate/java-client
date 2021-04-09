package technology.semi.weaviate.client.v1.classifications.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WhereFilterGeoRange {
  WhereFilterGeoRangeDistance distance;
  GeoCoordinates geoCoordinates;
}
