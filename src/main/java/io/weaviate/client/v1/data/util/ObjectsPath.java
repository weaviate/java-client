package io.weaviate.client.v1.data.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.TriConsumer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
      this::addQueryConsistencyLevel,
      this::addQueryTenantKey
    );
  }

  public String buildDelete(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addQueryConsistencyLevel
    );
  }

  public String buildUpdate(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedCheck,
      this::addPathId,
      this::addQueryConsistencyLevel
    );
  }

  public String buildCheck(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId
    );
  }

  public String buildGet(Params params) {
    return build(
      params,
      this::addQueryClassNameWithDeprecatedCheck,
      this::addQueryAdditionals,
      this::addQueryLimit,
      this::addQueryOffset,
      this::addQueryAfter
    );
  }

  public String buildGetOne(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addQueryAdditionals,
      this::addQueryConsistencyLevel,
      this::addQueryNodeName
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
        pathParams.add(StringUtils.trim(params.className));
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
        pathParams.add(StringUtils.trim(params.className));
      } else {
        support.warnDeprecatedNonClassNameNamespacedEndpointsForObjects();
      }
    }
  }

  private void addPathId(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.id)) {
      pathParams.add(StringUtils.trim(params.id));
    }
  }


  private void addQueryClassNameWithDeprecatedCheck(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isBlank(params.id) && StringUtils.isNotBlank(params.className)) {
      if (support.supportsClassNameNamespacedEndpoints()) {
        queryParams.add(String.format("%s=%s", "class", StringUtils.trim(params.className)));
      } else {
        support.warnNotSupportedClassParameterInEndpointsForObjects();
      }
    }
  }

  private void addQueryAdditionals(Params params, List<String> pathParams, List<String> queryParams) {
    if (ObjectUtils.isNotEmpty(params.additional)) {
      String include = Arrays.stream(params.additional)
        .map(StringUtils::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(","));

      if (StringUtils.isNotBlank(include)) {
        queryParams.add(String.format("%s=%s", "include", include));
      }
    }
  }

  private void addQueryLimit(Params params, List<String> pathParams, List<String> queryParams) {
    if (params.limit != null) {
      queryParams.add(String.format("%s=%s", "limit", params.limit));
    }
  }

  private void addQueryOffset(Params params, List<String> pathParams, List<String> queryParams) {
    if (params.offset != null) {
      queryParams.add(String.format("%s=%s", "offset", params.offset));
    }
  }

  private void addQueryAfter(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.after)) {
      queryParams.add(String.format("%s=%s", "after", params.after));
    }
  }

  private void addQueryConsistencyLevel(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.consistencyLevel)) {
      queryParams.add(String.format("%s=%s", "consistency_level", StringUtils.trim(params.consistencyLevel)));
    }
  }

  private void addQueryNodeName(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.nodeName)) {
      queryParams.add(String.format("%s=%s", "node_name", StringUtils.trim(params.nodeName)));
    }
  }

  private void addQueryTenantKey(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.tenantKey)) {
      queryParams.add(encodeQueryParam("tenant_key", params.tenantKey));
    }
  }

  private String encodeQueryParam(String key, String value) {
    return String.format("%s=%s", key, encode(StringUtils.trim(value)));
  }

  private String encode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      return value;
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
    String tenantKey;
  }
}
