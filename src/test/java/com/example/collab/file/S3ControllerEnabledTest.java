package com.example.collab.file;

import com.example.collab.file.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(S3Controller.class)
@TestPropertySource(properties = "aws.s3.enabled=true")
public class S3ControllerEnabledTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private S3Service s3Service;

    @Test
    @WithMockUser(roles = {"EDITOR"})
    public void getPresignedUpload_returnsUrl_whenEnabled() throws Exception {
        when(s3Service.generatePresignedUploadUrl(anyString(), anyInt())).thenReturn("https://example.com/upload");

        mvc.perform(get("/api/s3/presigned-upload").param("fileName", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com/upload"));
    }
}
