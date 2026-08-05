package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryQuantityRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.InventoryProduct;
import com.dentalcloud.dentalcloudbackend.domain.entity.ProductCategory;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.InventoryProductRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ProductCategoryRepository;
import com.dentalcloud.dentalcloudbackend.repositories.StockMovementRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock private InventoryProductRepository productRepository;
    @Mock private ProductCategoryRepository categoryRepository;
    @Mock private StockMovementRepository movementRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private InventoryService service;

    @Test
    void rejectsSaleThatWouldCreateNegativeStock() {
        UUID productId = UUID.randomUUID();
        User secretary = User.builder().id(UUID.randomUUID()).role(Rol.SECRETARIA).build();
        InventoryProduct product = InventoryProduct.builder().id(productId).quantity(2).minimumStock(1).build();
        InventoryQuantityRequestDTO request = new InventoryQuantityRequestDTO();
        request.setQuantity(3);
        request.setReason("Consumo clínico");
        when(userRepository.findByEmail("secretary@example.com")).thenReturn(Optional.of(secretary));
        when(productRepository.findByIdAndDeletedFalseForUpdate(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.sale(productId, request, "secretary@example.com"))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.ConflictException.class);
    }

    @Test
    void derivesLowStockStatusFromMinimumStock() {
        ProductCategory category = ProductCategory.builder().id(UUID.randomUUID()).name("Insumos").build();
        InventoryProduct product = InventoryProduct.builder().id(UUID.randomUUID()).name("Guantes")
                .description("Nitrilo").purchasePrice(java.math.BigDecimal.ONE).salePrice(java.math.BigDecimal.ONE)
                .quantity(2).minimumStock(2).unit("caja").category(category).build();
        when(productRepository.findByIdAndDeletedFalse(product.getId())).thenReturn(Optional.of(product));

        assertThat(service.getById(product.getId()).getStatus()).isEqualTo("BAJO");
    }
}
