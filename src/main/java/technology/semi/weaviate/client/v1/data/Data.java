package technology.semi.weaviate.client.v1.data;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.data.api.ObjectCreator;
import technology.semi.weaviate.client.v1.data.api.ObjectDeleter;
import technology.semi.weaviate.client.v1.data.api.ObjectUpdater;
import technology.semi.weaviate.client.v1.data.api.ObjectValidator;
import technology.semi.weaviate.client.v1.data.api.ObjectsGetter;
import technology.semi.weaviate.client.v1.data.api.ReferenceCreator;
import technology.semi.weaviate.client.v1.data.api.ReferenceDeleter;
import technology.semi.weaviate.client.v1.data.api.ReferenceReplacer;
import technology.semi.weaviate.client.v1.data.builder.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.data.api.ObjectsChecker;

public class Data {
  private final Config config;
  private final String version;

  public Data(Config config, String version) {
    this.config = config;
    this.version = version;
  }

  public ObjectCreator creator() {
    return new ObjectCreator(config, version);
  }

  public ObjectsGetter objectsGetter() {
    return new ObjectsGetter(config, version);
  }

  public ObjectsChecker checker() {
    return new ObjectsChecker(config, version);
  }

  public ObjectDeleter deleter() {
    return new ObjectDeleter(config, version);
  }

  public ObjectUpdater updater() {
    return new ObjectUpdater(config, version);
  }

  public ObjectValidator validator() {
    return new ObjectValidator(config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder();
  }

  public ReferenceCreator referenceCreator() {
    return new ReferenceCreator(config);
  }

  public ReferenceReplacer referenceReplacer() {
    return new ReferenceReplacer(config);
  }

  public ReferenceDeleter referenceDeleter() {
    return new ReferenceDeleter(config);
  }
}
