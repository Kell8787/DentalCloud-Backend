package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CategoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateCategoryRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryQuantityRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryUpdateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.InventoryProduct;
import com.dentalcloud.dentalcloudbackend.domain.entity.ProductCategory;
import com.dentalcloud.dentalcloudbackend.repositories.InventoryProductRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ProductCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryProductRepository inventoryProductRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public InventoryResponseDTO getById(UUID id) {
        InventoryProduct product = inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        return toResponse(product);
    }

    public List<CategoryResponseDTO> listAllCategories() {
        return productCategoryRepository.findAll().stream()
                .map(c -> CategoryResponseDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .build();
        ProductCategory saved = productCategoryRepository.save(category);
        return CategoryResponseDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }

    @Transactional
    public InventoryResponseDTO createProduct(InventoryUpdateRequestDTO request) {
        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        InventoryProduct product = InventoryProduct.builder()
                .name(request.getName())
                .description(request.getDescription())
                .purchasePrice(request.getPurchasePrice())
                .salePrice(request.getSalePrice())
                .quantity(request.getQuantity())
                .minimumStock(request.getMinimumStock() == null ? 0 : request.getMinimumStock())
                .unit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : "unidad")
                .category(category)
                .deleted(false)
                .build();

        InventoryProduct saved = inventoryProductRepository.save(product);
        return toResponse(saved);
    }

    public List<InventoryResponseDTO> getAll() {
        return inventoryProductRepository.findAllByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * @param categoryName nombre exacto de categoría (opcional)
     * @param nameFragment fragmento a buscar dentro del nombre del producto (opcional)
     */
    public List<InventoryResponseDTO> searchProducts(String categoryName, String nameFragment) {
        String cat = StringUtils.hasText(categoryName) ? categoryName.trim() : null;
        String frag = StringUtils.hasText(nameFragment) ? nameFragment.trim() : null;
        if (cat == null && frag == null) {
            return getAll();
        }
        return inventoryProductRepository
                .searchByCategoryNameAndProductNameNative(cat, frag)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<InventoryResponseDTO> getByCategory(UUID categoryId) {
        return inventoryProductRepository.findAllByCategoryIdAndDeletedFalse(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InventoryResponseDTO update(UUID id, InventoryUpdateRequestDTO request) {
        InventoryProduct product = inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setQuantity(request.getQuantity());
        if (request.getMinimumStock() != null) {
            product.setMinimumStock(request.getMinimumStock());
        }
        if (StringUtils.hasText(request.getUnit())) {
            product.setUnit(request.getUnit().trim());
        }
        product.setCategory(category);

        InventoryProduct saved = inventoryProductRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public InventoryResponseDTO purchase(UUID id, InventoryQuantityRequestDTO request) {
        InventoryProduct product = inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        product.setQuantity(product.getQuantity() + request.getQuantity());
        return toResponse(inventoryProductRepository.save(product));
    }

    @Transactional
    public InventoryResponseDTO sale(UUID id, InventoryQuantityRequestDTO request) {
        InventoryProduct product = inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        int current = product.getQuantity() == null ? 0 : product.getQuantity();
        int requested = request.getQuantity();

        if (requested > current) {
            throw new IllegalArgumentException("La cantidad solicitada excede el inventario disponible");
        }

        product.setQuantity(current - requested);
        return toResponse(inventoryProductRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        InventoryProduct product = inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        product.setDeleted(true);
        inventoryProductRepository.save(product);
    }

    private InventoryResponseDTO toResponse(InventoryProduct product) {
        return InventoryResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .quantity(product.getQuantity())
                .minimumStock(product.getMinimumStock())
                .unit(product.getUnit())
                .version(product.getVersion())
                .build();
    }
}
