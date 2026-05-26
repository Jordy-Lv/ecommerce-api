package com.ecommerce.service;

import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product() {
        Category category = Category.builder().id(1L).name("Electronics").build();
        return Product.builder()
                .id(1L)
                .name("Laptop Dell")
                .description("Laptop 15 pulgadas")
                .price(new BigDecimal("899.99"))
                .stock(10)
                .category(category)
                .build();
    }

    private ProductResponse response() {
        return new ProductResponse(1L, "Laptop Dell", new BigDecimal("899.99"), 10, "Electronics");
    }

    @Test
    void findAll_returnsPaginatedList() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product()), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);
        when(productMapper.toResponse(any(Product.class))).thenReturn(response());

        // when
        Page<ProductResponse> result = productService.findAll(pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop Dell", result.getContent().get(0).name());
    }

    @Test
    void findById_notFound() {
        // given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(EntityNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void create_success() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Laptop Dell", "Laptop 15 pulgadas", new BigDecimal("899.99"), 10, 1L);
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any(Product.class))).thenReturn(response());

        // when
        ProductResponse result = productService.create(request);

        // then
        assertEquals("Laptop Dell", result.name());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_success() {
        // given
        CreateProductRequest request = new CreateProductRequest(
                "Laptop HP", "Otra laptop", new BigDecimal("750.00"), 5, 1L);
        Product product = product();
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any(Product.class))).thenReturn(response());

        // when
        productService.update(1L, request);

        // then
        assertEquals("Laptop HP", product.getName());
        assertEquals(new BigDecimal("750.00"), product.getPrice());
        assertEquals(5, product.getStock());
    }

    @Test
    void delete_success() {
        // given
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // when
        productService.delete(1L);

        // then
        verify(productRepository).delete(product);
    }
}
