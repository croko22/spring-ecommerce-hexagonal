package com.example.ecommerce.product.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM ProductEntity p WHERE p.lowStockThreshold > 0 AND p.stock <= p.lowStockThreshold")
    List<ProductEntity> findLowStockProducts();

    @Query("SELECT p FROM ProductEntity p WHERE " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock <= 0)) AND " +
           "(:search IS NULL OR p.name LIKE CONCAT('%', :search, '%') OR p.description LIKE CONCAT('%', :search, '%'))")
    Page<ProductEntity> findByFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock,
            @Param("search") String search,
            Pageable pageable);

    // ========== Atomic stock operations ==========

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int releaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :id AND p.stock + :quantity >= 0")
    int adjustStock(@Param("id") Long id, @Param("quantity") int quantity);
}