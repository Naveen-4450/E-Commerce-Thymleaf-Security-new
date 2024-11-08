package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.csrf(csrf->csrf.disable());

        http.authorizeHttpRequests(auth-> auth

            .requestMatchers("/adminHome","/categoryManagement","/productsManagement","/ordersManagement", "/customerAllOrders",
                    "/addCategory","/viewCategories","/updateCategory/{id}","/getOne",
                    "/addProduct","/viewOneProduct/{prodId}","/viewProducts","/deleteProduct/{prodId}","/updateProduct/{prodId}",
                    "/updateOneProduct/{prodId}","/removeProductImage","/removeOneProductImage",
                    "/orderStatus/{orderId}").hasAuthority("ROLE_ADMIN")//***"/customer/{custId}"***"/customers"***(these two for Delete and getAll custs; not used)



            .requestMatchers("/displayProducts","/updateOrderAddress/{addId}/{prodId}","/updateOrderAddress/{addId}",
                  "/placeOneOrder","/viewOneOrder/{orderId}","/viewAllOrders/{custId}","/placeMultiOrders",
                  "/customerProfile/{custId}","/updateCustomer/{custId}",
                  "/addingAddress/{custId}","/addOrderAddress/{custId}","/updateAddress/{addId}","/deleteAddress/{addId}",
                  "/viewWishlist","/{custId}/addProdWishlist/{prodId}","/clear","/removeProductWishlist",
                  "/cart","/viewCart","/removeProdCart","/clearCart/{custId}","/updateQuantity")
                        .hasAuthority("ROLE_CUSTOMER")//***"/oneAdd/{addId}"***"/allAdd/{custId}"*** for Customer not used


               // .requestMatchers("/welcome","/adminWelcome","/customerWelcome","/customerRegister","/denied").permitAll()

                  .requestMatchers("/adminWelcome","/customerWelcome","/welcome","/admin-login","/customer-login","/customerRegister","/denied","/login","/error").permitAll()

                        .anyRequest().authenticated())

                .formLogin(form->form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler()) // if credentials are wrong it executes
                        .permitAll()
                )

                .logout(logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler())
                        .permitAll()
                )

                .exceptionHandling(exception->exception
                        .accessDeniedPage("/denied")
                );
        return http.build();
    }


    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler()
    {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler(){
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

}
