package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto validOrder;

    @BeforeEach
    void setUp() {
        validOrder = BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .customerId(UUID.randomUUID())
                .orderStatus(OrderStatusEnum.READY)
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(any(), any())).willReturn(validOrder);

            MvcResult result = mockMvc.perform((get("/api/v1/customers/{customerId}/orders/{orderId}", UUID.randomUUID(), UUID.randomUUID())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @DisplayName("List Ops - ")
    @Nested
    public class TestListOperations {

        @Captor
        ArgumentCaptor<UUID> customerIdCaptor;

        @Captor
        ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerOrderPagedList beerOrderPagedList;

        @BeforeEach
        void setUp() {
            List<BeerOrderDto> beerOrders = new ArrayList<>();
            beerOrders.add(validOrder);
            beerOrders.add(BeerOrderDto.builder()
                    .id(UUID.randomUUID())
                    .version(1)
                    .createdDate(OffsetDateTime.now())
                    .lastModifiedDate(OffsetDateTime.now())
                    .customerId(UUID.randomUUID())
                    .orderStatus(OrderStatusEnum.READY)
                    .build());

            beerOrderPagedList = new BeerOrderPagedList(beerOrders, PageRequest.of(1, 1), 2);

            given(beerOrderService.listOrders(customerIdCaptor.capture(), pageRequestCaptor.capture())).willReturn(beerOrderPagedList);
        }

        @DisplayName("Test list beerOrders - no parameters")
        @Test
        void testListBeers() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/customers/{customerId}/orders", UUID.randomUUID())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(validOrder.getId().toString())))
                    .andReturn();

            System.out.println(result.getResponse().getContentAsString());
        }
    }
}