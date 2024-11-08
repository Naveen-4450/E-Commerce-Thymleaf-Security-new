package com.example.controllers;

import com.example.models.dbModels.CustomerAddresses;
import com.example.models.dtoModels.CustomerAddressDto;
import com.example.services.CustomerAddressService;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@Tag(name = "CustomerAddress Controller API's", description = "This API's are Managed to Customer Addresses in Application")
public class CustomerAddController
{
    @Autowired
    private CustomerAddressService custAddSer;


    @GetMapping("/addingAddress/{custId}")
    public String addingAddress(@PathVariable int custId, Model model)
    {
        model.addAttribute("custId", custId);
        return "addingAddress";
    }

    @Operation(summary = "Add a Customer Address", description = "Adding a new CustomerAddress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer Address Added successfully")
    })
    @PostMapping("/addingAddress/{custId}")
    public String addingAddress(@PathVariable int custId,
                                @ModelAttribute CustomerAddressDto cADto)
    {
        CustomerAddresses addresses = custAddSer.addingAddress(custId,cADto);
        String msg = "New Address Added Successfully";
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/customerProfile/"+custId+"?status=success&msg="+msg;
    }


    @PostMapping("/addOrderAddress/{custId}")//while Ordering the product
    public String addNewAddWhilePlaceOrder(@PathVariable int custId,
                                            @RequestParam int prodId,
                                            @ModelAttribute CustomerAddressDto cADto)
    {
        CustomerAddresses addresses = custAddSer.addingAddress(custId,cADto);
        String msg = "New Address Added Successfully";
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/placeOneOrder?custId="+custId+"&prodId="+prodId+"&status=success&msg="+msg;
    }


    @GetMapping("/updateAddress/{addId}")
    public String updateAddress(@PathVariable int addId,Model model)
    {
        CustomerAddressDto address = custAddSer.getOneAddress(addId);
        model.addAttribute("custId",address.getCustomerId());
        model.addAttribute("address",address);
        return "updateAddress";
    }
    @Operation(summary = "Update Customer Address Details by AddressId", description = "Updating an existing customer Address Details by Address Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer Address Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Address Not Found to Update with address id")
    })
    @PostMapping("/updateAddress/{addId}")
    public String updateAddress(@PathVariable int addId,
                                @RequestParam int custId,
                                @ModelAttribute CustomerAddressDto AddDto)
    {
       String msg = custAddSer.updateAddress(addId,AddDto);
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/customerProfile/"+custId+"?status=success&msg="+msg;
    }


    //below api is for deleting address
    @Operation(summary = "Delete a Customer Address by AddId", description = "Deleting the Customer Address by Using Address Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer Address Deleted Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Address Not Found to Delete")
    })
    @PostMapping("/deleteAddress/{addId}")
    public String deleteAddress(@PathVariable int addId,
                                @RequestParam int custId)
    {
        String msg = custAddSer.deleteAddress(addId);
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
         return "redirect:/customerProfile/"+custId+"?status=success&msg="+msg;
    }










    @Operation(summary = "Get Customer Address by address id", description = "Get Customer Address by customerAddress Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Customer Address Retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer Address Not Found to Retrieve")
    })
    @GetMapping("/oneAdd/{addId}")
    public CustomerAddressDto getOneAddress(@PathVariable int addId)
    {
        return custAddSer.getOneAddress(addId);
    }


    @Operation(summary = "Get all Customer Addresses by customer Id", description = "Fetching all Customer Addresses by customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Customer Addresses Retrieved Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Doesn't add any Addresses to Retrieve")
    })
    @GetMapping("/allAdd/{custId}")
    public ResponseEntity<List<CustomerAddressDto>> getAllAddresses(@PathVariable int custId)
    {
        return custAddSer.getAllAddresses(custId);
    }







}
