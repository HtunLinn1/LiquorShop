package jp.co.cybermisisons.itspj.java.demo.controllers;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.cybermisisons.itspj.java.demo.models.AppUser;
import jp.co.cybermisisons.itspj.java.demo.models.AppUserRepository;
import jp.co.cybermisisons.itspj.java.demo.models.Product;
import jp.co.cybermisisons.itspj.java.demo.models.ProductRepository;
import jp.co.cybermisisons.itspj.java.demo.models.Purchase;
import jp.co.cybermisisons.itspj.java.demo.models.PurchaseDetail;
import jp.co.cybermisisons.itspj.java.demo.models.PurchaseDetailRepository;
import jp.co.cybermisisons.itspj.java.demo.models.PurchaseRepository;
import jp.co.cybermisisons.itspj.java.demo.models.RemainingStock;
import jp.co.cybermisisons.itspj.java.demo.models.RemainingStockRepository;
import jp.co.cybermisisons.itspj.java.demo.models.SaleRepository;
import lombok.RequiredArgsConstructor;

@RequestMapping("/liquorshop/owner-homepage")
@Controller
@RequiredArgsConstructor
public class OwnerController {

  private final AppUserRepository userrep;
  private final ProductRepository productrep;
  private final PurchaseRepository purchaserep;
  private final PurchaseDetailRepository purchasedetailrep;
  private final RemainingStockRepository rsrep;
  private final SaleRepository salerep;

  @GetMapping("")
  public String homepage(Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("rStocks", rsrep.findAllByOrderByStockAsc());

    Date current_date = new Date();
    Integer purchase_amount = purchaserep.selectWithCurrentDate(current_date);

    Integer sale_amount = salerep.selectWithCurrentDate(current_date);
    Integer net_amount = sale_amount - purchase_amount;

    model.addAttribute("purchase_amount", purchase_amount);
    model.addAttribute("sale_amount", sale_amount);
    model.addAttribute("net_amount", net_amount);

    return "owner/index";
  }

  @GetMapping("/create_new_product")
  public String create_new_product(Principal principal, @ModelAttribute Product product, Model model) {
    model.addAttribute("user", principal.getName());
    return "owner/create_new_product";
  }

  @PostMapping("/new_product/save")
  public String save(@Validated @ModelAttribute Product product, BindingResult result, Model model,
      RedirectAttributes attrs) {

    if (result.hasErrors()) {
      return "owner/create_new_product";
    }
    if (productrep.findProduct(product.getProduct_name()) == null) {
      productrep.save(product);

      RemainingStock rs = new RemainingStock();
      rs.setStock(0);
      rs.setProduct(product);
      rsrep.save(rs);

    } else {
      attrs.addFlashAttribute("alreadyexist", "Product already exists!");
      return "redirect:/liquorshop/owner-homepage/create_new_product";
    }

    attrs.addFlashAttribute("success", "Product was successfully created!");
    return "redirect:/liquorshop/owner-homepage/create_new_product";
  }

  @GetMapping("/create_new_purchase")
  public String create_new_purchase(Principal principal, @ModelAttribute PurchaseDetail purchaseDetail, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("products", productrep.findAll());
    return "owner/create_new_purchase";
  }

  @RequestMapping(value = "/savePurchaseList", method = RequestMethod.POST)
  public @ResponseBody String savePurchaseList(Principal principal, @RequestBody List<PurchaseDetail> dataObj,
      RedirectAttributes attrs) {
    Date purchaseDate = new Date();
    AppUser usr = userrep.findByUsername(principal.getName());
    Integer final_total = dataObj.get(dataObj.size() - 1).getFinal_total();

    purchaserep.insertPurchase(purchaseDate, final_total, usr);

    List<Purchase> purchase_id = purchaserep.findTopByOrderByIdDesc();
    Purchase purchase = purchase_id.get(0);

    for (int i = 0; i < dataObj.size(); i++) {
      purchasedetailrep.insertPurchaseDetail( //
          dataObj.get(i).getProduct_name(), //
          dataObj.get(i).getPrice(), //
          dataObj.get(i).getProduct_category(), //
          dataObj.get(i).getProduct_type(), //
          dataObj.get(i).getQuantity(), //
          dataObj.get(i).getTotal_amount(), //
          dataObj.get(i).getFinal_total(), //
          purchase //
      );

      rsrep.updateAddStockQuantity(dataObj.get(i).getQuantity(),
          productrep.findProduct(dataObj.get(i).getProduct_name()).getId());
    }

    return "owner/create_new_purchase";
  }

  @GetMapping("/purchase_history_list")
  public String purchase_history_list(Principal principal, @ModelAttribute Purchase purchase, Model model) {

    List<Purchase> purchases = purchaserep.findAll();
    for (int i = 0; i < purchases.size(); i++) {
      Integer final_total = purchasedetailrep.selectSumTotalAmount(purchases.get(i).getId());
      purchaserep.updatePurchaseFinalTotal(final_total, purchases.get(i).getId());
    }

    model.addAttribute("user", principal.getName());
    model.addAttribute("purchases", purchaserep.findAll());
    return "owner/purchase_history";
  }

