package io.weaviate.client.v1.data.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
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

public class ReferencesPath {

  private final DbVersionSupport support;

  public ReferencesPath(DbVersionSupport support) {
    this.support = support;
  }


  public String buildCreate(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addPathReferences,
      this::addPathProperty,
      this::addQueryConsistencyLevel,
      this::addQueryTenantKey
    );
  }

  public String buildDelete(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addPathReferences,
      this::addPathProperty,
      this::addQueryConsistencyLevel,
      this::addQueryTenantKey
    );
  }

  public String buildReplace(Params params) {
    return commonBuild(params);
  }

  private String commonBuild(Params params) {
    return build(
      params,
      this::addPathClassNameWithDeprecatedNotSupportedCheck,
      this::addPathId,
      this::addPathReferences,
      this::addPathProperty,
      this::addQueryConsistencyLevel
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

  private void addPathId(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.id)) {
      pathParams.add(StringUtils.trim(params.id));
    }
  }

  private void addPathReferences(Params params, List<String> pathParams, List<String> queryParams) {
    pathParams.add("references");
  }

  private void addPathProperty(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.property)) {
      pathParams.add(StringUtils.trim(params.property));
    }
  }


  private void addQueryConsistencyLevel(Params params, List<String> pathParams, List<String> queryParams) {
    if (StringUtils.isNotBlank(params.consistencyLevel)) {
      queryParams.add(String.format("%s=%s", "consistency_level", StringUtils.trim(params.consistencyLevel)));
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
    String consistencyLevel;
    String tenantKey;
    String property;
  }
}
