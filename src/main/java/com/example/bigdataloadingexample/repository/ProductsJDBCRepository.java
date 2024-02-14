package com.example.bigdataloadingexample.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductsJDBCRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void executeQuery(String query) {
        jdbcTemplate.update(query);
    }
}
