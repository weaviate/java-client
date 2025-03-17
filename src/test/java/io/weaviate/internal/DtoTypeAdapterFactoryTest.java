package io.weaviate.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.internal.DtoTypeAdapterFactory;

@RunWith(JParamsTestRunner.class)
public class DtoTypeAdapterFactoryTest {
  /** Person should be serialized to PersonDto. */
  record Person(String name) {
  }

  record PersonDto(String nickname) implements DtoTypeAdapterFactory.Dto<Person> {
    PersonDto(Person p) {
      this(p.name);
    }

    @Override
    public Person toModel() {
      return new Person(nickname);
    }
  }

  /** Car's DTO is a nested record. */
  record Car(String brand) {
    record CarDto(String manufacturer, Integer version) implements DtoTypeAdapterFactory.Dto<Car> {
      CarDto(Car c) {
        this(c.brand, 1);
      }

      @Override
      public Car toModel() {
        return new Car(manufacturer);
      }
    }
  }

  /** Normal does not have a DTO and should be serialized as usual. */
  record Normal(String key, String value) {
  }

  static {
    DtoTypeAdapterFactory.register(Person.class, PersonDto.class, m -> new PersonDto(m));
    DtoTypeAdapterFactory.register(Car.class, Car.CarDto.class, m -> new Car.CarDto(m));
  }

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new DtoTypeAdapterFactory())
      .create();

  public static Object[][] testCases() {
    return new Object[][] {
        { new Person("Josh"), "{\"nickname\": \"Josh\"}" },
        { new Car("Porsche"), "{\"manufacturer\": \"Porsche\", \"version\": 1}" },
        { new Normal("foo", "bar"), "{\"key\": \"foo\", \"value\": \"bar\"}" },
    };
  }

  @Test
  @DataMethod(source = DtoTypeAdapterFactoryTest.class, method = "testCases")
  public void testRoundtrip(Object model, String wantJson) {
    var gotJson = gson.toJson(model);
    Assertions.assertThat(JsonParser.parseString(gotJson))
        .as("serialized")
        .isEqualTo(JsonParser.parseString(wantJson));

    var deserialized = gson.fromJson(gotJson, model.getClass());
    Assertions.assertThat(deserialized).as("deserialized").isEqualTo(model);
  }
}