  @DeleteMapping("/purchase_history/{id}/delete")
  public String delete(@PathVariable int id, RedirectAttributes attrs) {
    List<PurchaseDetail> del_purchaseDetails = purchasedetailrep.findByPurchase(purchaserep.findById(id));

    for (int i = 0; i < del_purchaseDetails.size(); i++) {
      String product_name = del_purchaseDetails.get(i).getProduct_name();
      if (del_purchaseDetails.get(i).getQuantity() <= productrep.findProduct(product_name).getRemaining_stock()
          .getStock()) {
        Integer quantity = del_purchaseDetails.get(i).getQuantity();
        Integer product_id = productrep.findProduct(del_purchaseDetails.get(i).getProduct_name()).getId();
        rsrep.updateMinusStockQuantity(quantity, product_id);
      } else {
        attrs.addFlashAttribute("quantityError", "Quantity of " + product_name + " is greater than remaining stock!");
        return "redirect:/liquorshop/owner-homepage/purchase_history_list";
      }
    }
    purchaserep.deleteById(id);
    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/owner-homepage/purchase_history_list";
  }

  @GetMapping("/purchase_history/{id}/detail")
  public String detail(@PathVariable int id, Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("purchase_id", id);
    model.addAttribute("purchaseHistoryDetails", purchasedetailrep.findByPurchase(purchaserep.findById(id)));
    return "owner/purchase_history_detail";
  }

  @DeleteMapping("/purchase_history/{purchase_id}/detail/{detail_id}/delete")
  public String detail_delete(@PathVariable int detail_id, @PathVariable int purchase_id, RedirectAttributes attrs) {

    String product_name = purchasedetailrep.findById(detail_id).get().getProduct_name();
    Integer quantity = purchasedetailrep.findById(detail_id).get().getQuantity();
    Integer product_id = productrep.findProduct(product_name).getId();
    if (quantity <= productrep.findProduct(product_name).getRemaining_stock().getStock()) {
      rsrep.updateMinusStockQuantity(quantity, product_id);
      purchasedetailrep.deleteById(detail_id);
    } else {
      attrs.addFlashAttribute("quantityError", "Quantity is greater than remaining stock!");
      return "redirect:/liquorshop/owner-homepage/purchase_history/" + purchase_id + "/detail";
    }

    if (purchasedetailrep.findByPurchase(purchaserep.findById(purchase_id)).size() == 0) {
      purchaserep.deleteById(purchase_id);
      return "redirect:/liquorshop/owner-homepage/purchase_history_list";
    }

    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/owner-homepage/purchase_history/" + purchase_id + "/detail";
  }

  @GetMapping("/purchase_history/{purchase_id}/detail/{detail_id}/update")
  public String update(Principal principal, @PathVariable int detail_id, @PathVariable int purchase_id, Model model) {
    model.addAttribute("user", principal.getName());

    model.addAttribute("detail_id", detail_id);
    model.addAttribute("purchase_id", purchase_id);

    model.addAttribute("products", productrep.findAll());

    String product_name = purchasedetailrep.findById(detail_id).get().getProduct_name();
    model.addAttribute("stock", productrep.findProduct(product_name).getRemaining_stock().getStock());
    model.addAttribute("purchaseDetail", purchasedetailrep.findById(detail_id).get());
    return "owner/purchase_detail_update";
  }

  @PatchMapping("/purchase_history/{purchase_id}/detail/{detail_id}/detail_update")
  public String detail_update(@PathVariable int detail_id, @PathVariable int purchase_id, RedirectAttributes attrs,
      @Validated @ModelAttribute PurchaseDetail purchaseDetail, BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("products", productrep.findAll());
      attrs.addFlashAttribute("quantityError", "Insert quantity that is greater than 0!");
      return "redirect:/liquorshop/owner-homepage/purchase_history/" + purchase_id + "/detail/" + detail_id + "/update";
    }

    String product_name = purchaseDetail.getProduct_name().split(",")[0];
    Integer quantity = purchasedetailrep.findById(detail_id).get().getQuantity();
    Integer edit_quantity = purchaseDetail.getQuantity();
    Integer stock = productrep.findProduct(product_name).getRemaining_stock().getStock();

