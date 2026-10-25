package com.teammaravilla.api.product;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column(nullable = false, updatable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    protected Product() {
    }

    public Product(String id, String name, ProductCategory category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public ProductCategory getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
}
