package io.weaviate.client.base.http.async;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
import io.weaviate.client.v1.graphql.model.GraphQLTypedResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AbstractAsyncResponseConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public class WeaviateGraphQLTypedResponseConsumer<C> extends AbstractAsyncResponseConsumer<Result<GraphQLTypedResponse<C>>, byte[]> {
  private final Serializer serializer;
  private final Class<C> classOfT;

  public WeaviateGraphQLTypedResponseConsumer(Class<C> classOfT) {
    super(new BasicAsyncEntityConsumer());
    this.serializer = new Serializer();
    this.classOfT = classOfT;
  }

  @Override
  protected Result<GraphQLTypedResponse<C>> buildResult(HttpResponse response, byte[] entity, ContentType contentType) {
    String body = (entity != null) ? new String(entity, StandardCharsets.UTF_8) : "";
    return serializer.toGraphQLTypedResult(response.getCode(), body, classOfT);
  }

  @Override
  public void informationResponse(HttpResponse response, HttpContext context) throws HttpException, IOException {
  }
}
