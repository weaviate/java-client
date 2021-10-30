package technology.semi.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GeoCoordinatesParameter {
  Float latitude;
  Float longitude;
  Float maxDistance;

  public String build() {
    if (latitude != null && longitude != null && maxDistance != null) {
      return String.format("{geoCoordinates:{latitude:%s,longitude:%s},distance:{max:%s}}", latitude, longitude, maxDistance);
    }
    return "";
  }
}