    if (purchaseDetail.getQuantity() >= quantity) {
      purchaseDetail.setId(detail_id);
      purchasedetailrep.save(purchaseDetail);

      PurchaseDetail puchase_detail = purchasedetailrep.findById(detail_id).get();

      Integer total_amount = puchase_detail.getPrice() * puchase_detail.getQuantity();
      purchasedetailrep.updatePurchaseDetailAmount(product_name, total_amount, detail_id);

      Integer net_quantity = edit_quantity - quantity;
      Integer product_id = productrep.findProduct(product_name).getId();
      rsrep.updateAddStockQuantity(net_quantity, product_id);
    } else {
      Integer net_quantity = quantity - purchaseDetail.getQuantity();
      if (net_quantity <= stock) {
        purchaseDetail.setId(detail_id);
        purchasedetailrep.save(purchaseDetail);

        PurchaseDetail puchase_detail = purchasedetailrep.findById(detail_id).get();

        Integer total_amount = puchase_detail.getPrice() * puchase_detail.getQuantity();
        purchasedetailrep.updatePurchaseDetailAmount(product_name, total_amount, detail_id);

        Integer product_id = productrep.findProduct(product_name).getId();
        rsrep.updateMinusStockQuantity(net_quantity, product_id);
      } else {
        attrs.addFlashAttribute("quantityError", "Remaining Stock is not enough!");
        return "redirect:/liquorshop/owner-homepage/purchase_history/" + purchase_id + "/detail/" + detail_id
            + "/update";
      }
    }

    attrs.addFlashAttribute("success", "Purchase detail was successfully updated!");
    return "redirect:/liquorshop/owner-homepage/purchase_history/" + purchase_id + "/detail";
  }

  @GetMapping("/product_list")
  public String poduct_list(Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("products", productrep.findAll());
    return "owner/product_list";
  }

  @DeleteMapping("/product_list/{id}/delete")
  public String product_delete(@PathVariable int id, Model model, RedirectAttributes attrs) {
    productrep.deleteById(id);
    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/owner-homepage/product_list";
  }

  @GetMapping("/product_list/{id}/update")
  public String product_update(Principal principal, @PathVariable int id, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("products", productrep.findAll());
    model.addAttribute("product", productrep.findById(id).get());
    return "owner/product_list_update";
  }

  @PatchMapping("/product_list/{id}/update")
  public String product_update(@PathVariable int id, RedirectAttributes attrs,
      @Validated @ModelAttribute Product product, BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("products", productrep.findAll());
      return "owner/product_list_update";
    }
    product.setId(id);
    productrep.save(product);

    attrs.addFlashAttribute("success", "Product was successfully updated!");
    return "redirect:/liquorshop/owner-homepage/product_list";
  }

  @GetMapping("/user_list")
  public String user_list(Principal principal, Model model) {
    model.addAttribute("login_user", principal.getName());
    model.addAttribute("users", userrep.findAll());
    return "owner/user_list";
  }

  @GetMapping("/create_user")
  public String create_user(Principal principal, @ModelAttribute AppUser appUser, Model model) {
    model.addAttribute("user", principal.getName());
    return "owner/create_user";
  }

  @PostMapping("/create_user")
  public String post_user(Principal principal, @Validated @ModelAttribute AppUser appUser, BindingResult result,
      Model model, RedirectAttributes attrs) {
    if (result.hasErrors()) {
      model.addAttribute("user", principal.getName());
      return "owner/create_user";
    }

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String hashedPassword = passwordEncoder.encode(appUser.getPassword());

    if (userrep.findUsername(appUser.getUsername()).isEmpty()) {
      AppUser user = new AppUser();
      user.setUsername(appUser.getUsername());
      user.setEmail(appUser.getEmail());
      user.setPassword(hashedPassword);
      user.setStatus(appUser.getStatus());
      userrep.save(user);
    } else {
      attrs.addFlashAttribute("alreadyexist", "Product already exists!");
      return "redirect:/liquorshop/owner-homepage/create_user";
    }

    attrs.addFlashAttribute("success", "Successfully created!");
    return "redirect:/liquorshop/owner-homepage/user_list";
  }

  @DeleteMapping("/user_list/{id}/delete")
  public String user_delete(@PathVariable int id, Model model, RedirectAttributes attrs) {
    userrep.deleteById(id);
    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/owner-homepage/user_list";
  }

  @GetMapping("/user_list/{id}/edit")
  public String user_update(Principal principal, @PathVariable int id, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("appUser", userrep.findById(id).get());
    return "owner/user_list_update";
  }

  @PatchMapping("/user_list/{id}/edit")
  public String update(Principal principal, @PathVariable int id, @Validated @ModelAttribute AppUser appUser,
      BindingResult result, Model model, RedirectAttributes attrs) {
    if (result.hasErrors()) {
      model.addAttribute("user", principal.getName());
      return "owner/user_list_update";
    }
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String hashedPassword = passwordEncoder.encode(appUser.getPassword());

    AppUser user = new AppUser();
    user.setId(appUser.getId());
    user.setUsername(appUser.getUsername());
    user.setEmail(appUser.getEmail());
    user.setPassword(hashedPassword);
    user.setStatus(appUser.getStatus());
    userrep.save(user);

    attrs.addFlashAttribute("success", "User was successfully updated!");
    return "redirect:/liquorshop/owner-homepage/user_list";
  }

  @GetMapping("/stock_list")
  public String stock_list(Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("stocks", rsrep.findAll());
    return "owner/stock_list";
  }
}
