package io.weaviate.client6.v1.api.collections.vectorizers;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.WrappedVectorIndex;

public record NoneVectorizer() implements Vectorizer {
  @Override
  public Kind _kind() {
    return Vectorizer.Kind.NONE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static class Builder extends WrappedVectorIndex.Builder<Builder, NoneVectorizer> {

    @Override
    public NoneVectorizer build() {
      return new NoneVectorizer();
    }
  }

  public static final TypeAdapter<NoneVectorizer> TYPE_ADAPTER = new TypeAdapter<NoneVectorizer>() {

    @Override
    public void write(JsonWriter out, NoneVectorizer value) throws IOException {
      out.beginObject();
      out.name(value._kind().jsonValue());
      out.beginObject();
      out.endObject();
      out.endObject();
    }

    @Override
    public NoneVectorizer read(JsonReader in) throws IOException {
      // NoneVectorizer expects no parameters, so we just skip to the closing bracket.
      in.beginObject();
      while (in.peek() != JsonToken.END_OBJECT) {
        in.skipValue();
      }
      in.endObject();
      return new NoneVectorizer();
    }
  }.nullSafe();
}
