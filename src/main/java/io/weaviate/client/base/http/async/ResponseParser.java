package io.weaviate.client.base.http.async;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.Serializer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

public abstract class ResponseParser<T> {
  protected final Serializer serializer;

  public ResponseParser() {
    this.serializer = new Serializer();
  }

  public abstract Result<T> parse(HttpResponse response, String body, ContentType contentType);
}
