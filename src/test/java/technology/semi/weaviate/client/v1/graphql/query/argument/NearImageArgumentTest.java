package technology.semi.weaviate.client.v1.graphql.query.argument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.junit.Test;

public class NearImageArgumentTest extends TestCase {

  @Test
  public void testBuild() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String expected = String.format("nearImage: {image: \"%s\"}", base64File);
    // when
    String nearImage = NearImageArgument.builder().imageFile(imageFile).build().build();
    // then
    assertNotNull(nearImage);
    assertEquals(expected, nearImage);
  }

  @Test
  public void testBuildWithCertainty() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    Float certainty = 0.5f;
    String expected = String.format("nearImage: {image: \"%s\" certainty: %s}", base64File, certainty);
    // when
    String nearImage = NearImageArgument.builder().imageFile(imageFile).certainty(certainty).build().build();
    // then
    assertNotNull(nearImage);
    assertEquals(expected, nearImage);
  }

  @Test
  public void testBuildWithImage() throws FileNotFoundException {
    // given
    String image = "iVBORw0KGgoAAAANS";
    String expected = String.format("nearImage: {image: \"%s\"}", image);
    // when
    String nearImage = NearImageArgument.builder().image(image).build().build();
    // then
    assertNotNull(nearImage);
    assertEquals(expected, nearImage);
  }

  @Test
  public void testBuildWithBase64DataImage() throws FileNotFoundException {
    // given
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expected = "nearImage: {image: \"iVBORw0KGgoAAAANS\"}";
    // when
    String nearImage = NearImageArgument.builder().image(image).build().build();
    // then
    assertNotNull(nearImage);
    assertEquals(expected, nearImage);
  }

  @Test
  public void testBuildWithImageAndCertainty() throws FileNotFoundException {
    // given
    String image = "iVBORw0KGgoAAAANS";
    Float certainty = 0.5f;
    String expected = String.format("nearImage: {image: \"%s\" certainty: %s}", image, certainty);
    // when
    String nearImage = NearImageArgument.builder().image(image).certainty(certainty).build().build();
    // then
    assertNotNull(nearImage);
    assertEquals(expected, nearImage);
  }

  @Test
  public void testBuildWithBadFile() throws FileNotFoundException {
    // given
    File badImageFile = new File("");
    // when
    String nearImage = NearImageArgument.builder().imageFile(badImageFile).build().build();
    // then
    assertNotNull(nearImage);
    // builder will return a faulty nearImage arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearImage: {}", nearImage);
  }

  @Test
  public void testBuildWithoutAll() throws FileNotFoundException {
    // given
    // when
    String nearImage = NearImageArgument.builder().build().build();
    // then
    assertNotNull(nearImage);
    // builder will return a faulty nearImage arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearImage: {}", nearImage);
  }
}
