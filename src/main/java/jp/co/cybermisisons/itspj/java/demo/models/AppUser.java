package jp.co.cybermisisons.itspj.java.demo.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Size(max = 25)
  @NotBlank(message = "名前を入力してください。")
  @Column(name = "username", nullable = false)
  private String username;

  @Email(message = "フォーマットが間違っています。")
  @NotBlank(message = "emailを入力してください。")
  @Size(max = 30)
  @Column(name = "email", nullable = false)
  private String email;

  @NotBlank(message = "passwordを入力してください。")
  @Size(max = 255)
  @Column(name = "password", nullable = true)
  private String password;

  @NotBlank
  @Column(name = "status", nullable = false)
  private String status;

  @OneToMany(mappedBy = "app_user")
  private List<Purchase> purchases;

  @OneToMany(mappedBy = "app_user")
  private List<Sale> sales;
}
