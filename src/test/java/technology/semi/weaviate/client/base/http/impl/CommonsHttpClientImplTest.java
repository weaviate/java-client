package technology.semi.weaviate.client.base.http.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import technology.semi.weaviate.client.base.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CommonsHttpClientImplTest {

  private static final String URL = "http://example.weaviate";
  private static final int STATUS = 200;
  private static final String JSON_PAYLOAD = "[123]";
  private static final Map<String, String> ADDITIONAL_HEADERS = new HashMap<String, String>() {{
    put("X-Some-Header-1", "SomeValue1");
    put("X-Some-Header-2", "SomeValue2");
  }};

  @Captor
  private ArgumentCaptor<HttpUriRequest> captor;
  @Mock
  private CloseableHttpClient clientMock;
  @Mock
  private CloseableHttpResponse responseMock;
  @Mock
  private StatusLine statusLineMock;


  @Before
  public void before() throws IOException {
    Mockito.when(clientMock.execute(any(HttpUriRequest.class)))
      .thenReturn(responseMock);
    Mockito.when(responseMock.getStatusLine())
      .thenReturn(statusLineMock);
    Mockito.when(statusLineMock.getStatusCode())
      .thenReturn(STATUS);
  }

  @Test
  public void shouldAddHeadersOnGetRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "*/*");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendGetRequest(URL);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEmpty(request);
  }

  @Test
  public void shouldAddHeadersOnHeadRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "*/*");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendHeadRequest(URL);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpHead.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEmpty(request);
  }

  @Test
  public void shouldAddHeadersOnPostRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "application/json");
    expectedHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendPostRequest(URL, JSON_PAYLOAD);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpPost.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEqual(request, JSON_PAYLOAD);
  }

  @Test
  public void shouldAddHeadersOnPatchRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "application/json");
    expectedHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendPatchRequest(URL, JSON_PAYLOAD);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpPatch.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEqual(request, JSON_PAYLOAD);
  }

  @Test
  public void shouldAddHeadersOnPutRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "application/json");
    expectedHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendPutRequest(URL, JSON_PAYLOAD);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpPut.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEqual(request, JSON_PAYLOAD);
  }

  @Test
  public void shouldAddHeadersOnDeleteWithBodyRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "application/json");
    expectedHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendDeleteRequest(URL, JSON_PAYLOAD);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEqual(request, JSON_PAYLOAD);
  }

  @Test
  public void shouldAddHeadersOnDeleteRequest() throws Exception {
    Map<String, String> expectedHeaders = new HashMap<>(ADDITIONAL_HEADERS);
    expectedHeaders.put(HttpHeaders.ACCEPT, "*/*");

    CommonsHttpClientImpl client = new CommonsHttpClientImpl(ADDITIONAL_HEADERS, () -> clientMock);
    HttpResponse response = client.sendDeleteRequest(URL, null);

    assertThat(response.getStatusCode()).isEqualTo(STATUS);
    Mockito.verify(clientMock).execute(captor.capture());
    HttpUriRequest request = captor.getValue();
    assertThat(request.getMethod()).isEqualTo(HttpDelete.METHOD_NAME);
    assertThat(request.getURI()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEmpty(request);
  }

  private void assertHeadersMatch(HttpUriRequest request, Map<String, String> expectedHeaders) {
    Map<String, String> headers = Arrays.stream(request.getAllHeaders())
      .map(BasicHeader.class::cast)
      .collect(Collectors.toMap(BasicHeader::getName, BasicHeader::getValue));

    assertThat(headers).hasSize(expectedHeaders.size());
    assertThat(headers).containsAllEntriesOf(expectedHeaders);
  }

  private void assertContentEqual(HttpUriRequest request, String expectedContent) throws IOException {
    assertThat(request).isInstanceOf(HttpEntityEnclosingRequest.class);

    InputStreamReader inputStreamReader = new InputStreamReader(((HttpEntityEnclosingRequest) request).getEntity().getContent(), StandardCharsets.UTF_8);
    String content = new BufferedReader(inputStreamReader).readLine();

    assertThat(content).isEqualTo(expectedContent);
  }

  private void assertContentEmpty(HttpUriRequest request) {
    assertThat(request).isNotInstanceOf(HttpEntityEnclosingRequest.class);
  }
}
