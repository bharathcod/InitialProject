package com.example.collab.file;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(S3Controller.class)
public class S3ControllerDisabledTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = {"EDITOR"})
    public void getPresignedUpload_returns501_whenDisabled() throws Exception {
        mvc.perform(get("/api/s3/presigned-upload").param("fileName", "test.txt"))
                .andExpect(status().isNotImplemented());
    }
}
