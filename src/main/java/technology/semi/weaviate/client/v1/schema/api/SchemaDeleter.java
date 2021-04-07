package technology.semi.weaviate.client.v1.schema.api;


import java.util.List;
import technology.semi.weaviate.client.v1.schema.api.model.Class;
import technology.semi.weaviate.client.v1.schema.api.model.Schema;

public class SchemaDeleter {
  private SchemaGetter schemaGetter;
  private ClassDeleter classDeleter;

  public SchemaDeleter(SchemaGetter schemaGetter, ClassDeleter classDeleter) {
    this.schemaGetter = schemaGetter;
    this.classDeleter = classDeleter;
  }

  public Boolean run() {
    Schema schema = schemaGetter.run();
    List<Class> classes = schema.getClasses();
    if (classes != null) {
      for (Class clazz : classes) {
        Boolean deleteClass = classDeleter.withClassName(clazz.getClassName()).run();
        if (!deleteClass) {
          return false;
        }
      }
    }
    return true;
  }
}
