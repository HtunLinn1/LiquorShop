package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
  @Modifying
  @Query(nativeQuery = true, value = "insert into Sale (sale_date, final_total, user_id) Values(:date, :final_total, :userId)")
  @Transactional
  public int insertSale(@Param("date") Date date, @Param("final_total") Integer final_total,
      @Param("userId") AppUser userId);

  List<Sale> findTopByOrderByIdDesc();

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("update Sale set final_total =:final_total where id =:sale_id")
  void updateSaleFinalTotal(@Param("final_total") Integer final_total, @Param("sale_id") Integer sale_id);
}
