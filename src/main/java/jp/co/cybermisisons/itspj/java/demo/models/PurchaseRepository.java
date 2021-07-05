package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
  @Modifying
  @Query(nativeQuery = true, value = "insert into Purchase (purchase_date, final_total, user_id) Values(:date, :final_total, :userId)")
  @Transactional
  public int insertPurchase(@Param("date") Date date, @Param("final_total") Integer final_total,
      @Param("userId") AppUser userId);

  List<Purchase> findTopByOrderByIdDesc();

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("update Purchase set final_total =:final_total where id =:purchase_id")
  void updatePurchaseFinalTotal(@Param("final_total") Integer final_total, @Param("purchase_id") Integer purchase_id);

  @Query(value = "select sum(final_total) from purchase where MONTH(purchase_date) = MONTH(:current_date) ", nativeQuery = true)
  Integer selectWithCurrentDate(@Param("current_date") Date current_date);

}
