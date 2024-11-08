package com.example.controllers;

import com.example.models.dtoModels.CartDto;
import com.example.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Controller
@Tag(name = "Cart Controller API's", description = "This API's are Managed to Customer Cart's in Application")
public class CartController
{
    @Autowired
    private CartService cartSer;

    @Operation(summary = "Add Product to cart", description = "Adding a Product to a Customer cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Added to Cart successfully")
    })
    @PostMapping("/cart")
    public String addProductToCart(@RequestParam("prodId")int prodId,
                                                 @RequestParam("custId")int custId,
                                                 @RequestParam("quantity")int quantity)
    {
        cartSer.addProductToCart(prodId,custId,quantity);

        String msg = "Product successfully Added to Cart";
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/displayProducts?status=success&msg="+msg+"&custId="+custId;

    }

    @Operation(summary = "View the Customer Cart by customer id", description = "Viewing the Customer Added products in cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart Products Retrieved successfully")
    })
    @GetMapping("/viewCart")
    public String viewCustomerCart(@RequestParam("custId")int custId, Model model)
    {
        CartDto cartDto = cartSer.viewCustomerCart(custId);
        // Initialize cartDto if not found
        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setProductQuantities(new ArrayList<>());
        }
        model.addAttribute("cartItems",cartDto);
        model.addAttribute("custId",custId);

        double cartTotal = cartDto.getProductQuantities().stream()
                .mapToDouble(p->p.getPrice() * p.getQuantity()).sum();

        model.addAttribute("cartTotal",cartTotal);
        return "viewCart";
    }


    @Operation(summary = "Remove a Product in customer Cart by ProductId", description = "Removing a Product From a customer Cart by ProductId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Removed From a Customer Cart"),
            @ApiResponse(responseCode = "404", description = "Product Not Available in Cart(Product Id)")
    })
    @DeleteMapping("/removeProdCart")
    public String removeProductFromCart(@RequestParam int prodId,
                                        @RequestParam int custId)
    {
        cartSer.removeProductFromCart(prodId,custId);
        String msg = "Product Removed From Cart successfully";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewCart?status=success&msg="+msg+"&custId="+custId;
    }

    @Operation(summary = "Clear Customer Cart by customer Id", description = "Deleting all the Products from a customer cart by Using Customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart Cleared Successfully")
    })
    @DeleteMapping("/clearCart/{custId}")
    public String clearCart(@PathVariable int custId)
    {
        cartSer.clearCart(custId);
        String msg = "Cart Cleared Successfully";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewCart?status=success&msg="+msg+"&custId="+custId;
    }


    @Operation(summary = "Update Product Quantity in Cart", description = "Updating an existing Product Quantity in Cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart Product Quantity Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Your Passing id of Product Not available in Cart ")
    })
    @PostMapping("/updateQuantity")
    public String updateProductQuantity(@RequestParam int prodId,
                                        @RequestParam int custId,
                                        @RequestParam int quantity,
                                        @RequestParam String operation)
    {
        int newQuantity = quantity;
        if("increase".equals(operation)){
            newQuantity++;
        }else if("decrease".equals(operation) && quantity > 1){
            newQuantity--;
        }
        cartSer.updateProductQuantity(prodId,custId,newQuantity);
        return "redirect:/viewCart?custId="+custId;
    }

    /*@PutMapping("/updateQuantity")
    public String updateProductQuantity(@RequestParam int prodId,
                                        @RequestParam int custId,
                                        @RequestParam int quantity)
    {
        cartSer.updateProductQuantity(prodId,custId,quantity);
        return "";
    }
*/
}
