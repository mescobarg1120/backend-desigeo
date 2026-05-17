package com.backend_desigeo.desigeo_report_service.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadBase64Image(String base64Image, String reportId) throws IOException, InterruptedException {
        byte[] imageBytes = decodeBase64(base64Image);
        byte[] compressed = compress(imageBytes);
        String path = "reports/" + reportId + "/" + UUID.randomUUID() + ".jpg";
        return uploadToSupabase(compressed, path);
    }

    private byte[] decodeBase64(String base64) {
        String data = base64.contains(",") ? base64.split(",")[1] : base64;
        return Base64.getDecoder().decode(data);
    }

    private byte[] compress(byte[] imageBytes) throws IOException {
        ByteArrayInputStream in  = new ByteArrayInputStream(imageBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(in)
                .size(1280, 1280)
                .outputQuality(0.8)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }

    private String uploadToSupabase(byte[] data, String path) throws IOException, InterruptedException {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + serviceKey)
                .header("Content-Type", "image/jpeg")
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("Error subiendo imagen a Supabase Storage: " + response.body());
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }
}
