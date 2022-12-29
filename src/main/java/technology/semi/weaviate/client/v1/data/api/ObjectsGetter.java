package technology.semi.weaviate.client.v1.data.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.ObjectsListResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectsGetter extends BaseClient<ObjectsListResponse> implements ClientResult<List<WeaviateObject>> {

  private final ObjectsPath objectsPath;
  private final HashSet<String> additional;
  private final ObjectGetter objectGetter;
  private String id;
  private String className;
  private Integer limit;
  private String consistencyLevel;
  private String nodeName;

  public ObjectsGetter(Config config, ObjectsPath objectsPath) {
    super(config);
    this.objectGetter = new ObjectGetter(config);
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

  public ObjectsGetter withNodeName(String name) {
    this.nodeName = name;
    return this;
  }

  @Override
  public Result<List<WeaviateObject>> run() {
    ObjectsPath.Params params = ObjectsPath.Params.builder()
      .id(id)
      .className(className)
      .limit(limit)
      .additional(additional.toArray(new String[0]))
      .consistencyLevel(consistencyLevel)
      .nodeName(nodeName)
      .build();
    if (StringUtils.isNotBlank(id)) {
      return this.objectGetter.withPath(objectsPath.buildGetOne(params)).run();
    }
    Response<ObjectsListResponse> resp = sendGetRequest(objectsPath.buildGet(params), ObjectsListResponse.class);
    return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody().getObjects()), resp.getErrors());
  }

  private class ObjectGetter extends BaseClient<WeaviateObject> implements ClientResult<List<WeaviateObject>> {
    private String path;

    public ObjectGetter(Config config) {
      super(config);
    }

    public ObjectGetter withPath(String path) {
      this.path = path;
      return this;
    }

    @Override
    public Result<List<WeaviateObject>> run() {
      Response<WeaviateObject> resp = sendGetRequest(path, WeaviateObject.class);
      return new Result<>(resp.getStatusCode(), Collections.singletonList(resp.getBody()), resp.getErrors());
    }
  }
}
