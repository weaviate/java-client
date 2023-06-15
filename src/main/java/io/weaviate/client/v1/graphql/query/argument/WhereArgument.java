package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class WhereArgument implements Argument {

  WhereFilter filter;

  @Override
  public String build() {
    String whereStr = filter != null ? buildNestedFilter(filter) : "";
    return String.format("where:{%s}", whereStr);
  }

  private String buildNestedFilter(WhereFilter f) {
    Set<String> args = new LinkedHashSet<>();

    if (ArrayUtils.isNotEmpty(f.getOperands())) {
      if (f.getOperator() != null) {
        args.add(buildArg("operator", Serializer.escape(f.getOperator())));
      }
      args.add(String.format("operands:%s", Serializer.array(f.getOperands(), o -> String.format("{%s}", buildNestedFilter(o)))));
    } else {
      if (ArrayUtils.isNotEmpty(f.getPath())) {
        args.add(String.format("path:%s", Serializer.arrayWithQuotes(f.getPath())));
      }
      if (f.getValueInt() != null) {
        args.add(buildArg("valueInt", f.getValueInt()));
      }
      if (f.getValueNumber() != null) {
        args.add(buildArg("valueNumber", f.getValueNumber()));
      }
      if (f.getValueBoolean() != null) {
        args.add(buildArg("valueBoolean", f.getValueBoolean()));
      }
      if (f.getValueString() != null) {
        args.add(buildArg("valueString", Serializer.quote(f.getValueString())));
      }
      if (f.getValueText() != null) {
        args.add(buildArg("valueText", Serializer.quote(f.getValueText())));
      }
      if (f.getValueDate() != null) {
        String date = DateFormatUtils.format(f.getValueDate(), "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        args.add(buildArg("valueDate", Serializer.quote(date)));
      }
      if (f.getValueGeoRange() != null) {
        args.add(buildArg("valueGeoRange", buildGeoRange(f.getValueGeoRange())));
      }
      if (f.getOperator() != null) {
        args.add(buildArg("operator", Serializer.escape(f.getOperator())));
      }
    }

    return String.join(" ", args);
  }

  private String buildGeoRange(WhereFilter.GeoRange geoRange) {
    WhereFilter.GeoCoordinates geoCoordinates = geoRange.getGeoCoordinates();
    WhereFilter.GeoDistance distance = geoRange.getDistance();
    if (ObjectUtils.allNotNull(geoCoordinates, geoCoordinates.getLatitude(), geoCoordinates.getLongitude(),
      distance, distance.getMax())
    ) {
      return String.format("{geoCoordinates:{latitude:%s,longitude:%s},distance:{max:%s}}",
        geoCoordinates.getLatitude(), geoCoordinates.getLongitude(), distance.getMax());
    }
    return "";
  }

  private String buildArg(String name, Object value) {
    return String.format("%s:%s", name, value);
  }
}
