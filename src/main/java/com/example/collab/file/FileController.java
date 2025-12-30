package com.example.collab.file;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @PreAuthorize("hasAnyRole('EDITOR','OWNER')")
    @GetMapping("/presigned-upload")
    public ResponseEntity<?> presignedUpload(@RequestParam String fileName) {
        return ResponseEntity.ok("Stub presigned URL for " + fileName);
    }

    @PreAuthorize("hasAnyRole('VIEWER','EDITOR','OWNER')")
    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<?> getMetadata(@PathVariable String fileId) {
        return ResponseEntity.ok("metadata for " + fileId);
    }
}
