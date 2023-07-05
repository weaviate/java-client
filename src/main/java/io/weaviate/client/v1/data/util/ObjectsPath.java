package io.weaviate.client.v1.data.util;

import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.TriConsumer;
import io.weaviate.client.base.util.UrlEncoder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ObjectsPath {

  private final DbVersionSupport support;

  public ObjectsPath(DbVersionSupport support) {
    this.support = support;
  }


  public String buildCreate(Params params) {
    return build(
      params,
      this::addQueryConsistencyLevel
    );
  }

  public String buildDelete(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addQueryConsistencyLevel,
      this::addQueryTenant
    );
  }

  public String buildUpdate(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedCheck,
      this::addPathId,
      this::addQueryConsistencyLevel,
      this::addQueryTenant
    );
  }

  public String buildCheck(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addQueryTenant
    );
  }

  public String buildGet(Params params) {
    return build(
      params,
      this::addQueryClassNameWithDeprecatedCheck,
      this::addQueryAdditionals,
      this::addQueryLimit,
      this::addQueryOffset,
      this::addQueryAfter,
      this::addQueryTenant
    );
  }

  public String buildGetOne(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addQueryAdditionals,
      this::addQueryConsistencyLevel,
      this::addQueryNodeName,
      this::addQueryTenant
    );
  }


  @SafeVarargs
  private final String build(Params params, TriConsumer<Params, List<String>, List<String>>... appenders) {
    Objects.requireNonNull(params);

    List<String> pathParams = new ArrayList<>();
    List<String> queryParams = new ArrayList<>();

    pathParams.add("/objects");
    Arrays.stream(appenders).forEach(consumer -> consumer.accept(params, pathParams, queryParams));

    String path = String.join("/", pathParams);
    if (!queryParams.isEmpty()) {
      return path + "?" + String.join("&", queryParams);
    }
    return path;
  }


  private void addPathClassNameWithDeprecatedNotSupportedCheck(Params params, List<String> pathParams, List<String> queryParams) {
    if (support.supportsClassNameNamespacedEndpoints()) {
      if (StringUtils.isNotBlank(params.className)) {
        pathParams.add(UrlEncoder.encodePathParam(params.className));
      } else {
        support.warnDeprecatedNonClassNameNamespacedEndpointsForObjects();
      }
    } else if (StringUtils.isNotBlank(params.className)) {
      support.warnNotSupportedClassNamespacedEndpointsForObjects();
    }
  }

  private void addPathClassNameWithDeprecatedCheck(Params params, List<String> pathParams, List<String> queryParams) {
    if (support.supportsClassNameNamespacedEndpoints()) {
      if (StringUtils.isNotBlank(params.className)) {
        pathParams.add(UrlEncoder.encodePathParam(params.className));
      } else {
        support.warnDeprecatedNonClassNameNamespacedEndpointsForObjects();
      }
    }
  }

  private void addPathId(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.id)) {
      pathParams.add(UrlEncoder.encodePathParam(params.id));
    }
  }


  private void addQueryClassNameWithDeprecatedCheck(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isBlank(params.id) && StringUtils.isNotBlank(params.className)) {
      if (support.supportsClassNameNamespacedEndpoints()) {
        queryParams.add(UrlEncoder.encodeQueryParam("class", params.className));
      } else {
        support.warnNotSupportedClassParameterInEndpointsForObjects();
      }
    }
  }

  private void addQueryAdditionals(Params params, List<String> pathParams, List<String> queryParams) {
    if (ObjectUtils.isNotEmpty(params.additional)) {
      String include = Arrays.stream(params.additional)
        .map(UrlEncoder::encodePathParam)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(","));

      if (StringUtils.isNotBlank(include)) {
        queryParams.add(String.format("%s=%s", "include", include));
      }
    }
  }

  private void addQueryLimit(Params params, List<String> pathParams, List<String> queryParams) {
    if (params.limit != null) {
      queryParams.add(UrlEncoder.encodeQueryParam("limit", Integer.toString(params.limit)));
    }
  }

  private void addQueryOffset(Params params, List<String> pathParams, List<String> queryParams) {
    if (params.offset != null) {
      queryParams.add(UrlEncoder.encodeQueryParam("offset", Integer.toString(params.offset)));
    }
  }

  private void addQueryAfter(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.after)) {
      queryParams.add(UrlEncoder.encodeQueryParam("after", params.after));
    }
  }

  private void addQueryConsistencyLevel(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.consistencyLevel)) {
      queryParams.add(UrlEncoder.encodeQueryParam("consistency_level", params.consistencyLevel));
    }
  }

  private void addQueryNodeName(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.nodeName)) {
      queryParams.add(UrlEncoder.encodeQueryParam("node_name", params.nodeName));
    }
  }

  private void addQueryTenant(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.tenant)) {
      queryParams.add(UrlEncoder.encodeQueryParam("tenant", params.tenant));
    }
  }


  @Builder
  @ToString
  @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
  public static class Params {

    String id;
    String className;
    Integer limit;
    Integer offset;
    String after;
    String[] additional;
    String consistencyLevel;
    String nodeName;
    String tenant;
  }
}
