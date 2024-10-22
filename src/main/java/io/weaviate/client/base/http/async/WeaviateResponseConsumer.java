package io.weaviate.client.base.http.async;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
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
  private final ResponseParser<T> parser;

  public WeaviateResponseConsumer(Class<T> classOfT, ResponseParser<T> parser) {
    super(new BasicAsyncEntityConsumer());
    this.serializer = new Serializer();
    this.classOfT = classOfT;
    this.parser = parser;
  }

  @Override
  protected Result<T> buildResult(HttpResponse response, byte[] entity, ContentType contentType) {
    String body = new String(entity, StandardCharsets.UTF_8);
    if (this.parser != null) {
      return this.parser.parse(response, body, contentType);
    }
    return serializer.toResult(response.getCode(), body, classOfT);
  }

  @Override
  public void informationResponse(HttpResponse response, HttpContext context) throws HttpException, IOException {
  }
}
