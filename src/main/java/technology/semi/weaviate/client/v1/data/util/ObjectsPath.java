package technology.semi.weaviate.client.v1.data.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.base.util.DbVersionSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ObjectsPath {

  private final DbVersionSupport support;

  public ObjectsPath(DbVersionSupport support) {
    this.support = support;
  }

  public String buildCreate(Params pathParams) {
    return build(pathParams);
  }

  public String buildDelete(Params pathParams) {
    return build(pathParams, this::addClassNameDeprecatedNotSupportedCheck, this::addId);
  }

  public String buildCheck(Params pathParams) {
    return build(pathParams, this::addClassNameDeprecatedNotSupportedCheck, this::addId);
  }

  public String buildGetOne(Params pathParams) {
    return build(pathParams, this::addClassNameDeprecatedNotSupportedCheck, this::addId, this::addQueryParamsForGetOne);
  }

  public String buildGet(Params pathParams) {
    return build(pathParams, this::addQueryParams);
  }

  public String buildUpdate(Params pathParams) {
    return build(pathParams, this::addClassNameDeprecatedCheck, this::addId);
  }

  @SafeVarargs
  private final String build(Params pathParams, BiConsumer<StringBuilder, Params>... modifiers) {
    Objects.requireNonNull(pathParams);

    StringBuilder path = new StringBuilder("/objects");
    Arrays.stream(modifiers).forEach(consumer -> consumer.accept(path, pathParams));
    return path.toString();
  }

  private void addClassNameDeprecatedNotSupportedCheck(StringBuilder path, Params pathParams) {
    if (support.supportsClassNameNamespacedEndpoints()) {
      if (StringUtils.isNotBlank(pathParams.className)) {
        path.append("/").append(StringUtils.trim(pathParams.className));
      } else {
        support.warnDeprecatedNonClassNameNamespacedEndpointsForObjects();
      }
    } else if (StringUtils.isNotBlank(pathParams.className)) {
      support.warnNotSupportedClassNamespacedEndpointsForObjects();
    }
  }

  private void addClassNameDeprecatedCheck(StringBuilder path, Params pathParams) {
    if (support.supportsClassNameNamespacedEndpoints()) {
      if (StringUtils.isNotBlank(pathParams.className)) {
        path.append("/").append(StringUtils.trim(pathParams.className));
      } else {
        support.warnDeprecatedNonClassNameNamespacedEndpointsForObjects();
      }
    }
  }

  private void addId(StringBuilder path, Params pathParams) {
    if (StringUtils.isNotBlank(pathParams.id)) {
      path.append("/").append(StringUtils.trim(pathParams.id));
    }
  }

  private void addQueryParams(StringBuilder path, Params pathParams) {
    List<String> queryParams = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(pathParams.additional)) {
      queryParams.add(String.format("include=%s", StringUtils.join(pathParams.additional, ",")));
    }
    if (pathParams.limit != null) {
      queryParams.add(String.format("limit=%s", pathParams.limit));
    }
    if (StringUtils.isBlank(pathParams.id) && StringUtils.isNotBlank(pathParams.className)) {
      if (support.supportsClassNameNamespacedEndpoints()) {
        queryParams.add(String.format("class=%s", pathParams.className));
      } else {
        support.warnNotSupportedClassParameterInEndpointsForObjects();
      }
    }
    if (queryParams.size() > 0) {
      path.append("?").append(StringUtils.joinWith("&", queryParams.toArray()));
    }
  }

  private void addQueryParamsForGetOne(StringBuilder path, Params pathParams) {
    List<String> queryParams = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(pathParams.additional)) {
      queryParams.add(String.format("include=%s", StringUtils.join(pathParams.additional, ",")));
    }
    if (queryParams.size() > 0) {
      path.append("?").append(StringUtils.joinWith("&", queryParams.toArray()));
    }
  }


  @Builder
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class Params {

    String id;
    String className;
    Integer limit;
    String[] additional;
  }
}
