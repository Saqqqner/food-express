package ru.adel.manager.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import ru.adel.manager.client.BadRequestException;
import ru.adel.manager.client.ProductsRestClient;
import ru.adel.manager.controller.dto.NewProductDto;
import ru.adel.manager.domain.entity.Product;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @InjectMocks
    ProductsController productController;

    @Mock
    ProductsRestClient productsRestClient;
    @Test
     void getProductsList_ReturnsProductsListPage(){
        ConcurrentModel model = new ConcurrentModel();
        String filter = "Товар";
        List<Product> products = IntStream.range(1, 5)
                .mapToObj(i -> new Product(i, "Товар %d".formatted(i),
                        "Описание товара %d".formatted(i)))
                .collect(Collectors.toList());
        when(productsRestClient.findAllProducts(filter)).thenReturn(products);

        String result = productController.getProductsList(model, filter);

        assertEquals("catalogue/products/list",result);
        assertEquals(filter,model.getAttribute("filter"));
        assertEquals(products,model.getAttribute("products"));

    }
    @Test
     void getNewProductPage_ReturnNewProductPage(){
        String newProductPage = productController.getNewProductPage();
        assertEquals("catalogue/products/new_product",newProductPage);
    }

    @Test
     void createProduct_RequestIsValid_ReturnRedirectionToPage() {
        // given
        NewProductDto payload = new NewProductDto("Новый товар", "Описание нового товара");
        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletResponse response = new MockHttpServletResponse();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productsRestClient)
                .createProduct("Новый товар", "Описание нового товара");

        // when
        String result = this.productController.createProduct(payload, model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.productsRestClient).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
     void createProduct_RequestNotValid_TrowsBadRequestException() {
        // given
        NewProductDto payload = new NewProductDto("  ", null);
        ConcurrentModel model = new ConcurrentModel();
        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient)
                .createProduct("  ", null);

        // when
        String result = this.productController.createProduct(payload, model, response);

        // then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).createProduct("  ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

}