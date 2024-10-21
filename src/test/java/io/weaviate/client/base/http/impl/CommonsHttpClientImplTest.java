package io.weaviate.client.base.http.impl;

import io.weaviate.client.base.http.HttpResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommonsHttpClientImplTest {

  private static final String URL = "http://example.weaviate/";
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

  @Before
  public void before() throws IOException {
    Mockito.when(clientMock.execute(any(HttpUriRequest.class)))
      .thenReturn(responseMock);
    Mockito.when(responseMock.getCode())
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
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
    assertThat(request.getUri()).isEqualByComparingTo(URI.create(URL));
    assertHeadersMatch(request, expectedHeaders);
    assertContentEmpty(request);
  }

  private void assertHeadersMatch(HttpUriRequest request, Map<String, String> expectedHeaders) {
    Map<String, String> headers = Arrays.stream(request.getHeaders())
      .map(BasicHeader.class::cast)
      .collect(Collectors.toMap(BasicHeader::getName, BasicHeader::getValue));

    assertThat(headers).hasSize(expectedHeaders.size());
    assertThat(headers).containsAllEntriesOf(expectedHeaders);
  }

  private void assertContentEqual(HttpUriRequest request, String expectedContent) throws IOException {
    assertThat(request).isInstanceOf(HttpUriRequestBase.class);

    InputStreamReader inputStreamReader = new InputStreamReader(((HttpUriRequestBase) request).getEntity().getContent(), StandardCharsets.UTF_8);
    String content = new BufferedReader(inputStreamReader).readLine();

    assertThat(content).isEqualTo(expectedContent);
  }

  private void assertContentEmpty(HttpUriRequest request) {
    assertThat(request).isInstanceOfAny(HttpGet.class, HttpHead.class, HttpDelete.class);
  }
}
