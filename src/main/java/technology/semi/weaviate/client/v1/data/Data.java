package technology.semi.weaviate.client.v1.data;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.data.api.ObjectCreator;
import technology.semi.weaviate.client.v1.data.api.ObjectDeleter;
import technology.semi.weaviate.client.v1.data.api.ObjectUpdater;
import technology.semi.weaviate.client.v1.data.api.ObjectValidator;
import technology.semi.weaviate.client.v1.data.api.ObjectsGetter;

public class Data {
  private Config config;

  public Data(Config config) {
    this.config = config;
  }

  public ObjectCreator creator() {
    return new ObjectCreator(config);
  }

  public ObjectsGetter objectsGetter() {
    return new ObjectsGetter(config);
  }

  public ObjectDeleter deleter() {
    return new ObjectDeleter(config);
  }

  public ObjectUpdater updater() {
    return new ObjectUpdater(config);
  }

  public ObjectValidator validator() {
    return new ObjectValidator(config);
  }
}
