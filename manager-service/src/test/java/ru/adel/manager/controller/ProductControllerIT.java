package ru.adel.manager.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.adel.manager.domain.entity.Product;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void getProduct_ProductExist_ReturnsProductPage() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("saqner").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                        "id":1,
                        "title":"Продукт",
                        "details" : "Описание"
                        }
                        """)));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/product"),
                        model().attribute("product", new Product(1, "Продукт", "Описание"))
                );
    }

    @SneakyThrows
    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("saqner").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error","Товар не найден")
                );
    }
    @SneakyThrows
    @Test
    void getProduct_UserNotAuthorized_ReturnsForbidden() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1")
                .with(user("saqner"));


        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
    @SneakyThrows
    @Test
    void getProductEditPage_ProductExists_ReturnsProductEditPage(){
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("saqner").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                        "id":1,
                        "title":"Продукт",
                        "details" : "Описание"
                        }
                        """)));

        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/edit"),
                        model().attribute("product", new Product(1, "Продукт", "Описание"))
                );
    }
    @SneakyThrows
    @Test
    void getProductEditPage_ProductDoesNotExists_ReturnsError404Page(){
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("saqner").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.notFound()));

        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден"));
    }
    @SneakyThrows
    @Test
    void getProductEditPage_UserNotAuthorized_ReturnsForbidden() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/1/edit")
                .with(user("saqner"));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
    @SneakyThrows
    @Test
    void updateProduct_RequestIsValid_RedirectsToProductPage()   {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/1/edit")
                .param("title", "Новое название")
                .param("details", "Новое описание товара")
                .with(user("saqner").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Товар",
                            "details": "Описание товара"
                        }
                        """)));

        WireMock.stubFor(WireMock.patch("/catalogue-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание товара"
                        }"""))
                .willReturn(WireMock.noContent()));

        // when
       mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/products/1")
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "Новое название",
                            "details": "Новое описание товара"
                        }""")));
    }
}
