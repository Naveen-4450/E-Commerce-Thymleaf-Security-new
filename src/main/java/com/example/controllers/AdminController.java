package com.example.controllers;

import com.example.models.dbModels.Customers;
import com.example.models.dtoModels.FetchOrderDetailsDto;
import com.example.repositories.CustomersRepository;
import com.example.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class AdminController
{
    @Autowired
    private OrderService orderSer;

    @Autowired
    private CustomersRepository custRepo;

    @GetMapping("/adminWelcome")
    public String adminWelcomePage()
    {
        return "adminWelcome";
    }

    @GetMapping("/adminHome")
    public String adminHome()
    {
        return "adminHome";
    }

    @GetMapping("/categoryManagement")
    public String cateManagement()
    {
        return "categoryManagement";
    }

    @GetMapping("/productsManagement")
    public String prodManagement()
    {
        return "productsManagement";
    }

    @GetMapping("/ordersManagement")
    public String ordersManagement()
    {
        return "ordersManagement";
    }


    // for Orders Management extra API's required that i created here:

    @GetMapping("/customerAllOrders")
    public String customerAllOrders(@RequestParam int custId, Model model)
    {
        Customers customer = custRepo.findById(custId).orElse(null);
        if (customer == null){
            String msg = "Customer Not found with ID :"+custId;
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            return "redirect:/ordersManagement?status=fail&msg="+msg; //fail bcz red
        }else {
            List<FetchOrderDetailsDto> orders = orderSer.getOrderByCustomerId(custId);
            model.addAttribute("custId", custId);
            model.addAttribute("orders", orders);
            return "customerAllOrders";
        }
    }

}
