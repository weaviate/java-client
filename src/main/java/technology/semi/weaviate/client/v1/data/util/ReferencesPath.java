package technology.semi.weaviate.client.v1.data.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.base.util.DbVersionSupport;

public class ReferencesPath {

    private final DbVersionSupport support;

    public ReferencesPath(DbVersionSupport support) {
        this.support = support;
    }

    public String build(Params pathParams) {
        StringBuilder path = new StringBuilder("/objects");
        if (pathParams != null) {
            if (support.supportsClassNameNamespacedEndpoints()) {
                if (StringUtils.isNotBlank(pathParams.className)) {
                    path.append("/").append(StringUtils.trim(pathParams.className));
                } else {
                    support.warnDeprecatedNonClassNameNamespacedEndpointsForReferences();
                }
            } else if (StringUtils.isNotBlank(pathParams.className)) {
                support.warnUsageOfNotSupportedClassNamespacedEndpointsForReferences();
            }
            if (StringUtils.isNotBlank(pathParams.id)) {
                path.append("/").append(StringUtils.trim(pathParams.id));
            }
            path.append("/references");
            if (StringUtils.isNotBlank(pathParams.property)) {
                path.append("/").append(StringUtils.trim(pathParams.property));
            }
        }
        return path.toString();
    }



    @Builder
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public static class Params {

        String id;
        String className;
        String property;
    }
}
