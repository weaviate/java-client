package io.weaviate.client.v1.batch.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.v1.batch.model.BatchReference;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReferencePayloadBuilderTest {

    @Mock
    private BeaconPath beaconPathMock;

    @Before
    public void setUp() {
        Mockito.when(beaconPathMock.buildBatchFrom(Mockito.any(BeaconPath.Params.class)))
                .thenReturn("weaviate://beacon-mock-batch-from");
        Mockito.when(beaconPathMock.buildBatchTo(Mockito.any(BeaconPath.Params.class)))
                .thenReturn("weaviate://beacon-mock-batch-to");
    }

    @Test
    public void shouldHaveBeaconsFromBeaconPath() {
        ReferencePayloadBuilder builder = new ReferencePayloadBuilder(beaconPathMock)
                .withFromID("someFromId")
                .withFromClassName("someFromClass")
                .withFromRefProp("someFromProperty")
                .withToID("someToId")
                .withToClassName("someToClass");

        BatchReference payload = builder.payload();

        assertThat(payload.getFrom()).isEqualTo("weaviate://beacon-mock-batch-from");
        assertThat(payload.getTo()).isEqualTo("weaviate://beacon-mock-batch-to");
    }

    @Test
    public void shouldHaveDeprecatedBeacons() {
        ReferencePayloadBuilder builder = new ReferencePayloadBuilder()
                .withFromID("someFromId")
                .withFromClassName("someFromClass")
                .withFromRefProp("someFromProperty")
                .withToID("someToId")
                .withToClassName("someToClass");

        BatchReference payload = builder.payload();

        assertThat(payload.getFrom()).isEqualTo("weaviate://localhost/someFromClass/someFromId/someFromProperty");
        assertThat(payload.getTo()).isEqualTo("weaviate://localhost/someToId");
    }
}
