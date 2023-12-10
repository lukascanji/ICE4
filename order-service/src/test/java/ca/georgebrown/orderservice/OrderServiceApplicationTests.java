package ca.georgebrown.orderservice;

import ca.georgebrown.orderservice.dto.OrderRequest;
import ca.georgebrown.orderservice.dto.OrderLineItemDto;
import ca.georgebrown.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderServiceApplicationTests extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockServer inventoryServiceMockServer;
    private WireMockServer eurekaServerMockServer;

    @BeforeEach
    void setup() {
        // Mock for Inventory Service
        inventoryServiceMockServer = new WireMockServer(8083);
        inventoryServiceMockServer.start();
        inventoryServiceMockServer.stubFor(post(urlEqualTo("/api/inventory"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[{\"skuCode\": \"SKU123\", \"sufficientStock\": true}]")));

        // Mock for Eureka Server
        eurekaServerMockServer = new WireMockServer(8761);
        eurekaServerMockServer.start();
        eurekaServerMockServer.stubFor(post(urlPathMatching("/eureka/apps/.*"))
                .willReturn(aResponse()
                        .withStatus(204)));
    }

    @AfterEach
    void teardown() {
        if (inventoryServiceMockServer != null) {
            inventoryServiceMockServer.stop();
        }
        if (eurekaServerMockServer != null) {
            eurekaServerMockServer.stop();
        }
    }

    private OrderRequest createOrderRequest() {
        OrderLineItemDto lineItem = OrderLineItemDto.builder()
                .skuCode("SKU123")
                .price(new BigDecimal("100.00"))
                .quantity(1)
                .build();

        return OrderRequest.builder()
                .orderLineItems(Collections.singletonList(lineItem))
                .build();
    }

    @Test
    void createOrder() throws Exception {
        OrderRequest orderRequest = createOrderRequest();
        String orderRequestJson = objectMapper.writeValueAsString(orderRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Assertions.assertFalse(orderRepository.findAll().isEmpty());
    }
}