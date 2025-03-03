package io.weaviate.client.v1.data.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.data.model.ObjectsListResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectsGetter extends BaseClient<ObjectsListResponse> implements ClientResult<List<WeaviateObject>> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private Integer limit;
  private Integer offset;
  private String after;
  private final HashSet<String> additional;
  private String consistencyLevel;
  private String tenant;
  private String nodeName;

  private class ObjectGetter extends BaseClient<WeaviateObject> implements ClientResult<List<WeaviateObject>> {
    private String path;

    public ObjectGetter(HttpClient httpClient, Config config) {
      super(httpClient, config);
    }

    public ObjectGetter withPath(String path) {
      this.path = path;
      return this;
    }

    @Override
    public Result<List<WeaviateObject>> run() {
      Response<WeaviateObject> resp = sendGetRequest(path, WeaviateObject.class);
      WeaviateObject object = resp.getBody();
      List<WeaviateObject> objects = object == null
          ? null
          : Collections.singletonList(object);
      return new Result<>(resp.getStatusCode(), objects, resp.getErrors());
    }
  }

  private final ObjectGetter objectGetter;

  public ObjectsGetter(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
    super(httpClient, config);
    this.objectGetter = new ObjectGetter(httpClient, config);
    this.additional = new HashSet<>();
    this.objectsPath = Objects.requireNonNull(objectsPath);
  }

  public ObjectsGetter withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectsGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectsGetter withLimit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public ObjectsGetter withVector() {
    this.additional.add("vector");
    return this;
  }

  public ObjectsGetter withAdditional(String name) {
    this.additional.add(name);
    return this;
  }

  public ObjectsGetter withConsistencyLevel(String cl) {
    this.consistencyLevel = cl;
    return this;
  }

  public ObjectsGetter withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public ObjectsGetter withNodeName(String name) {
    this.nodeName = name;
    return this;
  }

  public ObjectsGetter withAfter(String after) {
    this.after = after;
    return this;
  }

  public ObjectsGetter withOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  @Override
  public Result<List<WeaviateObject>> run() {
    ObjectsPath.Params params = ObjectsPath.Params.builder()
        .id(id)
        .className(className)
        .limit(limit)
        .offset(offset)
        .after(after)
        .additional(additional.toArray(new String[0]))
        .consistencyLevel(consistencyLevel)
        .tenant(tenant)
        .nodeName(nodeName)
        .build();
    if (StringUtils.isNotBlank(id)) {
      return this.objectGetter.withPath(objectsPath.buildGetOne(params)).run();
    }
    Response<ObjectsListResponse> resp = sendGetRequest(objectsPath.buildGet(params), ObjectsListResponse.class);
    List<WeaviateObject> objects = resp.getBody() == null
        ? null
        : Arrays.asList(resp.getBody().getObjects());

    return new Result<>(resp.getStatusCode(), objects, resp.getErrors());
  }
}
