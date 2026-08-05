package com.dentalcloud.dentalcloudbackend;

import com.dentalcloud.dentalcloudbackend.domain.entity.InventoryProduct;
import com.dentalcloud.dentalcloudbackend.domain.entity.ProductCategory;
import com.dentalcloud.dentalcloudbackend.repositories.InventoryProductRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ProductCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class PostgreSqlFlywayIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dentalcloud_test")
            .withUsername("dentalcloud_test")
            .withPassword("dentalcloud_test");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryProductRepository inventoryProductRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void appliesFlywayMigrationAndLoadsApplicationAgainstPostgres() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = true",
                Integer.class
        );
        Integer seededDoctors = jdbcTemplate.queryForObject(
                "select count(*) from dental_users where role = 'DOCTOR'",
                Integer.class
        );
        Integer inventoryColumns = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where lower(table_name) = 'inventory_products'
                  and lower(column_name) in ('minimum_stock', 'unit', 'version')
                """,
                Integer.class
        );
        Integer stockMovementTables = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where lower(table_name) = 'stock_movements'
                """,
                Integer.class
        );
        Integer clinicalTables = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where lower(table_name) in (
                    'patient_treatment_plans',
                    'treatment_steps',
                    'clinical_documents',
                    'aftercare_instructions'
                )
                """,
                Integer.class
        );
        Integer appointmentColumns = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where lower(table_name) = 'citas'
                  and lower(column_name) in (
                    'starts_at', 'ends_at', 'status', 'source',
                    'treatment_plan_id', 'rescheduled_from_id',
                    'cancellation_reason', 'version'
                  )
                """,
                Integer.class
        );

        assertThat(appliedMigrations).isEqualTo(4);
        assertThat(seededDoctors).isGreaterThanOrEqualTo(2);
        assertThat(inventoryColumns).isEqualTo(3);
        assertThat(stockMovementTables).isEqualTo(1);
        assertThat(clinicalTables).isEqualTo(4);
        assertThat(appointmentColumns).isEqualTo(8);

        UUID categoryId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID productId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        jdbcTemplate.update(
                "insert into inventory_categories (id, name) values (?, ?)",
                categoryId, "Compatibilidad V2"
        );
        jdbcTemplate.update(
                """
                insert into inventory_products
                    (id, name, description, purchase_price, sale_price, quantity, deleted, category_id)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                productId, "Producto legado", "Conserva sus datos", 1.00, 1.50, 4, false, categoryId
        );

        var defaults = jdbcTemplate.queryForMap(
                "select minimum_stock, unit, version from inventory_products where id = ?",
                productId
        );
        assertThat(defaults.get("minimum_stock")).isEqualTo(0);
        assertThat(defaults.get("unit")).isEqualTo("unidad");
        assertThat(defaults.get("version")).isEqualTo(0L);
    }

    @Test
    void rejectsNegativeInventoryQuantities() {
        UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID productId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        jdbcTemplate.update(
                "insert into inventory_categories (id, name) values (?, ?)",
                categoryId, "Prueba migración"
        );

        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                insert into inventory_products
                    (id, name, description, purchase_price, sale_price, quantity,
                     minimum_stock, unit, version, deleted, category_id)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                productId, "Producto inválido", "No debe persistirse", 1.00, 1.50,
                -1, 0, "unidad", 0, false, categoryId
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    void incrementsInventoryVersionWhenProductChanges() {
        ProductCategory category = productCategoryRepository.save(ProductCategory.builder()
                .name("Versionado V2")
                .build());
        InventoryProduct product = inventoryProductRepository.saveAndFlush(InventoryProduct.builder()
                .name("Producto versionado")
                .description("Verifica optimistic locking")
                .purchasePrice(BigDecimal.ONE)
                .salePrice(BigDecimal.TEN)
                .quantity(4)
                .category(category)
                .build());

        assertThat(product.getVersion()).isZero();

        product.setQuantity(5);
        InventoryProduct updated = inventoryProductRepository.saveAndFlush(product);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }
}
