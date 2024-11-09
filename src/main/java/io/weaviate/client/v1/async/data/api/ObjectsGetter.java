package io.weaviate.client.v1.async.data.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.data.model.ObjectsListResponse;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.util.ObjectsPath;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

public class ObjectsGetter extends AsyncBaseClient<List<WeaviateObject>> implements AsyncClientResult<List<WeaviateObject>> {
  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private Integer limit;
  private Integer offset;
  private String after;
  private final Set<String> additional;
  private String consistencyLevel;
  private String tenant;
  private String nodeName;

  public ObjectsGetter(CloseableHttpAsyncClient client, Config config, ObjectsPath objectsPath) {
    super(client, config);
    this.objectsPath = objectsPath;
    this.additional = new HashSet<>();
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
  public Future<Result<List<WeaviateObject>>> run(FutureCallback<Result<List<WeaviateObject>>> callback) {
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
      String path = objectsPath.buildGetOne(params);
      return sendGetRequest(path, callback, new ResponseParser<List<WeaviateObject>>() {
        @Override
        public Result<List<WeaviateObject>> parse(HttpResponse response, String body, ContentType contentType) {
          Response<WeaviateObject> resp = serializer.toResponse(response.getCode(), body, WeaviateObject.class);
          WeaviateObject object = resp.getBody();
          List<WeaviateObject> objects = object == null
            ? null
            : Collections.singletonList(object);
          return new Result<>(resp.getStatusCode(), objects, resp.getErrors());
        }
      });
    }
    String path = objectsPath.buildGet(params);
    return sendGetRequest(path, callback, new ResponseParser<List<WeaviateObject>>() {
      @Override
      public Result<List<WeaviateObject>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<ObjectsListResponse> resp = serializer.toResponse(response.getCode(), body, ObjectsListResponse.class);
        List<WeaviateObject> objects = resp.getBody() == null
          ? null
          : Arrays.asList(resp.getBody().getObjects());
        return new Result<>(resp.getStatusCode(), objects, resp.getErrors());
      }
    });
  }
}
