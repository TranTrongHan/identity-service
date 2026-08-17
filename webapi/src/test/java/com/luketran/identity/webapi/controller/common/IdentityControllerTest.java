package com.luketran.identity.webapi.controller.common;

import com.luketran.identity.application.dto.response.TokenDataResponse;
import com.luketran.identity.application.interfaces.AccountLogoutService;
import com.luketran.identity.application.interfaces.IdentityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IdentityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IdentityService identityService;

    @Mock
    private AccountLogoutService accountLogoutService;

    private IdentityController controller;

    @BeforeEach
    void setUp() {
        controller = new IdentityController(identityService, accountLogoutService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginByPassword_AppCodeInHeader_PopulatesRequestAndCallsService() throws Exception {
        TokenDataResponse response = new TokenDataResponse();
        response.setAccessToken("dummy-access-token");
        response.setRefreshToken("dummy-refresh-token");

        when(identityService.loginByPassword(argThat(req -> 
                "TICKET_APP".equals(req.getAppCode()) 
                && "admin".equals(req.getUsername())
                && "123456".equals(req.getPassword())
        ))).thenReturn(response);

        // Body without appCode, appCode passed in header
        String requestJson = """
                {
                    "username": "admin",
                    "password": "123456"
                }
                """;

        mockMvc.perform(post("/Identity/Login/Password")
                        .header("appCode", "TICKET_APP")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("dummy-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("dummy-refresh-token"));

        verify(identityService).loginByPassword(argThat(req -> "TICKET_APP".equals(req.getAppCode())));
    }

    @Test
    void loginByPassword_AppCodeInBody_CallsServiceSuccessfully() throws Exception {
        TokenDataResponse response = new TokenDataResponse();
        response.setAccessToken("dummy-access-token-2");
        response.setRefreshToken("dummy-refresh-token-2");

        when(identityService.loginByPassword(argThat(req -> 
                "IDENTITY".equals(req.getAppCode()) 
                && "user1".equals(req.getUsername())
        ))).thenReturn(response);

        String requestJson = """
                {
                    "appCode": "IDENTITY",
                    "username": "user1",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/Identity/Login/Password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("dummy-access-token-2"));
    }
}
