package com.example.config;

import com.example.models.dbModels.Credentials;
import com.example.repositories.CredentialRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;


public class CustomLogoutSuccessHandler implements LogoutSuccessHandler
{

    @Autowired
    private CredentialRepository credRepo;
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        if (authentication != null) {
            // Get the roles of the user
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isCustomer = authorities.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

            if (isAdmin) {
                response.sendRedirect("/adminWelcome");
            } else if (isCustomer) {
                response.sendRedirect("/customerWelcome");
            } else {
                response.sendRedirect("/welcome");
            }
        } else {
            response.sendRedirect("/welcome");
        }
    }


    /*@Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException
    {
        String targetUrl = "/";
        if(authentication !=null) {

            String username = authentication.getName();
            Credentials credentials = credRepo.findByUsername(username);

            if (credentials != null) {
                if (credentials.getRoles().equals("ROLE_ADMIN")) {
                    targetUrl = "/adminWelcome";
                } else if (credentials.getRoles().equals("ROLE_CUSTOMER")) {
                    targetUrl = "/customerWelcome";
                }
            }
        }

        // Redirect to the determined URL
        response.sendRedirect(targetUrl);

    }*/
}
