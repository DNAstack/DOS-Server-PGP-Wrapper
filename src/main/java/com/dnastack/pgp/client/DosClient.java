package com.dnastack.pgp.client;

import com.dnastack.pgp.model.Ga4ghDataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public class DosClient {

    private static final ObjectMapper objectMapper = new ObjectMapper()
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
            String postBody = objectMapper.writeValueAsString(singletonMap("data_object", dataObject));

            HttpPost request = new HttpPost(baseUrl.resolve("dataobjects"));
            request.setEntity(new StringEntity(postBody));
            request.setHeader("Content-type", "application/json");
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

            HttpResponse httpResponse = httpClient.execute(request);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Post data object " + postBody + " to " + request.getURI() + " failed: " + httpResponse.getStatusLine() + "\n" +
                        Arrays.toString(httpResponse.getAllHeaders()) + "\n" +
                        responseBody);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


//    public void postDataBundles(JSONArray allData, String postUrl) throws ClientProtocolException, IOException {
//
//        // Finding max id of participants
//        int max_participant = 0;
//        for (int i = 0; i < allData.length(); i++) {
//            if (allData.getJSONObject(i).getJSONObject("participant").getInt("assignedIdentityNumber") > max_participant) {
//                max_participant = allData.getJSONObject(i).getJSONObject("participant").getInt("assignedIdentityNumber");
//            }
//        }
//
//        // Iterating
//        for (int i = 0; i < max_participant + 1; i++) {
//            // Getting data we need from the data set
//            List<String> data_object_ids = new ArrayList<String>();
//            String created = null;
//            String updated = null;
//            for (int j = 0; j < allData.length(); j++) {
//                JSONObject participant_json = allData.getJSONObject(j).getJSONObject("participant");
//                if (participant_json.getInt("assignedIdentityNumber") == i) {
//                    data_object_ids.add(String.valueOf(allData.getJSONObject(j).getInt("id")));
//                    created = participant_json.getString("createdAt");
//                    updated = participant_json.getString("lastModificationAt");
//                }
//            }
//
//            System.out.println(String.valueOf(i) + ' ' + data_object_ids.toString());
//
//            // Posting to server
//            if (!data_object_ids.isEmpty()) {
//                Gson gson = new Gson();
//                String json = gson.toJson(new Ga4ghDataBundle(String.valueOf(i), data_object_ids, created, updated));
//                json = "{\"data_bundle\":" + json + "}";
//                //System.out.println(json);
//
//                HttpClient httpClient = HttpClientBuilder.create().build();
//                HttpPost post = new HttpPost(postUrl);
//                post.setEntity(new StringEntity(json));
//                post.setHeader("Content-type", "application/json");
//                httpClient.execute(post);
//            }
//        }
//
//    }
}
