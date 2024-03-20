package ru.adel.catalogue.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import ru.adel.catalogue.controller.payload.UpdateProductDto;
import ru.adel.catalogue.domain.entity.Product;
import ru.adel.catalogue.service.ProductService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    @Mock
    ProductService productService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController productRestController;


    @Test
    void getProduct_ProductExist_ReturnsProduct() {
        // given
        Product product = new Product(1, "Название товара", "Описание товара");
        // when
        when(productService.findProduct(1)).thenReturn(Optional.of(product));

        Product product1 = productRestController.getProduct(1);
        //then
        assertEquals(product1, product);

    }

    @Test
    void getProduct_ProductDoesNotExist_TrowsNoSuchElementException() {
        NoSuchElementException noSuchElementException = assertThrows(NoSuchElementException.class,
                () -> productRestController.getProduct(1));
        assertEquals("catalogue.errors.product.not_found", noSuchElementException.getMessage());
    }

    @Test
    void findProduct_ProductExist_ReturnsProduct() {
        Product product = new Product(1, "Название товара", "Описание товара");
        Product product1 = productRestController.findProduct(product);
        assertEquals(product1, product);
    }

    @SneakyThrows
    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() {
        UpdateProductDto payload = new UpdateProductDto("Новое название", "Новое описание");
        BindingResult bindingResult = new MapBindingResult(Map.of(), "payload");

        ResponseEntity<?> response = productRestController.updateProduct(1, payload, bindingResult);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).updateProduct(1, "Новое название", "Новое описание");
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        UpdateProductDto payload = new UpdateProductDto("   ", null);
        BindingResult bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class, () -> productRestController.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnNoContent() {
        // given

        // when
        ResponseEntity<Void> result = productRestController.deleteProduct(1);
        // then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNotNull(result);
        verify(productService).deleteProduct(1);

    }
    @Test
    void handleNoSuchElementException_ReturnsNotFound() {
        // given
        NoSuchElementException exception = new NoSuchElementException("error_code");
        Locale locale = Locale.forLanguageTag("ru");

        doReturn("error details").when(this.messageSource)
                .getMessage("error_code", new Object[0], "error_code", Locale.forLanguageTag("ru"));

        // when
        ResponseEntity<ProblemDetail> result = productRestController.handleNoSuchElementException(exception, locale);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("error details", result.getBody().getDetail());

        verifyNoInteractions(this.productService);
    }
}


