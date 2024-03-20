package ru.adel.catalogue.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;
import ru.adel.catalogue.controller.payload.NewProductDto;
import ru.adel.catalogue.domain.entity.Product;
import ru.adel.catalogue.service.ProductService;


import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsRestControllerTest {

    @Mock
    ProductService productService;

    @InjectMocks
    ProductsRestController restController;

    @Test
    void findProducts_ReturnsProductsList() {
        // given
        String filter = "товар";

        // when
        when(productService.findAllProducts(filter)).thenReturn(List.of(new Product(1, "Первый товар", "Описание первого товара"),
                new Product(2, "Второй товар", "Описание второго товара")));
        Iterable<Product> result = restController.findProducts(filter);

        //then
        assertEquals(List.of(new Product(1, "Первый товар", "Описание первого товара"),
                new Product(2, "Второй товар", "Описание второго товара")), result);
    }

    @SneakyThrows
    @Test
    void createProduct_RequestIsValid_ReturnsCreated() {
        // given
        NewProductDto productDto = new NewProductDto("Новый продукт", "Описание");
        BindingResult bindingResult = new MapBindingResult(Map.of(), "payload");
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        when(this.productService.createProduct("Новый продукт", "Описание"))
                .thenReturn(new Product(1, "Новый продукт", "Описание"));
        // then
        ResponseEntity<?> result = restController.createProduct(productDto, bindingResult, uriComponentsBuilder);
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/catalogue-api/products/1"), result.getHeaders().getLocation());
        assertEquals(new Product(1, "Новый продукт", "Описание"), result.getBody());
        verify(this.productService).createProduct("Новый продукт", "Описание");
    }


    @Test
    void createProduct_RequestIsNotInvalid_ReturnBadRequest() {
        // given
        NewProductDto productDto = new NewProductDto("   ", null);
        BindingResult bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // then
        BindException exception = assertThrows(BindException.class,
                () -> restController.createProduct(productDto, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(productService);
    }
    @Test
    void createProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        NewProductDto productDto = new NewProductDto("   ", null);
        BindingResult bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // then
        BindException exception = assertThrows(BindException.class,
                () -> restController.createProduct(productDto, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(productService);
    }

}