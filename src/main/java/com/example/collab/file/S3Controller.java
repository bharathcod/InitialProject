package com.example.collab.file;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) { this.s3Service = s3Service; }

    @PreAuthorize("hasAnyRole('EDITOR','OWNER')")
    @GetMapping("/presigned-upload")
    public ResponseEntity<?> getPresignedUpload(@RequestParam String fileName) {
        String key = "uploads/" + System.currentTimeMillis() + "-" + fileName;
        String url = s3Service.generatePresignedUploadUrl(key, 15);
        return ResponseEntity.ok(url);
    }
}
