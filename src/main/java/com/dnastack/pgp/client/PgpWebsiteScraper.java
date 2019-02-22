package com.dnastack.pgp.client;

import com.dnastack.pgp.model.Checksum;
import com.dnastack.pgp.model.DosUrl;
import com.dnastack.pgp.model.Ga4ghDataObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.Value;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class PgpWebsiteScraper {

    private static final String PGP_LIST_URL = "https://personalgenomes.ca/v1/public/files/";
    private static final String PGP_DOWNLOAD_URL = "https://personalgenomes.ca/v1/public/files/%d/download";

    // TODO find out real mime types for these
    private static final Map<String, String> PGP_MIME_MAP = ImmutableMap.<String, String>builder()
            .put("BAM", "application/x-ga4gh-bam")
            .put("BASELINE_TRAIT", "application/x-ga4gh-baseline-trait")
            .put("FASTQ", "application/x-ga4gh-fastq")
            .put("INDEX", "application/x-ga4gh-index")
            .put("INTEGRITY_HASH", "application/x-ga4gh-integrity-hash")
            .put("VCF", "application/x-ga4gh-vcf")
            .build();

    private final HttpClient httpClient = HttpClientBuilder.create().build();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Data
    private static class PgpFilesPage {
        int totalPages;
        boolean last;
        List<PgpFile> content;
    }

    @Data
    private static class PgpFile {
        long id;
        String filename;
        String url;
        long fileSize;
        String fileType;
        PgpParticipant participant;
        List<PgpFile> additionalFiles;
        Instant createdAt;
        Instant lastModificationAt;
    }

    @Data
    private static class PgpParticipant {
        long assignedIdentityNumber;
        boolean optedIn;
        Instant optedInAt;
        long id;
        Instant createdAt;
        Instant lastModificationAt;
    }

    public List<Ga4ghDataObject> getData() throws IOException {
        List<Ga4ghDataObject> allData = new ArrayList<>();

        int pageNum = 0;
        PgpFilesPage currentPage;
        do {
            currentPage = getPage(pageNum);
            allData.addAll(extractDataObjects(currentPage.getContent()));
            pageNum++;
        } while (!currentPage.isLast());

        return allData;
    }

    private PgpFilesPage getPage(int page) throws IOException {
        HttpGet request = new HttpGet(PGP_LIST_URL + "?page=" + page);
        HttpResponse response = httpClient.execute(request);

        return objectMapper.readValue(response.getEntity().getContent(), PgpFilesPage.class);
    }

    private List<Ga4ghDataObject> extractDataObjects(List<PgpFile> files) {
        Map<String, String> hashes = files.stream()
                .filter(file -> file.getFileType().equals("INTEGRITY_HASH"))
                .map(file -> {
                    try {
                        HttpGet request = new HttpGet(downloadUrl(file));
                        HttpResponse response = httpClient.execute(request);
                        String md5 = EntityUtils.toString(response.getEntity());
                        System.out.println(md5);
                        md5 = md5.substring(0, md5.indexOf(' '));
                        System.out.println(md5);
                        return new FileChecksum(
                                file.getFilename().substring(0, file.getFilename().length() - ".md5sum".length()),
                                md5
                        );
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .filter(checksum -> checksum.md5 != null)
                .collect(toMap(FileChecksum::getFilename, FileChecksum::getMd5));

        return files.stream()
                .filter(file -> !file.getFileType().equals("INTEGRITY_HASH"))
                .map(file -> new Ga4ghDataObject(
                        stringOrNull(file.getId()),
                        downloadUrl(file),
                        stringOrNull(file.getFileSize()),
                        stringOrNull(file.getCreatedAt()),
                        stringOrNull(file.getLastModificationAt() != null ? file.getLastModificationAt() : file.getCreatedAt()),
                        "1",
                        PGP_MIME_MAP.get(file.getFileType()),
                        hashes.containsKey(file.getFilename())
                                ? ImmutableList.of(new Checksum(hashes.get(file.getFilename()), Checksum.Type.md5))
                                : ImmutableList.of(),
                        ImmutableList.of(new DosUrl(
                                file.getFilename(),
                                ImmutableMap.of(),
                                ImmutableMap.of())),
                        file.getFilename() + " (PGP Participant " + file.getParticipant().getAssignedIdentityNumber() + ")",
                        ImmutableList.of()))
                .collect(toList());
    }

    private static String stringOrNull(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    private String downloadUrl(PgpFile file) {
        return String.format(PGP_DOWNLOAD_URL, file.getId());
    }

    @Value
    private static class FileChecksum {
        String filename;
        String md5;
    }
}
