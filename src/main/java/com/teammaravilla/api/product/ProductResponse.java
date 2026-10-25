package com.teammaravilla.api.product;

public record ProductResponse(String id, String name, ProductCategory category, String imageUrl) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(),
                product.getCategory(), product.getImageUrl());
    }
}
