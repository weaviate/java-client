package technology.semi.weaviate.client.base.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

public class BeaconPath {

    private final DbVersionSupport support;

    public BeaconPath(DbVersionSupport support) {
        this.support = support;
    }

    public String buildBatchFrom(Params pathParams) {
        return build(pathParams, this::addClassName, this::addId, this::addProperty);
    }
    public String buildBatchTo(Params pathParams) {
        return build(pathParams, this::addClassNameDeprecatedNotSupportedCheck, this::addId);
    }
    public String buildSingle(Params pathParams) {
        return build(pathParams, this::addClassNameDeprecatedNotSupportedCheck, this::addId);
    }

    @SafeVarargs
    private final String build(Params pathParams, BiConsumer<StringBuilder, Params>... consumers) {
        Objects.requireNonNull(pathParams);

        StringBuilder path = new StringBuilder("weaviate://localhost");
        Arrays.stream(consumers).forEach(consumer -> consumer.accept(path, pathParams));
        return path.toString();
    }

    private void addClassNameDeprecatedNotSupportedCheck(StringBuilder path, Params pathParams) {
        if (support.supportsClassNameNamespacedEndpoints()) {
            if (StringUtils.isNotBlank(pathParams.className)) {
                path.append("/").append(StringUtils.trim(pathParams.className));
            } else {
                support.warnDeprecatedNonClassNameNamespacedEndpointsForBeacons();
            }
        } else if (StringUtils.isNotBlank(pathParams.className)) {
            support.warnUsageOfNotSupportedClassNamespacedEndpointsForBeacons();
        }
    }
    private void addClassName(StringBuilder path, Params pathParams) {
        if (StringUtils.isNotBlank(pathParams.className)) {
            path.append("/").append(StringUtils.trim(pathParams.className));
        }
    }
    private void addId(StringBuilder path, Params pathParams) {
        if (StringUtils.isNotBlank(pathParams.id)) {
            path.append("/").append(StringUtils.trim(pathParams.id));
        }
    }
    private void addProperty(StringBuilder path, Params pathParams) {
        if (StringUtils.isNotBlank(pathParams.property)) {
            path.append("/").append(StringUtils.trim(pathParams.property));
        }
    }



    @Builder
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public static class Params {

        String id;
        String className;
        String property;
    }
}
