package technology.semi.weaviate.client.v1.filters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WhereFilterUtil {

    public static String toGraphQLString(WhereFilter whereFilter) {
        String whereStr = whereFilter != null ? buildWhereFilter(whereFilter) : "";
        return String.format("where:{%s}", whereStr);
    }

    private static String buildWhereFilter(WhereFilter wf) {
        Set<String> args = new LinkedHashSet<>();
        if (wf.getOperands() != null && wf.getOperands().length > 0) {
            if (wf.getOperator() != null) {
                args.add(buildArg("operator", wf.getOperator()));
            }
            String operands = Arrays.stream(wf.getOperands())
                    .map(operand -> String.format("{%s}", buildWhereFilter(operand)))
                    .collect(Collectors.joining(","));
            args.add(String.format("operands:[%s]", operands));
        } else {
            if (wf.getPath() != null && wf.getPath().length > 0) {
                Object[] quoted = Arrays.stream(wf.getPath()).map(s -> String.format("\"%s\"", s)).toArray();
                args.add(String.format("path:[%s]", StringUtils.joinWith(",", quoted)));
            }
            if (wf.getValueInt() != null) {
                args.add(buildArg("valueInt", wf.getValueInt()));
            }
            if (wf.getValueNumber() != null) {
                args.add(buildArg("valueNumber", wf.getValueNumber()));
            }
            if (wf.getValueBoolean() != null) {
                args.add(buildArg("valueBoolean", wf.getValueBoolean()));
            }
            if (wf.getValueString() != null) {
                args.add(buildArg("valueString", String.format("\"%s\"", wf.getValueString())));
            }
            if (wf.getValueText() != null) {
                args.add(buildArg("valueText", String.format("\"%s\"", wf.getValueText())));
            }
            if (wf.getValueDate() != null) {
                args.add(buildArg("valueDate", DateFormatUtils.format(wf.getValueDate(), "yyyy-MM-dd'T'HH:mm:ssZZZZZ")));
            }
            if (wf.getValueGeoRange() != null) {
                args.add(buildArg("valueGeoRange", buildGeoRange(wf.getValueGeoRange())));
            }
            if (wf.getOperator() != null) {
                args.add(buildArg("operator", wf.getOperator()));
            }
        }
        return StringUtils.joinWith(" ", args.toArray());
    }

    private static String buildGeoRange(WhereFilter.GeoRange geoRange) {
        WhereFilter.GeoCoordinates geoCoordinates = geoRange.getGeoCoordinates();
        WhereFilter.GeoDistance distance = geoRange.getDistance();
        if (geoCoordinates != null && geoCoordinates.getLatitude() != null && geoCoordinates.getLongitude() != null
                && distance != null && distance.getMax() != null
        ) {
            return String.format("{geoCoordinates:{latitude:%s,longitude:%s},distance:{max:%s}}",
                    geoCoordinates.getLatitude(), geoCoordinates.getLongitude(), distance.getMax());

        }
        return "";
    }

    private static String buildArg(String name, Object value) {
        return String.format("%s:%s", name, value);
    }
}
