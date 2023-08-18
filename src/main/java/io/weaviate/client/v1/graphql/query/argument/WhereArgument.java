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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

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
      args.add(buildArg("operands", Serializer.array(f.getOperands(), o -> String.format("{%s}", buildNestedFilter(o)))));
    } else {
      if (ArrayUtils.isNotEmpty(f.getPath())) {
        args.add(buildArg("path", Serializer.arrayWithQuotes(f.getPath())));
      }
      addArgSingleOrArray("valueBoolean", f.getValueBoolean(), f.getValueBooleanArray(), args::add);
      addArgSingleOrArray("valueInt", f.getValueInt(), f.getValueIntArray(), args::add);
      addArgSingleOrArray("valueNumber", f.getValueNumber(), f.getValueNumberArray(), args::add);
      addArgSingleOrArray("valueString", f.getValueString(), f.getValueStringArray(), args::add, Serializer::quote);
      addArgSingleOrArray("valueText", f.getValueText(), f.getValueTextArray(), args::add, Serializer::quote);
      addArgSingleOrArray("valueDate", f.getValueDate(), f.getValueDateArray(), args::add, date -> {
        String dateString = DateFormatUtils.format(date, "yyyy-MM-dd'T'HH:mm:ssZZZZZ");
        return Serializer.quote(dateString);
      });

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

  private <T> void addArgSingleOrArray(String valueName, T value, T[] values, Function<String, Boolean> add,
                                       Function<T, String> valueMapper) {
    String valAsString = null;
    if (Objects.nonNull(value)) {
      valAsString = valueMapper.apply(value);
    } else if (ArrayUtils.isNotEmpty(values)) {
      valAsString = Serializer.array(values, valueMapper);
    }
    if (Objects.nonNull(valAsString)) {
      add.apply(buildArg(valueName, valAsString));
    }
  }

  private <T> void addArgSingleOrArray(String valueName, T value, T[] values, Function<String, Boolean> add) {
    addArgSingleOrArray(valueName, value, values, add, Objects::toString);
  }
}
