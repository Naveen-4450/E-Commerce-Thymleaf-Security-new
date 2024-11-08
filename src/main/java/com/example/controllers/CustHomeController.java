package com.example.controllers;

import com.example.models.dbModels.Products;
import com.example.models.dtoModels.CustomerAddressDto;
import com.example.repositories.ProductsRepository;
import com.example.services.CustomerAddressService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class CustHomeController
{
    @Autowired
    private ProductsRepository productRepo;

    @Autowired
    private CustomerAddressService custAddSer;

    @GetMapping("/customerWelcome")
    public String custWelcome()
    {
        return "customerWelcome";
    }


    @GetMapping("/displayProducts")
    public String displayProducts(@RequestParam int custId,HttpSession session,Model model)
    {
        session.setAttribute("custId", custId);
        List<Products> products = productRepo.findAll();

        model.addAttribute("products", products);
        model.addAttribute("custId",custId);

       /*

        @RequestParam(value = "msg", required = false) String msg,
        @RequestParam(value = "showModal", required = false) String showModal,
       // Add attributes for modal handling
        model.addAttribute("message", msg != null ? URLDecoder.decode(msg, StandardCharsets.UTF_8) : "");
        model.addAttribute("showModal", showModal != null && showModal.equals("true"));
*/
        return "displayProducts";
    }


    @GetMapping("/updateOrderAddress/{addId}/{prodId}")
    public String updateOrderAddress(@PathVariable int addId,
                                     @PathVariable int prodId,// this is for render the placeOrder page
                                     Model model)
    {
        CustomerAddressDto address = custAddSer.getOneAddress(addId);
        model.addAttribute("custId",address.getCustomerId());
        model.addAttribute("prodId",prodId);
        model.addAttribute("address",address);
        return "updateOrderAddress";
    }


    @PostMapping("/updateOrderAddress/{addId}")
    public String updateOrderAddress(@PathVariable int addId,
                                     @RequestParam int custId,
                                     @RequestParam int prodId,// this is for render the placeOrder page
                                     @ModelAttribute CustomerAddressDto AddDto)
    {
        String msg = custAddSer.updateAddress(addId,AddDto);
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/placeOneOrder?custId="+custId+"&prodId="+prodId+"&status=success&msg="+msg;


    }


}
