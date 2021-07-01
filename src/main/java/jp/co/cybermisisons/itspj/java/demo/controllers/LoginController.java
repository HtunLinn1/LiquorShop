package jp.co.cybermisisons.itspj.java.demo.controllers;

import java.security.Principal;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jp.co.cybermisisons.itspj.java.demo.models.AppUser;
import jp.co.cybermisisons.itspj.java.demo.models.AppUserRepository;
import lombok.RequiredArgsConstructor;

@RequestMapping("/liquorshop")
@Controller
@RequiredArgsConstructor
public class LoginController {

  private final AppUserRepository userrep;

  @GetMapping("")
  public String index(Model model) {
    return "login/index";
  }

  @GetMapping({ "/password", "/password/{pass}" })
  @ResponseBody
  public String password(@RequestParam(defaultValue = "12345") String pass) {
    BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    return enc.encode(pass);
  }

  @GetMapping("/index")
  public String index(Principal principal) {
    String loginuser = principal.getName();
    AppUser usr = userrep.findByUsername(loginuser);
    String status = usr.getStatus();
    String cashierowner = "";
    System.out.println(status);
    if (status.equals("admin")) {
      cashierowner = "redirect:/liquorshop/owner-homepage";
    } else if (status.equals("cashier")) {
      cashierowner = "redirect:/liquorshop/cashier-homepage";
    }
    return cashierowner;
  }
}
