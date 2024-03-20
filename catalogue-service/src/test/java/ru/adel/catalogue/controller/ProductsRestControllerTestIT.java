package ru.adel.catalogue.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductsRestControllerTestIT {

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void findProducts_ReturnProductList() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter", "товара")
                .with(jwt().jwt(jwt -> jwt.claim("scope", "view_catalogue")));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                {"id" : 1,"title": "Название товара 1","details" :"Описание товара 1" },
                                {"id" : 4,"title": "Название товара 4","details" :"Описание товара 4" },
                                {"id" : 5,"title": "Название товара 5","details" :"Описание товара 5" }
                                ]
                                """

                        )
                );
    }

    @SneakyThrows
    @Test
    void findProduct_UserIsNotAuthorized_ReturnForbidden() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter", "товара")
                .with(jwt());
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
    void createProduct_RequestIsValid_ReturnsNewProduct() {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title" : "new product", "details" : "product_details"}
                        """)
                .with(jwt().jwt(jwt -> jwt.claim("scope", "edit_catalogue")));
        // when
        mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/catalogue-api/products/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {"id": 1,
                                "title": "new product",
                                "details": "product_details"}
                                """)
                );


    }

    @SneakyThrows
    @Test
    void createProduct_RequestIsInvalid_ReturnsProblemDetail(){
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "  ", "details": null}""")
                .locale(Locale.forLanguageTag("ru"))
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": [
                                        "Название товара должно быть от 3 до 50 символов"
                                    ]
                                }"""));
    }
    @SneakyThrows
    @Test
    void createProduct_UserNotAuthorized_ReturnsForbidden(){
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "  ", "details": null}""")
                .locale(Locale.forLanguageTag("ru"))
                .with(jwt());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                        );
    }
}