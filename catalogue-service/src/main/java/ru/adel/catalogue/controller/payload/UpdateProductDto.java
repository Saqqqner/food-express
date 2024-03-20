package ru.adel.catalogue.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductDto(
        @NotNull(message = "{catalogue.products.update.errors.title_is_null}")
        @NotBlank(message ="{catalogue.products.update.errors.title_is_blank}")
        @Size(min = 3, max = 50, message = "{catalogue.products.update.errors.title_size_is_invalid}")
        String title,
        @Size(max = 1000, message = "{catalogue.products.update.errors.details_size_is_invalid}")
        String details) {
}
