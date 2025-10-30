package io.weaviate.client6.v1.api.backup;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListBackupsRequest(String backend, boolean startingTimeAsc) {

  @SuppressWarnings("unchecked")
  public static Endpoint<ListBackupsRequest, List<Backup>> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/backups/" + request.backend,
      request -> new HashMap<>() {
        {
          if (request.startingTimeAsc) {
            put("order", "asc");
          }
        }
      },
      (statusCode, response) -> (List<Backup>) JSON.deserialize(
          response, TypeToken.getParameterized(List.class, Backup.class)));

  public static ListBackupsRequest of(String backend) {
    return of(backend, ObjectBuilder.identity());
  }

  public static ListBackupsRequest of(String backend, Function<Builder, ObjectBuilder<ListBackupsRequest>> fn) {
    return fn.apply(new Builder(backend)).build();
  }

  public ListBackupsRequest(Builder builder) {
    this(builder.backend, builder.startingTimeAsc);
  }

  public static class Builder implements ObjectBuilder<ListBackupsRequest> {
    private final String backend;
    private boolean startingTimeAsc = false;

    public Builder(String backend) {
      this.backend = backend;
    }

    /** Sort the backups by their starting time, oldest to newest. */
    public Builder sortByStartingTimeAsc(boolean enable) {
      this.startingTimeAsc = enable;
      return this;
    }

    @Override
    public ListBackupsRequest build() {
      return new ListBackupsRequest(this);
    }
  }
}
