package com.example.controllers;

import com.example.models.dbModels.Wishlist;
import com.example.models.dtoModels.WishlistDto;
import com.example.repositories.WishlistRepository;
import com.example.services.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@Tag(name = "Wishlist Controller API's", description = "This API's are Managed to Customers Wishlists in Application")
@Slf4j
public class WishlistController
{
    @Autowired
    private WishlistService wishlistSer;


    @Operation(summary = "Add a Product to wishlist by product Id", description = "Adding a Product to a Customer Wishlist by Product Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Added in Customer Wishlist Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer/product Not found")
    })
    @PostMapping("/{custId}/addProdWishlist/{prodId}")
    public String addingProduct(@PathVariable int custId, @PathVariable int prodId)
    {
        Wishlist wishlist = wishlistSer.addingProduct(custId,prodId);
        String msg = "";
        if (wishlist != null) {
            msg = "Product successfully added to Wishlist";
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            return "redirect:/displayProducts?status=success&msg=" + msg + "&custId=" + custId;
        }else {
            msg = "Product Already Available in Wishlist";
            msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
            return "redirect:/displayProducts?status=fail&msg=" + msg +"&custId=" + custId;
        }
    }



    @Operation(summary = "Get Wishlist by customer Id", description = "Get Wishlist products of a Customer by customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist Retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wishlist Not found/ products Not found in wishlist")
    })
    @GetMapping("/viewWishlist")
    public String getWishlistByCustomerId(@RequestParam int custId, Model model)
    {
        log.info("Fetching wishlist for customer ID:{}",custId);
        List<WishlistDto> wishlist = wishlistSer.getWishlistByCustomerId(custId);

        model.addAttribute("wishlistItems", wishlist);
        model.addAttribute("custId",custId);
        return "viewWishlist";
    }



    @Operation(summary = "Clear Customer Wishlist by customer Id", description = "Deleting all the Products from a customer wishlist by Using Customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "wishlist Cleared Successfully")
    })
    @DeleteMapping("/clear")
    public String clearWishlist(@RequestParam int custId)
    {
        wishlistSer.clearWishlist(custId);

        String msg = "Your Wishlist is Cleared";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewWishlist?status=success&msg="+msg+"&custId="+custId;
    }



    @Operation(summary = "Remove a Product in customer Wishlist by ProductId", description = "Removing a Product From a customer Wishlist by ProductId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Removed From a Customer wishlist"),
            @ApiResponse(responseCode = "404", description = "Product Not Available in wishlist(check-->Product Id)")
    })
    @DeleteMapping("/removeProductWishlist")
    public String removeProdFromWishlist(@RequestParam int custId,
                                         @RequestParam int prodId)
    {
        String msg = wishlistSer.removeProdFromWishlist(custId,prodId);
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewWishlist?status=success&msg="+msg+"&custId="+custId;
    }

}














/*
// Not Works:reason

Immediate Return: The return statement inside the for loop caused the method to exit after the first iteration of the loop. This means that as soon as the loop checked one item, it either removed it or decided it wasn’t found, and then returned a result. This prevented the method from checking the rest of the items in the list.

Example: Imagine your wishlist has products with IDs 1, 2, and 3. If you wanted to remove product ID 2 but your code had a return inside the loop, it would:
Check product ID 1 (if it doesn’t match, it returns “Product Not Found” without checking further).

* public String removeProdFromWishlist(int custId, int prodId) {
    List<Wishlist> list = wishlistRepo.findByCustomerIdCustId(custId);
    for (Wishlist wishlist : list) {
        if (wishlist.getProductId().getProdId().equals(prodId)) {
            wishlistRepo.delete(wishlist);
            log.info("Removed product with ID: {} from wishlist for customer ID:{}", prodId, custId);
            return "Product Deleted from Wishlist";
        } else {
            log.warn("Product with id {} not found in wishlist", prodId);
            return "Product Not_Found in Wishlist";
        }
    }
    return "Customer Not Found/Customer not having wishlist";
}
* */