package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class NearVectorArgumentTest extends TestCase {

    @Test
    public void testBuild() {
        // given
        NearVectorArgument nearVector = NearVectorArgument.builder()
                .vector(new Float[]{1f, 2f, 3f}).certainty(0.8f).build();
        // when
        String arg = nearVector.build();
        // then
        Assert.assertEquals("nearVector: {vector: [1.0, 2.0, 3.0] certainty: 0.8}", arg);
    }

    @Test
    public void testBuildWithNoCertainty() {
        NearVectorArgument nearVector = NearVectorArgument.builder()
                .vector(new Float[]{1f, 2f, 3f}).build();
        // when
        String arg = nearVector.build();
        // then
        Assert.assertEquals("nearVector: {vector: [1.0, 2.0, 3.0]}", arg);
    }
}
