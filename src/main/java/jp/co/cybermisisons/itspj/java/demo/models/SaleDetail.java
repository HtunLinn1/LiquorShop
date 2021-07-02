package jp.co.cybermisisons.itspj.java.demo.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SaleDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @NotNull(message = "insert quantity!")
  @Range(min = 0, max = 20000)
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "total_amount", nullable = false)
  private Integer total_amount;

  @Column(name = "final_total", nullable = false)
  private Integer final_total;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "sale_id")
  private Sale sale;
}
