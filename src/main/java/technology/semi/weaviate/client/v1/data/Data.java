package technology.semi.weaviate.client.v1.data;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.data.api.ObjectCreator;
import technology.semi.weaviate.client.v1.data.api.ObjectDeleter;
import technology.semi.weaviate.client.v1.data.api.ObjectUpdater;
import technology.semi.weaviate.client.v1.data.api.ObjectValidator;
import technology.semi.weaviate.client.v1.data.api.ObjectsChecker;
import technology.semi.weaviate.client.v1.data.api.ObjectsGetter;
import technology.semi.weaviate.client.v1.data.api.ReferenceCreator;
import technology.semi.weaviate.client.v1.data.api.ReferenceDeleter;
import technology.semi.weaviate.client.v1.data.api.ReferenceReplacer;
import technology.semi.weaviate.client.v1.data.builder.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.data.util.ObjectsPath;
import technology.semi.weaviate.client.v1.data.util.ReferencesPath;

public class Data {
  private final Config config;
  private final ObjectsPath objectsPath;
  private final ReferencesPath referencesPath;
  private final BeaconPath beaconPath;

  public Data(Config config, DbVersionSupport dbVersionSupport) {
    this.config = config;
    this.objectsPath = new ObjectsPath(dbVersionSupport);
    this.referencesPath = new ReferencesPath(dbVersionSupport);
    this.beaconPath = new BeaconPath(dbVersionSupport);
  }

  public ObjectCreator creator() {
    return new ObjectCreator(config, objectsPath);
  }

  public ObjectsGetter objectsGetter() {
    return new ObjectsGetter(config, objectsPath);
  }

  public ObjectsChecker checker() {
    return new ObjectsChecker(config, objectsPath);
  }

  public ObjectDeleter deleter() {
    return new ObjectDeleter(config, objectsPath);
  }

  public ObjectUpdater updater() {
    return new ObjectUpdater(config, objectsPath);
  }

  public ObjectValidator validator() {
    return new ObjectValidator(config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferenceCreator referenceCreator() {
    return new ReferenceCreator(config, referencesPath);
  }

  public ReferenceReplacer referenceReplacer() {
    return new ReferenceReplacer(config, referencesPath);
  }

  public ReferenceDeleter referenceDeleter() {
    return new ReferenceDeleter(config, referencesPath);
  }
}
