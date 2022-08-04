package com.rakuten.felix.testsend.manager;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.findify.s3mock.S3Mock;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MockBean({Configuration.class})
public abstract class AbstractIntegrationTest {

    @Autowired
    protected AmazonS3 s3client;

    @Value("${felix.storage.bucket}")
    protected String bucket;

    @Inject
    protected ResourceLoader resourceLoader;

    protected ObjectMapper mapper;

    protected static S3Mock initS3Mock() {
        return new S3Mock.Builder()
                .withPort(8001).withInMemoryBackend()
                .build();
    }

    protected abstract S3Mock getS3Mock();

    public void init() throws IOException {
        mapper = new ObjectMapper();
        getS3Mock().start();
        s3client.createBucket(bucket);

    }

    public void stop() throws IOException {
        getS3Mock().stop();
    }

    protected <T> T loadData(String path, Class<T> clzz) {
        return Optional.ofNullable(path)
                .filter(StringUtils::isNotBlank)
                .map(resourceLoader::getResource).map(this::loadFile)
                .map(json -> readValue(json, clzz))
                .orElse(null);
    }

    @SneakyThrows
    protected File loadFile(Resource resource) {
        return resource.getFile();
    }

    @SneakyThrows
    protected <T> T readValue(File file, Class<T> clzz) {
        return mapper.readValue(file, clzz);
    }

    @SneakyThrows
    protected MockResponse createMockResponse(Response response) {
        return new MockResponse()
                .setResponseCode(response.status)
                .setBody(mapper.writeValueAsString(response.body))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @SneakyThrows
    protected String readResource(String path) {
        return Files.lines(Paths.get(ClassLoader.getSystemClassLoader()
                        .getResource(path)
                        .toURI()))
                .parallel()
                .collect(Collectors.joining());
    }

    protected String readResource(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected List<ApiData> registerServerResponses(List<String> apiDataPaths, MockWebServer server) {
        val apiData = new ArrayList<ApiData>();
        if (CollectionUtils.isNotEmpty(apiDataPaths)) {
            apiDataPaths.stream()
                    .map(path -> loadData(path, ApiData.class))
                    .peek(apiData::add)
                    .map(data -> createMockResponse(data.response))
                    .forEach(server::enqueue);
        }
        return apiData;
    }

    protected void validateServerExecution(List<ApiData> apiDataList, MockWebServer server, String serverName, BiConsumer<ApiData, String> pathAssertions) throws InterruptedException, JsonProcessingException, JSONException {
        for (val apiData : apiDataList) {
            val request = server.takeRequest();
            pathAssertions.accept(apiData, request.getRequestLine());
            val headers = request.getHeaders();
            apiData.request.headers.forEach((header, value) -> assertEquals(value, headers.get(header), serverName + " header " + header + " mismatch"));
            if (apiData.request.body != null) {
                val actualRequest = request.getBody().readUtf8();
                if (actualRequest.startsWith("{")) {
                    val expectRequest = mapper.writeValueAsString(apiData.request.body);
                    try {
                        JSONAssert.assertEquals(expectRequest, actualRequest, true);
                    } catch (AssertionError err) {
                        assertEquals(expectRequest, actualRequest, serverName + " request body mismatched");
                    }
                } else {
                    assertEquals(apiData.request.body, actualRequest, serverName + " request body mismatched");
                }
            }
        }
    }

    @Data
    @Builder(toBuilder = true)
    public static class Response {
        Integer status;
        Object body;
    }

    @Builder(toBuilder = true)
    public static class ApiData {
        Request request;
        Response response;
    }

    @Builder(toBuilder = true)
    public static class Request {
        Map<String, Object> headers;
        Map<String, Object> params;
        Object body;
    }
}
