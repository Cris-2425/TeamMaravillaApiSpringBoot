package com.teammaravilla.api.recipe;

import com.teammaravilla.api.product.Product;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @Column(nullable = false, updatable = false)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "recipe_products",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position", nullable = false)
    private List<Product> products = new ArrayList<>();

    protected Recipe() {
    }

    public Recipe(Integer id, String title, String imageUrl,
                  String instructions, List<Product> products) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.products = new ArrayList<>(products);
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getInstructions() { return instructions; }
    public List<Product> getProducts() { return List.copyOf(products); }
}
