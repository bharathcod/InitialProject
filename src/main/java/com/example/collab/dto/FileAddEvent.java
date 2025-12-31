package com.example.collab.dto;

import java.time.Instant;

public class FileAddEvent {
    private String modelId;
    private String fileId;
    private String fileName;
    private String author;
    private Instant timestamp;

    public FileAddEvent() {}

    public FileAddEvent(String modelId, String fileId, String fileName, String author, Instant timestamp) {
        this.modelId = modelId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.author = author;
        this.timestamp = timestamp;
    }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
