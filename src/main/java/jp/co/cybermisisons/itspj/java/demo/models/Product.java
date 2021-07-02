package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Product {
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

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<SaleDetail> sale_details;

  @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
  private RemainingStock remaining_stocks;

}
