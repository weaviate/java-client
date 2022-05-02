package technology.semi.weaviate.client.v1.classifications.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class WhereFilter {
  WhereFilter[] operands;
  String operator;
  String[] path;
  Boolean valueBoolean;
  String valueDate;
  WhereFilterGeoRange valueGeoRange;
  Integer valueInt;
  Double valueNumber;
  String valueString;
  String valueText;
}
