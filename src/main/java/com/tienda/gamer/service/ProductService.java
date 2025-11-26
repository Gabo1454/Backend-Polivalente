package com.tienda.gamer.service;

import com.tienda.gamer.model.Product;
import com.tienda.gamer.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lista productos.
     * - Si category != null -> filtra por categoría.
     * - Si offerOnly = true -> solo productos en oferta.
     */
    public List<Product> findAll(String category, Boolean offerOnly) {
        List<Product> products = productRepository.findAll();

        if (category != null && !category.isBlank()) {
            products = products.stream()
                    .filter(p -> p.getCategories() != null &&
                            p.getCategories().contains(category))
                    .toList();
        }

        if (offerOnly != null && offerOnly) {
            products = products.stream()
                    .filter(Product::isOffer)
                    .toList();
        }

        return products;
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    public Product create(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    public Product update(Long id, Product updated) {
        Product existing = findById(id);

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setDescription(updated.getDescription());
        existing.setImage(updated.getImage());
        existing.setOffer(updated.isOffer());
        existing.setCategories(updated.getCategories());

        return productRepository.save(existing);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado para eliminar: " + id);
        }
        productRepository.deleteById(id);
    }
}
