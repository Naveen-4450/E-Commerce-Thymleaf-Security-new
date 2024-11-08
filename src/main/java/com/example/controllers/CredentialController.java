package com.example.controllers;

import com.example.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CredentialController
{
     /*@GetMapping("/login")
    public String loginPage()
    {
        return "login";
    }*/

    @GetMapping("/customer-login")
    public String customerLogin(HttpServletRequest request)
    {
        request.getSession().setAttribute("loginContext","CUSTOMER");
        return "login";
    }

    @GetMapping("/admin-login")
    public String adminLogin(HttpServletRequest request)
    {
        request.getSession().setAttribute("loginContext","ADMIN");
        return "login";
    }

    //it's executes if give wrong credentials, and with the same loginContext
     @GetMapping("/login")
    public String loginPage(@RequestParam String error,
                            @RequestParam String loginContext, Model model)
    {
        if(error != null){
            model.addAttribute("error", true);
        }
        // Set the loginContext in the model, so it can be used in the Thymeleaf template
        model.addAttribute("loginContext", loginContext);
        return "login"; // Thymeleaf template
    }



    @GetMapping("/welcome")
    public String welcomePage()
    {
        return "welcome";
    }


    @GetMapping("/denied")
    public String deniedPage()
    {
        return "denied";
    }


   /* @GetMapping("/error")
    public String errorPage()
    {
        return "error";
    }*/

}
