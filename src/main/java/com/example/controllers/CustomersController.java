package com.example.controllers;

import com.example.models.dbModels.Customers;
import com.example.models.dtoModels.CustomersDto;
import com.example.services.CustomerService;
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
import java.util.List;

@Controller
@Tag(name = "Customers Controller API's", description = "This API's are Managed to Customers in Application")
public class CustomersController
{
    @Autowired
    private CustomerService customerSer;

    @GetMapping("/customerRegister") // permitAll
    public String addingCustomer()
    {
        return "customerRegister";
    }

    @Operation(summary = "Add/Register a Customer", description = "Adding a new Customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer Details Added successfully")
    })
    @PostMapping("/customerRegister")
    public String addingCustomer(@ModelAttribute CustomersDto cDto)
    {
       Customers customer = customerSer.addingCustomer(cDto);
        String msg = "Registration done Successfully, Login with your Credentials(Username/password)";
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
       return "redirect:/customerRegister?status=success&msg=" + msg;
       // after implementing security here we need to redirect to login form
    }


    @Operation(summary = "Get Customer by Id", description = "Get Customer Details by customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Customer Details Retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found to Retrieve")
    })
    @GetMapping("/customerProfile/{custId}")
    public String getCustomer(@PathVariable int custId, Model model)
    {
        Customers customer = customerSer.getCustomer(custId);
        model.addAttribute("customer",customer);
        model.addAttribute("custId",custId);
        return "customerProfile";
    }



    @GetMapping("/updateCustomer/{custId}")
    public String updateCustomer(@PathVariable int custId, Model model)
    {
        Customers customer = customerSer.getCustomer(custId);
        model.addAttribute("customer",customer);
        return "updateCustomer";
    }

    @Operation(summary = "Update one Customer Details by Id", description = "Updating an existing customer Details by Customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer Details Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found to Update")
    })
    @PostMapping("/updateCustomer/{custId}")
    public String updateCustomer(@PathVariable int custId,
                                @ModelAttribute CustomersDto cDto)
    {
        Customers customer = customerSer.updateCustomer(custId, cDto);

        String msg = "Your Details Updated Successfully";
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/customerProfile/"+custId+"?status=success&msg="+msg;
    }




    // didn't created or used this below api's in UI Part

    @Operation(summary = "Delete a  Customer by Id", description = "Deleting the Customer Details by Using Customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer Deleted Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found to Delete")
    })
    @DeleteMapping("/customer/{custId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable int custId)
    {
        return customerSer.deleteCustomer(custId);
    }


    @Operation(summary = "Get all Customers", description = "Fetching all Customer Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All Customer Details Retrieved Successfully")
    })
    @GetMapping("/customers")
    public ResponseEntity<List<Customers>> getAllCustomers()
    {
        return customerSer.getAllCustomers();
    }


}
