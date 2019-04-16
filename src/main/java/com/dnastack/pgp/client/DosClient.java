package com.dnastack.pgp.client;

import static java.util.Objects.requireNonNull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import com.dnastack.pgp.model.Ga4ghDataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DosClient {

    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final URI baseUrl;
    private final String authHeader;
    private final HttpClient httpClient;

    public DosClient(URI baseUrl, String username, String password) throws IOException {
        this.baseUrl = requireNonNull(baseUrl);

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        authHeader = "Basic " + new String(encodedAuth, StandardCharsets.ISO_8859_1);

        httpClient = HttpClientBuilder.create().build();
    }

    public void postDataObject(Ga4ghDataObject dataObject) {
        try {
            String postBody = objectMapper.writeValueAsString(dataObject);

            HttpPut request =
                    new HttpPut(baseUrl.resolve("ga4gh/drs/v1/objects/" + dataObject.getId()));
            request.setEntity(new StringEntity(postBody));
            request.setHeader("Content-type", "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            HttpResponse httpResponse = httpClient.execute(request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                throw new IOException(
                        "PUT data object "
                                + postBody
                                + " to "
                                + request.getURI()
                                + " failed: "
                                + httpResponse.getStatusLine()
                                + "\n"
                                + Arrays.toString(httpResponse.getAllHeaders())
                                + "\n"
                                + responseBody);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
