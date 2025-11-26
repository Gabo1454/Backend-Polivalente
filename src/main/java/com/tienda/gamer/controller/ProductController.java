package com.tienda.gamer.controller;

import com.tienda.gamer.model.Product;
import com.tienda.gamer.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "Operaciones CRUD de productos de la tienda gamer")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ============================================================
    // GET — PUBLICO (NO REQUIERE TOKEN)
    // ============================================================

    /**
     * GET /api/products
     * Filtrar por:
     *  - category="Juegos"
     *  - offerOnly=true
     */
    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Devuelve todos los productos, con filtros opcionales por categoría o si están en oferta"
    )
    public ResponseEntity<List<Product>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean offerOnly
    ) {
        return ResponseEntity.ok(
                productService.findAll(category, offerOnly)
        );
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // ============================================================
    // ADMIN: REQUIERE TOKEN + ROL ADMIN
    // ============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Crear un nuevo producto",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {

        Product created = productService.create(product);

        return ResponseEntity
                .created(URI.create("/api/products/" + created.getId()))
                .body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un producto existente",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Valid @RequestBody Product product
    ) {
        return ResponseEntity.ok(
                productService.update(id, product)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un producto",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
