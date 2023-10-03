package io.weaviate.integration.client.schema;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

class NestedObjectsUtils {

  WeaviateClass nestedClassEntireSchema(String className) {
    return WeaviateClass.builder()
      .className(className)
      .properties(Arrays.asList(
        Property.builder()
          .name("name")
          .dataType(Arrays.asList(DataType.TEXT))
          .build(),
        Property.builder()
          .name("objectProperty")
          .dataType(Arrays.asList(DataType.OBJECT))
          .nestedProperties(Arrays.asList(
            Property.NestedProperty.builder()
              .name("nestedInt")
              .dataType(Arrays.asList(DataType.INT))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedNumber")
              .dataType(Arrays.asList(DataType.NUMBER))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedText")
              .dataType(Arrays.asList(DataType.TEXT))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedObjects")
              .dataType(Arrays.asList(DataType.OBJECT_ARRAY))
              .nestedProperties(Arrays.asList(
                Property.NestedProperty.builder()
                  .name("nestedBoolLvl2")
                  .dataType(Arrays.asList(DataType.BOOLEAN))
                  .build(),
                Property.NestedProperty.builder()
                  .name("nestedDateLvl2")
                  .dataType(Arrays.asList(DataType.DATE))
                  .build(),
                Property.NestedProperty.builder()
                  .name("nestedNumbersLvl2")
                  .dataType(Arrays.asList(DataType.NUMBER_ARRAY))
                  .build()
              ))
              .build()
          ))
          .build()
      ))
      .build();
  }

  WeaviateClass nestedClassPartialSchema1(String className) {
    return WeaviateClass.builder()
      .className(className)
      .properties(Arrays.asList(
        Property.builder()
          .name("name")
          .dataType(Arrays.asList(DataType.TEXT))
          .build(),
        Property.builder()
          .name("objectProperty")
          .dataType(Arrays.asList(DataType.OBJECT))
          .nestedProperties(Arrays.asList(
            Property.NestedProperty.builder()
              .name("nestedInt")
              .dataType(Arrays.asList(DataType.INT))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedText")
              .dataType(Arrays.asList(DataType.TEXT))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedObjects")
              .dataType(Arrays.asList(DataType.OBJECT_ARRAY))
              .nestedProperties(Arrays.asList(
                Property.NestedProperty.builder()
                  .name("nestedBoolLvl2")
                  .dataType(Arrays.asList(DataType.BOOLEAN))
                  .build(),
                Property.NestedProperty.builder()
                  .name("nestedNumbersLvl2")
                  .dataType(Arrays.asList(DataType.NUMBER_ARRAY))
                  .build()
              ))
              .build()
          ))
          .build()
      ))
      .build();
  }

  WeaviateClass nestedClassPartialSchema2(String className) {
    return WeaviateClass.builder()
      .className(className)
      .properties(Arrays.asList(
        Property.builder()
          .name("name")
          .dataType(Arrays.asList(DataType.TEXT))
          .build(),
        Property.builder()
          .name("objectProperty")
          .dataType(Arrays.asList(DataType.OBJECT))
          .nestedProperties(Arrays.asList(
            Property.NestedProperty.builder()
              .name("nestedNumber")
              .dataType(Arrays.asList(DataType.NUMBER))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedText")
              .dataType(Arrays.asList(DataType.TEXT))
              .build(),
            Property.NestedProperty.builder()
              .name("nestedObjects")
              .dataType(Arrays.asList(DataType.OBJECT_ARRAY))
              .nestedProperties(Arrays.asList(
                Property.NestedProperty.builder()
                  .name("nestedDateLvl2")
                  .dataType(Arrays.asList(DataType.DATE))
                  .build(),
                Property.NestedProperty.builder()
                  .name("nestedNumbersLvl2")
                  .dataType(Arrays.asList(DataType.NUMBER_ARRAY))
                  .build()
              ))
              .build()
          ))
          .build()
      ))
      .build();
  }

