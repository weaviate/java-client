package technology.semi.weaviate.client.v1.data.builder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.v1.data.model.SingleRef;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReferencePayloadBuilderTest {

  @Mock
  private BeaconPath beaconPathMock;

  @Before
  public void setUp() {
    Mockito.when(beaconPathMock.buildSingle(Mockito.any(BeaconPath.Params.class)))
      .thenReturn("weaviate://beacon-mock-single");
  }

  @Test
  public void shouldHaveBeaconFromBeaconPath() {
    ReferencePayloadBuilder builder = new ReferencePayloadBuilder(beaconPathMock)
      .withID("someId")
      .withClassName("someClass");

    SingleRef payload = builder.payload();

    assertThat(payload.getBeacon()).isEqualTo("weaviate://beacon-mock-single");
  }

  @Test
  public void shouldHaveDeprecatedBeacon() {
    ReferencePayloadBuilder builder = new ReferencePayloadBuilder()
      .withID("someId")
      .withClassName("someClass");

    SingleRef payload = builder.payload();

    assertThat(payload.getBeacon()).isEqualTo("weaviate://localhost/someId");
  }
}
