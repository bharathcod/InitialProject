package com.example.collab.file;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    private final AmazonS3 s3;
    private final String bucket;

    public S3Service(@Value("${aws.s3.bucket:collab-bucket}") String bucket) {
        this.s3 = AmazonS3ClientBuilder.standard().build();
        this.bucket = bucket;
    }

    public String generatePresignedUploadUrl(String key, int minutesValid) {
        Date expiration = new Date(System.currentTimeMillis() + minutesValid * 60L * 1000L);
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);
        URL url = s3.generatePresignedUrl(req);
        return url.toString();
    }
}
