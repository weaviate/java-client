package io.weaviate.integration.client;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.InvertedIndexConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tokenization;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WeaviateTestDocumentPassageSchema {

  public final String DOCUMENT = "Document";
  public final String[] DOCUMENT_IDS = new String[] {
    "00000000-0000-0000-0000-00000000000a",
    "00000000-0000-0000-0000-00000000000b",
    "00000000-0000-0000-0000-00000000000c",
    "00000000-0000-0000-0000-00000000000d",

  };
  public final String PASSAGE = "Passage";
  public final String[] PASSAGE_IDS = new String[] {
    "00000000-0000-0000-0000-000000000001",
    "00000000-0000-0000-0000-000000000002",
    "00000000-0000-0000-0000-000000000003",
    "00000000-0000-0000-0000-000000000004",
    "00000000-0000-0000-0000-000000000005",
    "00000000-0000-0000-0000-000000000006",
    "00000000-0000-0000-0000-000000000007",
    "00000000-0000-0000-0000-000000000008",
    "00000000-0000-0000-0000-000000000009",
    "00000000-0000-0000-0000-000000000010",
    "00000000-0000-0000-0000-000000000011",
    "00000000-0000-0000-0000-000000000012",
    "00000000-0000-0000-0000-000000000013",
    "00000000-0000-0000-0000-000000000014",
    "00000000-0000-0000-0000-000000000015",
    "00000000-0000-0000-0000-000000000016",
    "00000000-0000-0000-0000-000000000017",
    "00000000-0000-0000-0000-000000000018",
    "00000000-0000-0000-0000-000000000019",
    "00000000-0000-0000-0000-000000000020"
  };

  public void createDocumentClass(WeaviateClient client) {
    Property titleProperty = Property.builder()
      .dataType(Collections.singletonList(DataType.TEXT))
      .name("title")
      .tokenization(Tokenization.FIELD)
      .build();
    WeaviateClass document = WeaviateClass.builder()
      .className(DOCUMENT)
      .properties(Collections.singletonList(titleProperty))
      .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
      .build();
    Result<Boolean> documentCreateStatus = client.schema().classCreator().withClass(document).run();
    assertNotNull(documentCreateStatus);
    assertTrue(documentCreateStatus.getResult());
  }

  public void createPassageClass(WeaviateClient client) {
    Property contentProperty = Property.builder()
      .dataType(Collections.singletonList(DataType.TEXT))
      .name("content")
      .tokenization(Tokenization.FIELD)
      .build();
    Property typeProperty = Property.builder()
      .dataType(Collections.singletonList(DataType.TEXT))
      .name("type")
      .tokenization(Tokenization.FIELD)
      .build();
    Property ofDocumentProperty = Property.builder()
      .dataType(Collections.singletonList(DOCUMENT))
      .name("ofDocument")
      .build();
    WeaviateClass document = WeaviateClass.builder()
      .className(PASSAGE)
      .properties(Arrays.asList(contentProperty, typeProperty, ofDocumentProperty))
      .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
      .build();
    Result<Boolean> documentCreateStatus = client.schema().classCreator().withClass(document).run();
    assertNotNull(documentCreateStatus);
    assertTrue(documentCreateStatus.getResult());
  }

  public void createSchema(WeaviateClient client) {
    createDocumentClass(client);
    createPassageClass(client);
  }

  public void insertData(WeaviateClient client) {
    WeaviateObject[] documents = new WeaviateObject[DOCUMENT_IDS.length];
    for(int i = 0; i < DOCUMENT_IDS.length; i++) {
      String title = String.format("Title of the document %s", i);
      WeaviateObject document = WeaviateObject.builder()
        .id(DOCUMENT_IDS[i])
        .className(DOCUMENT)
        .properties(new HashMap<String, Object>() {{
          put("title", title);
        }}).build();
      documents[i] = document;
    }
    WeaviateObject[] passages = new WeaviateObject[PASSAGE_IDS.length];
    for(int i = 0; i < PASSAGE_IDS.length; i++) {
      String content = String.format("Passage content %s", i);
      WeaviateObject passage = WeaviateObject.builder()
        .id(PASSAGE_IDS[i])
        .className(PASSAGE)
        .properties(new HashMap<String, Object>() {{
          put("content", content);
          put("type", "document-passage");
        }}).build();
      passages[i] = passage;
    }
    Result<ObjectGetResponse[]> insertStatus = client.batch().objectsBatcher()
      .withObjects(documents)
      .withObjects(passages)
      .run();
    assertNotNull(insertStatus);
    assertNull(insertStatus.getError());
    assertNotNull(insertStatus.getResult());
    // first 10 passages assign to document 1
    createReferences(client, documents[0], Arrays.copyOfRange(passages, 0, 10));
    // next 4 passages assign to document 2
    createReferences(client, documents[1], Arrays.copyOfRange(passages, 10, 14));
  }

  private void createReferences(WeaviateClient client, WeaviateObject document, WeaviateObject[] passages) {
    SingleRef ref = client.data().referencePayloadBuilder()
      .withID(document.getId()).withClassName(DOCUMENT).payload();
    for (WeaviateObject passage : passages) {
      Result<Boolean> createOfDocumentRef = client.data().referenceCreator()
        .withID(passage.getId())
        .withClassName(PASSAGE)
        .withReferenceProperty("ofDocument")
        .withReference(ref)
        .run();
      assertNotNull(createOfDocumentRef);
      assertNull(createOfDocumentRef.getError());
      assertTrue(createOfDocumentRef.getResult());
    }
  }

  public void createAndInsertData(WeaviateClient client) {
    createSchema(client);
    insertData(client);
  }

  public void cleanupWeaviate(WeaviateClient client) {
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
  }
}
