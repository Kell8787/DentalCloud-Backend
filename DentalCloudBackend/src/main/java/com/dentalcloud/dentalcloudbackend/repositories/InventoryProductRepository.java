package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.InventoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryProductRepository extends JpaRepository<InventoryProduct, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from InventoryProduct p where p.id = :id and p.deleted = false")
    Optional<InventoryProduct> findByIdAndDeletedFalseForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);
    Optional<InventoryProduct> findByIdAndDeletedFalse(UUID id);
    List<InventoryProduct> findAllByDeletedFalse();
    List<InventoryProduct> findAllByCategoryIdAndDeletedFalse(UUID categoryId);

    /**
     * PostgreSQL: usa ILIKE (no LOWER sobre columnas) para evitar errores si el tipo
     * en BD quedó mal como bytea, y para comparaciones insensibles a mayúsculas
     * cuando las columnas son texto/varchar.
     */
    @Query(value = """
            SELECT p.*
            FROM inventory_products p
            INNER JOIN inventory_categories c ON c.id = p.category_id
            WHERE p.deleted = false
            AND (:categoryName IS NULL
                 OR CAST(:categoryName AS text) ILIKE CAST(c.name AS text))
            AND (:nameFragment IS NULL
                 OR CAST(p.name AS text) ILIKE CONCAT('%', CAST(:nameFragment AS text), '%'))
            """, nativeQuery = true)
    List<InventoryProduct> searchByCategoryNameAndProductNameNative(
            @Param("categoryName") String categoryName,
            @Param("nameFragment") String nameFragment
    );
}
