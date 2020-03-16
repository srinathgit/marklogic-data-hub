/*
 * Copyright 2019 MarkLogic Corporation. All rights reserved.
 */
package com.marklogic.hub.oneui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
class AuthTest {
    private static final String BASE_URL = "/api";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String LOGOUT_URL = BASE_URL + "/logout";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestHelper testHelper;

    @Test
    void loginWithInvalidCredentials() throws Exception {
        String payload = testHelper.getLoginPayload("fake");
        mockMvc
            .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithValidAdminAndLogout() throws Exception {
        String payload = testHelper.getLoginPayload("admin");
        final MockHttpSession session[] = new MockHttpSession[1];
        // Login
        mockMvc
            .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andDo (
                result -> {
                    session[0] = (MockHttpSession) result.getRequest().getSession();
                    String strResponse = result.getResponse().getContentAsString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonResponse = mapper.readTree(strResponse);
                    assertTrue(jsonResponse.get("roles").isArray());
                    assertTrue(jsonResponse.get("authorities").isArray());
                    assertTrue(jsonResponse.get("authorities").toString().contains("canInstallDataHub"));
                })
            .andExpect(status().isOk());

        assertFalse(session[0].isInvalid());

        // Logout
        mockMvc.perform(get(LOGOUT_URL).session(session[0]))
            .andExpect(status().isOk());

        assertTrue(session[0].isInvalid());
    }

    @Test
    void loginWithDataHubManagerAndLogout() throws Exception {
        String payload = testHelper.getLoginPayload("data-hub-environment-manager");
        final MockHttpSession session[] = new MockHttpSession[1];
        // Login
        mockMvc
            .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andDo (
                result -> {
                    session[0] = (MockHttpSession) result.getRequest().getSession();
                    String strResponse = result.getResponse().getContentAsString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonResponse = mapper.readTree(strResponse);
                    assertTrue(jsonResponse.get("roles").isArray());
                    assertTrue(jsonResponse.get("authorities").isArray());
                    assertTrue(jsonResponse.get("authorities").toString().contains("canInstallDataHub"));
                })
            .andExpect(status().isOk());

        assertFalse(session[0].isInvalid());

        // Logout
        mockMvc.perform(get(LOGOUT_URL).session(session[0]))
            .andExpect(status().isOk());

        assertTrue(session[0].isInvalid());
    }

    @Test
    void loginWithDeveloperUserAndLogout() throws Exception {
        String payload = testHelper.getLoginPayload("data-hub-developer");
        final MockHttpSession session[] = new MockHttpSession[1];
        // Login
        mockMvc
            .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andDo (
                result -> {
                    session[0] = (MockHttpSession) result.getRequest().getSession();
                    String strResponse = result.getResponse().getContentAsString();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonResponse = mapper.readTree(strResponse);
                    assertTrue(jsonResponse.get("roles").isArray());
                    assertTrue(jsonResponse.get("authorities").isArray());
                    assertFalse(jsonResponse.get("authorities").toString().contains("canInstallDataHub"));
                })
            .andExpect(status().isOk());

        assertFalse(session[0].isInvalid());

        // Logout
        mockMvc.perform(get(LOGOUT_URL).session(session[0]))
            .andExpect(status().isOk());

        assertTrue(session[0].isInvalid());
    }
}
