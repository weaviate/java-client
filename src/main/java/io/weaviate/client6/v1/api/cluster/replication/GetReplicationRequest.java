package io.weaviate.client6.v1.api.cluster.replication;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetReplicationRequest(UUID uuid, boolean includeHistory) {

  static final Endpoint<GetReplicationRequest, Optional<Replication>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      request -> "GET",
      request -> "/replication/replicate/" + request.uuid(),
      request -> Collections.singletonMap("includeHistory", request.includeHistory()),
      Replication.class);

  public static GetReplicationRequest of(UUID uuid) {
    return of(uuid, ObjectBuilder.identity());
  }

  public static GetReplicationRequest of(UUID uuid, Function<Builder, ObjectBuilder<GetReplicationRequest>> fn) {
    return fn.apply(new Builder(uuid)).build();
  }

  public GetReplicationRequest(Builder builder) {
    this(builder.uuid, builder.includeHistory);
  }

  public static class Builder implements ObjectBuilder<GetReplicationRequest> {
    private final UUID uuid;
    private boolean includeHistory = false;

    public Builder(UUID uuid) {
      this.uuid = uuid;
    }

    /**
     * Include history of statuses for this replication.
     *
     * @see Replication#history
     */
    public Builder includeHistory(boolean includeHistory) {
      this.includeHistory = includeHistory;
      return this;
    }

    @Override
    public GetReplicationRequest build() {
      return new GetReplicationRequest(this);
    }
  }

}
