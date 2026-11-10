package io.weaviate.integration;

import java.util.List;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.tenants.Tenant;
import io.weaviate.containers.Container;
import io.weaviate.containers.Container.ContainerGroup;
import io.weaviate.containers.MinIo;
import io.weaviate.containers.Weaviate;

public class TenantsITest extends ConcurrentTest {
  private static final ContainerGroup compose = Container.compose(
      Weaviate.custom()
          .withOffloadS3(MinIo.ACCESS_KEY, MinIo.SECRET_KEY)
          .build(),
      Container.MINIO);

  private static final WeaviateClient client = compose.getClient();

  @Test
  public void test_tenantLifecycle() throws Exception {
    var nsThings = ns("Things");

    client.collections.create(
        nsThings, c -> c
            .multiTenancy(mt -> mt
                .autoTenantCreation(false)
                .autoTenantActivation(false)));

    var things = client.collections.use(nsThings);

    // No tenants at first
    Assertions.assertThat(things.tenants.list()).as("no tenants initially").isEmpty();

    var allison = Tenant.active("active-allison");
    var isaac = Tenant.inactive("inactive-isaac");
    var owen = Tenant.inactive("offloaded-owen");

    things.tenants.create(allison, isaac, owen);

    // Collection has 2 tenants creted just now.
    Assertions.assertThat(things.tenants.list()).as("list created tenants").hasSize(3);
    Assertions.assertThat(things.tenants.exists(allison.name()))
        .describedAs("%s exists", allison.name()).isTrue();
    Assertions.assertThat(things.tenants.exists(isaac.name()))
        .describedAs("%s exists", isaac.name()).isTrue();
    Assertions.assertThat(things.tenants.exists(owen.name()))
        .describedAs("%s exists", owen.name()).isTrue();

    things.tenants.activate(isaac.name());
    eventually(() -> things.tenants.get(isaac.name()).get().isActive(),
        200, 2, isaac.name() + " not activated");

    things.tenants.deactivate(allison.name());
    eventually(() -> things.tenants.get(allison.name()).get().isInactive(),
        200, 2, allison.name() + " not deactivated");

    things.tenants.offload(owen.name());
    eventually(() -> things.tenants.get(owen.name()).get().isOffloaded(),
        200, 2, owen.name() + " not offloaded");

    things.tenants.delete(allison.name(), isaac.name(), owen.name());
    Assertions.assertThat(things.tenants.list()).as("no tenants after deletion").isEmpty();
    Assertions.assertThat(things.tenants.exists(allison.name()))
        .describedAs("%s not exists", allison.name()).isFalse();
    Assertions.assertThat(things.tenants.exists(isaac.name()))
        .describedAs("%s not exists", isaac.name()).isFalse();
    Assertions.assertThat(things.tenants.exists(owen.name()))
        .describedAs("%s not exists", owen.name()).isFalse();
  }

  @Test
  public void test_updateMoreThanOneHundredTenants() throws Exception {
    var nsMany = ns("ManyTenants");
    client.collections.create(nsMany, c -> c.multiTenancy(mt -> mt.autoTenantCreation(false)));
    var many = client.collections.use(nsMany);

    List<String> names = IntStream.rangeClosed(1, 250).mapToObj(i -> "tenant-" + i).toList();
    many.tenants.create(names.stream().map(Tenant::active).toList());
    Assertions.assertThat(many.tenants.list()).as("created").hasSize(250);

    many.tenants.deactivate(names);

    eventually(() -> many.tenants.list().stream().allMatch(Tenant::isInactive),
        200, 5, "not all tenants were deactivated");

    many.tenants.activate(names);
    eventually(() -> many.tenants.list().stream().allMatch(Tenant::isActive),
        200, 5, "not all tenants were activated");
  }
}
