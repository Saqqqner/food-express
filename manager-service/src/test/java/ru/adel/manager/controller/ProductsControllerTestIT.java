package ru.adel.manager.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.adel.manager.domain.entity.Product;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class ProductsControllerTestIT {

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getProductList_ReturnsProductsListPage() {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "товар")
                .with(user("saqner").roles("MANAGER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter",WireMock.equalTo("товар"))
                .willReturn(WireMock.ok("""
                          [
                            {"id": 1, "title": "Товар №1", "details": "Описание товара №1"},
                            {"id": 2, "title": "Товар №2", "details": "Описание товара №2"}
                        ]""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter","товар"),
                        model().attribute("products",
                                List.of(new Product(1,"Товар №1","Описание товара №1"),
                                        new Product(2,"Товар №2","Описание товара №2")))
                );
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар")));
    }


    @Test
    void getNewProductPage_ReturnsNewProductPage() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("saqner").roles("MANAGER"));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(status().isOk(), view().name("catalogue/products/new_product"));


    }

    @Test
    void getProductList_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "товар")
                .with(user("j.daniels"));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
