package technology.semi.weaviate.client.v1.data.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.data.model.Object;
import technology.semi.weaviate.client.v1.data.model.ObjectsListResponse;

public class ObjectsGetter extends BaseClient<ObjectsListResponse> implements ClientResult<List<Object>> {

  private String id;
  private Integer limit;
  private HashSet<String> additional;

  private class ObjectGetter extends BaseClient<Object> implements ClientResult<List<Object>> {
    private String path;

    public ObjectGetter(Config config) {
      super(config);
    }

    public ObjectGetter withPath(String path) {
      this.path = path;
      return this;
    }

    @Override
    public Result<List<Object>> run() {
      Response<Object> resp = sendGetRequest(path, Object.class);
      return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody()), resp.getErrors());
    }
  }
  private ObjectGetter objectGetter;

  public ObjectsGetter(Config config) {
    super(config);
    objectGetter = new ObjectGetter(config);
    additional = new HashSet<>();
  }

  public ObjectsGetter withID(String id) {
    this.id = id;
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
    StringBuilder path = new StringBuilder();
    path.append("/objects");
    if (StringUtils.isNotBlank(id)) {
      path.append("/").append(id);
    }
    List<String> params = new ArrayList<>();
    if (additional.size() > 0) {
      params.add(String.format("include=%s", StringUtils.joinWith(",", additional.toArray())));
    }
    if (limit != null) {
      params.add(String.format("limit=%s", limit));
    }
    if (params.size() > 0) {
      path.append("?").append(StringUtils.joinWith("&", params.toArray()));
    }
    return path.toString();
  }

  @Override
  public Result<List<Object>> run() {
    if (StringUtils.isNotBlank(id)) {
      return this.objectGetter.withPath(getPath()).run();
    }
    Response<ObjectsListResponse> resp = sendGetRequest(getPath(), ObjectsListResponse.class);
    return new Result<>(resp.getStatusCode(), Arrays.asList(resp.getBody().getObjects()), resp.getErrors());
  }
}