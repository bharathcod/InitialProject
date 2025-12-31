package com.example.collab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authManager;

    @MockBean
    private com.example.collab.security.JwtUtil jwtUtil;

    @MockBean
    private com.example.collab.auth.UserRepository userRepository;

    @MockBean
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void register_creates_user() throws Exception {
        var req = new AuthController.RegisterRequest("dave", "davepass", java.util.Set.of("EDITOR"));
        when(userRepository.existsById("dave")).thenReturn(false);
        when(passwordEncoder.encode("davepass")).thenReturn("encoded");
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isCreated());
        verify(userRepository, times(1)).save(any());
    }
}
