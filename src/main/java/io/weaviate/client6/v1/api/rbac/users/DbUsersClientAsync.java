package io.weaviate.client6.v1.api.rbac.users;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class DbUsersClientAsync extends NamespacedUsersClientAsync {

  public DbUsersClientAsync(RestTransport restTransport) {
    super(restTransport, UserType.DB_USER);
  }

  /**
   * Create a new "db" user.
   *
   * @param userId User ID.
   * @return API key for the created user.
   */
  public CompletableFuture<String> create(String userId) throws IOException {
    return this.restTransport.performRequestAsync(new CreateDbUserRequest(userId), CreateDbUserRequest._ENDPOINT);
  }

  /**
   * Delete a "db" user.
   *
   * @param userId User ID.
   */
  public CompletableFuture<Void> delete(String userId) throws IOException {
    return this.restTransport.performRequestAsync(new DeleteDbUserRequest(userId), DeleteDbUserRequest._ENDPOINT);
  }

  /**
   * Activate a "db" user.
   *
   * @param userId User ID.
   */
  public CompletableFuture<Void> activate(String userId) throws IOException {
    return this.restTransport.performRequestAsync(new ActivateDbUserRequest(userId), ActivateDbUserRequest._ENDPOINT);
  }

  /**
   * Deactivate a "db" user.
   *
   * @param userId User ID.
   */
  public CompletableFuture<Void> deactivate(String userId) throws IOException {
    return this.restTransport.performRequestAsync(new DeactivateDbUserRequest(userId),
        DeactivateDbUserRequest._ENDPOINT);
  }

  /**
   * Rotate API key of the "db" user.
   *
   * @param userId User ID.
   */
  public CompletableFuture<String> rotateKey(String userId) throws IOException {
    return this.restTransport.performRequestAsync(new RotateDbUserKeyRequest(userId), RotateDbUserKeyRequest._ENDPOINT);
  }

  /**
   * Fetch "db" user info.
   *
   * @param userId User ID.
   */
  public CompletableFuture<Optional<DbUser>> byName(String userId) throws IOException {
    return this.restTransport.performRequestAsync(GetDbUserRequest.of(userId), GetDbUserRequest._ENDPOINT);
  }

  /**
   * Fetch "db" user info.
   *
   * @param userId User ID.
   * @param fn     Lambda expression for optional parameters.
   */
  public CompletableFuture<Optional<DbUser>> byName(String userId,
      Function<GetDbUserRequest.Builder, ObjectBuilder<GetDbUserRequest>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(GetDbUserRequest.of(userId, fn), GetDbUserRequest._ENDPOINT);
  }

  /** List all "db" users. */
  public CompletableFuture<List<DbUser>> list()
      throws IOException {
    return this.restTransport.performRequestAsync(ListDbUsersRequest.of(), ListDbUsersRequest._ENDPOINT);
  }

  /**
   * List all "db" users.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public CompletableFuture<List<DbUser>> list(
      Function<ListDbUsersRequest.Builder, ObjectBuilder<ListDbUsersRequest>> fn)
      throws IOException {
    return this.restTransport.performRequestAsync(ListDbUsersRequest.of(fn), ListDbUsersRequest._ENDPOINT);
  }
}
