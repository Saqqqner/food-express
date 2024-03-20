package ru.adel.catalogue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.adel.catalogue.domain.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findAllByTitleLikeIgnoreCase(String filter);
}
