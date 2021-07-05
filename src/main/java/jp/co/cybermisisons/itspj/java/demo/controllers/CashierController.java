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
import jp.co.cybermisisons.itspj.java.demo.models.ProductRepository;
import jp.co.cybermisisons.itspj.java.demo.models.RemainingStock;
import jp.co.cybermisisons.itspj.java.demo.models.RemainingStockRepository;
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
  private final RemainingStockRepository rsrep;

  @GetMapping("")
  public String homepage(Principal principal, Model model) {
    model.addAttribute("user", principal.getName());
    model.addAttribute("status", userrep.findByUsername(principal.getName()).getStatus());
    return "cashier/index";
  }

  @GetMapping("/create_new_sale")
  public String create_new_sale(Principal principal, @ModelAttribute SaleDetail saleDetail, Model model) {
    model.addAttribute("user", principal.getName());

    // model.addAttribute("products", productrep.findAll());
    List<RemainingStock> stocks = rsrep.findAll();
    model.addAttribute("rStocks", stocks);

    return "cashier/create_new_sale";
  }

  // @RequestMapping(value = "/createNewSaleWithProductId", method =
  // RequestMethod.POST)
  // public @ResponseBody String createNewSaleWithProductId(Principal principal,
  // @RequestBody Product productObj,
  // Model model) {
  // model.addAttribute("user", principal.getName());
  // model.addAttribute("products", productrep.findAll());

  // System.out.println(productObj.getId());
  // System.out.println(productObj.getProduct_name());
  // model.addAttribute("rStock",
  // rsrep.findByProductId(productObj.getId()).getStock());
  // System.out.println(rsrep.findByProductId(productObj.getId()).getStock());

  // return "cashier/create_new_sale";
  // }

  @RequestMapping(value = "/saveSaleList", method = RequestMethod.POST)
  public @ResponseBody String saveSaleList(Principal principal, @RequestBody List<SaleDetail> dataObj) {
    Date purchaseDate = new Date();
    AppUser usr = userrep.findByUsername(principal.getName());
    Integer final_total = dataObj.get(dataObj.size() - 1).getFinal_total();

    salerep.insertSale(purchaseDate, final_total, usr);

    List<Sale> sale_id = salerep.findTopByOrderByIdDesc();
    Sale sale = sale_id.get(0);

    for (int i = 0; i < dataObj.size(); i++) {
      saledetailtrep.insertSaleDetail( //
          dataObj.get(i).getQuantity(), //
          dataObj.get(i).getTotal_amount(), //
          dataObj.get(i).getFinal_total(), //
          productrep.findById(dataObj.get(i).getId()).get(), //
          sale //
      );

      rsrep.updateMinusStockQuantity(dataObj.get(i).getQuantity(), dataObj.get(i).getId());
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
    Optional<Sale> sale = salerep.findById(id);
    List<SaleDetail> saleDetails = saledetailtrep.findBySale(sale);
    for (int i = 0; i < saleDetails.size(); i++) {
      rsrep.updateAddStockQuantity(saleDetails.get(i).getQuantity(), saleDetails.get(i).getProduct().getId());
    }

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

    Optional<SaleDetail> saleDetails = saledetailtrep.findById(detail_id);
    rsrep.updateAddStockQuantity(saleDetails.get().getQuantity(), saleDetails.get().getProduct().getId());

    saledetailtrep.deleteById(detail_id);

    if (saledetailtrep.findBySale(salerep.findById(sale_id)).size() == 0) {
      salerep.deleteById(sale_id);
      attrs.addFlashAttribute("success", "Successfully deleted!");
      return "redirect:/liquorshop/cashier-homepage/sale_history_list";
    }

    attrs.addFlashAttribute("success", "Successfully deleted!");
    return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail";
  }

  @GetMapping("/sale_history/{sale_id}/detail/{detail_id}/update")
  public String update(Principal principal, @PathVariable int detail_id, @PathVariable int sale_id, Model model,
      @ModelAttribute SaleDetail saleDetail) {
    model.addAttribute("user", principal.getName());

    model.addAttribute("detail_id", detail_id);
    model.addAttribute("sale_id", sale_id);

    model.addAttribute("stock", saledetailtrep.findById(detail_id).get().getProduct().getRemaining_stock().getStock());

    model.addAttribute("products", productrep.findAll());
    model.addAttribute("saleDetail", saledetailtrep.findById(detail_id).get());
    return "cashier/sale_detail_update";
  }

  @PatchMapping("/sale_history/{sale_id}/detail/{detail_id}/detail_update")
  public String detail_update(Principal principal, @PathVariable int detail_id, @PathVariable int sale_id,
      RedirectAttributes attrs, @Validated @ModelAttribute SaleDetail saleDetail, BindingResult result, Model model) {
    if (result.hasErrors()) {
      attrs.addFlashAttribute("quantityError", "Insert quantity that is greater than 0!");
      return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail/" + detail_id + "/update";
    }

    SaleDetail sale_detail = saledetailtrep.findById(detail_id).get();

    Integer stock = sale_detail.getProduct().getRemaining_stock().getStock();
    if (saleDetail.getQuantity() <= sale_detail.getQuantity()) {
      Integer total_amount = sale_detail.getProduct().getPrice() * saleDetail.getQuantity();
      saledetailtrep.updateSaleDetailAmount(saleDetail.getQuantity(), total_amount, detail_id);

      Integer net = sale_detail.getQuantity() - saleDetail.getQuantity();
      Integer product_id = sale_detail.getProduct().getId();
      rsrep.updateAddStockQuantity(net, product_id);
    } else {
      Integer net_quantity = saleDetail.getQuantity() - sale_detail.getQuantity();
      Integer product_id = sale_detail.getProduct().getId();
      if (net_quantity <= stock) {
        Integer total_amount = sale_detail.getProduct().getPrice() * saleDetail.getQuantity();
        saledetailtrep.updateSaleDetailAmount(saleDetail.getQuantity(), total_amount, detail_id);

        rsrep.updateMinusStockQuantity(net_quantity, product_id);
      } else {
        attrs.addFlashAttribute("quantityError", "Remaining Stock is not enough!");
        return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail/" + detail_id + "/update";
      }
    }

    attrs.addFlashAttribute("success", "Sale detail was successfully updated!");
    return "redirect:/liquorshop/cashier-homepage/sale_history/" + sale_id + "/detail";
  }
}
