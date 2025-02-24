package io.weaviate.client.v1.users.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client.base.Serializer;
import io.weaviate.client.v1.users.model.User;

@RunWith(JParamsTestRunner.class)
public class WeaviateUserTest {
  private final Serializer ser = new Serializer();

  public static Object[][] deserializationTestCases() {
    return new Object[][] {
        {
            "has username, no user_id",
            "{\"username\": \"John Doe\"}",
            new User("John Doe", new ArrayList<>()),
        },
        {
            "has user_id, no username",
            "{\"user_id\": \"john_doe\"}",
            new User("john_doe", new ArrayList<>()),
        },
        {
            "has both user_id and username",
            "{\"user_id\": \"john_doe\", \"username\": \"John Doe\"}",
            new User("john_doe", new ArrayList<>()),
        },
    };
  }

  @DataMethod(source = WeaviateUserTest.class, method = "deserializationTestCases")
  @Test
  public void testToUser(String name, String json, User want) {
    User got = ser.toResponse(json, WeaviateUser.class).toUser();
    assertEquals(want.getUserId(), got.getUserId(), "user id");
  }
}
