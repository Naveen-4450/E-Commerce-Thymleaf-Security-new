package com.example.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException
    {
        System.out.println("CustomAuthenticationSuccessHandler class is called");
        CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
        Integer custId = userDetails.getCustId();

        String loginContext =  (String)request.getSession().getAttribute("loginContext");

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
       boolean isAdmin = authorities.stream().anyMatch(auth-> auth.getAuthority().equals("ROLE_ADMIN"));
       boolean isCustomer = authorities.stream().anyMatch(auth->auth.getAuthority().equals("ROLE_CUSTOMER"));




        if("ADMIN".equals(loginContext)){
            if(isAdmin){
                response.sendRedirect("/adminHome");
            }else{
                response.sendRedirect("/denied");
            }
        } else if ("CUSTOMER".equals(loginContext)){
            if(isCustomer){
                response.sendRedirect("/displayProducts?custId=" + custId);
            }else{
                response.sendRedirect("/denied");
            }
        }


    }
}

/*if("ADMIN".equals(loginContext)){
           if(isAdmin){
                response.sendRedirect("/adminHome");
           }else{
               response.sendRedirect("/denied");
           }
       } else if ("CUSTOMER".equals(loginContext)){
           if(isCustomer){
               response.sendRedirect("/displayProducts?custId=" + custId);
           }else{
               response.sendRedirect("/denied");
           }
       }else{
           response.sendRedirect("/error");
       }*/