package jp.co.cybermisisons.itspj.java.demo.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
  @Query(nativeQuery = true, value = "SELECT * FROM product where product_name = :product_name")
  Product findProduct(@Param("product_name") String product_name);

  // Product findByProductname(String product_name);
}
