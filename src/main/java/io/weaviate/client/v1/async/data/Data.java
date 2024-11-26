package io.weaviate.client.v1.async.data;

import io.weaviate.client.Config;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.async.data.api.ObjectCreator;
import io.weaviate.client.v1.async.data.api.ObjectDeleter;
import io.weaviate.client.v1.async.data.api.ObjectUpdater;
import io.weaviate.client.v1.async.data.api.ObjectValidator;
import io.weaviate.client.v1.async.data.api.ObjectsChecker;
import io.weaviate.client.v1.async.data.api.ObjectsGetter;
import io.weaviate.client.v1.async.data.api.ReferenceCreator;
import io.weaviate.client.v1.async.data.api.ReferenceDeleter;
import io.weaviate.client.v1.async.data.api.ReferenceReplacer;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.data.builder.ReferencePayloadBuilder;
import io.weaviate.client.v1.data.util.ObjectsPath;
import io.weaviate.client.v1.data.util.ReferencesPath;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Data {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;
  private final ObjectsPath objectsPath;
  private final ReferencesPath referencesPath;
  private final BeaconPath beaconPath;

  public Data(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider, DbVersionSupport dbVersionSupport) {
    this.client = client;
    this.config = config;
    this.tokenProvider = tokenProvider;
    this.objectsPath = new ObjectsPath(dbVersionSupport);
    this.referencesPath = new ReferencesPath(dbVersionSupport);
    this.beaconPath = new BeaconPath(dbVersionSupport);
  }

  public ObjectCreator creator() {
    return new ObjectCreator(client, config, tokenProvider, objectsPath);
  }

  public ObjectsGetter objectsGetter() {
    return new ObjectsGetter(client, config, tokenProvider, objectsPath);
  }

  public ObjectsChecker checker() {
    return new ObjectsChecker(client, config, tokenProvider, objectsPath);
  }

  public ObjectDeleter deleter() {
    return new ObjectDeleter(client, config, tokenProvider, objectsPath);
  }

  public ObjectUpdater updater() {
    return new ObjectUpdater(client, config, tokenProvider, objectsPath);
  }

  public ObjectValidator validator() {
    return new ObjectValidator(client, config, tokenProvider);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferenceCreator referenceCreator() {
    return new ReferenceCreator(client, config, tokenProvider, referencesPath);
  }

  public ReferenceReplacer referenceReplacer() {
    return new ReferenceReplacer(client, config, tokenProvider, referencesPath);
  }

  public ReferenceDeleter referenceDeleter() {
    return new ReferenceDeleter(client, config, tokenProvider, referencesPath);
  }
}
