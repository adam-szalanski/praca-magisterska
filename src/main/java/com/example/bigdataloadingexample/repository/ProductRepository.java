package com.example.bigdataloadingexample.repository;

import com.example.bigdataloadingexample.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE products", nativeQuery = true)
    void truncateTable();
}
