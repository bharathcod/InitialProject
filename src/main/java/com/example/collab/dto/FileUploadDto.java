package com.example.collab.dto;

public class FileUploadDto {
    public static record UploadRequest(String fileName, Integer minutesValid) {}

    public static class UploadResponse {
        public String url;
        public UploadResponse() {}
        public UploadResponse(String url) { this.url = url; }
    }
}
