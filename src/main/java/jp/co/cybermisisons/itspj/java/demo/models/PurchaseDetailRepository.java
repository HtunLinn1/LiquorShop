package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Integer> {
        @Modifying
        @Query(nativeQuery = true, value = "insert into purchase_detail (product_name, price, product_category, product_type, quantity, total_amount, final_total, purchase_id) Values(:product_name, :price, :product_category, :product_type, :quantity, :total_amount, :final_total, :purchase)")
        @Transactional
        public int insertPurchaseDetail( //
                        @Param("product_name") String product_name, //
                        @Param("price") Integer price, //
                        @Param("product_category") String product_category, //
                        @Param("product_type") String product_type, //
                        @Param("quantity") Integer quantity, //
                        @Param("total_amount") Integer total_amount, //
                        @Param("final_total") Integer final_total, //
                        @Param("purchase") Purchase purchase //
        );

        List<PurchaseDetail> findByPurchase(Object purchase);

        @Transactional
        @Modifying(clearAutomatically = true)
        @Query("update PurchaseDetail set product_name =:product_name, total_amount =:total_amount where id =:detail_id")
        void updatePurchaseDetailAmount(@Param("product_name") String product_name,
                        @Param("total_amount") Integer total_amount, @Param("detail_id") Integer detail_id);

        @Query(value = "select sum(pd.total_amount) from purchase_detail as pd where purchase_id = :purchase_id ", nativeQuery = true)
        Integer selectSumTotalAmount(@Param("purchase_id") Integer purchase_id);

        @Query(value = "select sum(pd.quantity) from purchase_detail as pd where product_name = :product_name ", nativeQuery = true)
        Integer selectSumStockQuantity(@Param("product_name") String product_name);
}
