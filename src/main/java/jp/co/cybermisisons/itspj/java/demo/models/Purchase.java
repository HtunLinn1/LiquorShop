package jp.co.cybermisisons.itspj.java.demo.models;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Purchase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @NotNull
  @DateTimeFormat(iso = ISO.DATE)
  @Column(name = "purchase_date", nullable = false)
  private LocalDate purchase_date;

  @Range(min = 0, max = 9000000)
  @Column(name = "final_total", nullable = false)
  private int final_total;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private AppUser app_user;

  @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL)
  private List<PurchaseDetail> purchase_details;
}
