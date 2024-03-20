package ru.adel.catalogue.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductRestControllerTestIT {

    @Autowired
    MockMvc mockMvc;


    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void findProduct_ProductExists_ReturnsProduct() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/catalogue-api/products/1")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "view_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {"id": 1 ,"title": "Название товара 1","details": "Описание товара 1"}
                                """));
    }

    @SneakyThrows
    @Test
    void findProduct_UserNotAuthorized_ReturnForbidden() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/catalogue-api/products/1")
                        .with(jwt());

        //when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(status().isForbidden()
                );
    }

    @SneakyThrows
    @Test
    void findProduct_ProductsNotExist_ReturnNotFound() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/catalogue-api/products/21312")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "view_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(status().isNotFound()
                );
    }

    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsValid_ReturnNoContent() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Молоко","details":"Молоко вкусное"}
                                """)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "edit_catalogue")));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(status().isNoContent()
                );
    }

    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsInvalid_ReturnBadRequest() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                        .locale(Locale.forLanguageTag("ru"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "   ",
                                "details": null
                                }""")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "edit_catalogue")));

        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": ["Название товара не может быть пустым"]
                                }""")
                );
    }

    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void updateProduct_UserNotAuthorized_ReturnForbidden() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Молоко","details":"Молоко вкусное"}
                                """)
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
    void updateProduct_ProductDoesNotExist_ReturnNotFound() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.patch("/catalogue-api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Молоко","details":"Молоко вкусное"}
                                """)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "edit_catalogue")));
        // when
        mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound()
                );
    }


    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_ProductExists_ReturnsNoContent() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "edit_catalogue")));
        // when
        mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );


    }
    @SneakyThrows
    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_UserNotAuthorized_ReturnsNoContent() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                        .with(jwt());
        // when
        mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
    @SneakyThrows
    @Test
    void deleteProduct_ProductDoesNotExist_ReturnNotFound() {
        // given
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/catalogue-api/products/1")
                                .with(jwt().jwt(jwt->jwt.claim("scope","edit_catalogue")));
        // when
        mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound()
                );
    }


}