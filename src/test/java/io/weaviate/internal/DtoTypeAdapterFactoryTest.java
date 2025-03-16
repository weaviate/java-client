package io.weaviate.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import io.weaviate.client6.internal.DtoTypeAdapterFactory;

public class DtoTypeAdapterFactoryTest {
  static {
    DtoTypeAdapterFactory.register(Model.class, ModelDTO.class, m -> new ModelDTO(m));
  }
  private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new DtoTypeAdapterFactory()).create();

  @Test
  public void test() {
    var person = new Model("Josh");
    var wantJson = "{\"nickname\": \"Josh\"}";

    var gotJson = gson.toJson(person);
    Assertions.assertThat(JsonParser.parseString(gotJson))
        .as("serialized")
        .isEqualTo(JsonParser.parseString(wantJson));

    var gotModel = gson.fromJson(gotJson, Model.class);
    Assertions.assertThat(gotModel).as("deserialized").isEqualTo(person);
  }
}

record Model(String name) {
}

record ModelDTO(String nickname) implements DtoTypeAdapterFactory.DTO<Model> {
  ModelDTO(Model m) {
    this(m.name());
  }

  @Override
  public Model toModel() {
    return new Model(nickname);
  }
}
