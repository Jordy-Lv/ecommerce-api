package com.ecommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token(String body) throws Exception {
        return objectMapper.readTree(body).get("token").asText();
    }

    // ---------- AUTH ----------

    @Test
    void register_returns201AndToken() throws Exception {
        String json = """
                {"name":"Test User","email":"reg1@test.com","password":"secret123"}""";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("reg1@test.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        String json = """
                {"name":"Dup","email":"dup@test.com","password":"secret123"}""";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidPayload_returns400() throws Exception {
        String json = """
                {"name":"","email":"not-an-email","password":"123"}""";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@store.com","password":"wrong-pass"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededAdmin_canLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@store.com","password":"admin123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // ---------- PRODUCTS (public reads) ----------

    @Test
    void listProducts_isPublic_returns200WithSeedData() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").exists());
    }

    @Test
    void getProductById_found_andNotFound() throws Exception {
        mockMvc.perform(get("/api/products/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    void listProductsByCategory_returns200() throws Exception {
        mockMvc.perform(get("/api/products").param("category", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ---------- CATEGORIES ----------

    @Test
    void listCategories_isPublic_returns200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    // ---------- SECURITY RULES ----------

    @Test
    void createProduct_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X","description":"Y","price":10.0,"stock":5,"categoryId":1}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_asCustomer_returns403() throws Exception {
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cust","email":"cust403@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + token(reg))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X","description":"Y","price":10.0,"stock":5,"categoryId":1}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_asAdmin_returns201() throws Exception {
        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@store.com","password":"admin123"}"""))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + token(login))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Producto Admin","description":"creado en test","price":50.0,"stock":7,"categoryId":1}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Producto Admin"));
    }

    @Test
    void listOrders_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
    }

    // ---------- FULL ORDER FLOW ----------

    @Test
    void orderFlow_createListGetCancel() throws Exception {
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Buyer","email":"buyer@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        String jwt = token(reg);

        // create address
        MvcResult addrResult = mockMvc.perform(post("/api/addresses").header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"street":"Calle 1","city":"Lima","country":"Peru","zipCode":"15001"}"""))
                .andExpect(status().isCreated())
                .andReturn();
        long addressId = objectMapper.readTree(addrResult.getResponse().getContentAsString()).get("id").asLong();

        // create order against seeded product 1
        MvcResult orderResult = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}],\"addressId\":" + addressId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();
        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();

        // list orders
        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        // get order detail
        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) orderId));

        // cancel order
        mockMvc.perform(delete("/api/orders/" + orderId).header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void createOrder_insufficientStock_returns400() throws Exception {
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Greedy","email":"greedy@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        String jwt = token(reg);

        MvcResult addrResult = mockMvc.perform(post("/api/addresses").header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"street":"Calle 2","city":"Lima","country":"Peru","zipCode":"15001"}"""))
                .andReturn();
        long addressId = objectMapper.readTree(addrResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":999999}],\"addressId\":" + addressId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_addressOfAnotherUser_returns403() throws Exception {
        // user A creates an address
        String regA = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"A","email":"owner@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        String jwtA = token(regA);
        MvcResult addrResult = mockMvc.perform(post("/api/addresses").header("Authorization", "Bearer " + jwtA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"street":"Privada","city":"Lima","country":"Peru","zipCode":"15001"}"""))
                .andReturn();
        long addressId = objectMapper.readTree(addrResult.getResponse().getContentAsString()).get("id").asLong();

        // user B tries to order to A's address
        String regB = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"B","email":"intruder@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        String jwtB = token(regB);

        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + jwtB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":1}],\"addressId\":" + addressId + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_asCustomer_returns403() throws Exception {
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"C","email":"statususer@test.com","password":"secret123"}"""))
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(patch("/api/orders/1/status").header("Authorization", "Bearer " + token(reg))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"CONFIRMED"}"""))
                .andExpect(status().isForbidden());
    }
}
