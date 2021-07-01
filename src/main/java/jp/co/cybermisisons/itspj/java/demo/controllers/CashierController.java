package jp.co.cybermisisons.itspj.java.demo.controllers;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.cybermisisons.itspj.java.demo.models.AppUser;
import jp.co.cybermisisons.itspj.java.demo.models.AppUserRepository;
import jp.co.cybermisisons.itspj.java.demo.models.Product;
import jp.co.cybermisisons.itspj.java.demo.models.ProductRepository;
import jp.co.cybermisisons.itspj.java.demo.models.Sale;
import jp.co.cybermisisons.itspj.java.demo.models.SaleDetail;
import jp.co.cybermisisons.itspj.java.demo.models.SaleDetailRepository;
import jp.co.cybermisisons.itspj.java.demo.models.SaleRepository;
import lombok.RequiredArgsConstructor;

@RequestMapping("/liquorshop/cashier-homepage")
@RequiredArgsConstructor
@Controller
public class CashierController {

  private final AppUserRepository userrep;
  private final ProductRepository productrep;
  private final SaleDetailRepository saledetailtrep;
  private final SaleRepository salerep;

  @GetMapping("")
  public String homepage(Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("status", userrep.findByUsername(principal.getName()).getStatus());
    return "cashier/index";
  }

  @GetMapping("/create_new_sale")
  public String create_new_sale(Principal principal, @ModelAttribute SaleDetail saleDetail, Model model) {
    model.addAttribute("user", principal.getName());

    model.addAttribute("products", productrep.findAll());

    return "cashier/create_new_sale";
  }

  @RequestMapping(value = "/saveSaleList", method = RequestMethod.POST)
  public @ResponseBody String saveSaleList(Principal principal, @RequestBody List<SaleDetail> dataObj) {
    Date purchaseDate = new Date();
    AppUser usr = userrep.findByUsername(principal.getName());
    Integer final_total = dataObj.get(dataObj.size() - 1).getFinal_total();

    salerep.insertSale(purchaseDate, final_total, usr);

    List<Sale> sale_id = salerep.findTopByOrderByIdDesc();
    Sale sale = sale_id.get(0);
    for (int i = 0; i < dataObj.size(); i++) {
      Product product = productrep.findById(dataObj.get(i).getId()).get();
    }

    for (int i = 0; i < dataObj.size(); i++) {
      saledetailtrep.insertSaleDetail( //
          dataObj.get(i).getQuantity(), //
          dataObj.get(i).getTotal_amount(), //
          dataObj.get(i).getFinal_total(), //
          productrep.findById(dataObj.get(i).getId()).get(), //
          sale //
      );
    }
    return "cashier/create_new_sale";
  }

  @GetMapping("/sale_history_list")
  public String sale_history_list(Principal principal, @ModelAttribute Sale sale, Model model) {

    List<Sale> sales = salerep.findAll();
    for (int i = 0; i < sales.size(); i++) {
      Integer final_total = saledetailtrep.selectSumTotalAmount(sales.get(i).getId());
      salerep.updateSaleFinalTotal(final_total, sales.get(i).getId());
    }

    model.addAttribute("user", principal.getName());
    model.addAttribute("sales", salerep.findAll());
    return "cashier/sale_history";
  }

  @DeleteMapping("/sale_history/{id}/delete")
  public String history_delete(@PathVariable int id, RedirectAttributes attrs) {
    salerep.deleteById(id);
    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/cashier-homepage/sale_history_list";
  }

  @GetMapping("/sale_history/{id}/detail")
  public String detail(@PathVariable int id, Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("sale_id", id);
    model.addAttribute("saleHistoryDetails", saledetailtrep.findBySale(salerep.findById(id)));
    return "cashier/sale_history_detail";
  }

  @DeleteMapping("/sale_history/{sale_id}/detail/{detail_id}/delete")
  public String detail_delete(@PathVariable int detail_id, @PathVariable int sale_id, RedirectAttributes attrs) {

    saledetailtrep.deleteById(detail_id);
    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail";
  }

  @GetMapping("/sale_history/{sale_id}/detail/{detail_id}/update")
  public String update(Principal principal, @PathVariable int detail_id, @PathVariable int sale_id, Model model) {
    model.addAttribute("user", principal.getName());

    model.addAttribute("detail_id", detail_id);
    model.addAttribute("sale_id", sale_id);

    model.addAttribute("products", productrep.findAll());
    model.addAttribute("saleDetail", saledetailtrep.findById(detail_id).get());
    return "cashier/sale_detail_update";
  }

  @PatchMapping("/sale_history/{sale_id}/detail/{detail_id}/detail_update")
  public String detail_update(@PathVariable int detail_id, @PathVariable int sale_id, RedirectAttributes attrs,
      @Validated @ModelAttribute SaleDetail saleDetail, BindingResult result, Model model) {
    if (result.hasErrors()) {
      //
      model.addAttribute("detail_id", detail_id);
      model.addAttribute("sale_id", sale_id);

      //

      model.addAttribute("products", productrep.findAll());
      return "cashier/sale_detail_update";
    }
    // saleDetail.setId(detail_id);
    // saledetailtrep.save(saleDetail);

    SaleDetail sale_detail = saledetailtrep.findById(detail_id).get();

    Integer total_amount = sale_detail.getProduct().getPrice() * saleDetail.getQuantity();
    saledetailtrep.updateSaleDetailAmount(saleDetail.getQuantity(), total_amount, detail_id);

    attrs.addFlashAttribute("success", "Sale detail was successfully updated!");
    return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail";
  }
}