  WeaviateObject nestedObject1(String className) {
    Map<String, Object> nestedPropsLvl2 = new HashMap<>();
    nestedPropsLvl2.put("nestedBoolLvl2", false);
    nestedPropsLvl2.put("nestedNumbersLvl2", Arrays.asList(1.1, 11.11));

    Map<String, Object> nestedPropsLvl1 = new HashMap<>();
    nestedPropsLvl1.put("nestedInt", 111);
    nestedPropsLvl1.put("nestedText", "some text 1");
    nestedPropsLvl1.put("nestedObjects", Arrays.asList(nestedPropsLvl2));

    Map<String, Object> props = new HashMap<>();
    props.put("name", "object1");
    props.put("objectProperty", nestedPropsLvl1);

    return WeaviateObject.builder()
      .className(className)
      .id("040f2b60-b1e8-4b4d-ba0d-14cedb5144ab")
      .properties(props)
      .build();
  }

  WeaviateObject expectedNestedObject1(String className) {
    // overwrite ints with doubles, as they are unmarshalled as that type
    WeaviateObject o = nestedObject1(className);
    Map<String, Object> objectProperty = (Map<String, Object>) o.getProperties().get("objectProperty");
    objectProperty.put("nestedInt", 111.0);

    return o;
  }

  WeaviateObject nestedObject2(String className) {
    Map<String, Object> nestedPropsLvl2 = new HashMap<>();
    nestedPropsLvl2.put("nestedDateLvl2", "2022-01-01T00:00:00+02:00");
    nestedPropsLvl2.put("nestedNumbersLvl2", Arrays.asList(2.2, 22.22));

    Map<String, Object> nestedPropsLvl1 = new HashMap<>();
    nestedPropsLvl1.put("nestedNumber", 222);
    nestedPropsLvl1.put("nestedText", "some text 2");
    nestedPropsLvl1.put("nestedObjects", Arrays.asList(nestedPropsLvl2));

    Map<String, Object> props = new HashMap<>();
    props.put("name", "object2");
    props.put("objectProperty", nestedPropsLvl1);

    return WeaviateObject.builder()
      .className(className)
      .id("d3ca0fc9-d392-4253-8f2a-0bce51efff80")
      .properties(props)
      .build();
  }

  WeaviateObject expectedNestedObject2(String className) {
    // overwrite ints with doubles, as they are unmarshalled as that type
    WeaviateObject o = nestedObject2(className);
    Map<String, Object> objectProperty = (Map<String, Object>) o.getProperties().get("objectProperty");
    objectProperty.put("nestedNumber", 222.0);

    return o;
  }

  void createClass(WeaviateClient client, WeaviateClass wvtClass) {
    Result<Boolean> createClass = client.schema().classCreator()
      .withClass(wvtClass)
      .run();

    assertThat(createClass).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }

  WeaviateClass getClass(WeaviateClient client, String className) {
    Result<Schema> getSchema = client.schema().getter().run();
    assertThat(getSchema).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    Optional<WeaviateClass> optionalClass = getSchema.getResult().getClasses().stream()
      .filter(c -> className.equals(c.getClassName()))
      .findFirst();
    assertThat(optionalClass).isPresent();

    return optionalClass.get();
  }

  WeaviateObject createObject(WeaviateClient client, WeaviateObject object) {
    Result<WeaviateObject> createObject = client.data().creator()
      .withID(object.getId())
      .withClassName(object.getClassName())
      .withProperties(object.getProperties())
      .run();

    assertThat(createObject).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).isNotNull();

