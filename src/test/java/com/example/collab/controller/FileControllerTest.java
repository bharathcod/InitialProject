package com.example.collab.controller;

import com.example.collab.file.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.example.collab.file.FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private S3Service s3Service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"EDITOR"})
    public void postPresignedUpload_returnsUrl_whenS3Enabled() throws Exception {
        when(s3Service.generatePresignedUploadUrl(anyString(), anyInt())).thenReturn("https://example.com/upload");

        var req = new com.example.collab.dto.FileUploadDto.UploadRequest("test.txt", 10);
        mvc.perform(post("/files/presigned-upload").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com/upload"));

        verify(s3Service, times(1)).generatePresignedUploadUrl(anyString(), eq(10));
    }
}
