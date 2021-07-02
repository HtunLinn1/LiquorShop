package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RemainingStockRepository extends JpaRepository<RemainingStock, Integer> {

    List<RemainingStock> findAllByOrderByStockAsc();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update remaining_stock as rs set rs.stock = (rs.stock + :add_stock) where rs.product_id = :input_product_id", nativeQuery = true)
    Integer updateAddStockQuantity(@Param("add_stock") Integer add_stock,
            @Param("input_product_id") Integer input_product_id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update remaining_stock as rs set rs.stock = (rs.stock - :sale_stock) where rs.product_id = :input_product_id", nativeQuery = true)
    Integer updateMinusStockQuantity(@Param("sale_stock") Integer sale_stock,
            @Param("input_product_id") Integer input_product_id);

    @Query(nativeQuery = true, value = "SELECT * FROM remaining_stock where product_id = :product_id")
    RemainingStock findByProductId(@Param("product_id") Integer product_id);
}
