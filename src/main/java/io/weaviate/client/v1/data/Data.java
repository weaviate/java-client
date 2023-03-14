package io.weaviate.client.v1.data;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.data.api.ObjectCreator;
import io.weaviate.client.v1.data.api.ObjectDeleter;
import io.weaviate.client.v1.data.api.ObjectUpdater;
import io.weaviate.client.v1.data.api.ObjectValidator;
import io.weaviate.client.v1.data.api.ObjectsChecker;
import io.weaviate.client.v1.data.api.ObjectsGetter;
import io.weaviate.client.v1.data.api.ReferenceCreator;
import io.weaviate.client.v1.data.api.ReferenceDeleter;
import io.weaviate.client.v1.data.api.ReferenceReplacer;
import io.weaviate.client.v1.data.builder.ReferencePayloadBuilder;
import io.weaviate.client.v1.data.util.ObjectsPath;
import io.weaviate.client.v1.data.util.ReferencesPath;
import io.weaviate.client.Config;

public class Data {
  private final Config config;
  private final HttpClient httpClient;
  private final ObjectsPath objectsPath;
  private final ReferencesPath referencesPath;
  private final BeaconPath beaconPath;

  public Data(HttpClient httpClient, Config config, DbVersionSupport dbVersionSupport) {
    this.config = config;
    this.httpClient = httpClient;
    this.objectsPath = new ObjectsPath(dbVersionSupport);
    this.referencesPath = new ReferencesPath(dbVersionSupport);
    this.beaconPath = new BeaconPath(dbVersionSupport);
  }

  public ObjectCreator creator() {
    return new ObjectCreator(httpClient, config, objectsPath);
  }

  public ObjectsGetter objectsGetter() {
    return new ObjectsGetter(httpClient, config, objectsPath);
  }

  public ObjectsChecker checker() {
    return new ObjectsChecker(httpClient, config, objectsPath);
  }

  public ObjectDeleter deleter() {
    return new ObjectDeleter(httpClient, config, objectsPath);
  }

  public ObjectUpdater updater() {
    return new ObjectUpdater(httpClient, config, objectsPath);
  }

  public ObjectValidator validator() {
    return new ObjectValidator(httpClient, config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferenceCreator referenceCreator() {
    return new ReferenceCreator(httpClient, config, referencesPath);
  }

  public ReferenceReplacer referenceReplacer() {
    return new ReferenceReplacer(httpClient, config, referencesPath);
  }

  public ReferenceDeleter referenceDeleter() {
    return new ReferenceDeleter(httpClient, config, referencesPath);
  }
}
