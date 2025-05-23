package io.weaviate.client6.v1.internal.json;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import lombok.EqualsAndHashCode;
import lombok.ToString;

public class UnwrappedTypeAdapterFactoryTest {

  @Test
  public void testGson() {
    var gson = new GsonBuilder()
        .registerTypeAdapterFactory(new UnwrappedTypeAdapterFactory())
        .create();
    var object = new Outer(1, false);

    // var want = "{\"id\": 1, \"shouldUnwrap\": true}";
    var want = "{\"id\": 1, \"inner\": {\"shouldUnwrap\": false}}";
    var got = gson.toJson(object);

    compareJson(want, got);

    var gotParsed = gson.fromJson(got, Outer.class);
    Assertions.assertThat(gotParsed).isEqualTo(object);
  }

  private static void compareJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }

  @EqualsAndHashCode
  @ToString
  class Inner implements Unwrapped {
    final boolean shouldUnwrap;

    Inner(boolean shouldUnwrap) {
      this.shouldUnwrap = shouldUnwrap;
    }

    @Override
    public boolean shouldUnwrap() {
      return shouldUnwrap;
    }
  }

  @EqualsAndHashCode
  @ToString
  class Outer {
    int id;
    Inner inner;

    Outer(int id, boolean shouldUnwrap) {
      this.id = id;
      this.inner = new Inner(shouldUnwrap);
    }
  }

}
