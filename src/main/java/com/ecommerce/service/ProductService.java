package com.ecommerce.service;

import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    // Lista todos los productos paginados (lo usa la pagina principal)
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    // Filtra productos por categoria, util para cuando el usuario navega por categorias
    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toResponse);
    }

    // Busca un producto por su id, si no existe lanza error
    public ProductResponse findById(Long id) {
        Product product = getProduct(id);
        return productMapper.toResponse(product);
    }

    // Crea un producto nuevo, solo lo pueden hacer los admins
    public ProductResponse create(CreateProductRequest request) {
        Category category = getCategory(request.categoryId()); // busco que la categoria exista

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(category)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    // Actualiza los datos de un producto existente
    public ProductResponse update(Long id, CreateProductRequest request) {
        Product product = getProduct(id);
        Category category = getCategory(request.categoryId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(category);

        return productMapper.toResponse(productRepository.save(product));
    }

    // Elimina un producto de la base de datos
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    // Metodo auxiliar para no repetir la busqueda en cada metodo
    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con id: " + id));
    }
}