    return createObject.getResult();
  }

  ObjectGetResponse[] batchObjects(WeaviateClient client, WeaviateObject... objects) {
    Result<ObjectGetResponse[]> batchObjects = client.batch().objectsBatcher()
      .withObjects(objects)
      .run();

    assertThat(batchObjects).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(objects.length);

    Arrays.stream(batchObjects.getResult()).forEach(obj -> {
      assertThat(obj).isNotNull()
        .extracting(ObjectGetResponse::getResult).isNotNull()
        .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
        .returns(null, ObjectsGetResponseAO2Result::getErrors);
    });

    return batchObjects.getResult();
  }

  void assertThatSchemaPropertiesHaveDataTypes(Map<String, String> expectedProps, WeaviateClass schemaClass) {
    Map<String, List<Map.Entry<String, String>>> propNumbers = expectedProps.entrySet().stream().collect(Collectors.groupingBy(entry -> {
      String[] parts = StringUtils.split(entry.getKey(), ".");
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < parts.length - 1; i++) {
        if (i != 0) {
          sb.append(".");
        }
        sb.append(parts[i]);
      }
      return sb.toString();
    }));

    propNumbers.forEach((propName, props) -> {
      if ("".equals(propName)) {
        assertThat(schemaClass.getProperties()).isNotNull()
          .hasSize(props.size());
      } else {
        String[] parts = StringUtils.split(propName, ".");
        Optional<Property> optionalProp = schemaClass.getProperties().stream()
          .filter(p -> parts[0].equals(p.getName()))
          .findFirst();
        assertThat(optionalProp).isPresent();

        List<Property.NestedProperty> nestedProps = optionalProp.get().getNestedProperties();

        for (int i = 1; i < parts.length; i++) {
          int index = i;
          Optional<Property.NestedProperty> optionalNestedProp = nestedProps.stream()
            .filter(np -> parts[index].equals(np.getName()))
            .findFirst();
          assertThat(optionalNestedProp).isPresent();
          nestedProps = optionalNestedProp.get().getNestedProperties();
        }

        assertThat(nestedProps).hasSize(props.size());
      }
    });

    expectedProps.forEach((name, dataType) -> {
      String[] parts = StringUtils.split(name, ".");
      if (parts.length > 0) {
        Optional<Property> optionalProp = schemaClass.getProperties().stream()
          .filter(p -> parts[0].equals(p.getName()))
          .findFirst();

        assertThat(optionalProp).isPresent();
        Property property = optionalProp.get();

        if (parts.length == 1) {
          assertThat(property)
            .extracting(Property::getDataType).asList()
            .first().isEqualTo(dataType);
        } else {
          Property.NestedProperty nestedProp = null;

          List<Property.NestedProperty> nestedProps = property.getNestedProperties();
          for (int i = 1; i < parts.length; i++) {
            int index = i;
            assertThat(nestedProps).isNotNull();
            Optional<Property.NestedProperty> optionalNestedProp = nestedProps.stream()
              .filter(np -> parts[index].equals(np.getName()))
              .findFirst();
            assertThat(optionalNestedProp).isPresent();
            nestedProp = optionalNestedProp.get();
            nestedProps = nestedProp.getNestedProperties();
          }

          assertThat(nestedProp).isNotNull()
            .extracting(Property.NestedProperty::getDataType).asList()
            .first().isEqualTo(dataType);
        }
      }
    });
  }

  void assertThatObjectsAreSimilar(WeaviateObject expectedObject, WeaviateObject object) {
    assertThat(object).isNotNull()
      .returns(expectedObject.getId(), WeaviateObject::getId)
      .returns(expectedObject.getClassName(), WeaviateObject::getClassName)
      .extracting(WeaviateObject::getProperties)
      .isEqualTo(expectedObject.getProperties());
  }
  void assertThatObjectsAreSimilar(WeaviateObject expectedObject, ObjectGetResponse object) {
    assertThat(object).isNotNull()
      .returns(expectedObject.getId(), ObjectGetResponse::getId)
      .returns(expectedObject.getClassName(), ObjectGetResponse::getClassName)
      .extracting(ObjectGetResponse::getProperties)
      .isEqualTo(expectedObject.getProperties());
  }
}
