package com.barnizexpress.infrastructure.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    private String bearer;

    @BeforeEach
    void login() throws Exception {
        String body =
                mvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"username\":\"demo\",\"password\":\"demo123\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.expiresAt").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        JsonNode node = json.readTree(body);
        bearer = "Bearer " + node.get("token").asText();
    }

    @Test
    @DisplayName("login with wrong credentials is 401")
    void wrongCredentials() throws Exception {
        mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"demo\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("protected routes need a valid token")
    void needsToken() throws Exception {
        mvc.perform(get("/api/v1/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/products").header("Authorization", "Bearer garbage"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("lists the four products")
    void products() throws Exception {
        mvc.perform(get("/api/v1/products").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].basePriceCop").isNumber())
                .andExpect(jsonPath("$[0].imageUrl").exists());
    }

    @Test
    @DisplayName("lists the five options with their incompatibilities")
    void optionsCatalog() throws Exception {
        mvc.perform(get("/api/v1/options").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.code=='CUSTOMS')].incompatibleWith[0]").value("EXPRESS"));
    }

    @Test
    @DisplayName("quotes a domestic shipment with layers from innermost to outermost")
    void quoteHappyPath() throws Exception {
        mvc.perform(
                        post("/api/v1/quotes")
                                .header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"productId\":\"tray-giralda\",\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"},"
                                                + "\"declaredValueCop\":185000,\"options\":[\"EXPRESS\",\"FRAGILE\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("COP"))
                .andExpect(jsonPath("$.baseCostCop").value(27000))
                .andExpect(jsonPath("$.totalCop").value(60750))
                .andExpect(jsonPath("$.layers[0].code").value("FRAGILE"))
                .andExpect(jsonPath("$.layers[1].code").value("EXPRESS"));
    }

    @Test
    @DisplayName("customs on a domestic destination is 400")
    void customsDomestic() throws Exception {
        mvc.perform(
                        post("/api/v1/quotes")
                                .header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"productId\":\"tray-giralda\",\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"},\"options\":[\"CUSTOMS\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("CUSTOMS requires an international destination"));
    }

    @Test
    @DisplayName("customs and express together is 400")
    void customsWithExpress() throws Exception {
        mvc.perform(
                        post("/api/v1/quotes")
                                .header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"productId\":\"tray-giralda\",\"destination\":{\"city\":\"Quito\",\"country\":\"Ecuador\"},\"options\":[\"CUSTOMS\",\"EXPRESS\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CUSTOMS and EXPRESS cannot be combined"));
    }

    @Test
    @DisplayName("gift wrap without a message is 400")
    void giftWithoutMessage() throws Exception {
        mvc.perform(
                        post("/api/v1/quotes")
                                .header("Authorization", bearer)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"productId\":\"tray-giralda\",\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"},\"options\":[\"GIFT\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("GIFT requires")));
    }

    @Test
    @DisplayName("unknown product, unknown option and negative value are 400")
    void invalidInputs() throws Exception {
        String dest = "\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"}";
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"nope\"," + dest + "}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest + ",\"options\":[\"TELEPORT\"]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest + ",\"declaredValueCop\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("text that looks like SQL injection is rejected, apostrophes are fine")
    void safeText() throws Exception {
        String dest = "\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"}";
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest
                                + ",\"options\":[\"GIFT\"],\"giftMessage\":\"x' OR 1=1 --\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest
                                + ",\"options\":[\"GIFT\"],\"giftMessage\":\"Mom's gift\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("gift message over 140 characters or with trailing spaces is 400")
    void giftMessageRules() throws Exception {
        String dest = "\"destination\":{\"city\":\"Pasto\",\"country\":\"Colombia\"}";
        String longMessage = "a".repeat(141);
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest
                                + ",\"options\":[\"GIFT\"],\"giftMessage\":\"" + longMessage + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/quotes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"tray-giralda\"," + dest
                                + ",\"options\":[\"GIFT\"],\"giftMessage\":\"hello \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CORS preflight from the frontend origin is allowed without a token")
    void corsPreflight() throws Exception {
        mvc.perform(
                        options("/api/v1/quotes")
                                .header("Origin", "http://localhost:5173")
                                .header("Access-Control-Request-Method", "POST")
                                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
