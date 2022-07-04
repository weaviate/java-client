package technology.semi.weaviate.client.v1.data.util;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ObjectsPathBuilder {

  String id;
  String className;
  Integer limit;
  String[] additional;

  public String buildPath() {
    return buildPath(null);
  }

  public String buildPath(String version) {
    StringBuilder path = new StringBuilder();
    path.append("/objects");
    if (supportsClassNameNamespacedEndpoints(version) && StringUtils.isNotBlank(className)) {
      path.append("/").append(StringUtils.trim(className));
    }
    if (StringUtils.isNotBlank(id)) {
      path.append("/").append(StringUtils.trim(id));
    }
    List<String> params = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(additional)) {
      params.add(String.format("include=%s", StringUtils.join(additional, ",")));
    }
    if (limit != null) {
      params.add(String.format("limit=%s", limit));
    }
    if (params.size() > 0) {
      path.append("?").append(StringUtils.joinWith("&", params.toArray()));
    }
    return path.toString();
  }

  private boolean supportsClassNameNamespacedEndpoints(String version) {
    String[] versionNumbers = StringUtils.split(version, ".");
    if (versionNumbers != null && versionNumbers.length >= 2) {
      int major = Integer.parseInt(versionNumbers[0]);
      int minor = Integer.parseInt(versionNumbers[1]);
      return (major == 1 && minor >= 14) || major >= 2;
    }
    return false;
  }
}
