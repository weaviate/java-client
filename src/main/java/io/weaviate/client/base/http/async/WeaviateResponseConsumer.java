package io.weaviate.client.base.http.async;

import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
import io.weaviate.client.base.WeaviateErrorResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.AbstractAsyncResponseConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public class WeaviateResponseConsumer<T> extends AbstractAsyncResponseConsumer<Result<T>, byte[]> {
  private final Serializer serializer;
  private final Class<T> classOfT;

  public WeaviateResponseConsumer(Class<T> classOfT) {
    super(new BasicAsyncEntityConsumer());
    this.serializer = new Serializer();
    this.classOfT = classOfT;
  }

  @Override
  protected Result<T> buildResult(HttpResponse response, byte[] entity, ContentType contentType) {
    String body = new String(entity, StandardCharsets.UTF_8);
    int statusCode = response.getCode();
    if (statusCode < 399) {
      T obj = serializer.toResponse(body, classOfT);
      Response<T> resp = new Response<>(statusCode, obj, null);
      return new Result<>(resp);
    }

    WeaviateErrorResponse error = serializer.toResponse(body, WeaviateErrorResponse.class);
    return new Result<>(statusCode, null, error);
  }

  @Override
  public void informationResponse(HttpResponse response, HttpContext context) throws HttpException, IOException {
  }
}
