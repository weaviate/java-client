package technology.semi.weaviate.client.v1.data.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.data.model.ObjectsListResponse;
import technology.semi.weaviate.client.v1.data.util.ObjectsPathBuilder;

public class ObjectsGetter extends BaseClient<ObjectsListResponse> implements ClientResult<List<WeaviateObject>> {

  private final String version;
  private String id;
  private String className;
  private Integer limit;
  private HashSet<String> additional;

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
      return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody()), resp.getErrors());
    }
  }

  private ObjectGetter objectGetter;

  public ObjectsGetter(Config config, String version) {
    super(config);
    this.objectGetter = new ObjectGetter(config);
    this.additional = new HashSet<>();
    this.version = version;
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

  private String getPath() {
    return ObjectsPathBuilder.builder()
      .id(this.id)
      .className(this.className)
      .limit(this.limit)
      .additional(this.additional.stream().toArray(String[]::new))
      .build()
      .buildPath(this.version);
  }

  @Override
  public Result<List<WeaviateObject>> run() {
    if (StringUtils.isNotBlank(id)) {
      return this.objectGetter.withPath(getPath()).run();
    }
    Response<ObjectsListResponse> resp = sendGetRequest(getPath(), ObjectsListResponse.class);
    return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody().getObjects()), resp.getErrors());
  }
}