package com.project.demo.rest.Product;

import com.project.demo.logic.entity.Product.Product;
import com.project.demo.logic.entity.Product.ProductRepository;
import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Products retrieved successfully",
                productPage.getContent(),
                HttpStatus.OK,
                meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductById(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if (foundProduct.isPresent()) {
            return new GlobalResponseHandler().handleResponse(
                    "Product retrieved successfully",
                    foundProduct.get(),
                    HttpStatus.OK,
                    request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Product with id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request);
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addProduct(@RequestBody Product product, HttpServletRequest request) {
        // Verificar que el categoryId está presente y es válido
        if (product.getCategoryId() == null) {
            return new GlobalResponseHandler().handleResponse(
                    "Category ID is missing",
                    HttpStatus.BAD_REQUEST,
                    request);
        }

        // Buscar la categoría usando el ID proporcionado
        Optional<Category> category = categoryRepository.findById(product.getCategoryId());
        if (category.isPresent()) {
            product.setCategory(category.get()); // Establecer la categoría en el producto
            Product savedProduct = productRepository.save(product); // Guardar el producto
            return new GlobalResponseHandler().handleResponse(
                    "Product successfully saved",
                    savedProduct,
                    HttpStatus.OK,
                    request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Category with id " + product.getCategoryId() + " not found",
                    HttpStatus.NOT_FOUND,
                    request);
        }
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProduct(@RequestBody Product product, HttpServletRequest request) {
        // Verificar que el categoryId está presente y es válido
        if (product.getCategoryId() == null) {
            return new GlobalResponseHandler().handleResponse(
                    "Category ID is missing",
                    HttpStatus.BAD_REQUEST,
                    request);
        }

        // Buscar la categoría usando el ID proporcionado
        Optional<Category> category = categoryRepository.findById(product.getCategoryId());
        if (category.isPresent()) {
            product.setCategory(category.get()); // Establecer la categoría en el producto
            Product updatedProduct = productRepository.save(product); // Guardar el producto actualizado
            return new GlobalResponseHandler().handleResponse(
                    "Product successfully updated",
                    updatedProduct,
                    HttpStatus.OK,
                    request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Category with id " + product.getCategoryId() + " not found",
                    HttpStatus.NOT_FOUND,
                    request);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() && hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if (foundProduct.isPresent()) {
            productRepository.deleteById(id);
            return new GlobalResponseHandler().handleResponse(
                    "Product successfully deleted",
                    HttpStatus.OK,
                    request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "Product with id " + id + " not found",
                    HttpStatus.NOT_FOUND,
                    request);
        }
    }
}