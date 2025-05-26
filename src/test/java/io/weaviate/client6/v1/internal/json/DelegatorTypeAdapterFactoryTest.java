package io.weaviate.client6.v1.internal.json;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

@RunWith(JParamsTestRunner.class)
public class DelegatorTypeAdapterFactoryTest {

  public static Object[][] testCases() {
    return new Object[][] {
        { new Person("Josh"), "{\"nickname\": \"Josh\"}" },
    };
  }

  @Test
  @DataMethod(source = DelegatorTypeAdapterFactoryTest.class, method = "testCases")
  public void test_toJson(Object model, String wantJson) {
    var gson = new GsonBuilder()
        .registerTypeAdapterFactory(new DelegatorTypeAdapterFactory())
        .create();

    var gotJson = gson.toJson(model);

    compareJson(wantJson, gotJson);
  }

  @Test
  @DataMethod(source = DelegatorTypeAdapterFactoryTest.class, method = "testCases")
  public void test_fromJson(Object want, String in) {
    var gson = new GsonBuilder()
        .registerTypeAdapterFactory(new DelegatorTypeAdapterFactory())
        .create();

    var got = gson.fromJson(in, Person.class);

    Assertions.assertThat(got).isEqualTo(want);
  }

  private static void compareJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }

  @DelegateJson(PersonDto.class)
  static record Person(String name) {
  }

  static class PersonDto extends JsonDelegate<Person> {
    public final String nickname;

    public PersonDto(Person p) {
      super(p);
      this.nickname = p.name;
    }

    @Override
    public Person toModel() {
      return new Person(nickname);
    }
  }
}
