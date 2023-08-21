package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.graphql.model.ExploreFields;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearAudioArgument;
import io.weaviate.client.v1.graphql.query.argument.NearDepthArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImuArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;
import io.weaviate.client.v1.graphql.query.argument.NearThermalArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVideoArgument;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExploreBuilderTest {

  @Test
  public void testBuildQuery() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .concepts(new String[]{"a1", "b2"}).force(0.1f).build();

    // when (certainty)
    NearTextArgument nearTextWithCert = NearTextArgument.builder()
      .concepts(new String[]{"a", "b"}).certainty(0.8f).moveTo(moveTo).build();
    String queryWithCert = ExploreBuilder.builder().withNearText(nearTextWithCert).fields(fields).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(nearText:{concepts:[\"a\",\"b\"] certainty:0.8 moveTo:{concepts:[\"a1\",\"b2\"] force:0.1}}){certainty,distance," +
      "beacon,className}}", queryWithCert);

    // when (distance)
    NearTextArgument nearTextWithDist = NearTextArgument.builder()
      .concepts(new String[]{"a", "b"}).distance(0.8f).moveTo(moveTo).build();
    String queryWithDist = ExploreBuilder.builder().withNearText(nearTextWithDist).fields(fields).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(nearText:{concepts:[\"a\",\"b\"] distance:0.8 moveTo:{concepts:[\"a1\",\"b2\"] force:0.1}}){certainty,distance," +
      "beacon,className}}", queryWithDist);
  }

  @Test
  public void testBuildSimpleExplore() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.DISTANCE};
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{"Cheese", "pineapple"}).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText:{concepts:[\"Cheese\",\"pineapple\"]}){certainty,beacon,distance}}", query);
  }

  @Test
  public void testBuildExploreWithLimitAndCertainty() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{"Cheese"}).certainty(0.71f).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText:{concepts:[\"Cheese\"] certainty:0.71}){beacon}}", query);
  }

  @Test
  public void testBuildExploreWithLimitAndDistance() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{"Cheese"}).distance(0.71f).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText:{concepts:[\"Cheese\"] distance:0.71}){beacon}}", query);
  }

  @Test
  public void testBuildExploreWithMove() {
    // given
    String[] concepts = new String[]{"Cheese"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .concepts(new String[]{"pizza", "pineapple"}).force(0.2f).build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters.builder()
      .concepts(new String[]{"fish"}).force(0.1f).build();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(concepts).moveTo(moveTo).moveAwayFrom(moveAwayFrom)
      .build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText:{concepts:[\"Cheese\"] " +
      "moveTo:{concepts:[\"pizza\",\"pineapple\"] force:0.2} " +
      "moveAwayFrom:{concepts:[\"fish\"] force:0.1}}){beacon}}", query);
  }

  @Test
  public void testBuildExploreWithAllParams() {
    // given
    String[] concepts = new String[]{"New Yorker"};
    Float certainty = 0.95f;
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .concepts(new String[]{"publisher", "articles"}).force(0.5f)
      .build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters.builder()
      .concepts(new String[]{"fashion", "shop"}).force(0.2f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(concepts).moveTo(moveTo).moveAwayFrom(moveAwayFrom)
      .build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText:{concepts:[\"New Yorker\"] moveTo:{concepts:[\"publisher\",\"articles\"] force:0.5} moveAwayFrom:{concepts:" +
      "[\"fashion\",\"shop\"] force:0.2}}){certainty,distance,beacon,className}}", query);
  }

  @Test
  public void testBuildExploreWithNearVector() {
    // given (certainty)
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE,
      ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearVectorArgument nearVectorWithCert = NearVectorArgument.builder()
      .vector(new Float[]{0f, 1f, 0.8f}).certainty(0.8f).build();

    // when (certainty)
    String queryWithCert = ExploreBuilder.builder()
      .fields(fields)
      .withNearVectorFilter(nearVectorWithCert).build().buildQuery();

    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(nearVector:{vector:[0.0,1.0,0.8] certainty:0.8})" +
      "{certainty,distance,beacon,className}}", queryWithCert);

    // given (distance)
    NearVectorArgument nearVectorWithDist = NearVectorArgument.builder()
      .vector(new Float[]{0f, 1f, 0.8f}).distance(0.8f).build();

    // when (distance)
    String queryWithDist = ExploreBuilder.builder()
      .fields(fields)
      .withNearVectorFilter(nearVectorWithDist).build().buildQuery();

    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Explore(nearVector:{vector:[0.0,1.0,0.8] distance:0.8})" +
      "{certainty,distance,beacon,className}}", queryWithDist);
  }

  @Test
  public void testBuildExploreWithNearObject() {
    // given (certainty)
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE,
      ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearObjectArgument nearObjectWithCert = NearObjectArgument.builder().id("some-uuid").certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = ExploreBuilder.builder()
      .fields(fields)
      .withNearObjectFilter(nearObjectWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(nearObject:{id:\"some-uuid\" certainty:0.8}){certainty,distance," +
      "beacon,className}}", queryWithCert);

    // given (distance)
    NearObjectArgument nearObjectWithDist = NearObjectArgument.builder().id("some-uuid").distance(0.8f).build();
    // when (distance)
    String queryWithDist = ExploreBuilder.builder()
      .fields(fields)
      .withNearObjectFilter(nearObjectWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Explore(nearObject:{id:\"some-uuid\" distance:0.8}){certainty,distance," +
        "beacon,className}}",
      queryWithDist);
  }

  @Test
  public void testBuildExploreWithAsk() {
    // given (certainty)
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE,
      ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    AskArgument askWithCert = AskArgument.builder().question("question?").rerank(true).certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = ExploreBuilder.builder()
      .fields(fields)
      .withAskArgument(askWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(ask:{question:\"question?\" certainty:0.8 rerank:true}){certainty,distance," +
      "beacon,className}}", queryWithCert);

    // given (distance)
    AskArgument askWithDist = AskArgument.builder().question("question?").rerank(true).distance(0.8f).build();
    // when (distance)
    String queryWithDist = ExploreBuilder.builder()
      .fields(fields)
      .withAskArgument(askWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Explore(ask:{question:\"question?\" distance:0.8 rerank:true}){certainty,distance," +
      "beacon,className}}", queryWithDist);
  }

  @Test
  public void testBuildExploreWithNearImage() {
    // given (certainty)
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.DISTANCE,
      ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearImageArgument nearImageWithCert = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = ExploreBuilder.builder()
      .fields(fields)
      .withNearImageFilter(nearImageWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Explore(nearImage:{image:\"iVBORw0KGgoAAAANS\" certainty:0.8}){certainty,distance," +
      "beacon,className}}", queryWithCert);

    // given (distance)
    NearImageArgument nearImageWithDist = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").distance(0.8f).build();
    // when (distance)
    String queryWithDist = ExploreBuilder.builder()
      .fields(fields)
      .withNearImageFilter(nearImageWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Explore(nearImage:{image:\"iVBORw0KGgoAAAANS\" distance:0.8}){certainty,distance," +
      "beacon,className}}", queryWithDist);
  }

  @Test
  public void shouldBuildExploreWithNearAudio() {
    NearAudioArgument nearAudio = NearAudioArgument.builder()
      .audio("iVBORw0KGgoAAAANS")
      .distance(0.1f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{
      ExploreFields.CERTAINTY,
      ExploreFields.DISTANCE,
      ExploreFields.BEACON,
      ExploreFields.CLASS_NAME,
    };

    String query = ExploreBuilder.builder()
      .fields(fields)
      .withNearAudioFilter(nearAudio)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Explore(nearAudio:{audio:\"iVBORw0KGgoAAAANS\" distance:0.1})" +
      "{certainty,distance,beacon,className}}");
  }

  @Test
  public void shouldBuildExploreWithNearVideo() {
    NearVideoArgument nearVideo = NearVideoArgument.builder()
      .video("iVBORw0KGgoAAAANS")
      .distance(0.1f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{
      ExploreFields.CERTAINTY,
      ExploreFields.DISTANCE,
      ExploreFields.BEACON,
      ExploreFields.CLASS_NAME,
    };

    String query = ExploreBuilder.builder()
      .fields(fields)
      .withNearVideoFilter(nearVideo)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Explore(nearVideo:{video:\"iVBORw0KGgoAAAANS\" distance:0.1})" +
      "{certainty,distance,beacon,className}}");
  }

  @Test
  public void shouldBuildExploreWithNearDepth() {
    NearDepthArgument nearDepth = NearDepthArgument.builder()
      .depth("iVBORw0KGgoAAAANS")
      .distance(0.1f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{
      ExploreFields.CERTAINTY,
      ExploreFields.DISTANCE,
      ExploreFields.BEACON,
      ExploreFields.CLASS_NAME,
    };

    String query = ExploreBuilder.builder()
      .fields(fields)
      .withNearDepthFilter(nearDepth)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Explore(nearDepth:{depth:\"iVBORw0KGgoAAAANS\" distance:0.1})" +
      "{certainty,distance,beacon,className}}");
  }

  @Test
  public void shouldBuildExploreWithNearThermal() {
    NearThermalArgument nearThermal = NearThermalArgument.builder()
      .thermal("iVBORw0KGgoAAAANS")
      .distance(0.1f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{
      ExploreFields.CERTAINTY,
      ExploreFields.DISTANCE,
      ExploreFields.BEACON,
      ExploreFields.CLASS_NAME,
    };

    String query = ExploreBuilder.builder()
      .fields(fields)
      .withNearThermalFilter(nearThermal)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Explore(nearThermal:{thermal:\"iVBORw0KGgoAAAANS\" distance:0.1})" +
      "{certainty,distance,beacon,className}}");
  }

  @Test
  public void shouldBuildExploreWithNearImu() {
    NearImuArgument nearImu = NearImuArgument.builder()
      .imu("iVBORw0KGgoAAAANS")
      .distance(0.1f)
      .build();
    ExploreFields[] fields = new ExploreFields[]{
      ExploreFields.CERTAINTY,
      ExploreFields.DISTANCE,
      ExploreFields.BEACON,
      ExploreFields.CLASS_NAME,
    };

    String query = ExploreBuilder.builder()
      .fields(fields)
      .withNearImuFilter(nearImu)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Explore(nearIMU:{imu:\"iVBORw0KGgoAAAANS\" distance:0.1})" +
      "{certainty,distance,beacon,className}}");
  }
}
