package com.example.collab.file;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;
    private final boolean s3Enabled;

    public S3Controller(S3Service s3Service, @org.springframework.beans.factory.annotation.Value("${aws.s3.enabled:false}") boolean s3Enabled) {
        this.s3Service = s3Service;
        this.s3Enabled = s3Enabled;
    }

    @PreAuthorize("hasAnyRole('EDITOR','OWNER')")
    @GetMapping("/presigned-upload")
    public ResponseEntity<?> getPresignedUpload(@RequestParam String fileName, @RequestParam(required = false) Integer minutes) {
        if (!s3Enabled) {
            return ResponseEntity.status(501).body("S3 not enabled. Set aws.s3.enabled=true and configure AWS credentials or use MinIO.");
        }
        int mins = minutes == null ? 15 : minutes;
        String key = "uploads/" + System.currentTimeMillis() + "-" + fileName;
        String url = s3Service.generatePresignedUploadUrl(key, mins);
        return ResponseEntity.ok(new com.example.collab.dto.FileUploadDto.UploadResponse(url));
    }
}
