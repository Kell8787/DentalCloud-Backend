package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CategoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateCategoryRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryQuantityRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryReconciliationResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryUpdateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.StockMovementResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.InventoryProduct;
import com.dentalcloud.dentalcloudbackend.domain.entity.ProductCategory;
import com.dentalcloud.dentalcloudbackend.domain.entity.StockMovement;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.StockMovementType;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.InventoryProductRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ProductCategoryRepository;
import com.dentalcloud.dentalcloudbackend.repositories.StockMovementRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryProductRepository inventoryProductRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;

    public InventoryResponseDTO getById(UUID id) {
        return toResponse(product(id));
    }

    public List<CategoryResponseDTO> listAllCategories() {
        return productCategoryRepository.findAll().stream()
                .map(c -> CategoryResponseDTO.builder().id(c.getId()).name(c.getName()).build()).toList();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        ProductCategory saved = productCategoryRepository.save(ProductCategory.builder().name(request.getName()).build());
        return CategoryResponseDTO.builder().id(saved.getId()).name(saved.getName()).build();
    }

    @Transactional
    public InventoryResponseDTO createProduct(InventoryUpdateRequestDTO request, String actorEmail) {
        User actor = actor(actorEmail);
        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        InventoryProduct product = InventoryProduct.builder()
                .name(request.getName()).description(request.getDescription())
                .purchasePrice(request.getPurchasePrice()).salePrice(request.getSalePrice())
                .quantity(request.getQuantity()).minimumStock(request.getMinimumStock() == null ? 0 : request.getMinimumStock())
                .unit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : "unidad")
                .category(category).deleted(false).build();
        InventoryProduct saved = inventoryProductRepository.save(product);
        if (saved.getQuantity() > 0) {
            saveMovement(saved, StockMovementType.ENTRADA, saved.getQuantity(), "ALTA_INVENTARIO", actor);
        }
        return toResponse(saved);
    }

    public List<InventoryResponseDTO> getAll() {
        return inventoryProductRepository.findAllByDeletedFalse().stream().map(this::toResponse).toList();
    }

    public List<InventoryResponseDTO> searchProducts(String categoryName, String nameFragment) {
        return searchProducts(categoryName, nameFragment, null);
    }

    public List<InventoryResponseDTO> searchProducts(String categoryName, String nameFragment, String status) {
        String cat = StringUtils.hasText(categoryName) ? categoryName.trim() : null;
        String fragment = StringUtils.hasText(nameFragment) ? nameFragment.trim() : null;
        List<InventoryResponseDTO> products = (cat == null && fragment == null
                ? getAll()
                : inventoryProductRepository.searchByCategoryNameAndProductNameNative(cat, fragment)
                .stream().map(this::toResponse).toList());
        if (!StringUtils.hasText(status)) {
            return products;
        }
        return products.stream().filter(product -> status.equalsIgnoreCase(product.getStatus())).toList();
    }

    public List<InventoryResponseDTO> getByCategory(UUID categoryId) {
        return inventoryProductRepository.findAllByCategoryIdAndDeletedFalse(categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public InventoryResponseDTO update(UUID id, InventoryUpdateRequestDTO request) {
        InventoryProduct product = productForUpdate(id);
        if (!request.getQuantity().equals(product.getQuantity())) {
            throw new BusinessException("La cantidad solo se modifica mediante movimientos de inventario");
        }
        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setMinimumStock(request.getMinimumStock() == null ? product.getMinimumStock() : request.getMinimumStock());
        if (StringUtils.hasText(request.getUnit())) product.setUnit(request.getUnit().trim());
        product.setCategory(category);
        return toResponse(inventoryProductRepository.save(product));
    }

    @Transactional
    public InventoryResponseDTO purchase(UUID id, InventoryQuantityRequestDTO request, String actorEmail) {
        User actor = actor(actorEmail);
        InventoryProduct product = productForUpdate(id);
        product.setQuantity(product.getQuantity() + request.getQuantity());
        InventoryProduct saved = inventoryProductRepository.save(product);
        saveMovement(saved, StockMovementType.ENTRADA, request.getQuantity(), request.getReason(), actor);
        return toResponse(saved);
    }

    @Transactional
    public InventoryResponseDTO sale(UUID id, InventoryQuantityRequestDTO request, String actorEmail) {
        User actor = actor(actorEmail);
        InventoryProduct product = productForUpdate(id);
        int current = product.getQuantity() == null ? 0 : product.getQuantity();
        if (request.getQuantity() > current) {
            throw new ConflictException("INSUFFICIENT_STOCK", "La cantidad solicitada excede el inventario disponible");
        }
        product.setQuantity(current - request.getQuantity());
        InventoryProduct saved = inventoryProductRepository.save(product);
        saveMovement(saved, StockMovementType.SALIDA, request.getQuantity(), request.getReason(), actor);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        InventoryProduct product = productForUpdate(id);
        product.setDeleted(true);
        inventoryProductRepository.save(product);
    }

    public List<StockMovementResponseDTO> movements(UUID productId) {
        product(productId);
        return stockMovementRepository.findByProductIdOrderByOccurredAtDesc(productId).stream()
                .map(movement -> StockMovementResponseDTO.builder().id(movement.getId())
                        .productId(movement.getProductId()).type(movement.getType()).quantity(movement.getQuantity())
                        .reason(movement.getReason()).actorId(movement.getActorId()).occurredAt(movement.getOccurredAt()).build())
                .toList();
    }

    public InventoryReconciliationResponseDTO reconciliation(UUID productId) {
        InventoryProduct product = product(productId);
        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByOccurredAtDesc(productId);
        int balance = 0;
        long entries = 0;
        long exits = 0;
        long adjustments = 0;
        for (StockMovement movement : movements) {
            switch (movement.getType()) {
                case ENTRADA -> {
                    balance += movement.getQuantity();
                    entries++;
                }
                case SALIDA -> {
                    balance -= movement.getQuantity();
                    exits++;
                }
                case AJUSTE -> {
                    balance += movement.getQuantity();
                    adjustments++;
                }
            }
        }
        int current = product.getQuantity() == null ? 0 : product.getQuantity();
        return InventoryReconciliationResponseDTO.builder().productId(productId).currentQuantity(current)
                .movementBalance(balance).discrepancy(current - balance).entries(entries).exits(exits)
                .adjustments(adjustments).consistent(current == balance).build();
    }

    private void saveMovement(InventoryProduct product, StockMovementType type, int quantity,
                              String reason, User actor) {
        stockMovementRepository.save(StockMovement.builder().productId(product.getId()).type(type)
                .quantity(quantity).reason(reason.trim()).actorId(actor.getId()).occurredAt(Instant.now()).build());
    }

    private InventoryProduct product(UUID id) {
        return inventoryProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private InventoryProduct productForUpdate(UUID id) {
        return inventoryProductRepository.findByIdAndDeletedFalseForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private User actor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private InventoryResponseDTO toResponse(InventoryProduct product) {
        return InventoryResponseDTO.builder().id(product.getId()).name(product.getName())
                .description(product.getDescription()).purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice()).categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName()).quantity(product.getQuantity())
                .minimumStock(product.getMinimumStock()).unit(product.getUnit()).version(product.getVersion())
                .status(status(product)).build();
    }

    private String status(InventoryProduct product) {
        if (product.getQuantity() == null || product.getQuantity() == 0) return "SIN_STOCK";
        return product.getQuantity() <= product.getMinimumStock() ? "BAJO" : "DISPONIBLE";
    }
}
