package jp.co.cybermisisons.itspj.java.demo.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PurchaseDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @NotBlank(message = "insert product name!")
  @Size(max = 50)
  @Column(name = "product_name", nullable = false)
  private String product_name;

  @NotNull(message = "insert price!")
  @Min(0)
  @Column(name = "price", nullable = false)
  private Integer price;

  @NotBlank(message = "select category!")
  @Size(max = 50)
  @Column(name = "product_category", nullable = false)
  private String product_category;

  @NotBlank(message = "select type!")
  @Size(max = 50)
  @Column(name = "product_type", nullable = false)
  private String product_type;

  @NotNull(message = "insert quantity!")
  @Range(min = 0, max = 2000)
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "total_amount", nullable = false)
  private Integer total_amount;

  @Column(name = "final_total", nullable = false)
  private Integer final_total;

  @ManyToOne
  @JoinColumn(name = "purchase_id")
  private Purchase purchase;
}
