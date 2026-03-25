package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.CategoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateCategoryRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryQuantityRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InventoryUpdateRequestDTO;
import com.dentalcloud.dentalcloudbackend.services.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDTO>> listCategories() {
        return ResponseEntity.ok(inventoryService.listAllCategories());
    }

    @PostMapping("/category")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @RequestBody @Valid CreateCategoryRequestDTO request
    ) {
        return ResponseEntity.ok(inventoryService.createCategory(request));
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createProduct(
            @RequestBody @Valid InventoryUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(inventoryService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    /**
     * Lista productos. Filtros opcionales por query string (evita confundir texto con {@code /{id}} UUID):
     * {@code ?categoryName=Insumos} — categoría por nombre exacto (sin distinguir mayúsculas)
     * {@code ?name=guante} — productos cuyo nombre contiene el texto (sin distinguir mayúsculas)
     * Se pueden combinar ambos.
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> listOrSearch(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(inventoryService.searchProducts(categoryName, name));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<InventoryResponseDTO>> getByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(inventoryService.getByCategory(categoryId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid InventoryUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(inventoryService.update(id, request));
    }

    @PutMapping("/{id}/purchase")
    public ResponseEntity<InventoryResponseDTO> purchase(
            @PathVariable UUID id,
            @RequestBody @Valid InventoryQuantityRequestDTO request
    ) {
        return ResponseEntity.ok(inventoryService.purchase(id, request));
    }

    @PutMapping("/{id}/sale")
    public ResponseEntity<InventoryResponseDTO> sale(
            @PathVariable UUID id,
            @RequestBody @Valid InventoryQuantityRequestDTO request
    ) {
        return ResponseEntity.ok(inventoryService.sale(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado correctamente"));
    }
}
