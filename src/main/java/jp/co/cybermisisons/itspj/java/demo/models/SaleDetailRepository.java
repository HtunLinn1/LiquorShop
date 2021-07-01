package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Integer> {
  @Modifying
  @Query(nativeQuery = true, value = "insert into sale_detail (quantity, total_amount, final_total, product_id, sale_id) Values(:quantity, :total_amount, :final_total, :product, :sale)")
  @Transactional
  public int insertSaleDetail( //
      @Param("quantity") Integer quantity, //
      @Param("total_amount") Integer total_amount, //
      @Param("final_total") Integer final_total, //
      @Param("product") Product product, //
      @Param("sale") Sale sale); //

  @Query(value = "select sum(sd.total_amount) from sale_detail as sd where sale_id = :sale_id ", nativeQuery = true)
  Integer selectSumTotalAmount(@Param("sale_id") Integer sale_id);

  List<SaleDetail> findBySale(Object sale);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("update SaleDetail set quantity =:quantity, total_amount =:total_amount where id =:detail_id")
  void updateSaleDetailAmount(@Param("quantity") Integer quantity, @Param("total_amount") Integer total_amount,
      @Param("detail_id") Integer detail_id);
}
