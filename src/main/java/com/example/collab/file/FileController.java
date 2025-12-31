package com.example.collab.file;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/files","/files"})
public class FileController {

    private final com.example.collab.file.S3Service s3Service;
    private final boolean s3Enabled;

    public FileController(com.example.collab.file.S3Service s3Service, @org.springframework.beans.factory.annotation.Value("${aws.s3.enabled:false}") boolean s3Enabled) {
        this.s3Service = s3Service;
        this.s3Enabled = s3Enabled;
    }

    @PreAuthorize("hasAnyRole('EDITOR','OWNER')")
    @GetMapping("/presigned-upload")
    public ResponseEntity<?> presignedUpload(@RequestParam String fileName) {
        // Keep old GET stub behavior for backwards compatibility
        return ResponseEntity.ok("Stub presigned URL for " + fileName);
    }

    @PreAuthorize("hasAnyRole('VIEWER','EDITOR','OWNER')")
    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<?> getMetadata(@PathVariable String fileId) {
        return ResponseEntity.ok("metadata for " + fileId);
    }

    // New: POST /files/presigned-upload (and /api/files/presigned-upload)
    @PreAuthorize("hasAnyRole('EDITOR','OWNER')")
    @PostMapping("/presigned-upload")
    public ResponseEntity<?> presignedUploadPost(@org.springframework.web.bind.annotation.RequestBody com.example.collab.dto.FileUploadDto.UploadRequest req) {
        if (!s3Enabled) {
            return ResponseEntity.status(501).body("S3 not enabled. Set aws.s3.enabled=true and configure AWS credentials or use MinIO.");
        }
        String key = "uploads/" + System.currentTimeMillis() + "-" + req.fileName();
        int minutes = req.minutesValid() == null ? 15 : req.minutesValid();
        String url = s3Service.generatePresignedUploadUrl(key, minutes);
        return ResponseEntity.ok(new com.example.collab.dto.FileUploadDto.UploadResponse(url));
    }
}
