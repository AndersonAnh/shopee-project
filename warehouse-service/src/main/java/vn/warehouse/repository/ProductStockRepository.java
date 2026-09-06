package vn.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import vn.warehouse.entity.ProductStock;

import java.util.Optional;
import java.util.UUID;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    Optional<ProductStock> findByProductId(UUID productId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT ps FROM ProductStock ps WHERE ps.productId = :productId")
    Optional<ProductStock> findByProductIdWithLock(@Param("productId") UUID productId);
}