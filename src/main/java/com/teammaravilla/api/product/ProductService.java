package com.teammaravilla.api.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return products.findAllByOrderByIdAsc().stream().map(ProductResponse::from).toList();
    }
}
